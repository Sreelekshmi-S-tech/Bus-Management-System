package service;

import database.DBConnection;

import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise RateLimiter backed by in-memory sliding windows and the `rate_limit_log` DB table.
 */
public class RateLimiter {

    private static final RateLimiter INSTANCE = new RateLimiter();

    // Login limits: max 5 failed attempts in 10 mins -> 15 min lock
    public static final int MAX_FAILED_LOGINS = 5;
    public static final long LOGIN_WINDOW_MINUTES = 10;
    public static final long LOGIN_LOCK_MINUTES = 15;

    // Signup limits: max 3 signups in 10 mins per machine
    public static final int MAX_SIGNUPS_PER_MACHINE = 3;
    public static final long SIGNUP_WINDOW_MINUTES = 10;

    // Booking limits: max 5 bookings per minute per user
    public static final int MAX_BOOKINGS_PER_MINUTE = 5;
    public static final long BOOKING_WINDOW_SECONDS = 60;

    // In-memory tracking for fast checks
    private final ConcurrentHashMap<String, AccountLock> lockedAccounts = new ConcurrentHashMap<>();

    private RateLimiter() {}

    public static RateLimiter getInstance() {
        return INSTANCE;
    }

    public static class AccountLock {
        public Instant lockedUntil;
        public int failedAttempts;

        public AccountLock(Instant lockedUntil, int failedAttempts) {
            this.lockedUntil = lockedUntil;
            this.failedAttempts = failedAttempts;
        }
    }

    /**
     * Resolves the current machine identifier (hostname).
     */
    public String getMachineIdentifier() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "LOCAL_MACHINE";
        }
    }

    /**
     * Logs an action to rate_limit_log table in MySQL.
     */
    public void logAction(String identifier, String action) {
        String sql = "INSERT INTO rate_limit_log (identifier, action, attempted_at) VALUES (?, ?, NOW())";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, identifier);
            ps.setString(2, action);
            ps.executeUpdate();
        } catch (Exception e) {
            // Log fallback without failing the whole process
            System.err.println("RateLimiter logAction failed: " + e.getMessage());
        }
    }

    /**
     * Checks if a user is currently locked out of logging in.
     * Returns remaining lock seconds (or 0 if not locked).
     */
    public long checkLoginLock(String username) {
        if (username == null || username.trim().isEmpty()) return 0;
        String userKey = username.trim().toLowerCase();

        // 1. Check in-memory lock
        AccountLock lock = lockedAccounts.get(userKey);
        if (lock != null && lock.lockedUntil != null) {
            long remaining = Instant.now().until(lock.lockedUntil, ChronoUnit.SECONDS);
            if (remaining > 0) {
                return remaining;
            } else {
                lockedAccounts.remove(userKey);
            }
        }

        // 2. Check DB locked_until
        String sql = "SELECT locked_until FROM users WHERE LOWER(username) = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp dbLock = rs.getTimestamp("locked_until");
                    if (dbLock != null) {
                        long remaining = Instant.now().until(dbLock.toInstant(), ChronoUnit.SECONDS);
                        if (remaining > 0) {
                            lockedAccounts.put(userKey, new AccountLock(dbLock.toInstant(), MAX_FAILED_LOGINS));
                            return remaining;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("RateLimiter DB check failed: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Records a failed login attempt. Locks the account if threshold exceeded.
     * Returns remaining lock seconds if just locked, or -1 if not locked.
     */
    public long recordFailedLogin(String username) {
        if (username == null || username.trim().isEmpty()) return -1;
        String userKey = username.trim().toLowerCase();
        logAction(userKey, "LOGIN_FAILED");

        AccountLock lock = lockedAccounts.compute(userKey, (k, v) -> {
            if (v == null) return new AccountLock(null, 1);
            v.failedAttempts++;
            return v;
        });

        if (lock.failedAttempts >= MAX_FAILED_LOGINS) {
            Instant lockUntil = Instant.now().plus(LOGIN_LOCK_MINUTES, ChronoUnit.MINUTES);
            lock.lockedUntil = lockUntil;

            // Update DB
            String sql = "UPDATE users SET failed_attempts = ?, locked_until = ? WHERE LOWER(username) = ?";
            try (Connection con = DBConnection.getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, lock.failedAttempts);
                ps.setTimestamp(2, Timestamp.from(lockUntil));
                ps.setString(3, userKey);
                ps.executeUpdate();
            } catch (Exception e) {
                System.err.println("Failed to lock user in DB: " + e.getMessage());
            }

            return Instant.now().until(lockUntil, ChronoUnit.SECONDS);
        }

        // Also sync failed attempts to DB
        String sql = "UPDATE users SET failed_attempts = ? WHERE LOWER(username) = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, lock.failedAttempts);
            ps.setString(2, userKey);
            ps.executeUpdate();
        } catch (Exception e) {
            // Ignore
        }

        return -1;
    }

    /**
     * Clears failed attempt counters upon successful login.
     */
    public void recordSuccessfulLogin(String username) {
        if (username == null) return;
        String userKey = username.trim().toLowerCase();
        lockedAccounts.remove(userKey);
        logAction(userKey, "LOGIN_SUCCESS");

        String sql = "UPDATE users SET failed_attempts = 0, locked_until = NULL WHERE LOWER(username) = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userKey);
            ps.executeUpdate();
        } catch (Exception e) {
            System.err.println("Failed to reset failed attempts in DB: " + e.getMessage());
        }
    }

    /**
     * Checks if signup is permitted for this machine.
     * Limit: max 3 signups per 10 minutes.
     * Returns true if allowed, false if rate limited.
     */
    public boolean canSignup() {
        String machineId = getMachineIdentifier();
        String sql = "SELECT COUNT(*) FROM rate_limit_log WHERE identifier = ? AND action = 'SIGNUP' " +
                     "AND attempted_at >= NOW() - INTERVAL 10 MINUTE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, machineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count < MAX_SIGNUPS_PER_MACHINE;
                }
            }
        } catch (Exception e) {
            System.err.println("Signup rate check error: " + e.getMessage());
        }
        return true;
    }

    public void recordSignup() {
        logAction(getMachineIdentifier(), "SIGNUP");
    }

    /**
     * Checks if booking is permitted for this user.
     * Limit: max 5 bookings per minute.
     * Returns true if allowed, false if rate limited.
     */
    public boolean canBook(String username) {
        if (username == null || username.trim().isEmpty()) return true;
        String userKey = username.trim().toLowerCase();
        String sql = "SELECT COUNT(*) FROM rate_limit_log WHERE identifier = ? AND action = 'BOOKING' " +
                     "AND attempted_at >= NOW() - INTERVAL 1 MINUTE";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, userKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count < MAX_BOOKINGS_PER_MINUTE;
                }
            }
        } catch (Exception e) {
            System.err.println("Booking rate check error: " + e.getMessage());
        }
        return true;
    }

    public void recordBooking(String username) {
        if (username != null) {
            logAction(username.trim().toLowerCase(), "BOOKING");
        }
    }
}

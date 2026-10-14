package service;

import dao.PassengerDAO;
import dao.UserDAO;
import model.Passenger;
import model.User;
import util.PasswordUtil;
import util.Validator;

public class AuthService {

    private static final AuthService INSTANCE = new AuthService();

    private final UserDAO userDAO = new UserDAO();
    private final PassengerDAO passengerDAO = new PassengerDAO();
    private final RateLimiter rateLimiter = RateLimiter.getInstance();

    private User currentUser = null;
    private Passenger currentPassenger = null;

    private AuthService() {}

    public static AuthService getInstance() {
        return INSTANCE;
    }

    public static class AuthResult {
        public final boolean success;
        public final String message;
        public final long lockSecondsRemaining;

        public AuthResult(boolean success, String message, long lockSecondsRemaining) {
            this.success = success;
            this.message = message;
            this.lockSecondsRemaining = lockSecondsRemaining;
        }

        public static AuthResult ok(String message) {
            return new AuthResult(true, message, 0);
        }

        public static AuthResult fail(String message) {
            return new AuthResult(false, message, 0);
        }

        public static AuthResult locked(String message, long seconds) {
            return new AuthResult(false, message, seconds);
        }
    }

    /**
     * Authenticates a user with rate limiting and salted PBKDF2 verification.
     */
    public AuthResult login(String username, char[] password) {
        if (username == null || username.trim().isEmpty() || password == null || password.length == 0) {
            return AuthResult.fail("Username and password are required.");
        }

        String userTrimmed = username.trim();

        // 1. Rate limiter check: is account locked?
        long lockRemaining = rateLimiter.checkLoginLock(userTrimmed);
        if (lockRemaining > 0) {
            long minutes = (lockRemaining / 60) + 1;
            return AuthResult.locked("Account is temporarily locked due to multiple failed login attempts.\n" +
                                     "Please wait " + minutes + " minute(s) before trying again.", lockRemaining);
        }

        // 2. Fetch user from DB
        User user = userDAO.findByUsername(userTrimmed);
        if (user == null) {
            rateLimiter.recordFailedLogin(userTrimmed);
            return AuthResult.fail("Invalid username or password.");
        }

        // 3. Verify salted PBKDF2 hash
        boolean passwordMatches = PasswordUtil.verifyPassword(password, user.getSalt(), user.getPasswordHash());

        if (!passwordMatches) {
            long justLockedSec = rateLimiter.recordFailedLogin(userTrimmed);
            if (justLockedSec > 0) {
                long min = (justLockedSec / 60) + 1;
                return AuthResult.locked("Account has now been locked for " + min + " minutes due to 5 failed attempts.", justLockedSec);
            }
            return AuthResult.fail("Invalid username or password.");
        }

        // 4. Success: reset rate limit counters and establish session
        rateLimiter.recordSuccessfulLogin(userTrimmed);
        currentUser = user;
        currentPassenger = passengerDAO.getPassengerByUserId(user.getUserId());

        return AuthResult.ok("Welcome back, " + user.getUsername() + "!");
    }

    /**
     * Registers a new user account with machine-level rate limiting and input validation.
     */
    public AuthResult register(String username, String email, char[] password, char[] confirmPassword, String role) {
        // 1. Rate limiter check (max 3 per 10 mins per machine)
        if (!rateLimiter.canSignup()) {
            return AuthResult.fail("Signup limit reached from this computer. Please try again after 10 minutes.");
        }

        // 2. Input validation
        if (!Validator.isValidUsername(username)) {
            return AuthResult.fail("Username must be 3-20 alphanumeric characters (letters, numbers, underscore).");
        }
        if (!Validator.isValidEmail(email)) {
            return AuthResult.fail("Please enter a valid email address.");
        }
        if (password == null || confirmPassword == null || !java.util.Arrays.equals(password, confirmPassword)) {
            return AuthResult.fail("Passwords do not match.");
        }

        String pwdError = Validator.getPasswordStrengthFeedback(password);
        if (pwdError != null) {
            return AuthResult.fail(pwdError);
        }

        // 3. Uniqueness checks
        if (userDAO.isUsernameTaken(username)) {
            return AuthResult.fail("Username '" + username + "' is already taken. Please choose another.");
        }
        if (userDAO.isEmailTaken(email)) {
            return AuthResult.fail("An account with email '" + email + "' already exists.");
        }

        // 4. Register
        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setEmail(email.trim());
        newUser.setRole((role != null && role.equalsIgnoreCase("ADMIN")) ? "ADMIN" : "USER");

        boolean created = userDAO.registerUser(newUser, password);
        if (created) {
            rateLimiter.recordSignup();
            return AuthResult.ok("Account created successfully! You can now log in.");
        } else {
            return AuthResult.fail("Registration failed due to a database error. Please try again.");
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public Passenger getCurrentPassenger() {
        return currentPassenger;
    }

    public void setCurrentPassenger(Passenger p) {
        this.currentPassenger = p;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public void logout() {
        currentUser = null;
        currentPassenger = null;
    }
}

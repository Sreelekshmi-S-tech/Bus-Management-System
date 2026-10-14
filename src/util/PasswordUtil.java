package util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;

/**
 * Enterprise password hashing utility using salted PBKDF2 with HMAC-SHA256.
 * Recommended by OWASP: Salt length >= 16 bytes, Iterations >= 65,536.
 */
public class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256; // 32 bytes
    private static final int SALT_LENGTH = 16;  // 16 bytes

    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a cryptographically secure random salt encoded in hex.
     */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        RANDOM.nextBytes(salt);
        return HexFormat.of().formatHex(salt);
    }

    /**
     * Hashes a password with a given hex salt using PBKDF2WithHmacSHA256.
     */
    public static String hashPassword(char[] password, String saltHex) {
        if (password == null || saltHex == null) {
            throw new IllegalArgumentException("Password and salt must not be null");
        }
        try {
            byte[] salt = HexFormat.of().parseHex(saltHex);
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            byte[] hash = factory.generateSecret(spec).getEncoded();
            spec.clearPassword();
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error hashing password with PBKDF2", e);
        }
    }

    /**
     * Verifies a candidate password against the stored salt and hash.
     */
    public static boolean verifyPassword(char[] candidatePassword, String saltHex, String storedHashHex) {
        if (candidatePassword == null || saltHex == null || storedHashHex == null) {
            return false;
        }
        String candidateHash = hashPassword(candidatePassword, saltHex);
        return candidateHash.equalsIgnoreCase(storedHashHex);
    }

    // Quick CLI helper to generate hashes for DB seed scripts
    public static void main(String[] args) {
        String adminSalt = generateSalt();
        String adminHash = hashPassword("1234".toCharArray(), adminSalt);
        System.out.println("admin (1234) -> salt=" + adminSalt + " | hash=" + adminHash);

        String userSalt = generateSalt();
        String userHash = hashPassword("User@1234".toCharArray(), userSalt);
        System.out.println("john_doe (User@1234) -> salt=" + userSalt + " | hash=" + userHash);
    }
}

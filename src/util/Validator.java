package util;

import java.util.regex.Pattern;

/**
 * Enterprise input validation utility.
 */
public class Validator {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10}$");
    private static final Pattern PASSWORD_UPPER = Pattern.compile("[A-Z]");
    private static final Pattern PASSWORD_LOWER = Pattern.compile("[a-z]");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile("[0-9]");

    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim().replaceAll("[\\s-]", "")).matches();
    }

    public static boolean isValidAge(int age) {
        return age >= 1 && age <= 120;
    }

    /**
     * Password strength requirement:
     * - Minimum 8 characters
     * - At least one uppercase letter
     * - At least one lowercase letter
     * - At least one digit
     */
    public static boolean isStrongPassword(char[] password) {
        if (password == null || password.length < 8) {
            return false;
        }
        String pwd = new String(password);
        return PASSWORD_UPPER.matcher(pwd).find() &&
               PASSWORD_LOWER.matcher(pwd).find() &&
               PASSWORD_DIGIT.matcher(pwd).find();
    }

    public static String getPasswordStrengthFeedback(char[] password) {
        if (password == null || password.length == 0) {
            return "Password cannot be empty.";
        }
        if (password.length < 8) {
            return "Password must be at least 8 characters long.";
        }
        String pwd = new String(password);
        if (!PASSWORD_UPPER.matcher(pwd).find()) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!PASSWORD_LOWER.matcher(pwd).find()) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!PASSWORD_DIGIT.matcher(pwd).find()) {
            return "Password must contain at least one numeric digit.";
        }
        return null; // Strong
    }
}

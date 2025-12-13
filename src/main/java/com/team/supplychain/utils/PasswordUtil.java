package com.team.supplychain.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Password hashing and verification using BCrypt.
 *
 * WHY BCrypt?
 * - Designed specifically for password hashing (unlike MD5/SHA which are too fast)
 * - Includes built-in salt (prevents rainbow table attacks)
 * - Adaptive cost factor - can increase difficulty as computers get faster
 * - Industry standard for secure password storage
 *
 * SECURITY NOTE:
 * This class has a temporary fallback for legacy plaintext passwords.
 * This is ONLY for migration purposes and should be removed once all
 * passwords in the database are BCrypt hashed.
 *
 * To migrate all passwords, run: DatabasePasswordUpdater.java
 */
public class PasswordUtil {

    /**
     * Hash a password using BCrypt with 10 salt rounds.
     *
     * Why 10 rounds?
     * - Each "round" doubles the computation time
     * - 10 rounds = ~100ms to hash (good balance between security and UX)
     * - Too few rounds (4-6) = vulnerable to brute force
     * - Too many rounds (13+) = login takes too long
     *
     * BCrypt is intentionally slow to prevent attackers from
     * trying millions of passwords per second.
     *
     * @param plainPassword The password to hash
     * @return BCrypt hash (60 characters, starts with $2a$10$)
     */
    public static String hashPassword(String plainPassword) {
        // BCrypt generates a random salt automatically, so same password
        // produces different hashes each time (this is good!)
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Check if a plain password matches a stored hash.
     *
     * SECURITY WARNING:
     * This method has a fallback for legacy plaintext passwords from the old system.
     * If the password doesn't look like a BCrypt hash ($2a$...), it does a
     * plain comparison instead. This is a HUGE security risk!
     *
     * TODO: Remove plaintext fallback after migrating all passwords
     * Run DatabasePasswordUpdater.java to convert all passwords to BCrypt
     *
     * @param plainPassword The password the user entered
     * @param hashedPassword The hash stored in the database
     * @return true if passwords match, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            // BCrypt hashes start with $2a, $2b, or $2y (version indicators)
            // If we see this, we know it's a proper BCrypt hash
            if (hashedPassword != null && hashedPassword.startsWith("$2")) {
                // Use BCrypt's secure comparison (also protects against timing attacks)
                return BCrypt.checkpw(plainPassword, hashedPassword);
            } else {
                // DANGER ZONE: Legacy plaintext password support
                // This exists because the old system stored passwords in plaintext
                // We can't upgrade everyone's passwords without them logging in
                System.out.println("⚠️ WARNING: Using plain text password comparison!");
                System.out.println("   User should be prompted to change password on next login");
                return plainPassword.equals(hashedPassword);
            }
        } catch (IllegalArgumentException e) {
            // This happens if someone manually corrupted the password hash in DB
            // or if BCrypt library can't parse it for some reason
            System.err.println("❌ Invalid password hash format: " + e.getMessage());

            // Last resort: try plaintext comparison
            // In production, you'd probably want to return false here instead
            return plainPassword.equals(hashedPassword);
        }
    }
}
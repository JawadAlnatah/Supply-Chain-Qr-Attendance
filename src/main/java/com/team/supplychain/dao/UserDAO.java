package com.team.supplychain.dao;

import com.team.supplychain.models.User;
import com.team.supplychain.enums.UserRole;
import com.team.supplychain.utils.DatabaseConnection;
import com.team.supplychain.utils.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for user management and authentication.
 *
 * This DAO handles all database operations related to users:
 * - Authentication (login validation)
 * - User CRUD operations (create, read, update, delete)
 * - Password management (hashing, updating)
 * - User statistics for dashboards
 *
 * SECURITY FEATURES:
 * - BCrypt password hashing via PasswordUtil (10 salt rounds)
 * - is_active check prevents disabled/deleted accounts from logging in
 * - PreparedStatements prevent SQL injection
 * - Password column is password_hash (not plaintext!)
 *
 * AUTHENTICATION FLOW:
 * 1. LoginController calls authenticate(username, password)
 * 2. Query checks username AND is_active = true
 * 3. If user found, verify password with BCrypt (PasswordUtil.checkPassword)
 * 4. If password matches, update last_login timestamp
 * 5. Return User object (or null if auth fails)
 *
 * NOTE: This is called from LoginController on a background thread
 * because BCrypt password verification takes ~100ms.
 */
public class UserDAO {
    
    /**
     * Authenticate a user by username and password.
     *
     * SECURITY CHECKS (in order):
     * 1. Username exists in database
     * 2. Account is active (is_active = true)
     * 3. Password matches BCrypt hash
     *
     * WHY is_active CHECK?
     * Admins can disable accounts without deleting them. Disabled accounts:
     * - Keep all their data intact (audit trail)
     * - Cannot log in (blocked at database level)
     * - Can be re-enabled later without recreating the account
     *
     * This is better than deleting users because:
     * - Preserves attendance history
     * - Preserves requisition history
     * - Prevents foreign key issues
     *
     * @param username The username to authenticate
     * @param password The plaintext password (will be checked against BCrypt hash)
     * @return User object if authentication succeeds, null if it fails
     */
    public User authenticate(String username, String password) {
        // IMPORTANT: Query filters by is_active = true at database level
        // This prevents disabled accounts from even being retrieved
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = true";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // User exists and is active - now check the password

                // Get the BCrypt hash from database (NOT plaintext!)
                // Column name is password_hash to make it obvious it's hashed
                String hashedPassword = rs.getString("password_hash");

                // Verify password using BCrypt comparison
                // This call takes ~100ms due to BCrypt's intentional slowness
                // That's why LoginController runs this on a background thread
                // PasswordUtil handles legacy plaintext passwords for migration
                if (PasswordUtil.checkPassword(password, hashedPassword)) {
                    // Password matches! Authentication successful

                    // Extract all user data from the result set
                    User user = extractUserFromResultSet(rs);

                    // Update last_login timestamp (helps track inactive accounts)
                    // This runs async - we don't wait for it to complete
                    updateLastLogin(user.getUserId());

                    return user;  // Authentication successful
                }
            }

            // If we reach here, authentication failed:
            // - Username doesn't exist, OR
            // - Account is inactive (is_active = false), OR
            // - Password doesn't match
            // For security, we don't tell the caller WHICH one failed

        } catch (SQLException e) {
            // Database error (connection failure, query syntax error, etc.)
            e.printStackTrace();
        }

        // Return null for any failure case
        // LoginController will show generic "Invalid username or password" message
        return null;
    }
    
    /**
     * Create a new user with a hashed password.
     *
     * IMPORTANT: This method takes a PLAINTEXT password and hashes it before storing.
     * We NEVER store plaintext passwords in the database.
     *
     * Password hashing happens via PasswordUtil.hashPassword() which uses BCrypt with 10 rounds.
     * Each call produces a different hash (random salt) even for the same password.
     *
     * @param user User object with username, email, role, etc. (but no password)
     * @param plainPassword The plaintext password to hash and store
     * @return true if user created successfully, false otherwise
     */
    public boolean createUser(User user, String plainPassword) {
        String sql = "INSERT INTO users (username, password_hash, email, role, " +
                    "first_name, last_name, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());

            // CRITICAL: Hash the password before storing it
            // PasswordUtil.hashPassword() uses BCrypt with 10 salt rounds
            // This takes ~100ms but only happens once (at user creation time)
            stmt.setString(2, PasswordUtil.hashPassword(plainPassword));

            stmt.setString(3, user.getEmail());

            // Store role as string (ADMIN, MANAGER, EMPLOYEE, SUPPLIER)
            // Database stores it as VARCHAR, we use enum in Java
            stmt.setString(4, user.getRole().name());

            stmt.setString(5, user.getFirstName());
            stmt.setString(6, user.getLastName());
            stmt.setBoolean(7, user.isActive());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the auto-generated user_id from the database
                // Statement.RETURN_GENERATED_KEYS enables this
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    // Set the generated ID back into the user object
                    // Now the caller knows the new user's ID
                    user.setUserId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            // Common causes:
            // - Duplicate username (UNIQUE constraint violation)
            // - Invalid email format
            // - Database connection failure
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Retrieve a user by their unique ID.
     * Used when you already know the user_id (e.g., from a session or foreign key).
     *
     * @param userId The user's ID (primary key)
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractUserFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Get all users in the system, sorted by creation date (newest first).
     * Used for admin user management screens.
     *
     * NOTE: This includes inactive users. If you only want active users,
     * filter the list or add a WHERE is_active = true clause.
     *
     * @return List of all users (may be empty, never null)
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();

        // Sort by created_at DESC so newest users appear first in admin UI
        String sql = "SELECT * FROM users ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(extractUserFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    
    /**
     * Update user information (but NOT the password - use updatePassword for that).
     *
     * IMPORTANT: This does NOT update the password.
     * Passwords require special handling (hashing) via updatePassword().
     *
     * SECURITY NOTE: Admins can use this to disable accounts by setting is_active = false.
     * Disabled accounts cannot log in (blocked by authenticate() query).
     *
     * @param user User object with updated fields (must have user_id set)
     * @return true if update succeeded, false otherwise
     */
    public boolean updateUser(User user) {
        // Notice: password_hash is NOT in this UPDATE statement
        // Use updatePassword() if you need to change the password
        String sql = "UPDATE users SET username = ?, email = ?, role = ?, " +
                    "first_name = ?, last_name = ?, is_active = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getRole().name());
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            stmt.setBoolean(6, user.isActive());
            stmt.setInt(7, user.getUserId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Update a user's password (separate from updateUser for security reasons).
     *
     * WHY A SEPARATE METHOD?
     * - Password changes require special validation (current password check, strength requirements)
     * - Hashing is expensive (~100ms) - don't want to hash on every updateUser() call
     * - Audit trail - password changes should be logged separately
     *
     * IMPORTANT: This takes a PLAINTEXT password and hashes it.
     * Don't pass an already-hashed password - it will be double-hashed!
     *
     * @param userId The user whose password to change
     * @param newPassword The new plaintext password (will be BCrypt hashed)
     * @return true if password updated successfully, false otherwise
     */
    public boolean updatePassword(int userId, String newPassword) {
        String sql = "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Hash the new password with BCrypt before storing
            // This generates a fresh salt, so the hash will be different even if
            // the user sets their password back to a previous value
            stmt.setString(1, PasswordUtil.hashPassword(newPassword));
            stmt.setInt(2, userId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Delete a user from the database.
     *
     * WARNING: This permanently deletes the user!
     * In most cases, you should use updateUser() to set is_active = false instead.
     *
     * WHY SOFT DELETE (is_active = false) IS BETTER:
     * - Preserves audit trail (who created this requisition?)
     * - Prevents foreign key errors (if employees, attendance, etc. reference this user)
     * - Allows account recovery if deletion was a mistake
     *
     * WHEN TO USE HARD DELETE:
     * - Compliance requirements (GDPR "right to be forgotten")
     * - Test data cleanup
     * - Duplicate accounts
     *
     * @param userId The user to delete
     * @return true if deletion succeeded, false otherwise
     */
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // Common cause: Foreign key constraint violation
            // (e.g., can't delete user if they have attendance records)
            e.printStackTrace();
        }
        return false;
    }

    // ========== STATISTICS METHODS FOR DASHBOARD ==========
    // These methods provide aggregate counts for admin dashboard metrics

    /**
     * Count all users in the system (both active and inactive).
     * Used for admin dashboard "Total Users" metric.
     *
     * @return Total number of users (0 if query fails)
     */
    public int getTotalUserCount() {
        String sql = "SELECT COUNT(*) as count FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;  // Return 0 instead of throwing exception (dashboard can show "N/A")
    }

    /**
     * Count users who can log in (is_active = true).
     * Used for admin dashboard "Active Users" metric.
     *
     * Active users can:
     * - Log in to the system
     * - Perform actions based on their role
     * - Show up in employee/manager lists
     *
     * @return Number of active users (0 if query fails)
     */
    public int getActiveUserCount() {
        String sql = "SELECT COUNT(*) as count FROM users WHERE is_active = true";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Count users who have been disabled (is_active = false).
     * Used for admin dashboard "Inactive Users" metric.
     *
     * Inactive users:
     * - Cannot log in (blocked by authenticate() query)
     * - Still exist in database (for audit trail)
     * - Can be re-activated by admin if needed
     *
     * This helps admins track:
     * - Former employees who left the company
     * - Temporarily suspended accounts
     * - Accounts pending deletion
     *
     * @return Number of inactive users (0 if query fails)
     */
    public int getInactiveUserCount() {
        String sql = "SELECT COUNT(*) as count FROM users WHERE is_active = false";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Count how many different roles are in use.
     * Used for admin dashboard to show role diversity.
     *
     * Possible roles (from UserRole enum):
     * - ADMIN
     * - MANAGER
     * - EMPLOYEE
     * - SUPPLIER
     *
     * Example results:
     * - If only admins and employees exist: returns 2
     * - If all 4 roles have at least one user: returns 4
     *
     * @return Number of distinct roles in use (0 if query fails)
     */
    public int getDistinctRoleCount() {
        String sql = "SELECT COUNT(DISTINCT role) as count FROM users";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    /**
     * Update the user's last login timestamp to NOW.
     * Called automatically after successful authentication.
     *
     * WHY TRACK LAST LOGIN?
     * - Helps admins identify inactive accounts (not logged in for 90+ days)
     * - Security monitoring (detect compromised accounts based on unusual login times)
     * - Audit trail for compliance
     *
     * NOTE: This method doesn't throw exceptions or return a value.
     * If it fails, it just logs the error and continues. We don't want to
     * block a successful login just because we couldn't update a timestamp.
     *
     * @param userId The user who just logged in
     */
    private void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = NOW() WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.executeUpdate();

            // We don't check if the update succeeded - this is a "best effort" operation
            // If it fails, the user still gets logged in (more important than tracking timestamp)

        } catch (SQLException e) {
            // Log the error but don't propagate it
            // A failed timestamp update shouldn't prevent login
            e.printStackTrace();
        }
    }
    
    /**
     * Extract a User object from a database result set.
     * This is a helper method used by all the query methods above.
     *
     * PATTERN: "Extractor Method"
     * Instead of repeating this mapping logic in every method, we centralize it here.
     * This makes it easier to add new fields to the User model - just update this one method.
     *
     * NULL HANDLING:
     * - created_at and last_login can be NULL in the database
     * - We check for null before converting Timestamp → LocalDateTime
     * - If null, the User object's field stays null (which is fine)
     *
     * ROLE CONVERSION:
     * - Database stores role as VARCHAR ("ADMIN", "MANAGER", etc.)
     * - Java uses enum (UserRole.ADMIN, UserRole.MANAGER, etc.)
     * - UserRole.valueOf() converts string → enum
     * - This will throw IllegalArgumentException if database has invalid role value
     *
     * @param rs The result set positioned at a user row
     * @return User object with all fields populated from the current row
     * @throws SQLException if column names don't exist or types don't match
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();

        // Extract primary key and basic fields
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));

        // Password hash (NOT plaintext!)
        // Column name is password_hash to make it obvious it's hashed
        user.setPasswordHash(rs.getString("password_hash"));

        user.setEmail(rs.getString("email"));

        // Convert string role to enum
        // Database: "ADMIN" (VARCHAR) → Java: UserRole.ADMIN (enum)
        user.setRole(UserRole.valueOf(rs.getString("role")));

        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setActive(rs.getBoolean("is_active"));

        // Handle nullable timestamp fields
        // created_at should always have a value (database default NOW())
        // last_login is null until first login
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        Timestamp lastLogin = rs.getTimestamp("last_login");
        if (lastLogin != null) {
            user.setLastLogin(lastLogin.toLocalDateTime());
        }

        return user;
    }
}
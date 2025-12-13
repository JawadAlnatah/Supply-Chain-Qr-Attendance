package com.team.supplychain.dao;

import com.team.supplychain.models.AuditLog;
import com.team.supplychain.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for audit logging and compliance tracking.
 *
 * WHY AUDIT LOGS?
 * Audit logs create an immutable record of WHO did WHAT, WHEN, and WHETHER it succeeded.
 * This is critical for:
 * - Security (detect unauthorized access, track suspicious activity)
 * - Compliance (GDPR, SOX, HIPAA all require audit trails)
 * - Debugging (what changed before the system broke?)
 * - Accountability (who approved that purchase order?)
 *
 * WHAT GETS LOGGED:
 * - User logins (successful and failed attempts)
 * - Data modifications (CREATE, UPDATE, DELETE)
 * - Security incidents (failed access attempts, password resets)
 * - Configuration changes (settings updates, user role changes)
 *
 * KEY FEATURES:
 * - Dynamic filtering (by action, module, result, search text)
 * - Pagination support (limit/offset for large datasets)
 * - Log archival (automatically delete old SUCCESS logs, keep failures)
 * - Anonymous logging (for failed logins where user_id is unknown)
 *
 * PERFORMANCE CONSIDERATIONS:
 * - audit_logs table can grow VERY large (thousands of rows per day)
 * - All queries use indexes on timestamp, action_type, module, result
 * - Archive old SUCCESS logs regularly to keep table manageable
 * - Failed/Warning logs are preserved indefinitely (security requirement)
 *
 * THREAD SAFETY:
 * All methods are called from background threads (LoginController, etc.)
 * to avoid blocking the UI during logging operations.
 */
public class AuditLogDAO {

    /**
     * Create an audit log entry for tracking system actions.
     *
     * This is the core logging method - all other log methods call this one.
     *
     * NULLABLE user_id:
     * userId can be null for actions where we don't know the user (e.g., failed login attempts).
     * We still want to log the attempt even if we can't identify the user.
     *
     * LOG CODE GENERATION:
     * Each log gets a unique code like "LOG0001247" for easy reference.
     * This is generated BEFORE the insert to avoid race conditions.
     *
     * PRIVACY NOTE:
     * IP address and location tracking were intentionally removed to avoid GDPR compliance issues.
     * We don't need to know WHERE someone logged in from, just THAT they logged in.
     *
     * COMMON ACTION TYPES:
     * - LOGIN / LOGOUT (authentication events)
     * - CREATE / UPDATE / DELETE (data modifications)
     * - VIEW / EXPORT (data access)
     * - SECURITY_INCIDENT (suspicious activity)
     *
     * COMMON MODULES:
     * - Authentication, Users, Employees, Inventory, Requisitions, Settings, Security
     *
     * @param userId The ID of the user performing the action (null for anonymous actions)
     * @param username The username performing the action (helps with failed login tracking)
     * @param actionType Type of action (CREATE, UPDATE, DELETE, LOGIN, etc.)
     * @param module The module/feature being accessed (Users, Inventory, Settings, etc.)
     * @param description Detailed description of what happened
     * @param result SUCCESS, FAILED, or WARNING
     * @return true if log was created successfully, false otherwise
     */
    public boolean createAuditLog(
            Integer userId,
            String username,
            String actionType,
            String module,
            String description,
            String result) {

        // Generate log code BEFORE opening database connection
        // This avoids holding a connection while generating the code
        String logCode = generateLogCode();

        String sql = "INSERT INTO audit_logs (log_code, user_id, username, action_type, " +
                    "module, description, result) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, logCode);

            // Handle nullable user_id
            // Failed login attempts have no user_id (we don't know who they are yet)
            if (userId != null) {
                stmt.setInt(2, userId);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }

            stmt.setString(3, username);
            stmt.setString(4, actionType);
            stmt.setString(5, module);
            stmt.setString(6, description);
            stmt.setString(7, result);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            // If logging fails, we don't want to crash the application
            // Just print the error and continue
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Convenience method for logging successful actions.
     * Automatically sets result = "SUCCESS" so you don't have to.
     *
     * Usage example:
     * auditLogDAO.logSuccess(user.getUserId(), user.getUsername(),
     *                        "CREATE", "Inventory", "Added 50 units of SKU-12345");
     *
     * @return true if log created successfully
     */
    public boolean logSuccess(Integer userId, String username, String actionType,
                             String module, String description) {
        return createAuditLog(userId, username, actionType, module, description, "SUCCESS");
    }

    /**
     * Convenience method for logging failed actions.
     * Automatically sets result = "FAILED" so you don't have to.
     *
     * Use this for:
     * - Failed login attempts
     * - Validation errors
     * - Permission denied errors
     * - Database errors
     *
     * @return true if log created successfully
     */
    public boolean logFailure(Integer userId, String username, String actionType,
                             String module, String description) {
        return createAuditLog(userId, username, actionType, module, description, "FAILED");
    }

    /**
     * Log a security incident with WARNING severity.
     *
     * Use this for suspicious activity that should be reviewed:
     * - Multiple failed login attempts (possible brute force)
     * - Access attempts to unauthorized resources
     * - Unusual activity patterns
     * - QR code tampering attempts
     *
     * WARNING logs are NEVER archived - they're kept indefinitely for security audits.
     *
     * @param userId User ID if known (can be null for anonymous incidents)
     * @param username Username associated with incident
     * @param description What happened
     * @return true if log created successfully
     */
    public boolean logSecurityIncident(Integer userId, String username, String description) {
        return createAuditLog(userId, username, "SECURITY_INCIDENT", "Security", description, "WARNING");
    }

    /**
     * Generate a unique log code for easy reference (e.g., "LOG0001247").
     *
     * FORMAT: LOG + 7-digit zero-padded number
     * Examples: LOG0000001, LOG0001247, LOG9999999
     *
     * GENERATION STRATEGY:
     * 1. Find the highest log_id currently in the database
     * 2. Add 1 to get the next number
     * 3. Format as LOG + zero-padded number
     *
     * RACE CONDITION WARNING:
     * There's a potential race condition here if two threads call this simultaneously:
     * - Thread 1: reads MAX(log_id) = 100
     * - Thread 2: reads MAX(log_id) = 100 (same value!)
     * - Thread 1: generates LOG0000101
     * - Thread 2: generates LOG0000101 (duplicate!)
     *
     * In practice, this is unlikely because:
     * - Audit logging happens on background threads (not super frequent)
     * - The window for collision is tiny (~1ms)
     * - Even if it happens, log_id (primary key) is still unique - only the log_code duplicates
     *
     * BETTER SOLUTION (for future):
     * Use a database sequence or UUID instead of MAX(log_id).
     * But this works fine for current usage levels.
     *
     * FALLBACK:
     * If MAX query fails (e.g., empty table), use timestamp-based code.
     * This ensures logging always works, even on first run.
     *
     * @return Unique log code string (e.g., "LOG0001247")
     */
    private String generateLogCode() {
        String sql = "SELECT MAX(log_id) as max_id FROM audit_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int maxId = rs.getInt("max_id");

                // Format as LOG + 7-digit zero-padded number
                // String.format("%07d", 123) → "0000123"
                return String.format("LOG%07d", maxId + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fallback for errors or empty table
        // Uses current timestamp in milliseconds to ensure uniqueness
        // Example: "LOG1702476123456"
        return "LOG" + System.currentTimeMillis();
    }

    /**
     * Get count of audit logs for statistics
     */
    public int getAuditLogCount() {
        String sql = "SELECT COUNT(*) as count FROM audit_logs";

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
     * Get count of recent audit logs (last 24 hours)
     */
    public int getRecentAuditLogCount() {
        String sql = "SELECT COUNT(*) as count FROM audit_logs " +
                    "WHERE timestamp >= DATE_SUB(NOW(), INTERVAL 24 HOUR)";

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
     * Get all audit logs with pagination
     * @param limit Maximum number of records to return
     * @param offset Number of records to skip
     * @return List of AuditLog objects ordered by timestamp DESC
     */
    public List<AuditLog> getAllAuditLogs(int limit, int offset) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Get filtered audit logs with dynamic WHERE clause.
     *
     * DYNAMIC SQL PATTERN:
     * This method builds a SQL query on-the-fly based on which filters are provided.
     * Instead of writing 16 different methods for all filter combinations,
     * we construct one flexible query.
     *
     * HOW IT WORKS:
     * 1. Start with base query: "SELECT * FROM audit_logs WHERE 1=1"
     *    (WHERE 1=1 is a trick that makes it easy to append AND conditions)
     * 2. For each non-null filter, append "AND column = ?" to the SQL
     * 3. Add the parameter value to a List<Object> in the same order
     * 4. Use setObject() to bind all parameters to the PreparedStatement
     *
     * EXAMPLE:
     * Input: actionType="LOGIN", module=null, result="FAILED", searchText=null
     * SQL becomes: "SELECT * FROM audit_logs WHERE 1=1 AND action_type = ? AND result = ? ORDER BY..."
     * Params: ["LOGIN", "FAILED", limit, offset]
     *
     * WHY THIS IS SAFE:
     * Even though we're building SQL dynamically, we NEVER concatenate user input directly.
     * We always use ? placeholders and PreparedStatement.setObject().
     * This prevents SQL injection attacks.
     *
     * SEARCH PATTERN:
     * searchText uses LIKE with % wildcards to match anywhere in the field.
     * "admin" matches: "admin", "Administrator", "badmin", etc.
     *
     * SPECIAL FILTER VALUES:
     * "All Actions", "All Modules", "All Results" mean "no filter" (from UI dropdowns).
     * We treat these the same as null.
     *
     * @param actionType Filter by action type (null for no filter)
     * @param module Filter by module (null for no filter)
     * @param result Filter by result (null for no filter)
     * @param searchText Search in username, description, log_code (null for no filter)
     * @param limit Maximum number of records to return (for pagination)
     * @param offset Number of records to skip (for pagination)
     * @return List of filtered AuditLog objects, ordered by timestamp (newest first)
     */
    public List<AuditLog> getFilteredAuditLogs(String actionType, String module, String result,
                                                 String searchText, int limit, int offset) {
        List<AuditLog> logs = new ArrayList<>();

        // Start with base query
        // WHERE 1=1 is a trick: it's always true, but lets us append "AND ..." conditions easily
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs WHERE 1=1");

        // Store parameters in order they appear in the query
        // This list will be used to call setObject(index, value)
        List<Object> params = new ArrayList<>();

        // Build WHERE clause dynamically based on which filters are provided
        if (actionType != null && !actionType.isEmpty() && !"All Actions".equals(actionType)) {
            sql.append(" AND action_type = ?");
            params.add(actionType);
        }
        if (module != null && !module.isEmpty() && !"All Modules".equals(module)) {
            sql.append(" AND module = ?");
            params.add(module);
        }
        if (result != null && !result.isEmpty() && !"All Results".equals(result)) {
            sql.append(" AND result = ?");
            params.add(result);
        }
        if (searchText != null && !searchText.trim().isEmpty()) {
            // Search in multiple columns with OR
            // LIKE '%text%' matches text anywhere in the field
            sql.append(" AND (username LIKE ? OR description LIKE ? OR log_code LIKE ?)");

            String searchPattern = "%" + searchText.trim() + "%";
            params.add(searchPattern);  // For username LIKE ?
            params.add(searchPattern);  // For description LIKE ?
            params.add(searchPattern);  // For log_code LIKE ?
        }

        // Always sort by newest first (DESC = descending)
        sql.append(" ORDER BY timestamp DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Bind all parameters to the PreparedStatement
            // setObject() works with any type (String, Integer, etc.)
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));  // JDBC indexes start at 1, not 0!
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * Get count of filtered audit logs (for pagination).
     *
     * This uses the SAME dynamic WHERE clause as getFilteredAuditLogs(),
     * but returns COUNT(*) instead of the actual rows.
     *
     * WHY A SEPARATE METHOD?
     * Pagination needs two pieces of information:
     * 1. The actual logs for the current page (from getFilteredAuditLogs)
     * 2. The total count to calculate total pages (from this method)
     *
     * Example: 573 total results ÷ 20 per page = 29 pages
     *
     * IMPORTANT: Keep the WHERE clause logic IDENTICAL to getFilteredAuditLogs()!
     * If they differ, the count won't match the actual results.
     *
     * @param actionType Filter by action type (null for no filter)
     * @param module Filter by module (null for no filter)
     * @param result Filter by result (null for no filter)
     * @param searchText Search in username, description, log_code (null for no filter)
     * @return Total count of matching records (for calculating page count)
     */
    public int getFilteredAuditLogCount(String actionType, String module, String result, String searchText) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as count FROM audit_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Build WHERE clause (MUST match getFilteredAuditLogs exactly!)
        if (actionType != null && !actionType.isEmpty() && !"All Actions".equals(actionType)) {
            sql.append(" AND action_type = ?");
            params.add(actionType);
        }
        if (module != null && !module.isEmpty() && !"All Modules".equals(module)) {
            sql.append(" AND module = ?");
            params.add(module);
        }
        if (result != null && !result.isEmpty() && !"All Results".equals(result)) {
            sql.append(" AND result = ?");
            params.add(result);
        }
        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND (username LIKE ? OR description LIKE ? OR log_code LIKE ?)");
            String searchPattern = "%" + searchText.trim() + "%";
            params.add(searchPattern);
            params.add(searchPattern);
            params.add(searchPattern);
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Bind parameters (same pattern as getFilteredAuditLogs)
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get count of today's activities
     * @return Number of audit logs created today
     */
    public int getTodayActivityCount() {
        String sql = "SELECT COUNT(*) as count FROM audit_logs WHERE DATE(timestamp) = CURDATE()";

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
     * Get count of logs by module
     * @param module The module name (e.g., "Database", "Settings", "Inventory")
     * @return Count of logs for this module
     */
    public int getCountByModule(String module) {
        String sql = "SELECT COUNT(*) as count FROM audit_logs WHERE module = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, module);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get count of logs by user type (system vs non-system)
     * @param isSystemUser true for system logs, false for user logs
     * @return Count of logs
     */
    public int getCountByUserType(boolean isSystemUser) {
        String sql;
        if (isSystemUser) {
            sql = "SELECT COUNT(*) as count FROM audit_logs WHERE username = 'system' OR username LIKE 'SYSTEM%'";
        } else {
            sql = "SELECT COUNT(*) as count FROM audit_logs WHERE username != 'system' AND username NOT LIKE 'SYSTEM%'";
        }

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
     * Get a single audit log by ID (for detail view)
     * @param logId The log ID
     * @return AuditLog object or null if not found
     */
    public AuditLog getAuditLogById(int logId) {
        String sql = "SELECT * FROM audit_logs WHERE log_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, logId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToAuditLog(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Archive (delete) old successful logs to prevent table bloat.
     *
     * WHY DELETE OLD LOGS?
     * The audit_logs table can grow MASSIVE - thousands of entries per day.
     * After 1 year, you could have 365,000+ rows! This slows down queries.
     *
     * SELECTIVE DELETION:
     * - Deletes: SUCCESS logs (routine operations that completed normally)
     * - Keeps: FAILED and WARNING logs (security incidents, errors)
     *
     * WHY KEEP FAILURES/WARNINGS?
     * These indicate potential security issues or system problems.
     * Security audits often require keeping failed login attempts indefinitely.
     *
     * RECOMMENDED SCHEDULE:
     * Run this monthly with daysOld = 90 (keep last 3 months of successful logs).
     * Failed/warning logs accumulate much slower since they're rare.
     *
     * EXAMPLE USAGE:
     * archiveOldLogs(90) - delete SUCCESS logs older than 90 days
     *
     * DATE_SUB EXPLAINED:
     * DATE_SUB(NOW(), INTERVAL ? DAY) subtracts days from current date.
     * If today is 2024-01-15 and daysOld=90, it returns 2024-10-17.
     *
     * @param daysOld Number of days - logs older than this will be deleted
     * @return Number of logs deleted (0 if none match or error occurs)
     */
    public int archiveOldLogs(int daysOld) {
        // Only delete SUCCESS logs - keep FAILED and WARNING forever
        String sql = "DELETE FROM audit_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY) AND result = 'SUCCESS'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, daysOld);
            return stmt.executeUpdate();  // Returns number of rows deleted
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Extract an AuditLog object from a database result set.
     * This is a helper method used by all the query methods above.
     *
     * PATTERN: "Mapper Method"
     * Instead of repeating this mapping logic in every query method,
     * we centralize it here. Makes it easy to add new fields to AuditLog.
     *
     * NULLABLE FIELDS:
     * - user_id can be NULL (for failed login attempts where we don't know the user)
     * - ip_address is always NULL (removed for privacy/GDPR compliance)
     * - We use rs.getObject() instead of rs.getInt() for user_id to handle NULL properly
     *
     * TIMESTAMP CONVERSION:
     * - Database stores: TIMESTAMP (SQL type)
     * - Java uses: LocalDateTime (java.time package)
     * - Conversion: getTimestamp().toLocalDateTime()
     *
     * @param rs The result set positioned at an audit log row
     * @return AuditLog object with all fields populated from the current row
     * @throws SQLException if column names don't exist or types don't match
     */
    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        return new AuditLog(
            rs.getInt("log_id"),
            rs.getString("log_code"),

            // Convert database TIMESTAMP to Java LocalDateTime
            rs.getTimestamp("timestamp").toLocalDateTime(),

            // user_id is nullable - use getObject() to handle NULL correctly
            // getInt() would return 0 for NULL, which could be confusing
            (Integer) rs.getObject("user_id"),

            rs.getString("username"),
            rs.getString("action_type"),
            rs.getString("module"),
            rs.getString("description"),

            // ip_address is always NULL (privacy feature)
            rs.getString("ip_address"),

            rs.getString("result")
        );
    }
}

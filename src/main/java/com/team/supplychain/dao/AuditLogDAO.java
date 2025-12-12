package com.team.supplychain.dao;

import com.team.supplychain.models.AuditLog;
import com.team.supplychain.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    /**
     * Logs an action to the audit_logs table
     * Note: IP address and location tracking removed for privacy and security reasons
     *
     * @param userId The ID of the user performing the action
     * @param username The username of the user performing the action
     * @param actionType Type of action: CREATE, UPDATE, DELETE, LOGIN, LOGOUT, SECURITY_INCIDENT, etc.
     * @param module The module/feature: Users, Inventory, Settings, Authentication, Security, etc.
     * @param description Detailed description of what was done
     * @param result SUCCESS, FAILED, or WARNING
     * @return true if log was created successfully
     */
    public boolean createAuditLog(
            Integer userId,
            String username,
            String actionType,
            String module,
            String description,
            String result) {

        // Generate log code first (before opening the main connection)
        String logCode = generateLogCode();

        String sql = "INSERT INTO audit_logs (log_code, user_id, username, action_type, " +
                    "module, description, result) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, logCode);
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
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Convenience method for successful actions
     */
    public boolean logSuccess(Integer userId, String username, String actionType,
                             String module, String description) {
        return createAuditLog(userId, username, actionType, module, description, "SUCCESS");
    }

    /**
     * Convenience method for failed actions
     */
    public boolean logFailure(Integer userId, String username, String actionType,
                             String module, String description) {
        return createAuditLog(userId, username, actionType, module, description, "FAILED");
    }

    /**
     * Log security incident (e.g., when security guard rejects someone)
     */
    public boolean logSecurityIncident(Integer userId, String username, String description) {
        return createAuditLog(userId, username, "SECURITY_INCIDENT", "Security", description, "WARNING");
    }

    /**
     * Generate unique log code like LOG0001247
     */
    private String generateLogCode() {
        String sql = "SELECT MAX(log_id) as max_id FROM audit_logs";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                return String.format("LOG%07d", maxId + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Fallback to timestamp-based code
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
     * Get filtered audit logs with dynamic WHERE clause
     * @param actionType Filter by action type (null for no filter)
     * @param module Filter by module (null for no filter)
     * @param result Filter by result (null for no filter)
     * @param searchText Search in username, description, log_code (null for no filter)
     * @param limit Maximum number of records
     * @param offset Number of records to skip
     * @return List of filtered AuditLog objects
     */
    public List<AuditLog> getFilteredAuditLogs(String actionType, String module, String result,
                                                 String searchText, int limit, int offset) {
        List<AuditLog> logs = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM audit_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Build WHERE clause dynamically
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

        sql.append(" ORDER BY timestamp DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // Set parameters
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
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
     * Get count of filtered audit logs (for pagination)
     * @param actionType Filter by action type (null for no filter)
     * @param module Filter by module (null for no filter)
     * @param result Filter by result (null for no filter)
     * @param searchText Search in username, description, log_code (null for no filter)
     * @return Total count of matching records
     */
    public int getFilteredAuditLogCount(String actionType, String module, String result, String searchText) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) as count FROM audit_logs WHERE 1=1");
        List<Object> params = new ArrayList<>();

        // Build WHERE clause (same as getFilteredAuditLogs)
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

            // Set parameters
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
     * Archive (delete) old successful logs
     * Only deletes SUCCESS logs, preserves FAILED and WARNING logs
     * @param daysOld Number of days - logs older than this will be deleted
     * @return Number of logs deleted
     */
    public int archiveOldLogs(int daysOld) {
        String sql = "DELETE FROM audit_logs WHERE timestamp < DATE_SUB(NOW(), INTERVAL ? DAY) AND result = 'SUCCESS'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, daysOld);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Helper method to map ResultSet to AuditLog object
     * @param rs ResultSet positioned at a row
     * @return AuditLog object
     * @throws SQLException if database access error occurs
     */
    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        return new AuditLog(
            rs.getInt("log_id"),
            rs.getString("log_code"),
            rs.getTimestamp("timestamp").toLocalDateTime(),
            (Integer) rs.getObject("user_id"),  // Nullable
            rs.getString("username"),
            rs.getString("action_type"),
            rs.getString("module"),
            rs.getString("description"),
            rs.getString("ip_address"),
            rs.getString("result")
        );
    }
}

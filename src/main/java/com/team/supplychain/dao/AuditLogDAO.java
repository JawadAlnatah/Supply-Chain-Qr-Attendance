package com.team.supplychain.dao;

import com.team.supplychain.utils.DatabaseConnection;

import java.sql.*;

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
}

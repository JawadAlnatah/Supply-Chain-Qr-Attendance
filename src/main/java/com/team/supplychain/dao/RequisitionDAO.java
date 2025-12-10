package com.team.supplychain.dao;

import com.team.supplychain.models.Requisition;
import com.team.supplychain.models.RequisitionItem;
import com.team.supplychain.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for Requisition operations
 * Handles database operations for purchase requisitions
 */
public class RequisitionDAO {

    /**
     * Create a new requisition with its items
     */
    public Integer createRequisition(Requisition requisition) {
        String requisitionSql = "INSERT INTO requisitions (requisition_code, requested_by, supplier_id, " +
                "category, department, priority, justification, status, total_amount, total_items, request_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(requisitionSql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, requisition.getRequisitionCode());
            stmt.setInt(2, requisition.getRequestedBy());

            if (requisition.getSupplierId() != null) {
                stmt.setInt(3, requisition.getSupplierId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }

            stmt.setString(4, requisition.getCategory());
            stmt.setString(5, requisition.getDepartment());
            stmt.setString(6, requisition.getPriority());
            stmt.setString(7, requisition.getJustification());
            stmt.setString(8, requisition.getStatus());
            stmt.setBigDecimal(9, requisition.getTotalAmount());
            stmt.setInt(10, requisition.getTotalItems());
            stmt.setTimestamp(11, Timestamp.valueOf(requisition.getRequestDate()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer requisitionId = generatedKeys.getInt(1);
                        requisition.setRequisitionId(requisitionId);

                        // Insert requisition items
                        if (requisition.getItems() != null && !requisition.getItems().isEmpty()) {
                            createRequisitionItems(requisitionId, requisition.getItems());
                        }

                        return requisitionId;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create requisition items for a requisition
     */
    private void createRequisitionItems(Integer requisitionId, List<RequisitionItem> items) {
        String sql = "INSERT INTO requisition_items (requisition_id, item_name, category, quantity, unit_price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (RequisitionItem item : items) {
                stmt.setInt(1, requisitionId);
                stmt.setString(2, item.getItemName());
                stmt.setString(3, item.getCategory());
                stmt.setInt(4, item.getQuantity());
                stmt.setBigDecimal(5, item.getUnitPrice());
                stmt.setBigDecimal(6, item.getSubtotal());
                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all requisitions for a specific user
     */
    public List<Requisition> getRequisitionsByUser(Integer userId) {
        List<Requisition> requisitions = new ArrayList<>();
        String sql = "SELECT r.*, u.first_name, u.last_name, " +
                "rv.first_name as reviewer_first_name, rv.last_name as reviewer_last_name " +
                "FROM requisitions r " +
                "LEFT JOIN users u ON r.requested_by = u.user_id " +
                "LEFT JOIN users rv ON r.reviewed_by = rv.user_id " +
                "WHERE r.requested_by = ? " +
                "ORDER BY r.request_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Requisition requisition = mapResultSetToRequisition(rs);
                    requisitions.add(requisition);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Load items for each requisition in a separate pass to avoid nested connection issues
        for (Requisition requisition : requisitions) {
            requisition.setItems(getRequisitionItems(requisition.getRequisitionId()));
        }

        return requisitions;
    }

    /**
     * Get requisition items for a specific requisition
     */
    private List<RequisitionItem> getRequisitionItems(Integer requisitionId) {
        List<RequisitionItem> items = new ArrayList<>();
        String sql = "SELECT * FROM requisition_items WHERE requisition_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requisitionId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RequisitionItem item = new RequisitionItem();
                    item.setItemId(rs.getInt("item_id"));
                    item.setRequisitionId(rs.getInt("requisition_id"));
                    item.setItemName(rs.getString("item_name"));
                    item.setCategory(rs.getString("category"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getBigDecimal("unit_price"));
                    item.setSubtotal(rs.getBigDecimal("subtotal"));
                    items.add(item);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return items;
    }

    /**
     * Get a requisition by ID
     */
    public Requisition getRequisitionById(Integer requisitionId) {
        String sql = "SELECT r.*, u.first_name, u.last_name, " +
                "rv.first_name as reviewer_first_name, rv.last_name as reviewer_last_name " +
                "FROM requisitions r " +
                "LEFT JOIN users u ON r.requested_by = u.user_id " +
                "LEFT JOIN users rv ON r.reviewed_by = rv.user_id " +
                "WHERE r.requisition_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, requisitionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Requisition requisition = mapResultSetToRequisition(rs);
                    requisition.setItems(getRequisitionItems(requisitionId));
                    return requisition;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Update requisition status (Approve/Reject)
     */
    public boolean updateRequisitionStatus(Integer requisitionId, String status, Integer reviewedBy, String reviewNotes) {
        String sql = "UPDATE requisitions SET status = ?, reviewed_by = ?, review_date = ?, review_notes = ? " +
                "WHERE requisition_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, reviewedBy);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(4, reviewNotes);
            stmt.setInt(5, requisitionId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Get all pending requisitions (for managers)
     */
    public List<Requisition> getPendingRequisitions() {
        return getRequisitionsByStatus("Pending");
    }

    /**
     * Get requisitions by status
     */
    public List<Requisition> getRequisitionsByStatus(String status) {
        List<Requisition> requisitions = new ArrayList<>();
        String sql = "SELECT r.*, u.first_name, u.last_name, " +
                "rv.first_name as reviewer_first_name, rv.last_name as reviewer_last_name " +
                "FROM requisitions r " +
                "LEFT JOIN users u ON r.requested_by = u.user_id " +
                "LEFT JOIN users rv ON r.reviewed_by = rv.user_id " +
                "WHERE r.status = ? " +
                "ORDER BY r.request_date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Requisition requisition = mapResultSetToRequisition(rs);
                    requisitions.add(requisition);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Load items for each requisition in a separate pass to avoid nested connection issues
        for (Requisition requisition : requisitions) {
            requisition.setItems(getRequisitionItems(requisition.getRequisitionId()));
        }

        return requisitions;
    }

    /**
     * Generate unique requisition code
     */
    public String generateRequisitionCode() {
        String sql = "SELECT MAX(requisition_id) as max_id FROM requisitions";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                int maxId = rs.getInt("max_id");
                return String.format("REQ-%05d", maxId + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "REQ-00001";
    }

    /**
     * Map ResultSet to Requisition object
     */
    private Requisition mapResultSetToRequisition(ResultSet rs) throws SQLException {
        Requisition requisition = new Requisition();

        requisition.setRequisitionId(rs.getInt("requisition_id"));
        requisition.setRequisitionCode(rs.getString("requisition_code"));
        requisition.setRequestedBy(rs.getInt("requested_by"));

        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        if (firstName != null && lastName != null) {
            requisition.setRequesterName(firstName + " " + lastName);
        }

        Integer supplierId = rs.getInt("supplier_id");
        if (!rs.wasNull()) {
            requisition.setSupplierId(supplierId);
        }

        requisition.setCategory(rs.getString("category"));
        requisition.setDepartment(rs.getString("department"));
        requisition.setPriority(rs.getString("priority"));
        requisition.setJustification(rs.getString("justification"));
        requisition.setStatus(rs.getString("status"));
        requisition.setTotalAmount(rs.getBigDecimal("total_amount"));
        requisition.setTotalItems(rs.getInt("total_items"));

        Timestamp requestDate = rs.getTimestamp("request_date");
        if (requestDate != null) {
            requisition.setRequestDate(requestDate.toLocalDateTime());
        }

        Integer reviewedBy = rs.getInt("reviewed_by");
        if (!rs.wasNull()) {
            requisition.setReviewedBy(reviewedBy);

            String reviewerFirstName = rs.getString("reviewer_first_name");
            String reviewerLastName = rs.getString("reviewer_last_name");
            if (reviewerFirstName != null && reviewerLastName != null) {
                requisition.setReviewerName(reviewerFirstName + " " + reviewerLastName);
            }
        }

        Timestamp reviewDate = rs.getTimestamp("review_date");
        if (reviewDate != null) {
            requisition.setReviewDate(reviewDate.toLocalDateTime());
        }

        requisition.setReviewNotes(rs.getString("review_notes"));

        return requisition;
    }

    /**
     * Get requisition count by status for a user
     */
    public int getRequisitionCountByStatus(Integer userId, String status) {
        String sql = "SELECT COUNT(*) as count FROM requisitions WHERE requested_by = ? AND status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, status);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }
}

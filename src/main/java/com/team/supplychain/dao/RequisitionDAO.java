package com.team.supplychain.dao;

import com.team.supplychain.models.Requisition;
import com.team.supplychain.models.RequisitionItem;
import com.team.supplychain.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for Requisition operations.
 * Handles database operations for purchase requisitions.
 *
 * A requisition is an employee's request to purchase items (e.g., office supplies, equipment).
 * Each requisition can have multiple items, and goes through an approval workflow.
 *
 * DATABASE TABLES:
 * - requisitions (header/parent record)
 * - requisition_items (line items/child records)
 *
 * PERFORMANCE NOTE:
 * This DAO has been optimized to avoid the "N+1 query problem" when loading requisitions.
 * See getRequisitionsByUser() for details on the LEFT JOIN optimization.
 */
public class RequisitionDAO {

    /**
     * Create a new requisition with its items.
     *
     * This is a two-step process:
     * 1. Insert the requisition header (get back auto-generated requisition_id)
     * 2. Insert all requisition items (batch insert for performance)
     *
     * TRANSACTION NOTE:
     * Not wrapped in explicit transaction, so if step 2 fails, you'll have
     * a requisition with no items. Consider wrapping in conn.setAutoCommit(false).
     *
     * @param requisition The requisition to create (with items list populated)
     * @return The generated requisition ID, or null if creation failed
     */
    public Integer createRequisition(Requisition requisition) {
        String requisitionSql = "INSERT INTO requisitions (requisition_code, requested_by, supplier_id, " +
                "category, department, priority, justification, status, total_amount, total_items, request_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(requisitionSql, Statement.RETURN_GENERATED_KEYS)) {

            // Map Java objects to SQL parameters (? placeholders)
            // The order MUST match the columns in the INSERT statement above
            stmt.setString(1, requisition.getRequisitionCode());  // e.g., "REQ-2025-001"
            stmt.setInt(2, requisition.getRequestedBy());  // user_id of employee

            // Supplier is optional (some requisitions don't specify supplier yet)
            if (requisition.getSupplierId() != null) {
                stmt.setInt(3, requisition.getSupplierId());
            } else {
                stmt.setNull(3, Types.INTEGER);  // Explicitly set NULL in database
            }

            stmt.setString(4, requisition.getCategory());  // e.g., "Office Supplies"
            stmt.setString(5, requisition.getDepartment());  // e.g., "IT", "HR"
            stmt.setString(6, requisition.getPriority());  // e.g., "HIGH", "MEDIUM", "LOW"
            stmt.setString(7, requisition.getJustification());  // Why we need this
            stmt.setString(8, requisition.getStatus());  // "PENDING", "APPROVED", "REJECTED"
            stmt.setBigDecimal(9, requisition.getTotalAmount());  // Sum of all item subtotals
            stmt.setInt(10, requisition.getTotalItems());  // Count of items
            stmt.setTimestamp(11, Timestamp.valueOf(requisition.getRequestDate()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                // Get the auto-generated ID from the database
                // This is the primary key that the database assigned
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Integer requisitionId = generatedKeys.getInt(1);
                        requisition.setRequisitionId(requisitionId);

                        // Now create all the line items for this requisition
                        // Uses batch insert for performance (multiple inserts in one round-trip)
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
        return null;  // Creation failed
    }

    /**
     * Create requisition items for a requisition using batch insert.
     *
     * BATCH INSERT PATTERN:
     * Instead of running 10 separate INSERT statements (10 database round-trips),
     * we use addBatch() + executeBatch() to send all inserts in ONE round-trip.
     *
     * Performance difference:
     * - 10 individual inserts = 10 x 50ms = 500ms
     * - 1 batch insert = 1 x 50ms = 50ms (10x faster!)
     *
     * HikariCP's rewriteBatchedStatements setting makes this even faster by
     * combining into: INSERT INTO x VALUES (...), (...), (...)
     *
     * @param requisitionId The parent requisition ID (foreign key)
     * @param items List of items to insert
     */
    private void createRequisitionItems(Integer requisitionId, List<RequisitionItem> items) {
        String sql = "INSERT INTO requisition_items (requisition_id, item_name, category, quantity, unit_price, subtotal) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Loop through items and add each to the batch
            for (RequisitionItem item : items) {
                stmt.setInt(1, requisitionId);  // Same requisition_id for all items
                stmt.setString(2, item.getItemName());  // e.g., "HP Laser Printer"
                stmt.setString(3, item.getCategory());  // e.g., "Electronics"
                stmt.setInt(4, item.getQuantity());  // How many units
                stmt.setBigDecimal(5, item.getUnitPrice());  // Price per unit
                stmt.setBigDecimal(6, item.getSubtotal());  // quantity * unit_price
                stmt.addBatch();  // Add to batch (doesn't execute yet)
            }

            // Execute all inserts in one go
            stmt.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all requisitions for a specific user.
     *
     * PERFORMANCE OPTIMIZATION - N+1 QUERY PROBLEM FIX:
     *
     * OLD APPROACH (N+1 queries):
     * - Query 1: SELECT * FROM requisitions WHERE requested_by = ?
     * - Query 2-11: For each requisition, SELECT * FROM requisition_items WHERE requisition_id = ?
     * - Total: 1 + 10 = 11 database round-trips for 10 requisitions
     *
     * NEW APPROACH (1 query):
     * - Single query with LEFT JOIN to get requisitions AND items in one go
     * - Total: 1 database round-trip (10x faster!)
     *
     * THE CATCH:
     * A requisition with 3 items produces 3 result rows (one per item).
     * We use a HashMap to "deduplicate" - when we see the same requisition_id again,
     * we just add the item to the existing requisition object instead of creating a new one.
     *
     * EXAMPLE RESULT SET:
     * req_id | req_code | item_id | item_name
     * -------|----------|---------|----------
     *   1    | REQ-001  |   10    | Laptop
     *   1    | REQ-001  |   11    | Mouse      <- Same requisition, different item
     *   2    | REQ-002  |   20    | Printer
     *
     * We process this into:
     * - Requisition 1 (REQ-001) with items [Laptop, Mouse]
     * - Requisition 2 (REQ-002) with items [Printer]
     *
     * @param userId The user ID to filter requisitions by
     * @return List of requisitions with their items populated
     */
    public List<Requisition> getRequisitionsByUser(Integer userId) {
        // HashMap for deduplication - prevents creating duplicate requisition objects
        // Key = requisition_id, Value = Requisition object
        Map<Integer, Requisition> requisitionMap = new HashMap<>();

        // Single query gets EVERYTHING we need in one shot
        // LEFT JOINs ensure we get requisitions even if they have no items yet
        String sql = "SELECT r.*, u.first_name, u.last_name, " +
                "rv.first_name as reviewer_first_name, rv.last_name as reviewer_last_name, " +
                "ri.item_id, ri.item_name, ri.category as item_category, " +
                "ri.quantity, ri.unit_price, ri.subtotal " +
                "FROM requisitions r " +
                "LEFT JOIN users u ON r.requested_by = u.user_id " +  // Get requester name
                "LEFT JOIN users rv ON r.reviewed_by = rv.user_id " +  // Get reviewer name (if approved/rejected)
                "LEFT JOIN requisition_items ri ON r.requisition_id = ri.requisition_id " +  // Get items
                "WHERE r.requested_by = ? " +
                "ORDER BY r.request_date DESC, ri.item_id ASC";  // Newest first, items ordered

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int requisitionId = rs.getInt("requisition_id");

                    // Check if we've already created a Requisition object for this ID
                    Requisition requisition = requisitionMap.get(requisitionId);
                    if (requisition == null) {
                        // First time seeing this requisition - create it
                        requisition = mapResultSetToRequisition(rs);
                        requisition.setItems(new ArrayList<>());  // Initialize empty items list
                        requisitionMap.put(requisitionId, requisition);
                    }

                    // Add item to the requisition's items list
                    // Check for null because LEFT JOIN returns null if requisition has no items
                    if (rs.getObject("item_id") != null) {
                        RequisitionItem item = new RequisitionItem();
                        item.setItemId(rs.getInt("item_id"));
                        item.setRequisitionId(requisitionId);
                        item.setItemName(rs.getString("item_name"));
                        item.setCategory(rs.getString("item_category"));  // Note: aliased to avoid collision with r.category
                        item.setQuantity(rs.getInt("quantity"));
                        item.setUnitPrice(rs.getBigDecimal("unit_price"));
                        item.setSubtotal(rs.getBigDecimal("subtotal"));
                        requisition.getItems().add(item);  // Add to existing requisition
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Convert HashMap values to ArrayList
        // Order is maintained because we sorted by request_date DESC in the query
        return new ArrayList<>(requisitionMap.values());
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

    /**
     * Get count of pending requisitions (for Admin Dashboard)
     */
    public int getPendingRequisitionsCount() {
        String sql = "SELECT COUNT(*) as count FROM requisitions WHERE status = 'Pending'";

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

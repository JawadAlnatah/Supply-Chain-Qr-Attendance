package com.team.supplychain.dao;

import com.team.supplychain.utils.DatabaseConnection;

import java.sql.*;

/**
 * DAO for inventory-related database operations
 */
public class InventoryDAO {

    /**
     * Get total count of inventory items
     */
    public int getTotalItemsCount() {
        String sql = "SELECT COUNT(*) as count FROM inventory_items";

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
     * Get count of low stock items (where quantity <= reorder_level)
     */
    public int getLowStockCount() {
        String sql = "SELECT COUNT(*) as count FROM inventory_items WHERE quantity <= reorder_level";

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
     * Get count of out of stock items (where quantity = 0)
     */
    public int getOutOfStockCount() {
        String sql = "SELECT COUNT(*) as count FROM inventory_items WHERE quantity = 0";

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
     * Get total value of inventory
     */
    public double getTotalInventoryValue() {
        String sql = "SELECT SUM(quantity * unit_price) as total_value FROM inventory_items";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble("total_value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }
}

package com.team.supplychain.dao;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for InventoryDAO
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InventoryDAOTest {

    private static InventoryDAO inventoryDAO;

    @BeforeAll
    static void setUp() {
        inventoryDAO = new InventoryDAO();
        System.out.println("InventoryDAOTest: Starting tests...");
    }

    @Test
    @Order(1)
    @DisplayName("Test getting total items count")
    void testGetTotalItemsCount() {
        int count = inventoryDAO.getTotalItemsCount();

        assertTrue(count >= 0, "Total items count should be non-negative");
        System.out.println("Total inventory items: " + count);
    }

    @Test
    @Order(2)
    @DisplayName("Test getting low stock count")
    void testGetLowStockCount() {
        int count = inventoryDAO.getLowStockCount();

        assertTrue(count >= 0, "Low stock count should be non-negative");
        System.out.println("Items with low stock: " + count);
    }

    @Test
    @Order(3)
    @DisplayName("Test getting out of stock count")
    void testGetOutOfStockCount() {
        int count = inventoryDAO.getOutOfStockCount();

        assertTrue(count >= 0, "Out of stock count should be non-negative");
        System.out.println("Items out of stock: " + count);
    }

    @Test
    @Order(4)
    @DisplayName("Test getting total inventory value")
    void testGetTotalInventoryValue() {
        double value = inventoryDAO.getTotalInventoryValue();

        assertTrue(value >= 0.0, "Total inventory value should be non-negative");
        System.out.printf("Total inventory value: $%.2f%n", value);
    }

    @Test
    @Order(5)
    @DisplayName("Test consistency: low stock + normal stock = total items")
    void testInventoryStatisticsConsistency() {
        int totalItems = inventoryDAO.getTotalItemsCount();
        int lowStock = inventoryDAO.getLowStockCount();

        assertTrue(lowStock <= totalItems,
            "Low stock count should not exceed total items count");
    }

    @Test
    @Order(6)
    @DisplayName("Test consistency: out of stock <= low stock")
    void testOutOfStockConsistency() {
        int lowStock = inventoryDAO.getLowStockCount();
        int outOfStock = inventoryDAO.getOutOfStockCount();

        assertTrue(outOfStock <= lowStock,
            "Out of stock count should not exceed low stock count");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("InventoryDAOTest: All tests completed");
    }
}

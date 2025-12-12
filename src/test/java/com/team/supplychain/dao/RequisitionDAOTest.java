package com.team.supplychain.dao;

import com.team.supplychain.models.Requisition;
import com.team.supplychain.models.RequisitionItem;
import org.junit.jupiter.api.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for RequisitionDAO
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RequisitionDAOTest {

    private static RequisitionDAO requisitionDAO;
    private static Integer testRequisitionId;

    @BeforeAll
    static void setUp() {
        requisitionDAO = new RequisitionDAO();
        System.out.println("RequisitionDAOTest: Starting tests...");
    }

    @Test
    @Order(1)
    @DisplayName("Test generating requisition code")
    void testGenerateRequisitionCode() {
        String code1 = requisitionDAO.generateRequisitionCode();

        assertNotNull(code1, "Requisition code should not be null");
        assertTrue(code1.startsWith("REQ-"), "Code should start with REQ-");
        assertEquals(9, code1.length(), "Code should be REQ-XXXXX format");
        System.out.println("Generated code: " + code1);

        // Note: The generateRequisitionCode() uses MAX(requisition_id), so calling it twice
        // without creating a requisition in between will return the same code
        // This is expected behavior
    }

    @Test
    @Order(2)
    @DisplayName("Test creating a new requisition")
    void testCreateRequisition() {
        Requisition requisition = new Requisition();
        requisition.setRequisitionCode(requisitionDAO.generateRequisitionCode());
        requisition.setRequestedBy(1); // Admin user
        requisition.setCategory("Office Supplies");
        requisition.setDepartment("IT");
        requisition.setPriority("Medium");
        requisition.setJustification("Testing requisition creation");
        requisition.setStatus("Pending");
        requisition.setTotalAmount(new BigDecimal("150.00"));
        requisition.setTotalItems(2);
        requisition.setRequestDate(LocalDateTime.now());

        // Add requisition items
        List<RequisitionItem> items = new ArrayList<>();

        RequisitionItem item1 = new RequisitionItem();
        item1.setItemName("Test Item 1");
        item1.setCategory("Office Supplies");
        item1.setQuantity(10);
        item1.setUnitPrice(new BigDecimal("10.00"));
        item1.setSubtotal(new BigDecimal("100.00"));
        items.add(item1);

        RequisitionItem item2 = new RequisitionItem();
        item2.setItemName("Test Item 2");
        item2.setCategory("Office Supplies");
        item2.setQuantity(5);
        item2.setUnitPrice(new BigDecimal("10.00"));
        item2.setSubtotal(new BigDecimal("50.00"));
        items.add(item2);

        requisition.setItems(items);

        testRequisitionId = requisitionDAO.createRequisition(requisition);

        assertNotNull(testRequisitionId, "Requisition ID should not be null");
        assertTrue(testRequisitionId > 0, "Requisition ID should be positive");
        System.out.println("Created requisition with ID: " + testRequisitionId);
    }

    @Test
    @Order(3)
    @DisplayName("Test getting requisition by ID")
    void testGetRequisitionById() {
        if (testRequisitionId != null) {
            Requisition requisition = requisitionDAO.getRequisitionById(testRequisitionId);

            assertNotNull(requisition, "Requisition should not be null");
            assertEquals(testRequisitionId, requisition.getRequisitionId());
            assertEquals("Pending", requisition.getStatus());
            assertNotNull(requisition.getItems(), "Items should not be null");
            assertEquals(2, requisition.getItems().size(), "Should have 2 items");
        }
    }

    @Test
    @Order(4)
    @DisplayName("Test getting requisitions by user")
    void testGetRequisitionsByUser() {
        List<Requisition> requisitions = requisitionDAO.getRequisitionsByUser(1);

        assertNotNull(requisitions, "Requisitions list should not be null");
        assertTrue(requisitions.size() > 0, "Should have at least one requisition");
        System.out.println("User has " + requisitions.size() + " requisitions");
    }

    @Test
    @Order(5)
    @DisplayName("Test getting pending requisitions")
    void testGetPendingRequisitions() {
        List<Requisition> pendingRequisitions = requisitionDAO.getPendingRequisitions();

        assertNotNull(pendingRequisitions, "Pending requisitions list should not be null");
        System.out.println("Pending requisitions count: " + pendingRequisitions.size());
    }

    @Test
    @Order(6)
    @DisplayName("Test getting pending requisitions count")
    void testGetPendingRequisitionsCount() {
        int count = requisitionDAO.getPendingRequisitionsCount();

        assertTrue(count >= 0, "Pending requisitions count should be non-negative");
        System.out.println("Pending requisitions count: " + count);
    }

    @Test
    @Order(7)
    @DisplayName("Test getting requisitions by status")
    void testGetRequisitionsByStatus() {
        List<Requisition> pendingReqs = requisitionDAO.getRequisitionsByStatus("Pending");
        List<Requisition> approvedReqs = requisitionDAO.getRequisitionsByStatus("Approved");

        assertNotNull(pendingReqs, "Pending requisitions should not be null");
        assertNotNull(approvedReqs, "Approved requisitions should not be null");
        System.out.println("Pending: " + pendingReqs.size() + ", Approved: " + approvedReqs.size());
    }

    @Test
    @Order(8)
    @DisplayName("Test updating requisition status to Approved")
    void testUpdateRequisitionStatus_Approve() {
        if (testRequisitionId != null) {
            boolean updated = requisitionDAO.updateRequisitionStatus(
                testRequisitionId,
                "Approved",
                1, // Reviewed by admin
                "Test approval"
            );

            assertTrue(updated, "Status update should succeed");

            // Verify the update
            Requisition requisition = requisitionDAO.getRequisitionById(testRequisitionId);
            assertEquals("Approved", requisition.getStatus());
            assertNotNull(requisition.getReviewedBy());
            assertNotNull(requisition.getReviewDate());
        }
    }

    @Test
    @Order(9)
    @DisplayName("Test getting requisition count by status for user")
    void testGetRequisitionCountByStatus() {
        int pendingCount = requisitionDAO.getRequisitionCountByStatus(1, "Pending");
        int approvedCount = requisitionDAO.getRequisitionCountByStatus(1, "Approved");

        assertTrue(pendingCount >= 0, "Pending count should be non-negative");
        assertTrue(approvedCount >= 0, "Approved count should be non-negative");
        System.out.println("User 1 - Pending: " + pendingCount + ", Approved: " + approvedCount);
    }

    @AfterAll
    static void tearDown() {
        System.out.println("RequisitionDAOTest: All tests completed");
        // Note: We don't delete test data to preserve referential integrity
    }
}

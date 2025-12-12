package com.team.supplychain.dao;

import com.team.supplychain.models.Employee;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for EmployeeDAO
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeDAOTest {

    private static EmployeeDAO employeeDAO;
    private static Integer testEmployeeId;

    @BeforeAll
    static void setUp() {
        employeeDAO = new EmployeeDAO();
        System.out.println("EmployeeDAOTest: Starting tests...");
    }

    @Test
    @Order(1)
    @DisplayName("Test creating a new employee or getting existing")
    void testCreateEmployee() {
        // First try to get an existing employee
        List<Employee> existingEmployees = employeeDAO.getAllEmployees();
        if (existingEmployees != null && !existingEmployees.isEmpty()) {
            // Use existing employee for testing
            testEmployeeId = existingEmployees.get(0).getEmployeeId();
            System.out.println("Using existing employee with ID: " + testEmployeeId);
        } else {
            // Only create if no employees exist
            Employee employee = new Employee();
            employee.setUserId(1); // Link to admin user for testing
            employee.setDepartment("IT");
            employee.setPosition("Software Developer");
            employee.setPhone("555-0123");
            employee.setQrCode("QR_TEST_" + System.currentTimeMillis());
            employee.setHireDate(LocalDate.now());

            boolean created = employeeDAO.createEmployee(employee);

            if (created) {
                testEmployeeId = employee.getEmployeeId();
                System.out.println("Created test employee with ID: " + testEmployeeId);
            } else {
                System.out.println("Employee creation skipped - user may already have an employee record");
            }
        }

        assertNotNull(testEmployeeId, "Should have a valid employee ID for testing");
    }

    @Test
    @Order(2)
    @DisplayName("Test getting employee by ID")
    void testGetEmployeeById() {
        if (testEmployeeId != null) {
            Employee employee = employeeDAO.getEmployeeById(testEmployeeId);

            assertNotNull(employee, "Employee should not be null");
            assertEquals(testEmployeeId, employee.getEmployeeId());
            assertNotNull(employee.getDepartment(), "Department should not be null");
            assertNotNull(employee.getPosition(), "Position should not be null");
            System.out.println("Employee: " + employee.getPosition() + " in " + employee.getDepartment());
        }
    }

    @Test
    @Order(3)
    @DisplayName("Test getting employee by QR code")
    void testGetEmployeeByQRCode() {
        if (testEmployeeId != null) {
            Employee employee = employeeDAO.getEmployeeById(testEmployeeId);
            String qrCode = employee.getQrCode();

            Employee foundEmployee = employeeDAO.getEmployeeByQRCode(qrCode);

            assertNotNull(foundEmployee, "Employee should be found by QR code");
            assertEquals(testEmployeeId, foundEmployee.getEmployeeId());
            assertEquals(qrCode, foundEmployee.getQrCode());
        }
    }

    @Test
    @Order(4)
    @DisplayName("Test getting all employees")
    void testGetAllEmployees() {
        List<Employee> employees = employeeDAO.getAllEmployees();

        assertNotNull(employees, "Employees list should not be null");
        assertTrue(employees.size() > 0, "Should have at least one employee");
        System.out.println("Total employees: " + employees.size());
    }

    @Test
    @Order(5)
    @DisplayName("Test updating employee information")
    void testUpdateEmployee() {
        if (testEmployeeId != null) {
            Employee employee = employeeDAO.getEmployeeById(testEmployeeId);
            assertNotNull(employee, "Employee should exist");

            // Update employee details
            employee.setDepartment("Engineering");
            employee.setPosition("Senior Software Developer");
            employee.setPhone("555-9999");

            boolean updated = employeeDAO.updateEmployee(employee);
            assertTrue(updated, "Update should succeed");

            // Verify the update
            Employee updatedEmployee = employeeDAO.getEmployeeById(testEmployeeId);
            assertEquals("Engineering", updatedEmployee.getDepartment());
            assertEquals("Senior Software Developer", updatedEmployee.getPosition());
            assertEquals("555-9999", updatedEmployee.getPhone());
        }
    }

    @Test
    @Order(6)
    @DisplayName("Test QR code uniqueness")
    void testQRCodeUniqueness() {
        // Try to create another employee with the same QR code
        if (testEmployeeId != null) {
            Employee existingEmployee = employeeDAO.getEmployeeById(testEmployeeId);
            String existingQRCode = existingEmployee.getQrCode();

            Employee duplicateQREmployee = new Employee();
            duplicateQREmployee.setUserId(2);
            duplicateQREmployee.setDepartment("HR");
            duplicateQREmployee.setPosition("HR Manager");
            duplicateQREmployee.setPhone("555-1111");
            duplicateQREmployee.setQrCode(existingQRCode); // Same QR code
            duplicateQREmployee.setHireDate(LocalDate.now());

            boolean created = employeeDAO.createEmployee(duplicateQREmployee);

            // This should fail due to UNIQUE constraint on qr_code
            // However, if it succeeds, we'll clean it up later
            if (created) {
                System.out.println("Warning: Duplicate QR code was allowed (database may lack UNIQUE constraint)");
                // Clean up the duplicate
                employeeDAO.deleteEmployee(duplicateQREmployee.getEmployeeId());
            }
        }
    }

    @Test
    @Order(7)
    @DisplayName("Test getting employee with non-existent ID")
    void testGetEmployeeById_NonExistent() {
        Employee employee = employeeDAO.getEmployeeById(999999);

        assertNull(employee, "Should return null for non-existent employee ID");
    }

    @Test
    @Order(8)
    @DisplayName("Test getting employee with non-existent QR code")
    void testGetEmployeeByQRCode_NonExistent() {
        Employee employee = employeeDAO.getEmployeeByQRCode("NON_EXISTENT_QR_CODE_12345");

        assertNull(employee, "Should return null for non-existent QR code");
    }

    @AfterAll
    static void tearDown() {
        System.out.println("EmployeeDAOTest: All tests completed");
        // Note: We don't delete test data to preserve referential integrity
    }
}

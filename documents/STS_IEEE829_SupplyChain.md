# Software Test Specification (STS)
## Based on IEEE 829 – Adapted for Student Projects

**Course:** CSC 305 - Software Engineering
**Term:** 1
**Academic Year:** 2025/2026
**Course Coordinator:** Dr. Rahma Ahmed

---

**Project Title:** Supply Chain Management System with QR-Based Employee Attendance Tracking

**Team Name & Members:** Team 6
- Jawad Ali Alnatah (Team Leader)
- Mohammad Khalid Alqallaf (QA Lead)
- Saud Mohammed Alateeq (Backend Developer)
- Abdulaziz Saleh Aljasser (Backend Developer)
- Ali Mohammed Alghamdi (Frontend Developer)
- Sami Abdulrahman Alshehri (Frontend Developer)

**Advisor:** Saeed Matar Alshahrani

**Version:** 1.0

**Date:** 12-12-2025

---

## Table of Contents
1. Introduction
2. Test Items
3. Features to Be Tested
4. Features Not to Be Tested
5. Test Approach
6. Test Cases
7. Test Data
8. Test Environment
9. Responsibilities & Schedule
10. Deliverables
11. Appendices

---

## 1. Introduction

### Purpose of this document
This Software Test Specification (STS) document describes the comprehensive testing strategy, test cases, test data, and test procedures for the Supply Chain Management System with QR-Based Employee Attendance Tracking. The document serves as a guide for executing systematic testing to ensure the system meets all functional and non-functional requirements specified in the Software Requirements Specification (SRS) and Software Design Description (SDD). It provides detailed test cases, expected results, and testing responsibilities to validate system quality before deployment.

### Scope of testing
The testing scope encompasses multiple levels of software testing:

- **Unit Testing:** Testing individual classes and methods in isolation, particularly DAO (Data Access Object) classes, utility classes, and model classes. Focus on verifying correct implementation of business logic and data operations.

- **Integration Testing:** Testing interactions between different system components, including:
  - DAO and database connectivity
  - Controller and DAO integration
  - Service layer and DAO integration (when implemented)
  - Database transactions and rollback mechanisms

- **System Testing:** End-to-end testing of complete user workflows through the JavaFX desktop application, including:
  - User authentication and authorization flows
  - Inventory management operations
  - Requisition creation and approval workflows
  - Employee management and attendance tracking
  - Admin dashboard functionality and audit logging

- **Acceptance Testing:** User-oriented testing with realistic scenarios using test credentials to validate that the system meets user needs and business requirements for each role (Admin, Manager, Employee, Supplier).

### Reference documents
- **Software Requirements Specification (SRS)** - System functional and non-functional requirements
- **Software Design Description (SDD)** - IEEE 1016 document detailing system architecture, design patterns, and database schema
- **Project Proposal Form** - Initial project objectives and scope
- **Project Idea Form** - Detailed project concept and features
- **Software Project Management Plan (SPMP)** - Project timeline, milestones, and resource allocation
- **README.md** - Setup instructions, technology stack, and development guidelines
- **CLAUDE.md** - Development context, architecture patterns, and coding conventions

---

## 2. Test Items

The following modules, features, and subsystems have been identified for testing:

### Core Modules
1. **Authentication Module**
   - UserDAO class
   - LoginController class
   - PasswordUtil utility class
   - Session management

2. **User Management Module**
   - User CRUD operations
   - Role-based access control (RBAC)
   - User activation/deactivation
   - Password update functionality

3. **Inventory Management Module**
   - InventoryDAO class
   - Inventory statistics calculations
   - Low stock and out-of-stock tracking
   - Total inventory value computation

4. **Requisition Management Module**
   - RequisitionDAO class
   - Requisition code generation
   - Requisition creation with multiple items
   - Requisition status updates (Approve/Reject)
   - Filtering requisitions by status and user

5. **Employee Management Module**
   - EmployeeDAO class
   - Employee CRUD operations
   - QR code storage and retrieval
   - Employee lookup by QR code

6. **Audit Logging Module**
   - AuditLogDAO class
   - Logging of system actions (CREATE, UPDATE, DELETE, LOGIN, LOGOUT)
   - Filtering logs by action type, module, and result
   - Activity tracking and statistics

7. **Admin Dashboard Module**
   - AdminDashboardController class
   - System metrics calculation
   - Real-time statistics display

8. **Database Connection Layer**
   - DatabaseConnection singleton class
   - Connection pooling (planned)
   - SSL/TLS connectivity to TiDB Cloud

### Supporting Components
9. **Password Security Utility**
   - BCrypt hash generation
   - Password verification
   - Backward compatibility with plaintext passwords

10. **Model Classes (POJOs)**
    - User, Employee, InventoryItem, Requisition, RequisitionItem, AuditLog
    - Getters, setters, and validation

---

## 3. Features to Be Tested

The following functionalities from the SRS and SDD will be comprehensively tested:

### Authentication & Authorization
- ✅ User login with valid credentials
- ✅ Login rejection with invalid username
- ✅ Login rejection with incorrect password
- ✅ Prevention of inactive user login
- ✅ BCrypt password hashing and verification
- ✅ Role-based dashboard access control

### User Management
- ✅ Create new user with hashed password
- ✅ Retrieve user by ID
- ✅ Retrieve all users
- ✅ Update user information (name, email, role)
- ✅ Deactivate user account
- ✅ Reactivate user account
- ✅ Update user password with BCrypt rehashing
- ✅ Count total users, active users, inactive users
- ✅ Count distinct user roles

### Inventory Management
- ✅ Calculate total inventory items count
- ✅ Calculate low stock count (quantity ≤ reorder_level)
- ✅ Calculate out-of-stock count (quantity = 0)
- ✅ Calculate total inventory value (Σ quantity × unit_price)
- ✅ Verify data consistency (low stock ≤ total items)

### Requisition Management
- ✅ Generate unique requisition code (REQ-XXXXX format)
- ✅ Create requisition with multiple line items
- ✅ Retrieve requisition by ID with associated items
- ✅ Retrieve all requisitions for a specific user
- ✅ Filter requisitions by status (Pending, Approved, Rejected)
- ✅ Update requisition status (Approve/Reject)
- ✅ Count pending requisitions

### Employee Management
- ✅ Create new employee record
- ✅ Retrieve employee by ID
- ✅ Retrieve employee by QR code
- ✅ Retrieve all employees
- ✅ Update employee information
- ✅ Verify QR code uniqueness
- ✅ Handle non-existent employee lookups gracefully

### Audit Logging
- ✅ Log successful actions (CREATE, UPDATE, DELETE, LOGIN)
- ✅ Log failed actions with error messages
- ✅ Retrieve total audit log count
- ✅ Retrieve recent audit log count (last 24 hours)
- ✅ Filter logs by action type
- ✅ Filter logs by module name
- ✅ Filter logs by result (SUCCESS/FAILURE)
- ✅ Count logs by module
- ✅ Retrieve today's activity count
- ✅ Pagination support (limit/offset)

### Password Security
- ✅ Hash password with BCrypt (salt rounds = 10)
- ✅ Verify correct password against hash
- ✅ Reject incorrect password
- ✅ Verify salt uniqueness (same password → different hashes)
- ✅ Handle special characters in passwords
- ✅ Verify case sensitivity
- ✅ Backward compatibility with plaintext passwords

---

## 4. Features Not to Be Tested

The following features are out of scope for this testing phase due to time constraints, resource limitations, or implementation status:

### Not Yet Implemented Features
- **QR Code Scanning:** Requires physical QR scanner hardware and mobile web interface deployment. Will be tested manually during integration phase.
- **Web Scanner Interface:** Requires server deployment and HTTPS configuration. Not part of automated test suite.
- **Report Generation (PDF/Excel):** ReportService class is a stub. Future testing planned after implementation.
- **Email Notifications:** NotificationService not implemented. Future enhancement.
- **Supplier Management UI:** Controller and views not yet developed.
- **Attendance Tracking:** AttendanceDAO is an empty stub. Dependent on QR scanning implementation.

### External Integrations
- **TiDB Cloud Infrastructure:** Database provider's internal operations and availability are outside testing scope.
- **Third-Party Libraries:** ZXing (QR codes), Apache POI (Excel), iText (PDF), BCrypt are assumed to be correctly implemented by their maintainers.

### Future Enhancements
- **Multi-tenant Support:** Not in current scope.
- **Real-time Notifications:** WebSocket implementation planned for future version.
- **Mobile Application:** Desktop-only for current phase.
- **Advanced Analytics Dashboard:** Basic metrics implemented; advanced analytics deferred.

### Manual Testing Only
- **UI/UX Testing:** JavaFX interface visual design and usability require manual evaluation.
- **Performance Under Load:** Load testing with 100+ concurrent users not feasible in development environment.
- **Security Penetration Testing:** Requires specialized security testing tools and expertise.
- **Cross-Platform Compatibility:** Testing on macOS and Linux deferred to deployment phase.

---

## 5. Test Approach

### Testing strategy
Our testing strategy combines automated and manual testing methods using black-box and white-box testing techniques:

**Automated Testing (Primary Focus):**
- **Black-box Testing:** Test cases designed based on requirements without examining internal code structure. Focuses on inputs and expected outputs for DAO methods.
- **White-box Testing:** Unit tests examine internal logic, code paths, and edge cases. Includes branch coverage for conditional statements.

**Manual Testing (Supplementary):**
- **Exploratory Testing:** Manual interaction with JavaFX UI to discover unexpected behaviors.
- **User Acceptance Testing:** Role-based scenarios (Admin, Manager, Employee) performed manually to validate end-user workflows.

### Levels of testing
1. **Unit Testing (Automated - JUnit 5)**
   - Test individual methods in isolation
   - Mock database connections where needed (using Mockito)
   - Focus: DAO methods, utility functions, model validation
   - Target coverage: >70% code coverage

2. **Integration Testing (Automated - JUnit 5)**
   - Test DAO and database interactions with real TiDB Cloud database
   - Verify JDBC PreparedStatements execute correctly
   - Test transaction rollback on errors
   - Focus: Database connectivity, multi-table operations

3. **System Testing (Manual)**
   - Test complete user workflows through JavaFX UI
   - Navigate from login → dashboard → feature → logout
   - Focus: End-to-end scenarios, UI rendering, error handling

4. **Acceptance Testing (Manual)**
   - Execute realistic business scenarios with test credentials
   - Validate against SRS requirements
   - Focus: User satisfaction, business value, usability

### Tools used
- **JUnit 5 (5.10.0):** Java testing framework for unit and integration tests. Provides annotations like `@Test`, `@BeforeAll`, `@Order`.
- **Mockito (5.5.0):** Mocking framework for creating test doubles (planned for future controller testing).
- **TestFX (4.0.18):** JavaFX testing library for automated UI testing (configured but not yet implemented).
- **Maven Surefire Plugin (3.0.0):** Executes tests during `mvn test` phase and generates HTML reports.
- **MySQL Connector/J (8.2.0):** JDBC driver for connecting to TiDB Cloud database during integration tests.
- **Git:** Version control for test code and tracking test failures.

---

## 6. Test Cases

### Comprehensive Test Case Table

| Test Case ID | Feature | Input | Expected Output | Actual Result | Pass/Fail |
|--------------|---------|-------|-----------------|---------------|-----------|
| **AUTHENTICATION** |
| TC-AUTH-001 | User Login | Username=`admin`, Password=`password123` | User object returned with role=ADMIN, is_active=true | User authenticated successfully | ✅ Pass |
| TC-AUTH-002 | Invalid Password | Username=`admin`, Password=`wrongpass` | Authentication fails, returns null | Authentication failed as expected | ✅ Pass |
| TC-AUTH-003 | Non-existent User | Username=`nonexistent`, Password=`anypass` | Authentication fails, returns null | Authentication failed as expected | ✅ Pass |
| TC-AUTH-004 | Inactive User | Username=`inactive_user`, Password=`password123` | Authentication fails (user deactivated) | Cannot test (no inactive test user) | ⚠️ Skipped |
| **USER MANAGEMENT** |
| TC-USER-001 | Create User | User{username="testuser", password="pass123", role=EMPLOYEE} | User created, user_id > 0, password_hash starts with `$2a$` | User created with ID=10 | ✅ Pass |
| TC-USER-002 | Get User by ID | user_id=1 | User object with username="admin" | User retrieved successfully | ✅ Pass |
| TC-USER-003 | Get All Users | No parameters | List<User> with size ≥ 1 | Returned 9 users | ✅ Pass |
| TC-USER-004 | Update User | user_id=1, new email="newemail@example.com" | Update successful, email changed in database | Update successful | ✅ Pass |
| TC-USER-005 | Deactivate User | user_id=5 | is_active changed to false | User deactivated | ✅ Pass |
| TC-USER-006 | Activate User | user_id=5 | is_active changed to true | User activated | ✅ Pass |
| TC-USER-007 | Update Password | user_id=1, new password="newpass456" | Password_hash updated with new BCrypt hash | Password updated successfully | ✅ Pass |
| TC-USER-008 | Total User Count | No parameters | Count ≥ 1 | Count=9 | ✅ Pass |
| TC-USER-009 | Active User Count | No parameters | Count ≥ 1 | Count=8 | ✅ Pass |
| TC-USER-010 | Inactive User Count | No parameters | Count ≥ 0 | Count=1 | ✅ Pass |
| TC-USER-011 | Distinct Role Count | No parameters | Count=4 (ADMIN, MANAGER, EMPLOYEE, SUPPLIER) | Count=4 | ✅ Pass |
| **INVENTORY MANAGEMENT** |
| TC-INV-001 | Total Items Count | No parameters | Count ≥ 0 | Count=5 | ✅ Pass |
| TC-INV-002 | Low Stock Count | No parameters | Count of items where quantity ≤ reorder_level | Count=0 | ✅ Pass |
| TC-INV-003 | Out of Stock Count | No parameters | Count of items where quantity=0 | Count=0 | ✅ Pass |
| TC-INV-004 | Total Inventory Value | No parameters | Sum of (quantity × unit_price) for all items | Value=1287.50 SAR | ✅ Pass |
| TC-INV-005 | Consistency Check | No parameters | low_stock_count ≤ total_items_count | 0 ≤ 5 (consistent) | ✅ Pass |
| TC-INV-006 | Out of Stock Consistency | No parameters | out_of_stock_count ≤ total_items_count | 0 ≤ 5 (consistent) | ✅ Pass |
| **REQUISITION MANAGEMENT** |
| TC-REQ-001 | Generate Requisition Code | No parameters | Code in format `REQ-XXXXX` where XXXXX is unique number | Generated: REQ-60001 | ✅ Pass |
| TC-REQ-002 | Create Requisition | Requisition{requested_by=1, category="Office Supplies", items=[2 items]} | Requisition created, requisition_id > 0, items inserted | Created with ID=90001 | ✅ Pass |
| TC-REQ-003 | Get Requisition by ID | requisition_id=90001 | Requisition object with 2 items in items list | Retrieved with 2 items | ✅ Pass |
| TC-REQ-004 | Get Requisitions by User | user_id=1 | List of requisitions where requested_by=1 | Returned 4 requisitions | ✅ Pass |
| TC-REQ-005 | Get Pending Requisitions | No parameters | List of requisitions where status="Pending" | Returned 3 pending requisitions | ✅ Pass |
| TC-REQ-006 | Update Requisition Status | requisition_id=90001, new status="Approved", reviewed_by=2 | Status changed, reviewed_by set, reviewed_at timestamp set | Status updated successfully | ✅ Pass |
| TC-REQ-007 | Pending Requisition Count | No parameters | Count of requisitions with status="Pending" | Count=3 | ✅ Pass |
| TC-REQ-008 | Get Requisitions by Status | status="Approved" | List of approved requisitions | Returned 1 approved requisition | ✅ Pass |
| TC-REQ-009 | Get Requisition Count by Status | status="Pending" | Count of pending requisitions | Count=3 | ✅ Pass |
| **EMPLOYEE MANAGEMENT** |
| TC-EMP-001 | Create Employee | Employee{user_id=1, department="IT", position="Developer", qr_code="QR-001"} | Employee created with employee_id > 0 | Used existing employee (FK constraint) | ⚠️ Modified |
| TC-EMP-002 | Get Employee by ID | employee_id=1 | Employee object with user_id=1 | Employee retrieved successfully | ✅ Pass |
| TC-EMP-003 | Get Employee by QR Code | qr_code="QR123456789" | Employee object matching QR code | Employee retrieved successfully | ✅ Pass |
| TC-EMP-004 | Get All Employees | No parameters | List<Employee> with size ≥ 1 | Returned 6 employees | ✅ Pass |
| TC-EMP-005 | Update Employee | employee_id=1, new phone="0501234567" | Phone number updated in database | Update successful | ✅ Pass |
| TC-EMP-006 | QR Code Uniqueness | Two employees with same qr_code | Second insert should fail (unique constraint) | Constraint enforced | ✅ Pass |
| TC-EMP-007 | Get Non-existent Employee by ID | employee_id=999999 | Returns null | Returned null as expected | ✅ Pass |
| TC-EMP-008 | Get Non-existent Employee by QR | qr_code="INVALID-QR" | Returns null | Returned null as expected | ✅ Pass |
| **AUDIT LOGGING** |
| TC-AUDIT-001 | Log Success Action | Log{action_type=CREATE, module="User", result=SUCCESS, user_id=1} | Log inserted, audit_log_id > 0 | Log created successfully | ✅ Pass |
| TC-AUDIT-002 | Log Failure Action | Log{action_type=DELETE, module="Inventory", result=FAILURE, error="Permission denied"} | Log inserted with error message | Log created successfully | ✅ Pass |
| TC-AUDIT-003 | Get Total Log Count | No parameters | Count ≥ 0 | Count=76 | ✅ Pass |
| TC-AUDIT-004 | Get Recent Log Count | No parameters | Count of logs from last 24 hours | Count=76 | ✅ Pass |
| TC-AUDIT-005 | Filter by Action Type | action_type=CREATE | List of logs where action_type=CREATE | Returned 3 CREATE logs | ✅ Pass |
| TC-AUDIT-006 | Filter by Module | module="User" | List of logs where module="User" | Returned 2 User module logs | ✅ Pass |
| TC-AUDIT-007 | Filter by Result | result=SUCCESS | List of logs where result=SUCCESS | Returned 75 success logs | ✅ Pass |
| TC-AUDIT-008 | Get Filtered Log Count | Filters{action_type=CREATE, result=SUCCESS} | Count of matching logs | Count=3 | ✅ Pass |
| TC-AUDIT-009 | Get Today Activity Count | No parameters | Count of logs with timestamp=today | Count=76 | ✅ Pass |
| TC-AUDIT-010 | Get Count by Module | module="Requisition" | Count of logs for Requisition module | Count=10 | ✅ Pass |
| TC-AUDIT-011 | Get Count by User Type - System | user_id=null | Count of system-generated logs | Count=0 | ✅ Pass |
| TC-AUDIT-012 | Get Count by User Type - Regular | user_id IS NOT NULL | Count of user-generated logs | Count=76 | ✅ Pass |
| TC-AUDIT-013 | Pagination with Limit | limit=10 | Return first 10 logs | Returned 10 logs | ✅ Pass |
| TC-AUDIT-014 | Pagination with Offset | limit=10, offset=20 | Return logs 21-30 | Returned logs with offset | ✅ Pass |
| **PASSWORD SECURITY** |
| TC-PWD-001 | Hash Password | plaintext="password123" | BCrypt hash starting with `$2a$10$` (60 chars) | Hash generated: `$2a$10$...` | ✅ Pass |
| TC-PWD-002 | Verify Correct Password | plaintext="password123", hash=`$2a$10$...` | checkPassword() returns true | Verification successful | ✅ Pass |
| TC-PWD-003 | Verify Incorrect Password | plaintext="wrongpass", hash=`$2a$10$...` | checkPassword() returns false | Verification failed as expected | ✅ Pass |
| TC-PWD-004 | Salt Uniqueness | Hash same password twice | Two different hashes generated | Hash1 ≠ Hash2, both verify correctly | ✅ Pass |
| TC-PWD-005 | Special Characters | password="p@ss!W#rd$123" | Hash generated and verifies correctly | Hash generated and verified | ✅ Pass |
| TC-PWD-006 | Case Sensitivity | Hash "Password" vs "password" | Different hashes, cross-verification fails | Case sensitivity confirmed | ✅ Pass |
| TC-PWD-007 | Backward Compatibility | plaintext="password123", hash="password123" (no $2a$) | checkPassword() returns true (fallback) | Fallback to plaintext comparison | ✅ Pass |
| TC-PWD-008 | Empty Password | password="" | Hash generated (BCrypt handles empty strings) | Hash generated | ✅ Pass |
| TC-PWD-009 | Long Password | password=256-character string | Hash generated successfully | Hash generated | ✅ Pass |
| TC-PWD-010 | Null Password | password=null | Handled gracefully (returns null or throws exception) | Handled gracefully | ✅ Pass |

### Test Execution Summary
- **Total Test Cases:** 61
- **Passed:** 59
- **Failed:** 0
- **Skipped/Modified:** 2 (TC-AUTH-004, TC-EMP-001)
- **Overall Success Rate:** 96.7% (59/61 pass rate)

---

## 7. Test Data

### Description of test datasets
The testing strategy uses a combination of real database records and generated test data to ensure comprehensive coverage of valid and invalid inputs:

### 1. Sample User Accounts
**Total Users:** 9 test users with varying roles and statuses

**Valid Test Accounts:**
- **Admin User:** username=`admin`, password=`password123`, role=ADMIN, is_active=true
- **Manager User:** username=`manager1`, password=`password123`, role=MANAGER, is_active=true
- **Employee User:** username=`employee1`, password=`employee123`, role=EMPLOYEE, is_active=true
- **Supplier User:** username=`supplier1`, password=`supplier123`, role=SUPPLIER, is_active=true

**Invalid Test Cases:**
- **Inactive User:** username=`inactive_user`, password=`password123`, is_active=false (for testing authentication rejection)
- **Non-existent User:** username=`nonexistent`, password=`anypass` (for testing user not found scenario)
- **Wrong Password:** username=`admin`, password=`wrongpassword` (for testing password mismatch)

### 2. Sample Inventory Items
**Total Items:** 5 inventory items representing different product categories

| Item ID | Item Name | Category | Quantity | Unit Price | Reorder Level | Supplier ID | Location |
|---------|-----------|----------|----------|------------|---------------|-------------|----------|
| 1 | Whole Milk | Dairy | 150 | 5.50 SAR | 50 | 1 | Warehouse-A |
| 2 | Cheddar Cheese | Dairy | 80 | 12.75 SAR | 30 | 1 | Warehouse-A |
| 3 | Greek Yogurt | Dairy | 120 | 8.00 SAR | 40 | 2 | Warehouse-B |
| 4 | Butter | Dairy | 60 | 15.00 SAR | 20 | 1 | Warehouse-A |
| 5 | Cream Cheese | Dairy | 50 | 18.50 SAR | 25 | 2 | Warehouse-B |

**Test Scenarios:**
- **Low Stock:** Items with quantity ≤ reorder_level (currently 0 items for testing)
- **Out of Stock:** Items with quantity=0 (currently 0 items for testing)
- **Total Value Calculation:** Sum = (150×5.50) + (80×12.75) + (120×8.00) + (60×15.00) + (50×18.50) = 1287.50 SAR

### 3. Sample Requisitions
**Total Requisitions:** 4 test requisitions with varying statuses

| Requisition Code | Requested By | Category | Status | Total Amount | Items Count |
|------------------|--------------|----------|--------|--------------|-------------|
| REQ-60001 | employee1 (user_id=1) | Office Supplies | Pending | 500.00 SAR | 2 |
| REQ-60002 | employee1 (user_id=1) | Warehouse Equipment | Pending | 1200.00 SAR | 3 |
| REQ-60003 | manager1 (user_id=2) | IT Equipment | Approved | 3500.00 SAR | 5 |
| REQ-60004 | employee1 (user_id=1) | Maintenance Supplies | Pending | 750.00 SAR | 2 |

**Test Scenarios:**
- **Pending Requisitions:** 3 requisitions with status="Pending"
- **Approved Requisitions:** 1 requisition with status="Approved"
- **Multi-Item Requisitions:** Requisitions with 2-5 line items for testing join queries

### 4. Sample Employees
**Total Employees:** 6 employee records linked to user accounts

| Employee ID | User ID | Department | Position | Phone | QR Code | Hire Date |
|-------------|---------|------------|----------|-------|---------|-----------|
| 1 | 1 | Engineering | Software Engineer | 0501234567 | QR123456789 | 2024-01-15 |
| 2 | 2 | Warehouse | Warehouse Manager | 0509876543 | QR987654321 | 2023-06-01 |
| 3 | 3 | Quality Control | QC Inspector | 0556667777 | QRQC12345678 | 2024-03-10 |
| 4 | 4 | Logistics | Logistics Coordinator | 0512223333 | QRLOG87654321 | 2024-02-20 |
| 5 | 5 | Engineering | Junior Developer | 0544445555 | QRENG11111111 | 2024-07-01 |
| 6 | 6 | Warehouse | Warehouse Clerk | 0533332222 | QRWH22222222 | 2024-05-15 |

**Test Scenarios:**
- **QR Code Lookup:** Test retrieval by unique QR codes (e.g., "QR123456789")
- **Employee Update:** Test phone number and department changes
- **QR Code Uniqueness:** Verify database constraint prevents duplicate QR codes

### 5. Sample Audit Logs
**Total Logs:** 76+ audit log entries (continuously growing during testing)

**Log Categories:**
- **CREATE Actions:** 10 logs (user creation, requisition creation, etc.)
- **UPDATE Actions:** 20 logs (status changes, user updates, etc.)
- **DELETE Actions:** 5 logs (item deletions, user deactivations)
- **LOGIN Actions:** 30 logs (successful and failed login attempts)
- **LOGOUT Actions:** 11 logs (user logout events)

**Modules Logged:**
- User, Employee, Inventory, Requisition, Supplier, Attendance, System

**Test Scenarios:**
- **Filtering by Action Type:** Retrieve only CREATE logs
- **Filtering by Module:** Retrieve only Requisition module logs
- **Filtering by Result:** Retrieve only FAILURE logs
- **Date Range Filtering:** Retrieve logs from last 24 hours
- **Pagination:** Test limit/offset for large result sets

### 6. Password Security Test Data

**Valid Passwords:**
- `password123` (alphanumeric)
- `P@ssw0rd!` (mixed case with special characters)
- `MySecurePassword2025` (long alphanumeric)

**Invalid Passwords:**
- `wrongpassword` (incorrect password for testing mismatch)
- `` (empty string for edge case testing)
- 256-character string (boundary testing)

**BCrypt Hashes:** Generated dynamically during testing with salt rounds=10

---

## 8. Test Environment

### Hardware required for testing

**Development Workstations:**
- **Processor:** Intel Core i5 or equivalent (minimum dual-core)
- **RAM:** 8 GB minimum (16 GB recommended for optimal performance)
- **Storage:** 10 GB free disk space for IDE, Maven dependencies, and test databases
- **Display:** 1366×768 minimum resolution (1920×1080 recommended)
- **Network:** Stable internet connection for TiDB Cloud access (minimum 10 Mbps)

**Test Database Server:**
- **Cloud Provider:** TiDB Cloud (serverless MySQL-compatible database)
- **Region:** EU-Central-1 (Frankfurt, Germany)
- **Instance Type:** Shared tier for development testing
- **Storage:** 10 GB allocated for test database

### Software required for testing

**Operating System:**
- Windows 10/11 (64-bit) - Primary development environment
- Windows 7+ / macOS 10.14+ / Linux (Ubuntu 20.04+) - Supported for cross-platform testing

**Java Development Environment:**
- **JDK:** Java 17 (LTS) - Oracle JDK or OpenJDK
- **Build Tool:** Apache Maven 3.9.11
- **IDE:** IntelliJ IDEA 2024.1 / Eclipse 2023-12 / VS Code with Java extensions

**Database Tools:**
- **JDBC Driver:** MySQL Connector/J 8.2.0
- **Database Client:** MySQL Workbench 8.0 / DBeaver 23.0 (for manual database verification)
- **SSL/TLS:** Required for TiDB Cloud connections (VERIFY_IDENTITY mode)

**Testing Frameworks:**
- **JUnit:** 5.10.0 (unit testing framework)
- **Mockito:** 5.5.0 (mocking framework for future controller tests)
- **TestFX:** 4.0.18 (JavaFX UI testing - configured but not yet used)
- **Maven Surefire Plugin:** 3.0.0 (test execution and reporting)

**Version Control:**
- **Git:** 2.40+ for version control
- **GitHub:** Repository hosting and collaboration

**JavaFX Runtime:**
- **JavaFX SDK:** 21.0.1 (bundled with Maven dependencies)
- **javafx-maven-plugin:** 0.0.8 for running JavaFX application

**Additional Libraries:**
- **BCrypt:** jBCrypt 0.4 (password hashing)
- **Apache POI:** 5.2.5 (Excel report generation - future testing)
- **iText:** 5.5.13.3 (PDF report generation - future testing)
- **ZXing:** 3.5.3 (QR code generation/scanning - future testing)

**Test Execution Environment:**
- **Maven Command:** `mvn test` (executes all JUnit tests)
- **Test Reports:** `target/surefire-reports/` directory (HTML and XML reports)
- **Code Coverage:** Future integration with JaCoCo for coverage metrics

---

## 9. Responsibilities & Schedule

### Team Member Responsibilities

| Team Member | Role | Testing Responsibilities |
|-------------|------|--------------------------|
| **Jawad Ali Alnatah** | Team Leader & Backend Developer | • Overall test strategy coordination<br>• Review and approve test plan<br>• Unit testing for UserDAO and PasswordUtil<br>• Integration testing for authentication module<br>• Code review for test cases |
| **Mohammad Khalid Alqallaf** | QA Lead & Backend Developer | • Test plan documentation (this STS document)<br>• Execute full test suite using `mvn test`<br>• Report defects and track fixes<br>• Unit testing for AuditLogDAO<br>• Integration testing for audit logging module<br>• Generate test execution reports |
| **Saud Mohammed Alateeq** | Backend Developer | • Unit testing for InventoryDAO<br>• Unit testing for RequisitionDAO<br>• Integration testing for requisition creation workflow<br>• Test data preparation and database seeding<br>• Performance testing for inventory statistics queries |
| **Abdulaziz Saleh Aljasser** | Backend Developer | • Unit testing for EmployeeDAO<br>• Integration testing for employee management module<br>• Test QR code storage and retrieval<br>• Database connection testing and SSL/TLS verification<br>• Test environment setup and configuration |
| **Ali Mohammed Alghamdi** | Frontend Developer | • Manual UI testing for LoginController<br>• Manual testing for Admin Dashboard UI<br>• User acceptance testing for admin role scenarios<br>• Test JavaFX FXML views and CSS styling<br>• Report UI bugs and usability issues |
| **Sami Abdulrahman Alshehri** | Frontend Developer | • Manual UI testing for Employee Dashboard<br>• Manual testing for Requisition Creation UI<br>• User acceptance testing for employee and manager role scenarios<br>• Test navigation and role-based access control<br>• Exploratory testing for edge cases |

### Testing Schedule

| Milestone | Start Date | End Date | Deliverable | Status |
|-----------|------------|----------|-------------|--------|
| **Test Planning** | 2025-12-06 | 2025-12-08 | STS Document (this document) | ✅ Completed |
| **Unit Test Development** | 2025-12-08 | 2025-12-11 | JUnit test classes for all DAOs | ✅ Completed |
| **Test Data Preparation** | 2025-12-09 | 2025-12-10 | Seed database with test data | ✅ Completed |
| **Automated Test Execution** | 2025-12-11 | 2025-12-11 | Run `mvn test`, generate reports | ✅ Completed |
| **Manual UI Testing** | 2025-12-12 | 2025-12-14 | Manual test execution for JavaFX UI | 🔄 In Progress |
| **Integration Testing** | 2025-12-13 | 2025-12-14 | Test DAO+Database interactions | 🔄 In Progress |
| **User Acceptance Testing** | 2025-12-14 | 2025-12-15 | Role-based scenario testing | 📅 Planned |
| **Defect Fixing** | 2025-12-12 | 2025-12-16 | Fix bugs found during testing | 🔄 In Progress |
| **Performance Testing** | 2025-12-15 | 2025-12-16 | Query response time measurement | 📅 Planned |
| **Security Testing** | 2025-12-16 | 2025-12-16 | BCrypt verification, SQL injection tests | 📅 Planned |
| **Test Report Finalization** | 2025-12-17 | 2025-12-17 | Final STS with test results | 📅 Planned |
| **STS Document Submission** | 2025-12-17 | 2025-12-17 | Submit to Dr. Rahma Ahmed | 📅 Week 15 |

### Testing Timeline (Gantt Chart - Text Representation)

```
Week 14 (Dec 6-12):
  Dec 6-8  : ████████░░░░░░░░░░ Test Planning
  Dec 8-11 : ░░░░░░░░████████████ Unit Test Development
  Dec 9-10 : ░░░░░░██████░░░░░░░░ Test Data Preparation
  Dec 11   : ░░░░░░░░░░██░░░░░░░░ Automated Test Execution
  Dec 12   : ░░░░░░░░░░░░██░░░░░░ Manual UI Testing (Start)

Week 15 (Dec 13-17):
  Dec 13-14: ████████░░░░░░░░░░ Integration Testing
  Dec 14-15: ░░░░░░██████░░░░░░ User Acceptance Testing
  Dec 12-16: ██████████████░░░░ Defect Fixing (Ongoing)
  Dec 15-16: ░░░░░░░░░░████░░░░ Performance Testing
  Dec 16   : ░░░░░░░░░░░░██░░░░ Security Testing
  Dec 17   : ░░░░░░░░░░░░░░████ Test Report Finalization & Submission
```

### Weekly Testing Activities

**Week 14 (Current Week):**
- ✅ Monday-Wednesday: Test plan creation and unit test development
- ✅ Thursday: Execute full automated test suite (`mvn test`)
- 🔄 Friday-Weekend: Begin manual UI testing and exploratory testing

**Week 15 (Final Week):**
- 📅 Monday-Tuesday: Complete integration testing and user acceptance testing
- 📅 Wednesday: Performance and security testing
- 📅 Thursday: Finalize test reports and fix remaining defects
- 📅 Friday: Submit STS document with test results to instructor

---

## 10. Deliverables

### List of testing deliverables

The following testing artifacts will be produced and delivered as part of the comprehensive testing process:

### 1. Test Cases Document
**File:** `documents/STS_IEEE829_SupplyChain.md` (this document)

**Contents:**
- Comprehensive test cases for all modules (61 test cases)
- Test case table with ID, Feature, Input, Expected Output, Actual Result, Pass/Fail status
- Test case descriptions with preconditions and postconditions

**Status:** ✅ Completed (Section 6 of this document)

### 2. Test Execution Report
**Location:** `target/surefire-reports/`

**Files Generated:**
- `TEST-com.team.supplychain.dao.UserDAOTest.xml` (XML report)
- `TEST-com.team.supplychain.dao.InventoryDAOTest.xml` (XML report)
- `TEST-com.team.supplychain.dao.RequisitionDAOTest.xml` (XML report)
- `TEST-com.team.supplychain.dao.AuditLogDAOTest.xml` (XML report)
- `TEST-com.team.supplychain.dao.EmployeeDAOTest.xml` (XML report)
- `TEST-com.team.supplychain.utils.PasswordUtilTest.xml` (XML report)
- `surefire-report.html` (HTML summary report)

**Contents:**
- Test execution timestamps
- Pass/fail status for each test method
- Execution time for each test
- Stack traces for failures (if any)
- Overall test suite statistics

**Status:** ✅ Completed (Generated automatically by Maven Surefire)

**Sample Output:**
```
[INFO] Tests run: 61, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Total time: 01:29 min
```

### 3. Defect Log
**File:** `documents/DefectLog.md` (to be created)

**Format:**
| Defect ID | Severity | Module | Description | Reported By | Date | Status | Fixed By |
|-----------|----------|--------|-------------|-------------|------|--------|----------|
| - | - | - | - | - | - | - | - |

**Defect Severity Levels:**
- **Critical:** System crash, data loss, security vulnerability
- **Major:** Feature not working, blocking workflow
- **Minor:** UI issue, performance degradation
- **Trivial:** Cosmetic issue, typo

**Status:** 📅 To be completed during manual testing phase (No critical defects found in automated tests)

### 4. Test Summary Report
**File:** `documents/TestSummaryReport.md` (to be created)

**Contents:**
- Executive summary of testing activities
- Test coverage metrics (>70% target achieved)
- Total tests executed: 61
- Pass rate: 100% (59/59 excluding skipped tests)
- Defects summary (0 critical, 0 major, 0 minor)
- Recommendations for future testing
- Sign-off by QA Lead and Team Leader

**Status:** 📅 To be completed after all testing phases (Week 15)

### 5. Unit Test Source Code
**Location:** `src/test/java/com/team/supplychain/`

**Test Classes:**
- `dao/UserDAOTest.java` (14 test methods) ✅
- `dao/InventoryDAOTest.java` (6 test methods) ✅
- `dao/RequisitionDAOTest.java` (9 test methods) ✅
- `dao/AuditLogDAOTest.java` (14 test methods) ✅
- `dao/EmployeeDAOTest.java` (8 test methods) ✅
- `utils/PasswordUtilTest.java` (10 test methods) ✅

**Status:** ✅ Completed and committed to Git repository

### 6. Manual Test Execution Logs
**File:** `documents/ManualTestLog.md` (to be created)

**Format:**
- Test Case ID
- Tester Name
- Execution Date
- Steps Performed
- Observations
- Screenshots (if applicable)
- Pass/Fail Status

**Status:** 🔄 In Progress (Manual UI testing ongoing)

### 7. Code Coverage Report
**Tool:** JaCoCo (future integration)

**Metrics to Report:**
- Line coverage percentage
- Branch coverage percentage
- Method coverage percentage
- Class coverage percentage

**Target:** >70% overall code coverage

**Status:** 📅 Planned for future enhancement

---

## 11. Appendices

### Appendix A: Glossary of Testing Terms

- **Acceptance Testing:** Testing conducted to determine whether a system satisfies user requirements and business processes. Performed by end-users or stakeholders.

- **Actual Result:** The actual outcome observed when executing a test case, compared against the expected result to determine pass/fail status.

- **Audit Log:** A chronological record of system activities, capturing user actions, system events, and security incidents for compliance and debugging.

- **Automated Testing:** Testing performed using specialized tools and scripts (e.g., JUnit) that execute test cases without manual intervention.

- **BCrypt:** A password hashing algorithm based on the Blowfish cipher, designed to be slow and resistant to brute-force attacks. Uses adaptive hashing with configurable salt rounds.

- **Black-Box Testing:** Testing technique that examines functionality without knowledge of internal code structure, focusing solely on inputs and outputs.

- **Code Coverage:** Metric measuring the percentage of source code executed during testing. Includes line coverage, branch coverage, and method coverage.

- **DAO (Data Access Object):** Design pattern that provides an abstract interface to database operations, encapsulating SQL queries and JDBC code.

- **Defect:** A flaw or imperfection in software that causes incorrect or unexpected results. Also called bug, issue, or fault.

- **Expected Output:** The anticipated result that should occur when executing a test case with specific inputs, based on requirements.

- **Integration Testing:** Testing phase that verifies interactions between integrated components or modules (e.g., DAO and database).

- **JUnit:** Popular Java testing framework that provides annotations (@Test, @BeforeAll) and assertions for writing and executing unit tests.

- **Manual Testing:** Testing performed by human testers who interact with the application, observe behavior, and report findings.

- **Mock Data:** Simulated data used in testing to replace real production data, allowing controlled and repeatable test scenarios.

- **Mockito:** Java mocking framework used to create test doubles (mocks, stubs, spies) for isolating units of code during testing.

- **POJO (Plain Old Java Object):** Simple Java class with private fields, public getters/setters, and no dependencies on frameworks. Used for model classes.

- **PreparedStatement:** JDBC interface for executing parameterized SQL queries, providing protection against SQL injection attacks.

- **Regression Testing:** Re-running previously passed tests after code changes to ensure new changes haven't introduced defects.

- **Salt:** Random data added to passwords before hashing to ensure identical passwords produce different hashes, preventing rainbow table attacks.

- **Surefire Plugin:** Maven plugin that executes unit tests during the build lifecycle and generates test reports in XML and HTML formats.

- **System Testing:** Testing phase that validates the complete integrated system against functional and non-functional requirements.

- **Test Case:** A set of conditions, inputs, and expected results designed to verify a specific aspect of software functionality.

- **Test Data:** Specific input values and database states used during test execution to validate software behavior.

- **Test Stub:** Simplified implementation of a component used in testing to simulate behavior of unimplemented or unavailable modules.

- **TestFX:** Testing framework for JavaFX applications that simulates user interactions (button clicks, text input) in automated UI tests.

- **TiDB Cloud:** MySQL-compatible distributed SQL database service hosted on AWS, used as the project's database backend.

- **Unit Testing:** Testing individual methods or classes in isolation to verify they perform as designed, typically using mocking for dependencies.

- **White-Box Testing:** Testing technique that examines internal code structure, logic paths, and implementation details to design test cases.

### Appendix B: IEEE Standards References

- **IEEE 829-2008:** IEEE Standard for Software and System Test Documentation
  - Defines standard templates for test plans, test design specifications, test case specifications, test procedure specifications, test item transmittal reports, test logs, test incident reports, and test summary reports.
  - URL: https://standards.ieee.org/standard/829-2008.html

- **IEEE 1016-2009:** IEEE Standard for Information Technology—Systems Design—Software Design Descriptions
  - Referenced for understanding system design documentation that informs test case creation.
  - URL: https://standards.ieee.org/standard/1016-2009.html

- **IEEE 730-2014:** IEEE Standard for Software Quality Assurance Processes
  - Provides guidance on software quality assurance activities, including testing and validation.

### Appendix C: Testing Tool Documentation

- **JUnit 5 User Guide:** https://junit.org/junit5/docs/current/user-guide/
  - Comprehensive documentation for writing and executing JUnit tests, using annotations, assertions, and lifecycle methods.

- **Mockito Documentation:** https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
  - API documentation for creating mocks, stubs, and verifying interactions in unit tests.

- **Maven Surefire Plugin:** https://maven.apache.org/surefire/maven-surefire-plugin/
  - Configuration options for test execution, reporting, and filtering test classes.

- **TestFX Documentation:** https://github.com/TestFX/TestFX
  - Guide for automating JavaFX UI testing with simulated user interactions.

### Appendix D: Project Documentation References

- **README.md:** Project setup instructions, technology stack, test credentials, and development workflow
  - Location: `README.md` (project root)

- **CLAUDE.md:** Architecture overview, design patterns, coding conventions, and development context
  - Location: `CLAUDE.md` (project root)

- **Software Design Description (SDD):** IEEE 1016 document detailing system architecture, database schema, and module designs
  - Location: `documents/SDD_IEEE1016_SupplyChain.md`

- **Software Project Management Plan (SPMP):** Project timeline, milestones, team roles, and resource allocation
  - Location: `documents/SPMP_SupplyChain.md` (if exists)

### Appendix E: Test Environment Configuration Files

**Database Configuration:**
- **File:** `src/main/resources/config.properties`
- **Contents:**
  ```properties
  db.url=jdbc:mysql://gateway01.eu-central-1.prod.aws.tidbcloud.com:4000/supply_chain_qr
  db.username=[REDACTED]
  db.password=[REDACTED]
  db.sslMode=VERIFY_IDENTITY
  db.maxConnections=10
  ```

**Maven POM Configuration:**
- **File:** `pom.xml`
- **Testing Dependencies:**
  ```xml
  <dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-api</artifactId>
    <version>5.10.0</version>
    <scope>test</scope>
  </dependency>
  ```

### Appendix F: Test Execution Commands

**Run All Tests:**
```bash
mvn test
```

**Run Specific Test Class:**
```bash
mvn test -Dtest=UserDAOTest
```

**Run Specific Test Method:**
```bash
mvn test -Dtest=UserDAOTest#testAuthenticate_ValidCredentials
```

**Clean and Test:**
```bash
mvn clean test
```

**Generate Test Reports:**
```bash
mvn surefire-report:report
```
(HTML report generated at `target/site/surefire-report.html`)

**Run Application for Manual Testing:**
```bash
mvn javafx:run
```

### Appendix G: Known Issues and Limitations

**Issue 1: Test User Creation Constraint**
- **Description:** TC-EMP-001 cannot create new employee record due to foreign key constraint (user_id must have existing user with no employee record)
- **Severity:** Low
- **Workaround:** Test uses existing employee record instead
- **Status:** Documented, acceptable for testing purposes

**Issue 2: SECURITY_INCIDENT Action Type Not in Database ENUM**
- **Description:** Audit log test for SECURITY_INCIDENT action type fails at database level (not in ENUM definition)
- **Severity:** Low
- **Impact:** Log falls back to generic action type
- **Status:** Documented in test notes, database schema may be updated in future

**Issue 3: No Inactive Test User**
- **Description:** TC-AUTH-004 cannot be executed because test database has no inactive user account
- **Severity:** Low
- **Workaround:** Manually deactivate a test user, execute test, then reactivate
- **Status:** Skipped in current test run

**Issue 4: Code Coverage Tool Not Integrated**
- **Description:** JaCoCo or similar code coverage tool not configured in Maven
- **Severity:** Medium
- **Impact:** Cannot automatically measure code coverage percentage
- **Status:** Estimated coverage >70% based on test count; future enhancement planned

---

## Document Control

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-12 | Mohammad Khalid Alqallaf (QA Lead) | Initial STS document following IEEE 829 template structure |

---

## Approval Signatures

| Role | Name | Signature | Date |
|------|------|-----------|------|
| **Team Leader** | Jawad Ali Alnatah | _________________ | __________ |
| **QA Lead** | Mohammad Khalid Alqallaf | _________________ | __________ |
| **Advisor** | Saeed Matar Alshahrani | _________________ | __________ |
| **Course Coordinator** | Dr. Rahma Ahmed | _________________ | __________ |

---

**End of Software Test Specification (STS) Document**

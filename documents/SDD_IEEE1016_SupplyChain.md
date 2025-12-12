# Software Design Description (SDD)
## Supply Chain Management System with QR-Based Employee Attendance Tracking

**CSC 305: Software Engineering**
**Term:** 1
**Academic Year:** 2025/2026
**Course Coordinator:** Dr. Rahma Ahmed
**Team:** Team 6
**Date:** December 12, 2025

---

## Table of Contents
- 1. Introduction
- 2. System Overview
- 3. Design Considerations
- 4. Architectural Design
- 5. Detailed Design
- 6. Data Design
- 7. External Interfaces
- 8. Appendices

---

## 1. Introduction

### Purpose
This Software Design Description (SDD) describes the architecture and detailed design of the Supply Chain Management System with QR-Based Employee Attendance Tracking for Fresh Dairy Co. This document provides technical specifications and design decisions to guide the development, testing, and maintenance of the system.

### Intended Audience
- **Developers:** Implementation guidance for coding and integration
- **Testers:** Understanding system architecture for test planning
- **Instructor/Advisor:** Evaluation of design approach and technical decisions
- **Team Members:** Reference for consistent development practices

### References to Related Documents
- **Software Requirements Specification (SRS):** Requirements and use cases (Week 11)
- **Software Project Management Plan (SPMP):** Project timeline, roles, and deliverables (Week 8)
- **Project Proposal Form:** Initial project concept and objectives (Week 4)
- **Project Idea Form:** Problem statement and solution overview (Week 3)
- **README.md:** Setup instructions, technology stack, and troubleshooting
- **CLAUDE.md:** Development guidelines and coding standards

---

## 2. System Overview

### High-Level Description
The Supply Chain Management System is a JavaFX-based desktop application integrated with a web-based QR scanner for employee attendance tracking. The system serves Fresh Dairy Co., a dairy processing company that manages inventory of finished products, supplier relationships, purchase orders, and employee attendance.

**Core Functionality:**
- **Inventory Management:** Track finished dairy products with automated reorder alerts
- **Supplier Management:** Maintain relationships with dairy farms and packaging suppliers
- **Purchase Order System:** Automate procurement workflows from requisition to receipt
- **QR-Based Attendance:** Security guard-verified employee check-in/check-out with photo verification
- **Admin Dashboard:** Real-time metrics including inventory levels, active users, and system health
- **Audit Logging:** Track all critical operations for compliance and security

### Design Strategy
The system follows a **layered architecture** with clear separation of concerns:

1. **Presentation Layer:** JavaFX GUI (FXML + Controllers) + HTML5 Web Scanner
2. **Application Layer:** Business logic (Controllers, Services, DAOs)
3. **Data Layer:** TiDB Cloud database (MySQL-compatible)

**Architectural Style:** Model-View-Controller (MVC) Pattern
- **Model:** Domain objects (POJOs) representing business entities
- **View:** FXML files defining UI layout and styling
- **Controller:** Event handlers connecting UI to business logic

**Key Design Patterns:**
- **Data Access Object (DAO):** Encapsulates database operations
- **Singleton:** DatabaseConnection for centralized connection management
- **Factory (Planned):** Service layer will use factories for object creation

### Context Diagram

```
┌─────────────────────────────────────────────────────────┐
│                  External Entities                      │
│                                                         │
│  [Dairy Farm      [Packaging      [Warehouse      [Security│
│   Suppliers]       Suppliers]      Employees]      Guard]  │
│        │               │                │              │    │
└────────┼───────────────┼────────────────┼──────────────┼────┘
         │               │                │              │
         │               │                │              │
         ▼               ▼                ▼              ▼
┌─────────────────────────────────────────────────────────┐
│         Supply Chain Management System                  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │          Desktop Application (JavaFX)            │  │
│  │  - Admin Dashboard    - Manager Dashboard        │  │
│  │  - Employee Dashboard - Requisition Management   │  │
│  │  - Audit Logs         - Purchase Orders          │  │
│  └──────────────────────────────────────────────────┘  │
│                         │                               │
│  ┌──────────────────────────────────────────────────┐  │
│  │      Business Logic Layer (Controllers + DAOs)   │  │
│  └──────────────────────────────────────────────────┘  │
│                         │                               │
│  ┌──────────────────────────────────────────────────┐  │
│  │        TiDB Cloud Database (MySQL)               │  │
│  │  [users] [employees] [inventory] [suppliers]     │  │
│  │  [requisitions] [audit_logs]                     │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │      Web QR Scanner (HTML5 + JavaScript)         │  │
│  │  - Camera access    - QR decoding                │  │
│  │  - Employee verification  - Attendance recording │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Design Considerations

### Assumptions
1. **Network Connectivity:** Users have stable internet connection to access TiDB Cloud database
2. **Single Gate Entry:** Security guard stationed at single main gate for attendance verification
3. **Physical QR Badges:** Employees carry printed QR code badges
4. **Photo Storage:** Employee photos uploaded to cloud storage with URLs stored in database
5. **Desktop Environment:** Users have Windows 10+, macOS 11+, or Ubuntu 20.04+ with Java 17 installed
6. **Browser Compatibility:** QR scanner works on iOS Safari 14+ and Chrome 90+ with camera access

### Constraints

**Technical Constraints:**
- **Java Version:** Must use Java 17 (LTS) - no earlier versions supported due to modern language features
- **Database:** TiDB Cloud enforces SSL/TLS connections (mandatory, cannot be disabled)
- **JavaFX Version:** JavaFX 21 required for desktop GUI
- **Screen Resolution:** Minimum 1366×768 pixels for desktop application
- **Database Compatibility:** Must maintain MySQL 8.0 compatibility for TiDB Cloud

**Performance Constraints:**
- Database query response time: <2 seconds for 95% of queries
- Application startup time: <5 seconds
- QR scan to verification display: <2 seconds
- Dashboard metrics refresh: <30 seconds for real-time data
- Concurrent users supported: 50 maximum (scalability limit)

**Security Constraints:**
- All passwords must be BCrypt hashed with minimum 10 salt rounds
- All database connections must use SSL/TLS encryption
- Role-based access control required for all features
- PreparedStatements mandatory for all SQL queries (no string concatenation)
- Session timeout after 30 minutes of inactivity (future enhancement)

**Development Constraints:**
- Maven 3.9+ for build automation (consistent across team)
- Git with feature branch workflow (no direct commits to main)
- Code reviews required for all pull requests
- JUnit 5 for unit testing with >70% code coverage target

### Design Goals

1. **Maintainability:** Clear separation of concerns (MVC pattern), well-documented code (Javadoc), modular architecture
2. **Security:** BCrypt password hashing, SQL injection prevention, role-based access control, audit logging
3. **Usability:** Intuitive UI requiring minimal training, clear error messages, consistent design patterns
4. **Reliability:** Graceful error handling, data integrity checks, no application crashes, backup/recovery support
5. **Testability:** DAO layer fully unit-testable, mock-friendly design, automated test suite
6. **Performance:** Fast query responses, efficient database connection management, optimized UI rendering
7. **Scalability:** Support growth to 50+ concurrent users, handle 10,000+ inventory items, 5 years of attendance history

### Trade-offs

**1. Controllers Call DAOs Directly vs. Service Layer**
- **Decision:** Controllers bypass service layer and call DAOs directly
- **Trade-off:** Faster initial development but less business logic encapsulation
- **Rationale:** Academic project timeline prioritizes working features over perfect architecture
- **Future:** Service layer package exists as stub for future refactoring

**2. TiDB Cloud vs. Local MySQL**
- **Decision:** Use TiDB Cloud serverless database
- **Trade-off:** Internet dependency but automatic backups, scaling, and team accessibility
- **Rationale:** No local database setup required, team can access shared data

**3. JavaFX vs. Web Application**
- **Decision:** JavaFX desktop app for main interface
- **Trade-off:** Desktop-only but richer UI components and offline capability
- **Rationale:** Course requirement, better performance for data-intensive operations

**4. QR Scanner: Web vs. Desktop Integration**
- **Decision:** Separate HTML5 web scanner for mobile devices
- **Trade-off:** Two platforms to maintain but better mobile camera access
- **Rationale:** JavaFX camera support limited, web provides universal mobile compatibility

---

## 4. Architectural Design

### Overall System Architecture

The system follows a **3-tier layered architecture**:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                       │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Desktop Application (JavaFX 21)                          │
│   ┌───────────────────────────────────────────────────┐    │
│   │  FXML Views                                       │    │
│   │  - Login.fxml                                     │    │
│   │  - AdminDashboard.fxml                            │    │
│   │  - ManagerDashboard.fxml                          │    │
│   │  - EmployeeDashboard.fxml                         │    │
│   │  - EmployeeCreateRequisitionView.fxml             │    │
│   │  - EmployeeRequisitionsView.fxml                  │    │
│   │  - AdminAuditLogs.fxml                            │    │
│   │  - ManagerPurchaseOrders.fxml                     │    │
│   └───────────────────────────────────────────────────┘    │
│                                                             │
│   Web QR Scanner (HTML5 + JavaScript)                      │
│   ┌───────────────────────────────────────────────────┐    │
│   │  - Camera access (getUserMedia API)               │    │
│   │  - QR decoding (ZXing JavaScript library)         │    │
│   │  - Employee verification display                  │    │
│   │  - Security guard confirmation interface          │    │
│   └───────────────────────────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                   APPLICATION LAYER                         │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   Controllers (UI Event Handlers)                          │
│   ┌───────────────────────────────────────────────────┐    │
│   │  - LoginController                                │    │
│   │  - AdminDashboardController                       │    │
│   │  - ManagerDashboardController                     │    │
│   │  - EmployeeDashboardController                    │    │
│   │  - EmployeeCreateRequisitionViewController        │    │
│   │  - EmployeeRequisitionsController                 │    │
│   │  - AdminAuditLogsController                       │    │
│   │  - ManagerPurchaseOrdersController                │    │
│   └───────────────────────────────────────────────────┘    │
│                                                             │
│   Services (Business Logic - Future)                       │
│   ┌───────────────────────────────────────────────────┐    │
│   │  - AuthenticationService (stub)                   │    │
│   │  - InventoryService (stub)                        │    │
│   │  - AttendanceService (stub)                       │    │
│   └───────────────────────────────────────────────────┘    │
│                                                             │
│   Data Access Objects (DAOs)                               │
│   ┌───────────────────────────────────────────────────┐    │
│   │  - UserDAO                                        │    │
│   │  - EmployeeDAO                                    │    │
│   │  - InventoryDAO                                   │    │
│   │  - SupplierDAO (stub)                             │    │
│   │  - RequisitionDAO                                 │    │
│   │  - AuditLogDAO                                    │    │
│   │  - AttendanceDAO (stub)                           │    │
│   └───────────────────────────────────────────────────┘    │
│                                                             │
│   Domain Models (POJOs)                                    │
│   ┌───────────────────────────────────────────────────┐    │
│   │  - User, Employee, Requisition                    │    │
│   │  - RequisitionItem, AuditLog                      │    │
│   └───────────────────────────────────────────────────┘    │
│                                                             │
│   Utilities                                                 │
│   ┌───────────────────────────────────────────────────┐    │
│   │  - DatabaseConnection (Singleton)                 │    │
│   │  - PasswordUtil (BCrypt wrapper)                  │    │
│   └───────────────────────────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                          ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│   TiDB Cloud Database (MySQL 8.0 compatible)               │
│   ┌───────────────────────────────────────────────────┐    │
│   │  Tables:                                          │    │
│   │  - users (authentication, roles)                  │    │
│   │  - employees (QR codes, departments)              │    │
│   │  - inventory_items (stock, reorder levels)        │    │
│   │  - suppliers (contacts, ratings)                  │    │
│   │  - requisitions (purchase requests)               │    │
│   │  - requisition_items (line items)                 │    │
│   │  - purchase_orders (supplier orders)              │    │
│   │  - attendance (check-in/check-out records)        │    │
│   │  - audit_logs (system activity tracking)          │    │
│   │                                                   │    │
│   │  Connection: SSL/TLS, PreparedStatements          │    │
│   └───────────────────────────────────────────────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

### Major Components and Interactions

**Component 1: Desktop Application (JavaFX)**
- **Responsibility:** Primary user interface for all roles (Admin, Manager, Employee)
- **Technology:** JavaFX 21, FXML, CSS
- **Interactions:**
  - Communicates with DAOs for database operations
  - Displays data from domain models
  - Handles user input and validation

**Component 2: Web QR Scanner**
- **Responsibility:** Mobile/tablet interface for security guard attendance verification
- **Technology:** HTML5, JavaScript, ZXing library
- **Interactions:**
  - Accesses device camera via getUserMedia API
  - Decodes QR code and sends employee_id to backend
  - Receives employee data (photo, name, department) for verification
  - Sends guard confirmation (approve/deny) to attendance system

**Component 3: Controllers**
- **Responsibility:** Handle UI events, coordinate between views and DAOs
- **Technology:** Java 17, JavaFX annotations (@FXML)
- **Interactions:**
  - Receive user input from FXML views
  - Call DAO methods for CRUD operations
  - Update UI with results
  - Handle navigation between views

**Component 4: Data Access Objects (DAOs)**
- **Responsibility:** Encapsulate all database operations, provide clean API to controllers
- **Technology:** Java 17, JDBC (MySQL Connector/J 8.2.0), PreparedStatements
- **Interactions:**
  - Use DatabaseConnection singleton to get connections
  - Execute SQL queries with PreparedStatements
  - Map ResultSets to domain model objects
  - Return data to controllers

**Component 5: TiDB Cloud Database**
- **Responsibility:** Persistent data storage, enforce data integrity
- **Technology:** TiDB Cloud (MySQL 8.0 compatible), SSL/TLS
- **Interactions:**
  - Accepts connections from DAOs
  - Enforces foreign key constraints
  - Provides ACID transactions
  - Automatic backups and replication

### Deployment Diagram

```
┌──────────────────────────────────────────────────────────┐
│               User Workstation (Desktop)                 │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Java 17 Runtime Environment                       │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  Supply Chain Management Application         │  │  │
│  │  │  (supply-chain-management-1.0.0.jar)         │  │  │
│  │  │  - JavaFX GUI                                │  │  │
│  │  │  - Controllers + DAOs                        │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────┘  │
│                        │                                 │
│                        │ JDBC over                       │
│                        │ SSL/TLS                         │
│                        │                                 │
└────────────────────────┼─────────────────────────────────┘
                         ▼
         ┌───────────────────────────────┐
         │   TiDB Cloud (AWS EU-Central) │
         │  ┌───────────────────────────┐│
         │  │  supply_chain_qr Database ││
         │  │  - MySQL 8.0 compatible   ││
         │  │  - Auto-scaling           ││
         │  │  - Daily backups          ││
         │  └───────────────────────────┘│
         └───────────────────────────────┘
                         ▲
                         │
                         │ HTTPS
                         │
┌────────────────────────┼─────────────────────────────────┐
│    Mobile Device/Tablet (Gate Scanner)                   │
│  ┌────────────────────────────────────────────────────┐  │
│  │  Web Browser (Chrome/Safari)                       │  │
│  │  ┌──────────────────────────────────────────────┐  │  │
│  │  │  QR Scanner Web App                          │  │  │
│  │  │  - HTML5 + JavaScript                        │  │  │
│  │  │  - Camera access (getUserMedia)              │  │  │
│  │  │  - ZXing QR decoder                          │  │  │
│  │  └──────────────────────────────────────────────┘  │  │
│  └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

---

## 5. Detailed Design

### Module/Component Details

#### Module 1: Authentication Module

**Responsibilities:**
- Validate user credentials (username/password)
- Verify BCrypt password hashes
- Establish user session
- Route users to appropriate dashboard based on role
- Update last_login timestamp

**Inputs:**
- Username (String)
- Password (String, plaintext)

**Outputs:**
- User object (if authentication successful)
- null (if authentication fails)

**Classes:**
- `LoginController` - Handles login UI events
- `UserDAO` - Database operations for users table
- `PasswordUtil` - BCrypt hashing and verification

**Algorithms:**
1. User enters username and password in LoginController
2. LoginController calls `UserDAO.authenticate(username, password)`
3. UserDAO queries database: `SELECT * FROM users WHERE username = ? AND is_active = true`
4. If user found, UserDAO calls `PasswordUtil.checkPassword(plaintext, hash)`
5. BCrypt.checkpw() verifies password against stored hash
6. If valid, UserDAO updates last_login timestamp
7. User object returned to LoginController
8. LoginController navigates to role-specific dashboard (Admin/Manager/Employee)

**Sequence Diagram:**

```
User → LoginController → UserDAO → Database
                             ↓
                        PasswordUtil
                             ↓
                    [Valid? Yes/No]
                             ↓
User ← DashboardController ← User object
```

---

#### Module 2: Admin Dashboard Module

**Responsibilities:**
- Display real-time system metrics (active users, inventory counts, low stock, pending tasks)
- Calculate and display system health status
- Show critical alerts (low stock, out of stock, pending requisitions)
- Provide navigation to all system modules

**Inputs:**
- Current logged-in User object

**Outputs:**
- Dashboard metrics displayed in UI
- Critical alerts list
- System health status (Excellent/Good/Fair/Poor/Critical)

**Classes:**
- `AdminDashboardController` - Main dashboard UI controller
- `UserDAO` - Get active user count
- `InventoryDAO` - Get inventory statistics
- `RequisitionDAO` - Get pending requisitions count
- `AuditLogDAO` - Get recent activity count

**Algorithms:**

**System Health Calculation:**
```java
int healthScore = 100;
int totalItems = inventoryDAO.getTotalItemsCount();
int lowStockCount = inventoryDAO.getLowStockCount();

if (totalItems > 0) {
    double lowStockPercentage = (lowStockCount * 100.0) / totalItems;
    if (lowStockPercentage > 30) healthScore -= 40;
    else if (lowStockPercentage > 15) healthScore -= 20;
    else if (lowStockPercentage > 5) healthScore -= 10;
}

int activeUsers = userDAO.getActiveUserCount();
if (activeUsers < 5) healthScore -= 10;

String status;
if (healthScore >= 90) status = "Excellent";
else if (healthScore >= 75) status = "Good";
else if (healthScore >= 60) status = "Fair";
else if (healthScore >= 40) status = "Poor";
else status = "Critical";
```

**Notes:**
- Dashboard metrics refresh on page load
- Future enhancement: Auto-refresh every 30 seconds
- Critical alerts styled with color coding (red=critical, yellow=warning, blue=info)

---

#### Module 3: Requisition Management Module

**Responsibilities:**
- Allow employees to create purchase requisitions
- Search and select inventory items
- Calculate subtotals and totals automatically
- Generate unique requisition codes (REQ-XXXXX)
- Submit requisitions for manager approval
- Display employee's requisition history

**Inputs:**
- Selected inventory items with quantities
- Category, department, priority, justification

**Outputs:**
- Created requisition with unique requisition_id
- Requisition items linked to requisition
- Success/failure confirmation message

**Classes:**
- `EmployeeCreateRequisitionViewController` - UI for creating requisitions
- `EmployeeRequisitionsController` - Display employee's requisitions
- `RequisitionDAO` - Database operations for requisitions and items
- `Requisition` (Model) - Requisition domain object
- `RequisitionItem` (Model) - Line item domain object

**Algorithms:**

**Requisition Creation Flow:**
1. Employee searches inventory items by name/category
2. Employee adds items to cart with specified quantities
3. System calculates subtotal: `quantity × unit_price`
4. System calculates total: `sum of all subtotals`
5. Employee fills form: category, department, priority, justification
6. System generates unique code: `REQ-XXXXX` (5-digit number from MAX(requisition_id) + 1)
7. Employee clicks "Submit Requisition"
8. Controller creates Requisition object with items list
9. Controller calls `RequisitionDAO.createRequisition(requisition)`
10. DAO inserts requisition row, gets generated requisition_id
11. DAO inserts all requisition_items rows with requisition_id foreign key
12. Success message displayed, employee redirected to "My Requisitions"

**Class Diagram:**

```
┌──────────────────────────────────┐
│ EmployeeCreateRequisitionView   │
│ Controller                       │
├──────────────────────────────────┤
│ - inventoryData: ObservableList  │
│ - selectedItems: ObservableList  │
│ - inventoryDAO: InventoryDAO     │
│ - requisitionDAO: RequisitionDAO │
├──────────────────────────────────┤
│ + handleSearch()                 │
│ + handleAddItem()                │
│ + handleRemoveItem()             │
│ + handleSubmit()                 │
│ - calculateTotal(): BigDecimal   │
└──────────────────────────────────┘
            │
            │ uses
            ▼
┌──────────────────────────────────┐
│ RequisitionDAO                   │
├──────────────────────────────────┤
│ + createRequisition()            │
│ + getRequisitionsByUser()        │
│ + getPendingRequisitions()       │
│ + updateRequisitionStatus()      │
│ + generateRequisitionCode()      │
└──────────────────────────────────┘
            │
            │ creates
            ▼
┌──────────────────────────────────┐
│ Requisition                      │
├──────────────────────────────────┤
│ - requisitionId: Integer         │
│ - requisitionCode: String        │
│ - requestedBy: Integer           │
│ - category: String               │
│ - department: String             │
│ - priority: String               │
│ - status: String                 │
│ - totalAmount: BigDecimal        │
│ - items: List<RequisitionItem>   │
└──────────────────────────────────┘
```

---

#### Module 4: Audit Logging Module

**Responsibilities:**
- Log all critical system operations (CREATE, UPDATE, DELETE, LOGIN, etc.)
- Support filtering by action type, module, result, date range
- Provide pagination for large log sets
- Export audit logs to Excel for compliance
- Archive old logs (delete logs older than X days)

**Inputs:**
- User ID, username, action type, module, description

**Outputs:**
- Log entry created with unique log_code and timestamp
- Filtered list of audit logs for viewing
- Export file (Excel) for reporting

**Classes:**
- `AdminAuditLogsController` - UI for viewing/filtering logs
- `AuditLogDAO` - Database operations for audit_logs table
- `AuditLog` (Model) - Audit log domain object

**Algorithms:**

**Logging Success Action:**
```java
public boolean logSuccess(Integer userId, String username,
                          String actionType, String module, String description) {
    String logCode = generateLogCode(); // LOG-YYYYMMDDHHMMSS-XXX
    String sql = "INSERT INTO audit_logs (log_code, user_id, username, " +
                 "action_type, module, description, result, timestamp) " +
                 "VALUES (?, ?, ?, ?, ?, ?, 'Success', NOW())";

    try (Connection conn = DatabaseConnection.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, logCode);
        stmt.setInt(2, userId);
        stmt.setString(3, username);
        stmt.setString(4, actionType);
        stmt.setString(5, module);
        stmt.setString(6, description);
        return stmt.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
```

**Action Types:** CREATE, UPDATE, DELETE, LOGIN, LOGOUT, READ, BACKUP

---

#### Module 5: Inventory Statistics Module

**Responsibilities:**
- Calculate total inventory item count
- Identify low stock items (quantity ≤ reorder_level)
- Identify out-of-stock items (quantity = 0)
- Calculate total inventory value (sum of quantity × unit_price)

**Inputs:** None (queries entire inventory_items table)

**Outputs:**
- Total items count (Integer)
- Low stock count (Integer)
- Out of stock count (Integer)
- Total inventory value (Double)

**Classes:**
- `InventoryDAO` - Database queries for statistics

**Algorithms:**

```java
public int getLowStockCount() {
    String sql = "SELECT COUNT(*) as count FROM inventory_items " +
                 "WHERE quantity <= reorder_level";
    // Execute query, return count
}

public double getTotalInventoryValue() {
    String sql = "SELECT SUM(quantity * unit_price) as total_value " +
                 "FROM inventory_items";
    // Execute query, return total_value
}
```

---

## 6. Data Design

### Database Schema

The system uses TiDB Cloud (MySQL 8.0 compatible) with the following tables:

#### Table: users
```sql
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(60) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('ADMIN', 'MANAGER', 'EMPLOYEE', 'SUPPLIER') NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);
```

**Purpose:** Store user authentication and profile data
**Key Fields:**
- `password_hash` - BCrypt hash with $2a$ prefix (60 characters)
- `role` - ENUM restricts to valid roles only
- `is_active` - Soft delete (deactivate instead of delete)

---

#### Table: employees
```sql
CREATE TABLE employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    department VARCHAR(50),
    position VARCHAR(50),
    phone VARCHAR(20),
    qr_code VARCHAR(100) UNIQUE,
    hire_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

**Purpose:** Store employee-specific data linked to user accounts
**Key Fields:**
- `user_id` - UNIQUE constraint ensures one employee record per user
- `qr_code` - UNIQUE ensures no duplicate QR codes
**Relationships:**
- Many-to-One with users (each employee has one user account)

---

#### Table: inventory_items
```sql
CREATE TABLE inventory_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    quantity INT NOT NULL DEFAULT 0,
    unit_price DECIMAL(10,2),
    reorder_level INT,
    reorder_quantity INT,
    supplier_id INT,
    location VARCHAR(100),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL
);
```

**Purpose:** Track finished product inventory levels
**Key Fields:**
- `reorder_level` - Triggers low stock alert when quantity ≤ reorder_level
- `reorder_quantity` - Suggested quantity for purchase orders
- `last_updated` - Automatic timestamp on any update

---

#### Table: suppliers
```sql
CREATE TABLE suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    rating DECIMAL(2,1),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

**Purpose:** Manage supplier contacts and ratings
**Key Fields:**
- `rating` - 1.0 to 5.0 star rating for supplier performance
- `is_active` - Filter out inactive suppliers

---

#### Table: requisitions
```sql
CREATE TABLE requisitions (
    requisition_id INT PRIMARY KEY AUTO_INCREMENT,
    requisition_code VARCHAR(50) UNIQUE NOT NULL,
    requested_by INT NOT NULL,
    supplier_id INT,
    category VARCHAR(50),
    department VARCHAR(50),
    priority VARCHAR(20),
    justification TEXT,
    status VARCHAR(20) DEFAULT 'Pending',
    total_amount DECIMAL(10,2),
    total_items INT,
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    reviewed_by INT,
    review_date TIMESTAMP,
    review_notes TEXT,
    FOREIGN KEY (requested_by) REFERENCES users(user_id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL,
    FOREIGN KEY (reviewed_by) REFERENCES users(user_id)
);
```

**Purpose:** Store purchase requisitions from employees
**Key Fields:**
- `requisition_code` - Unique identifier (REQ-XXXXX format)
- `status` - "Pending", "Approved", "Rejected"
- `reviewed_by` - Manager who approved/rejected

---

#### Table: requisition_items
```sql
CREATE TABLE requisition_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    requisition_id INT NOT NULL,
    item_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (requisition_id) REFERENCES requisitions(requisition_id) ON DELETE CASCADE
);
```

**Purpose:** Line items for each requisition
**Relationships:**
- Many-to-One with requisitions (cascade delete when requisition deleted)

---

#### Table: audit_logs
```sql
CREATE TABLE audit_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    log_code VARCHAR(50) UNIQUE NOT NULL,
    user_id INT,
    username VARCHAR(50) NOT NULL,
    action_type ENUM('CREATE','UPDATE','DELETE','LOGIN','LOGOUT','READ','BACKUP') NOT NULL,
    module VARCHAR(50) NOT NULL,
    description TEXT,
    result VARCHAR(20) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL
);
```

**Purpose:** Track all critical system operations for compliance and security
**Key Fields:**
- `log_code` - Unique identifier (LOG-YYYYMMDDHHMMSS-XXX)
- `action_type` - ENUM restricts to valid actions
- `result` - "Success" or "Failure"

---

### Entity-Relationship Diagram (ERD)

```
┌─────────────┐          ┌───────────────┐
│   users     │──────────│   employees   │
│             │ 1      1 │               │
│ user_id (PK)│──────────│ employee_id   │
│ username    │          │ user_id (FK)  │
│ password_hash│         │ qr_code       │
│ role        │          │ department    │
│ is_active   │          └───────────────┘
└─────────────┘
      │
      │ 1
      │
      │ *
┌─────────────┐          ┌──────────────────┐
│requisitions │──────────│ requisition_items│
│             │ 1      * │                  │
│requisition_id│─────────│ item_id (PK)     │
│req_code     │          │ requisition_id   │
│requested_by │          │ item_name        │
│(FK→users)   │          │ quantity         │
│reviewed_by  │          │ subtotal         │
│(FK→users)   │          └──────────────────┘
│supplier_id  │
│(FK→suppliers)│
└─────────────┘
      │
      │ *
      │
      │ 1
┌─────────────┐
│  suppliers  │
│             │
│supplier_id  │
│supplier_name│
│rating       │
│is_active    │
└─────────────┘
      │
      │ 1
      │
      │ *
┌─────────────┐
│ inventory_  │
│   items     │
│             │
│ item_id (PK)│
│ item_name   │
│ quantity    │
│ reorder_level│
│ supplier_id │
│   (FK)      │
└─────────────┘


┌─────────────┐
│ audit_logs  │
│             │
│ log_id (PK) │
│ user_id (FK)│
│ action_type │
│ module      │
│ result      │
│ timestamp   │
└─────────────┘
```

---

## 7. External Interfaces

### 7.1 User Interfaces

**Desktop Application (JavaFX)**
- **Login Screen:** Username, password fields, login button
- **Admin Dashboard:** Metrics cards, critical alerts panel, navigation menu
- **Manager Dashboard:** Purchase orders table, approval actions
- **Employee Dashboard:** Create requisition, view my requisitions
- **Audit Logs View:** Filterable table with export button

**Web QR Scanner**
- **Scanner Interface:** Camera viewfinder, scan status indicator
- **Verification Screen:** Employee photo, name, department, confirm/deny buttons

### 7.2 Hardware Interfaces

**Desktop Hardware:**
- Minimum 8GB RAM, 2 GHz processor
- Screen resolution 1366×768 minimum
- Keyboard and mouse

**Mobile/Tablet Hardware (QR Scanner):**
- Rear camera for QR code scanning
- iOS/Android device with modern browser
- Stable internet connection (4G/WiFi)

### 7.3 Software Interfaces

**Database Interface:**
- **Protocol:** JDBC (MySQL Connector/J 8.2.0)
- **Connection:** SSL/TLS encrypted (enforced by TiDB Cloud)
- **Connection String:**
  ```
  jdbc:mysql://gateway01.eu-central-1.prod.aws.tidbcloud.com:4000/supply_chain_qr?sslMode=VERIFY_IDENTITY
  ```
- **Authentication:** Username/password credentials
- **Singleton Pattern:** DatabaseConnection class manages single connection pool

**External Libraries:**
- **JavaFX 21:** UI framework for desktop application
- **BCrypt (jBCrypt 0.4):** Password hashing
- **Apache POI 5.2.5:** Excel export for reports
- **iText PDF 5.5.13.3:** PDF generation
- **ZXing 3.5.3:** QR code generation and scanning
- **Gson 2.10.1:** JSON processing for web scanner communication

### 7.4 Communication Protocols

**Desktop ↔ Database:**
- **Protocol:** JDBC over SSL/TLS
- **Port:** 4000 (TiDB Cloud default)
- **Data Format:** SQL queries, ResultSets

**Web Scanner ↔ Backend:**
- **Protocol:** HTTPS (future implementation)
- **Data Format:** JSON
- **Request:** POST employee_id from scanned QR code
- **Response:** JSON with employee data (name, photo_url, department)

**Sample JSON Response:**
```json
{
  "employee_id": 1,
  "name": "Ahmed Ali",
  "department": "Warehouse Operations",
  "position": "Warehouse Worker",
  "photo_url": "https://storage.tidbcloud.com/employee_001.jpg",
  "status": "active"
}
```

---

## 8. Appendices

### Appendix A: Glossary of Terms

| Term | Definition |
|------|------------|
| **DAO** | Data Access Object - Design pattern encapsulating database operations |
| **BCrypt** | Password hashing algorithm with built-in salt, industry standard for security |
| **FXML** | FX Markup Language - XML-based language for defining JavaFX UI layouts |
| **MVC** | Model-View-Controller - Design pattern separating data, UI, and logic |
| **RBAC** | Role-Based Access Control - Security model restricting features by user role |
| **PreparedStatement** | JDBC API for safe SQL execution preventing SQL injection |
| **Singleton** | Design pattern ensuring only one instance of a class exists |
| **TiDB Cloud** | MySQL-compatible serverless database platform |
| **QR Code** | Quick Response code - 2D barcode storing data for rapid scanning |
| **CRUD** | Create, Read, Update, Delete - Basic database operations |
| **SKU** | Stock Keeping Unit - Unique identifier for inventory items |
| **PO** | Purchase Order - Formal document requesting goods from supplier |

### Appendix B: Supporting Diagrams

**Use Case Diagram: Employee Creates Requisition**
```
   ┌──────────┐
   │ Employee │
   └─────┬────┘
         │
         │ (1) Searches inventory items
         ▼
   ┌──────────────┐
   │   System     │
   └──────┬───────┘
          │ (2) Displays available items
          ▼
   ┌──────────┐
   │ Employee │
   └─────┬────┘
         │ (3) Adds items with quantities
         ▼
   ┌──────────────┐
   │   System     │
   └──────┬───────┘
          │ (4) Calculates totals
          ▼
   ┌──────────┐
   │ Employee │
   └─────┬────┘
         │ (5) Submits requisition
         ▼
   ┌──────────────┐
   │   System     │
   └──────┬───────┘
          │ (6) Creates requisition in database
          ▼
   ┌──────────┐
   │ Manager  │  (receives notification)
   └──────────┘
```

### Appendix C: References

**Standards and Specifications:**
- IEEE 1016-2009: Software Design Descriptions
- IEEE 830-1998: Software Requirements Specification
- IEEE 829-2008: Software Test Documentation

**Technology Documentation:**
- JavaFX 21 Documentation: https://openjfx.io/
- MySQL 8.0 Reference Manual: https://dev.mysql.com/doc/refman/8.0/en/
- BCrypt Algorithm: https://en.wikipedia.org/wiki/Bcrypt
- Apache Maven Guide: https://maven.apache.org/guides/

**Project Documents:**
- README.md - Setup instructions and troubleshooting
- CLAUDE.md - Development guidelines and best practices
- PROJECT_IDEA_DETAILED.md - Comprehensive requirements
- SPMP (Week 8) - Project management plan

### Appendix D: Team Information

| Name | Role | Contribution |
|------|------|--------------|
| Jawad Ali Alnatah | Team Leader & Backend Developer | Architecture design, DAO implementation |
| Mustafa AbdulKarim AbdRabAlameer | Backend Developer & Database Designer | Database schema, audit logging |
| Abdullah Jaffer Masiri | UI/UX Designer | FXML layouts, CSS styling |
| Ahmed Hussain Alghazwe | Frontend Developer | Controllers, UI event handling |
| Abdullah Abdulaziz Alhamadi | Frontend Developer & GUI Designer | Dashboard design, navigation |
| Mohammad Khalid Alqallaf | Quality Assurance & Documentation | Testing, SDD documentation |

**Advisor:** Saeed Matar Alshahrani
**Instructor:** Dr. Rahma Ahmed

---

**Document Control**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-12-12 | Team 6 | Initial SDD following IEEE 1016 template |

**Approval Signatures**

| Role | Name | Signature | Date |
|------|------|-----------|------|
| Team Leader | Jawad Ali Alnatah | ______________ | ______ |
| Advisor | Saeed Matar Alshahrani | ______________ | ______ |
| Instructor | Dr. Rahma Ahmed | ______________ | ______ |

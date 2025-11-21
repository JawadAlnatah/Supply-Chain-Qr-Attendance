# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Development Commands

**Build Tool:** Apache Maven 3.9.11

**Essential Commands:**
```bash
# Run the JavaFX application  (PRIMARY COMMAND)
mvn javafx:run

# Clean and build with dependencies
mvn clean install

# Compile only
mvn compile

# Run tests
mvn test

# Package as executable JAR
mvn package
```

**Project Details:**
- **Java Version:** 17 (LTS)
- **Main Class:** `com.team.supplychain.Main`
- **Entry Point:** [src/main/java/com/team/supplychain/Main.java](src/main/java/com/team/supplychain/Main.java)

## Architecture Overview

This is a **JavaFX desktop application** using a **layered MVC architecture** with the DAO pattern:

```
PRESENTATION LAYER (View + Controller)
├── FXML Views (src/main/resources/fxml/)
├── Controllers (com.team.supplychain.controllers)
└── CSS Styling (src/main/resources/css/)

BUSINESS LOGIC LAYER (Service) - MOSTLY EMPTY STUBS
├── Services (com.team.supplychain.services)
└── Note: Controllers currently bypass services and call DAOs directly

DATA ACCESS LAYER (DAO Pattern)
├── DAOs (com.team.supplychain.dao)
└── Implements CRUD with PreparedStatements

DOMAIN MODEL LAYER
├── Models (com.team.supplychain.models) - POJOs
└── Enums (com.team.supplychain.enums)

UTILITY LAYER
└── Utils (com.team.supplychain.utils)
```

**Implementation Status:**
- ✅ **Complete:** Login system, Dashboard with role-based access, UserDAO, EmployeeDAO
- ⚠️ **Stubs/Planned:** Inventory, Supplier, Attendance, QR code scanning, Report generation
- 🔧 **Technical Debt:** Service layer exists but is empty; controllers call DAOs directly

## Key Technologies

**Core Stack:**
- Java 17 + JavaFX 21
- Maven for build automation
- TiDB Cloud (MySQL-compatible serverless database)

**Key Libraries:**
- `mysql-connector-j` (8.2.0) - Database connectivity
- `zxing` (3.5.3) - QR code generation/scanning (planned feature)
- `jbcrypt` (0.4) - BCrypt password hashing with 10 salt rounds
- `apache-poi` (5.2.5) - Excel report generation
- `itextpdf` (5.5.13.3) - PDF report generation
- `gson` (2.10.1) - JSON processing

**Testing:**
- JUnit 5 + TestFX + Mockito (configured but tests not yet implemented)

## Database Configuration

**Connection Pattern:**
- Singleton pattern via [DatabaseConnection.java](src/main/java/com/team/supplychain/utils/DatabaseConnection.java)
- Configuration file: [src/main/resources/config.properties](src/main/resources/config.properties)
- **SSL/TLS required** for TiDB Cloud (`sslMode=VERIFY_IDENTITY`)

**Connection Usage Pattern:**
```java
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {

    stmt.setString(1, parameter);
    ResultSet rs = stmt.executeQuery();
    // Process results

} catch (SQLException e) {
    e.printStackTrace();
}
```

**Test Credentials** (from README):
- Admin: `admin` / `password123`
- Manager: `manager1` / `password123`
- Employee: `employee1` / `employee123`
- Supplier: `supplier1` / `supplier123`

**Test Connection:**
Run [TestConnection.java](src/main/java/com/team/supplychain/test/java/com/supplychain/test/TestConnection.java) or call `DatabaseConnection.testConnection()`

## Critical Patterns & Conventions

### 1. DAO Pattern
All database operations go through DAOs with PreparedStatements to prevent SQL injection. DAOs return model objects or primitives.

**Example:** [UserDAO.java](src/main/java/com/team/supplychain/dao/UserDAO.java), [EmployeeDAO.java](src/main/java/com/team/supplychain/dao/EmployeeDAO.java)

### 2. Controllers Bypass Services
Despite having a service layer package, **controllers currently call DAOs directly**. This is intentional for the current development stage. Services are empty stubs for future refactoring.

### 3. Authentication & Security
- **Password hashing:** BCrypt with 10 rounds via [PasswordUtil.java](src/main/java/com/team/supplychain/utils/PasswordUtil.java)
- **Backward compatibility:** Falls back to plaintext comparison if hash doesn't start with `$2` (legacy migration support)
- **Database column:** `password_hash` (not `password`)
- **Known issue:** Some legacy passwords may still be plaintext in database

### 4. Role-Based Access Control (RBAC)
- Enum: `UserRole.ADMIN`, `MANAGER`, `EMPLOYEE`, `SUPPLIER`
- Dashboard buttons enabled/disabled based on `User.getRole()` in [DashboardController.java:164-195](src/main/java/com/team/supplychain/controllers/DashboardController.java#L164-L195)
- Access control configured in `configureAccessBasedOnRole()`

### 5. JavaFX Navigation
**Two patterns:**
- **Scene switching:** For major transitions (e.g., Login → Dashboard)
  ```java
  FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
  Scene scene = new Scene(loader.load());
  stage.setScene(scene);
  ```
- **BorderPane center content:** For in-app navigation within Dashboard
  ```java
  mainContentArea.setCenter(newContent);
  ```

### 6. FXML + Controller Pattern
- FXML defines UI structure declaratively
- Controllers handle events and logic
- `@FXML` annotations inject UI components
- `fx:id` in FXML maps to controller field names

### 7. Resource Management
Always use try-with-resources for JDBC connections to prevent leaks. Connection obtained from `DatabaseConnection.getConnection()`.

## Module Development Pattern

When adding a new module (e.g., Inventory, Supplier), follow this pattern:

1. **Create/Update Model** in [models/](src/main/java/com/team/supplychain/models/)
   - POJO with getters/setters
   - Example: [Employee.java](src/main/java/com/team/supplychain/models/Employee.java)

2. **Create DAO** in [dao/](src/main/java/com/team/supplychain/dao/)
   - CRUD methods with PreparedStatements
   - Use try-with-resources
   - Example: [UserDAO.java](src/main/java/com/team/supplychain/dao/UserDAO.java)

3. **Create Controller** in [controllers/](src/main/java/com/team/supplychain/controllers/)
   - `@FXML` annotations for UI components
   - Call DAO methods directly (bypass service layer for now)
   - Example: [LoginController.java](src/main/java/com/team/supplychain/controllers/LoginController.java)

4. **Create FXML View** in [src/main/resources/fxml/](src/main/resources/fxml/)
   - Define UI structure
   - Set `fx:controller` attribute to your controller class
   - Example: [Login.fxml](src/main/resources/fxml/Login.fxml)

5. **Integrate with Dashboard**
   - Add button in [Dashboard.fxml](src/main/resources/fxml/Dashboard.fxml)
   - Add event handler in [DashboardController.java](src/main/java/com/team/supplychain/controllers/DashboardController.java)
   - Configure role-based access in `configureAccessBasedOnRole()`

## QR Code System (Planned Feature)

**Current Status:** Libraries included but functionality not yet implemented

**Architecture:**
- `employees.qr_code` column stores unique QR code string
- [QRCodeService.java](src/main/java/com/team/supplychain/services/QRCodeService.java) (empty stub) will generate codes using ZXing
- [EmployeeDAO.getEmployeeByQRCode()](src/main/java/com/team/supplychain/dao/EmployeeDAO.java) method ready for lookup
- Attendance tracking via [AttendanceDAO.java](src/main/java/com/team/supplychain/dao/AttendanceDAO.java) (empty stub)
- AttendanceStatus enum ready: `CHECKED_IN`, `CHECKED_OUT`, `ABSENT`, `LATE`

**Expected Flow:**
1. Generate unique QR code per employee (employee_id encoded)
2. Employee scans QR via web interface (smartphone)
3. Backend looks up employee via `getEmployeeByQRCode()`
4. Create attendance record with timestamp and status

## Database Schema

**Key Tables:**
- `users` - user_id, username, password_hash, email, role, first_name, last_name, is_active, created_at, last_login
- `employees` - employee_id, user_id (FK), department, position, phone, qr_code, hire_date
- `inventory_items` - item_id, item_name, category, quantity, unit_price, reorder_level, supplier_id (FK), location
- `suppliers` - supplier_id, supplier_name, contact_person, email, phone, address, rating, is_active
- `attendance` - (schema exists, DAO stub only)
- `purchase_orders` - (DAO stub only)

**Naming Conventions:**
- Tables: `snake_case`
- Columns: `snake_case`
- Foreign keys: `{table}_id` pattern
- Java: PascalCase (classes), camelCase (methods/variables)

## Known Issues & Technical Debt

1. **Service layer bypass:** Controllers call DAOs directly; service layer are empty stubs
2. **Legacy passwords:** Some database passwords may still be plaintext (migration script exists: [DatabasePasswordUpdater.java](src/main/java/com/team/supplychain/utils/DatabasePasswordUpdater.java))
3. **Single connection:** No connection pooling despite `db.maxConnections` config
4. **Error handling:** Uses `printStackTrace()` instead of SLF4J logging
5. **File misplacement:** [PurchaseOrderDAO.java](src/main/java/com/team/supplychain/controllers/PurchaseOrderDAO.java) is in controllers package, should be in dao/

## Project Context

This is a **university project** (CSC 305: Software Engineering) developed by 6 students at Imam Abdulrahman Bin Faisal University over a 16-week timeline. See [README.md](README.md) for comprehensive setup instructions, development status, and team workflow.

**Project Documents:** Available in [documents/](documents/) directory
- Project Idea Form
- Project Proposal Form
- SPMP (Software Project Management Plan)

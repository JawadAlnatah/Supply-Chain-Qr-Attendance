# Supply Chain Management System - Complete Codebase Guide

**For Team Members New to the Codebase**

This guide explains how the entire system works, from the desktop application to the mobile QR scanner, and how everything connects together through the database.

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [How to Run the Application](#2-how-to-run-the-application)
3. [Application Entry Point](#3-application-entry-point)
4. [JavaFX Architecture - How the Desktop App Works](#4-javafx-architecture---how-the-desktop-app-works)
5. [Database Architecture](#5-database-architecture)
6. [Role-Based Access Control](#6-role-based-access-control)
7. [Web Server & QR Attendance System](#7-web-server--qr-attendance-system)
8. [Key Development Patterns](#8-key-development-patterns)
9. [Common Workflows](#9-common-workflows)
10. [Adding New Features](#10-adding-new-features)
11. [Technical Debt & Known Issues](#11-technical-debt--known-issues)
12. [Key Files Reference](#12-key-files-reference)
13. [Glossary](#13-glossary)
14. [FAQ](#14-faq)

---

## 1. Project Overview

### What is This System?

This is a **Supply Chain Management System** with two components:
1. **Desktop Application** (JavaFX) - For admins, managers, and employees to manage inventory, suppliers, employees, and attendance
2. **Mobile Web Application** (HTML5 + JavaScript) - For QR code-based employee attendance tracking

### High-Level Architecture

```
┌─────────────────────────────────────────────┐
│      DESKTOP APPLICATION (JavaFX)            │
│                                              │
│  • Login & Authentication                   │
│  • Role-Based Dashboards                    │
│  • Employee Management                      │
│  • Inventory Management                     │
│  • Supplier Management                      │
│  • Attendance Reporting                     │
└──────────────┬──────────────────────────────┘
               │
               │ Both share same database
               │
               ↓
┌────────────────────────────────────────────┐
│         TiDB CLOUD DATABASE                │
│      (MySQL-Compatible Cloud DB)           │
│                                             │
│  Tables: users, employees,                 │
│          attendance_records,                │
│          inventory_items, suppliers         │
└──────────────┬─────────────────────────────┘
               │
               │ Both access same data
               │
               ↓
┌─────────────────────────────────────────────┐
│   MOBILE WEB APPLICATION (HTML5)            │
│                                              │
│  • QR Code Scanner (Camera)                 │
│  • Employee Check-In/Check-Out              │
│  • Embedded Jetty Web Server                │
│  • REST API: /api/attendance/scan           │
└─────────────────────────────────────────────┘
```

### Technology Stack

| Component | Technology | Purpose |
|-----------|------------|---------|
| **Language** | Java 17 | Core programming language |
| **UI Framework** | JavaFX 21 | Desktop graphical interface |
| **Build Tool** | Maven 3.9.11 | Dependency management & build |
| **Database** | TiDB Cloud | MySQL-compatible cloud database |
| **Connection Pooling** | HikariCP 5.1.0 | Fast database connection reuse |
| **Password Security** | BCrypt (jbcrypt 0.4) | Secure password hashing |
| **QR Codes** | ZXing 3.5.3 | QR code generation/scanning |
| **Web Server** | Jetty 11.0.19 | Embedded HTTP/HTTPS server |
| **JSON Processing** | Gson 2.10.1 | REST API data serialization |
| **Reports** | Apache POI 5.2.5, iText 5.5.13 | Excel & PDF generation |

---

## 2. How to Run the Application

### Prerequisites
- Java 17 installed
- Maven 3.9+ installed
- Internet connection (for TiDB Cloud database)

### Running the Desktop Application

```bash
# Navigate to project root
cd Supply-Chain-Management-System-With-QR-Based-Attendance-Employee-Tracking-

# Run the JavaFX application
mvn javafx:run
```

### Running the Web Server (QR Scanner)

```bash
# Run the embedded web server
mvn exec:java -Dexec.mainClass="com.team.supplychain.api.WebServerLauncher"

# Access URLs:
# Desktop: http://localhost:8080/scanner.html
# Mobile:  https://192.168.x.x:8443/scanner.html (use your local IP)
```

### Test Credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin` | `password123` |
| Manager | `manager1` | `password123` |
| Employee | `employee1` | `employee123` |
| Supplier | `supplier1` | `supplier123` |

---

## 3. Application Entry Point

### Where Does the Application Start?

**File:** [src/main/java/com/team/supplychain/Main.java](src/main/java/com/team/supplychain/Main.java)

This is the **entry point** of the application. When you run `mvn javafx:run`, Maven calls the `Main.main()` method.

```java
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the Login FXML file
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Login.fxml")
            );
            Parent root = loader.load();  // Parse XML and create UI

            // Create a Scene (the container for UI components)
            Scene scene = new Scene(root);

            // Configure and show the Stage (the window)
            primaryStage.setTitle("Supply Chain Management System - Login");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);  // JavaFX launches the application
    }
}
```

### What Happens When the App Starts?

1. **Maven executes** `Main.main()`
2. **JavaFX launches** and calls `start(Stage primaryStage)`
3. **FXMLLoader** reads `/fxml/Login.fxml` and creates the login screen UI
4. **Scene** is created with the loaded UI
5. **Stage** (the window) displays the scene
6. **User sees** the login screen

---

## 4. JavaFX Architecture - How the Desktop App Works

The desktop application uses **JavaFX**, a framework for building graphical user interfaces. JavaFX follows a pattern where **FXML files** define what the UI looks like, **Controllers** define how the UI behaves, and **CSS files** define how the UI is styled.

### 4.1 FXML Files (The Views)

**FXML** is an XML-based language for defining user interfaces. Think of it like HTML for desktop applications.

**Location:** `src/main/resources/fxml/*.fxml`

**Example:** [src/main/resources/fxml/Login.fxml](src/main/resources/fxml/Login.fxml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<AnchorPane xmlns="http://javafx.com/javafx/17"
            xmlns:fx="http://javafx.com/fxml/1"
            fx:controller="com.team.supplychain.controllers.LoginController"
            prefHeight="700.0" prefWidth="1100.0"
            stylesheets="@../css/styles.css">

    <!-- Text field for username -->
    <TextField fx:id="usernameField"
              promptText="Enter your username"
              styleClass="modern-text-field-dark"/>

    <!-- Password field -->
    <PasswordField fx:id="passwordField"
                  promptText="Enter your password"
                  styleClass="modern-password-field-dark"/>

    <!-- Login button -->
    <Button fx:id="loginButton"
           text="Sign In"
           onAction="#handleLogin"
           styleClass="login-button-dark"/>

    <!-- Error message label (hidden by default) -->
    <Label fx:id="errorLabel"
          styleClass="error-label-modern"
          visible="false"/>

</AnchorPane>
```

#### Key FXML Attributes Explained

| Attribute | Purpose | Example |
|-----------|---------|---------|
| `fx:controller` | Links this FXML to a Java controller class | `fx:controller="com.team.supplychain.controllers.LoginController"` |
| `fx:id` | Gives a unique ID to a component so Java can access it | `fx:id="usernameField"` |
| `onAction` | Specifies which method to call when clicked | `onAction="#handleLogin"` |
| `styleClass` | Applies CSS classes for styling | `styleClass="login-button-dark"` |
| `stylesheets` | Links CSS files | `stylesheets="@../css/styles.css"` |

### 4.2 Controllers (The Logic)

**Controllers** are Java classes that handle the logic for FXML views. They respond to button clicks, validate input, and update the UI.

**Location:** `src/main/java/com/team/supplychain/controllers/*.java`

**Example:** [src/main/java/com/team/supplychain/controllers/LoginController.java](src/main/java/com/team/supplychain/controllers/LoginController.java)

```java
package com.team.supplychain.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    // @FXML annotation tells JavaFX to inject these components
    // The field names MUST match the fx:id in the FXML file
    @FXML private TextField usernameField;        // Matches fx:id="usernameField"
    @FXML private PasswordField passwordField;    // Matches fx:id="passwordField"
    @FXML private Button loginButton;             // Matches fx:id="loginButton"
    @FXML private Label errorLabel;               // Matches fx:id="errorLabel"

    private final UserDAO userDAO = new UserDAO();

    /**
     * This method is called AUTOMATICALLY by JavaFX after FXML is loaded
     * and all @FXML fields are injected.
     *
     * Use it for initialization logic (setting up event handlers, etc.)
     */
    @FXML
    private void initialize() {
        // Allow Enter key to trigger login
        usernameField.setOnAction(e -> handleLogin(null));
        passwordField.setOnAction(e -> handleLogin(null));
    }

    /**
     * This method is called when the login button is clicked.
     * It matches the onAction="#handleLogin" in the FXML file.
     *
     * The # symbol means "look for this method in the controller"
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Disable button to prevent double-clicks
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        // Authenticate on background thread (prevents UI freezing)
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                // This runs on a background thread
                return userDAO.authenticate(username, password);
            }
        };

        // Handle authentication result on UI thread
        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();
            if (user != null) {
                // Success! Navigate to dashboard
                openDashboard(user);
            } else {
                // Failed - show error
                showError("Invalid username or password");
                loginButton.setDisable(false);
            }
        });

        // Start the background task
        new Thread(loginTask).start();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void openDashboard(User user) {
        // Navigate to role-specific dashboard (see Section 6)
        // ...
    }
}
```

#### The @FXML Annotation Magic

```
┌─────────────────────┐
│   Login.fxml        │
│                     │
│  <TextField         │
│    fx:id="username  │──┐
│    Field"/>         │  │
└─────────────────────┘  │
                         │ JavaFX automatically connects these
                         │
┌─────────────────────┐  │
│ LoginController     │  │
│                     │  │
│ @FXML               │◄─┘
│ private TextField   │
│   usernameField;    │
└─────────────────────┘
```

When the FXML file is loaded, JavaFX:
1. Finds all components with `fx:id` attributes
2. Looks for matching `@FXML` fields in the controller
3. Automatically injects the component into the field
4. You can now use `usernameField` in your code!

### 4.3 CSS Styling (The Look)

**CSS** (Cascading Style Sheets) defines how components look - colors, fonts, borders, shadows, etc.

**Location:** `src/main/resources/css/*.css`

**Example:** [src/main/resources/css/styles.css](src/main/resources/css/styles.css)

```css
/* Root - applies to all nodes */
.root {
    -fx-font-family: "Segoe UI", "Arial", "Helvetica", sans-serif;
    -fx-background-color: #f8f9fa;
}

/* Login button styling */
.login-button-dark {
    -fx-background-color: linear-gradient(to bottom, #3498db, #2980b9);
    -fx-text-fill: white;
    -fx-font-size: 16px;
    -fx-font-weight: bold;
    -fx-padding: 15px 30px;
    -fx-background-radius: 10px;
    -fx-cursor: hand;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 5);
}

/* Hover effect */
.login-button-dark:hover {
    -fx-background-color: linear-gradient(to bottom, #2980b9, #21618c);
    -fx-scale-x: 1.05;
    -fx-scale-y: 1.05;
}

/* Menu button styling */
.menu-button-modern {
    -fx-background-color: transparent;
    -fx-text-fill: #ecf0f1;
    -fx-alignment: CENTER_LEFT;
    -fx-padding: 15 20;
    -fx-cursor: hand;
}

.menu-button-modern:hover {
    -fx-background-color: rgba(255, 255, 255, 0.1);
}

/* Disabled buttons are semi-transparent */
.menu-button-modern:disabled {
    -fx-opacity: 0.4;
    -fx-cursor: default;
}
```

#### How CSS is Applied

**Method 1: Stylesheet in FXML**
```xml
<AnchorPane stylesheets="@../css/styles.css">
    <!-- All children inherit these styles -->
</AnchorPane>
```

**Method 2: CSS Class in FXML**
```xml
<Button styleClass="login-button-dark" text="Login"/>
```

**Method 3: Inline Styles in FXML**
```xml
<Label style="-fx-font-size: 18px; -fx-text-fill: red;" text="Error"/>
```

**Priority:** Inline Styles > CSS Classes > Stylesheet Defaults

#### Role-Specific Themes

The system has different color themes for each role:

| Role | Theme File | Primary Color |
|------|------------|---------------|
| Admin | `dashboard-admin.css` | Cyan (#22d3ee) |
| Manager | `dashboard-manager.css` | Blue (#3498db) |
| Employee | `dashboard-employee.css` | Green (#10b981) |

### 4.4 Navigation Patterns

The application uses **two different navigation patterns** depending on the context:

#### Pattern 1: Scene Switching (Full Screen Change)

**Used for:** Major transitions like Login → Dashboard

```java
// Load a completely new FXML file
FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
Parent root = loader.load();

// Get the controller to pass data
DashboardController controller = loader.getController();
controller.setCurrentUser(user);

// Replace the entire scene
Stage stage = (Stage) loginButton.getScene().getWindow();
Scene scene = new Scene(root);
stage.setScene(scene);
stage.setTitle("Dashboard");
stage.show();
```

**Visual Effect:** The entire window content is replaced

```
┌────────────────┐       ┌────────────────┐
│  Login Screen  │  -->  │   Dashboard    │
│                │       │                │
│  [Username]    │       │  [Menu] Content│
│  [Password]    │       │                │
│  [Login]       │       │                │
└────────────────┘       └────────────────┘
```

#### Pattern 2: Content Replacement (In-App Navigation)

**Used for:** Dashboard menu clicks (Inventory, Employees, Suppliers, etc.)

The Dashboard uses a **BorderPane** layout with 5 regions:

```
┌────────────────────────────────────────┐
│           TOP (Header Bar)             │
├──────────┬─────────────────────────────┤
│          │                             │
│  LEFT    │      CENTER                 │
│  (Menu)  │   (Dynamic Content)         │
│          │                             │
│          │  <- This area changes       │
│          │     when menu items         │
│          │     are clicked             │
├──────────┴─────────────────────────────┤
│         BOTTOM (Status Bar)            │
└────────────────────────────────────────┘
```

**Code:** [src/main/java/com/team/supplychain/controllers/DashboardController.java](src/main/java/com/team/supplychain/controllers/DashboardController.java)

```java
@FXML private BorderPane mainContainer;

@FXML
private void handleInventory() {
    loadView("/fxml/Inventory.fxml", "Inventory Management");
}

@FXML
private void handleEmployees() {
    loadView("/fxml/Employee.fxml", "Employee Management");
}

private void loadView(String fxmlPath, String title) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent view = loader.load();

        // Replace ONLY the center content, keep menu and header
        mainContainer.setCenter(view);

        // Update window title
        Stage stage = (Stage) mainContainer.getScene().getWindow();
        stage.setTitle(title);

    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

**Visual Effect:** Only the center content changes

```
Before Click:               After Click:
┌────────────────┐         ┌────────────────┐
│    Header      │         │    Header      │
├────┬───────────┤         ├────┬───────────┤
│Menu│ Dashboard │   -->   │Menu│ Inventory │
│    │  Content  │         │    │  Content  │
│    │           │         │    │           │
└────┴───────────┘         └────┴───────────┘
     ↑ Menu stays              ↑ Only center
       the same                  changes
```

---

## 5. Database Architecture

### 5.1 Database Connection

The application connects to **TiDB Cloud**, a MySQL-compatible cloud database.

#### Configuration File

**File:** [src/main/resources/config.properties](src/main/resources/config.properties)

```properties
# TiDB Cloud Connection
db.url=jdbc:mysql://gateway01.eu-central-1.prod.aws.tidbcloud.com:4000/supply_chain_qr?sslMode=VERIFY_IDENTITY&useSSL=true&requireSSL=true
db.username=3uB8fqJmu4peKdN.root
db.password=46dmNGakAQIh5Q0v
db.driver=com.mysql.cj.jdbc.Driver

# Connection Pool Settings
db.maxConnections=10
db.connectionTimeout=30000
```

#### Connection Pooling with HikariCP

**File:** [src/main/java/com/team/supplychain/utils/DatabaseConnection.java](src/main/java/com/team/supplychain/utils/DatabaseConnection.java)

**Why Connection Pooling?**
- Creating a new database connection takes **500-1000ms**
- Connection pooling **reuses existing connections** (takes <1ms)
- HikariCP maintains a **pool of 10 ready-to-use connections**
- When you need a connection, you borrow one from the pool
- When you're done, you return it to the pool (not close it)

```java
public class DatabaseConnection {

    private static HikariDataSource dataSource = null;

    /**
     * Singleton pattern - only one connection pool for the entire application
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initializePool();
        }

        // Borrow a connection from the pool
        return dataSource.getConnection();
    }

    private static void initializePool() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver"));

        // Pool configuration
        config.setMaximumPoolSize(10);    // Max 10 concurrent connections
        config.setMinimumIdle(2);          // Keep 2 connections always ready
        config.setConnectionTimeout(30000); // Wait max 30s for connection

        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        dataSource = new HikariDataSource(config);
        System.out.println("✓ Database connection pool initialized");
    }
}
```

### 5.2 DAO Pattern (Data Access Objects)

**DAO** = Data Access Object. DAOs handle all database operations (Create, Read, Update, Delete).

**Location:** `src/main/java/com/team/supplychain/dao/*.java`

#### Why DAOs?

```
WITHOUT DAOs:
Controllers directly write SQL queries
↓ Problem: SQL code scattered everywhere
↓ Problem: Hard to change database queries
↓ Problem: SQL injection vulnerabilities

WITH DAOs:
Controllers call DAO methods
↓ DAOs handle all SQL
↓ Centralized database logic
↓ PreparedStatements prevent SQL injection
```

#### Example: UserDAO

**File:** [src/main/java/com/team/supplychain/dao/UserDAO.java](src/main/java/com/team/supplychain/dao/UserDAO.java)

```java
public class UserDAO {

    /**
     * Authenticate a user by username and password
     *
     * @param username The username to check
     * @param password The plain-text password to verify
     * @return User object if authentication succeeds, null otherwise
     */
    public User authenticate(String username, String password) {
        // SQL query with ? placeholder for parameter
        String sql = "SELECT * FROM users WHERE username = ? AND is_active = true";

        // Try-with-resources ensures connection is returned to pool
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Set parameter 1 (the first ?) to username value
            // This is safe from SQL injection!
            stmt.setString(1, username);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // User exists - now check password
                String hashedPassword = rs.getString("password_hash");

                // BCrypt verification takes ~100ms (intentionally slow)
                if (PasswordUtil.checkPassword(password, hashedPassword)) {
                    // Password matches - extract user data
                    User user = extractUserFromResultSet(rs);
                    updateLastLogin(user.getUserId());
                    return user;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return null for any failure (don't reveal which part failed)
        return null;
    }

    /**
     * Create a new user with hashed password
     */
    public boolean createUser(User user, String plainPassword) {
        String sql = "INSERT INTO users (username, password_hash, email, role, " +
                    "first_name, last_name, is_active) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql,
                                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());

            // CRITICAL: Hash password before storing!
            stmt.setString(2, PasswordUtil.hashPassword(plainPassword));

            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getRole().name());  // Enum to String
            stmt.setString(5, user.getFirstName());
            stmt.setString(6, user.getLastName());
            stmt.setBoolean(7, user.isActive());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                // Get the auto-generated user_id
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    user.setUserId(rs.getInt(1));
                }
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Convert database row to User object
     */
    private User extractUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUserId(rs.getInt("user_id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setRole(UserRole.valueOf(rs.getString("role")));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setActive(rs.getBoolean("is_active"));

        // Handle nullable timestamps
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }

        return user;
    }
}
```

#### PreparedStatements Prevent SQL Injection

**UNSAFE CODE (SQL Injection Vulnerability):**
```java
// NEVER DO THIS!
String sql = "SELECT * FROM users WHERE username = '" + username + "'";

// If username = "admin' OR '1'='1"
// Query becomes: SELECT * FROM users WHERE username = 'admin' OR '1'='1'
// This returns ALL users! Attacker can bypass authentication!
```

**SAFE CODE (With PreparedStatement):**
```java
String sql = "SELECT * FROM users WHERE username = ?";
PreparedStatement stmt = conn.prepareStatement(sql);
stmt.setString(1, username);  // Parameter sent separately, automatically escaped

// Even if username = "admin' OR '1'='1"
// Database treats it as a literal string to match
// No SQL injection possible!
```

#### Try-With-Resources Pattern

```java
// Old way (BAD - connection might not get closed):
Connection conn = DatabaseConnection.getConnection();
PreparedStatement stmt = conn.prepareStatement(sql);
ResultSet rs = stmt.executeQuery();
// If exception happens here, connection never closes!
conn.close();

// New way (GOOD - connection always returned to pool):
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {

    ResultSet rs = stmt.executeQuery();
    // Process results

} // Connection automatically returned to pool here, even if exception occurs
```

### 5.3 Database Schema

#### Users Table

```sql
CREATE TABLE users (
    user_id         INT PRIMARY KEY AUTO_INCREMENT,
    username        VARCHAR(50) UNIQUE NOT NULL,
    password_hash   VARCHAR(60) NOT NULL,      -- BCrypt hash
    email           VARCHAR(100) UNIQUE,
    role            VARCHAR(20) NOT NULL,       -- ADMIN, MANAGER, EMPLOYEE, SUPPLIER
    first_name      VARCHAR(50),
    last_name       VARCHAR(50),
    is_active       BOOLEAN DEFAULT true,       -- Soft delete flag
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login      TIMESTAMP NULL
);
```

#### Employees Table

```sql
CREATE TABLE employees (
    employee_id     INT PRIMARY KEY AUTO_INCREMENT,
    user_id         INT UNIQUE NOT NULL,         -- FK to users.user_id
    department      VARCHAR(50),
    position        VARCHAR(50),
    phone           VARCHAR(20),
    qr_code         VARCHAR(100) UNIQUE,         -- For QR attendance
    hire_date       DATE,

    FOREIGN KEY (user_id) REFERENCES users(user_id)
);
```

**Relationship:** One User → One Employee (1:1)

#### Attendance Records Table

```sql
CREATE TABLE attendance_records (
    record_id       INT PRIMARY KEY AUTO_INCREMENT,
    employee_id     INT NOT NULL,                -- FK to employees.employee_id
    check_in_time   DATETIME NOT NULL,
    check_out_time  DATETIME NULL,
    date            DATE NOT NULL,
    status          VARCHAR(20) NOT NULL,        -- PRESENT, LATE, ABSENT
    location        VARCHAR(100),
    qr_scan_data    VARCHAR(100),
    notes           TEXT,

    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);
```

**Relationship:** One Employee → Many Attendance Records (1:N)

#### Inventory Items Table

```sql
CREATE TABLE inventory_items (
    item_id         INT PRIMARY KEY AUTO_INCREMENT,
    item_name       VARCHAR(100) NOT NULL,
    category        VARCHAR(50),
    quantity        INT DEFAULT 0,
    unit_price      DECIMAL(10, 2),
    reorder_level   INT,
    supplier_id     INT,                          -- FK to suppliers.supplier_id
    location        VARCHAR(100),

    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
);
```

#### Suppliers Table

```sql
CREATE TABLE suppliers (
    supplier_id     INT PRIMARY KEY AUTO_INCREMENT,
    supplier_name   VARCHAR(100) NOT NULL,
    contact_person  VARCHAR(100),
    email           VARCHAR(100),
    phone           VARCHAR(20),
    address         VARCHAR(200),
    rating          DECIMAL(3, 2),
    is_active       BOOLEAN DEFAULT true
);
```

### 5.4 Security

#### BCrypt Password Hashing

**File:** [src/main/java/com/team/supplychain/utils/PasswordUtil.java](src/main/java/com/team/supplychain/utils/PasswordUtil.java)

**Why BCrypt?**
- **Intentionally slow** (~100ms to hash) - prevents brute force attacks
- **Automatic salt generation** - same password produces different hashes
- **Configurable rounds** - can increase difficulty as computers get faster

```java
public class PasswordUtil {

    /**
     * Hash a plain-text password using BCrypt with 10 rounds
     *
     * Same password produces DIFFERENT hashes each time:
     * "password123" -> "$2a$10$kk0kBfWZQlEDzNYVTXqKrOvvRMWHYvN7..."
     * "password123" -> "$2a$10$abc123XYZdefghi..." (different!)
     *
     * This is GOOD! The salt is embedded in the hash.
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    /**
     * Check if a plain-text password matches a BCrypt hash
     *
     * BCrypt extracts the salt from the hash and recomputes
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        try {
            if (hashedPassword != null && hashedPassword.startsWith("$2")) {
                // BCrypt hash format: $2a$ or $2b$ or $2y$
                return BCrypt.checkpw(plainPassword, hashedPassword);
            } else {
                // LEGACY: Plaintext password (technical debt)
                System.out.println("⚠️ WARNING: Using plaintext password comparison!");
                return plainPassword.equals(hashedPassword);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("❌ Invalid hash format: " + e.getMessage());
            return false;
        }
    }
}
```

**BCrypt Hash Structure:**
```
$2a$10$kk0kBfWZQlEDzNYVTXqKrOvvRMWHYvN7mFx8V8hV9mK8pQ3K0fH9G
 ^   ^  ^
 |   |  └─ Salt (16 bytes) + Hash (24 bytes)
 |   └──── Rounds (10 = 2^10 = 1024 iterations)
 └──────── Version (2a, 2b, or 2y)
```

### 5.5 Model Classes (POJOs)

**POJO** = Plain Old Java Object (simple class with getters/setters)

**Location:** `src/main/java/com/team/supplychain/models/*.java`

#### User Model

**File:** [src/main/java/com/team/supplychain/models/User.java](src/main/java/com/team/supplychain/models/User.java)

```java
public class User {
    private int userId;
    private String username;
    private String passwordHash;  // NEVER store plain-text passwords!
    private String email;
    private UserRole role;         // Enum: ADMIN, MANAGER, EMPLOYEE, SUPPLIER
    private String firstName;
    private String lastName;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    // Getters and setters...

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
```

#### UserRole Enum

**File:** [src/main/java/com/team/supplychain/enums/UserRole.java](src/main/java/com/team/supplychain/enums/UserRole.java)

```java
public enum UserRole {
    ADMIN,      // Full system access
    MANAGER,    // Manage inventory, suppliers, employees
    EMPLOYEE,   // View inventory, track own attendance
    SUPPLIER    // View orders, update shipments
}
```

#### Attendance Model

**File:** [src/main/java/com/team/supplychain/models/Attendance.java](src/main/java/com/team/supplychain/models/Attendance.java)

```java
public class Attendance {
    private int recordId;
    private int employeeId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDate date;
    private AttendanceStatus status;  // PRESENT, LATE, ABSENT
    private String location;
    private String qrScanData;
    private String notes;

    // Helper methods

    public Duration getWorkDuration() {
        if (checkInTime != null && checkOutTime != null) {
            return Duration.between(checkInTime, checkOutTime);
        }
        return Duration.ZERO;
    }

    public String getFormattedHours() {
        Duration duration = getWorkDuration();
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return String.format("%dh %02dm", hours, minutes);
    }

    public boolean isCheckedIn() {
        return checkInTime != null && checkOutTime == null;
    }
}
```

---

## 6. Role-Based Access Control

### 6.1 Authentication Flow

**Complete flow from login button to dashboard:**

```
1. User enters username & password in Login screen
   └─ File: Login.fxml, LoginController.java

2. User clicks "Sign In" button
   └─ Triggers: LoginController.handleLogin()

3. Controller validates input (not empty)

4. Controller calls: userDAO.authenticate(username, password)
   └─ Runs on background thread to prevent UI freezing

5. UserDAO queries database:
   SELECT * FROM users WHERE username = ? AND is_active = true

6. If user found:
   ├─ Get password_hash from database
   ├─ Call: PasswordUtil.checkPassword(password, password_hash)
   ├─ BCrypt verification takes ~100ms
   └─ If match: return User object

7. If no match or user not found:
   └─ Return null

8. LoginController receives result:
   ├─ If User object: openDashboard(user)
   └─ If null: show error "Invalid username or password"

9. openDashboard() checks user role:
   ├─ ADMIN    → load AdminDashboard.fxml
   ├─ MANAGER  → load ManagerDashboard.fxml
   ├─ EMPLOYEE → load EmployeeDashboard.fxml
   └─ SUPPLIER → load Dashboard.fxml

10. Dashboard controller receives User object
    └─ Calls: configureAccessBasedOnRole()
    └─ Enable/disable menu buttons based on role
```

### 6.2 Role-Specific Dashboards

Each role has a customized dashboard:

| Role | FXML File | Controller | Theme Color |
|------|-----------|------------|-------------|
| Admin | `AdminDashboard.fxml` | `AdminDashboardController.java` | Cyan |
| Manager | `ManagerDashboard.fxml` | `ManagerDashboardController.java` | Blue |
| Employee | `EmployeeDashboard.fxml` | `EmployeeDashboardController.java` | Green |

**File:** [src/main/java/com/team/supplychain/controllers/AdminDashboardController.java](src/main/java/com/team/supplychain/controllers/AdminDashboardController.java)

```java
public class AdminDashboardController {

    @FXML private Label userNameLabel;
    @FXML private Button usersButton;       // Only admins see this
    @FXML private Button systemButton;      // Only admins see this
    @FXML private Button auditButton;       // Only admins see this

    private User currentUser;

    /**
     * Called from LoginController to pass authenticated user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;

        // Update UI with user info
        userNameLabel.setText("Welcome, " + user.getFullName());

        // Load dashboard data asynchronously
        loadDashboardData();
    }
}
```

### 6.3 Access Control Logic

**File:** [src/main/java/com/team/supplychain/controllers/DashboardController.java](src/main/java/com/team/supplychain/controllers/DashboardController.java)

```java
private void configureAccessBasedOnRole() {
    if (currentUser == null) return;

    UserRole role = currentUser.getRole();

    switch (role) {
        case ADMIN:
            // Admins see everything
            enableAllButtons();
            break;

        case MANAGER:
            // Managers see everything except settings
            enableAllButtons();
            settingsButton.setDisable(true);
            break;

        case EMPLOYEE:
            // Employees have limited access
            inventoryButton.setDisable(false);      // Can view
            attendanceButton.setDisable(false);     // Can track own

            suppliersButton.setDisable(true);       // Cannot access
            employeesButton.setDisable(true);       // Cannot access
            purchaseOrdersButton.setDisable(true);  // Cannot access
            reportsButton.setDisable(true);         // Cannot access
            settingsButton.setDisable(true);        // Cannot access
            break;

        case SUPPLIER:
            // Suppliers only see their orders
            purchaseOrdersButton.setDisable(false);

            inventoryButton.setDisable(true);
            suppliersButton.setDisable(true);
            employeesButton.setDisable(true);
            attendanceButton.setDisable(true);
            reportsButton.setDisable(true);
            settingsButton.setDisable(true);
            break;
    }
}

private void enableAllButtons() {
    inventoryButton.setDisable(false);
    suppliersButton.setDisable(false);
    employeesButton.setDisable(false);
    attendanceButton.setDisable(false);
    purchaseOrdersButton.setDisable(false);
    reportsButton.setDisable(false);
    settingsButton.setDisable(false);
}
```

**Visual Result:**

```
ADMIN Dashboard:
┌────────────────────────┐
│ • Inventory   ✓        │  All buttons enabled
│ • Suppliers   ✓        │
│ • Employees   ✓        │
│ • Attendance  ✓        │
│ • Orders      ✓        │
│ • Reports     ✓        │
│ • Settings    ✓        │
└────────────────────────┘

EMPLOYEE Dashboard:
┌────────────────────────┐
│ • Inventory   ✓        │  Only 2 buttons enabled
│ • Suppliers   ✗        │  (others grayed out)
│ • Employees   ✗        │
│ • Attendance  ✓        │
│ • Orders      ✗        │
│ • Reports     ✗        │
│ • Settings    ✗        │
└────────────────────────┘
```

---

## 7. Web Server & QR Attendance System

### 7.1 Embedded Jetty Server

The QR attendance system runs as a **separate web server** that can be accessed from mobile devices.

**File:** [src/main/java/com/team/supplychain/api/WebServerLauncher.java](src/main/java/com/team/supplychain/api/WebServerLauncher.java)

**Starting the Web Server:**
```bash
mvn exec:java -Dexec.mainClass="com.team.supplychain.api.WebServerLauncher"
```

**What Happens:**
1. Jetty server starts on ports 8080 (HTTP) and 8443 (HTTPS)
2. Serves HTML/CSS/JS files from `src/main/webapp/`
3. Provides REST API at `/api/attendance/scan`
4. Prints access URLs for desktop and mobile

```java
public class WebServerLauncher {

    private static final int PORT = 8080;        // HTTP port
    private static final int HTTPS_PORT = 8443;  // HTTPS port (for mobile camera)

    public void start() throws Exception {
        Server server = new Server();

        // Configure SSL/HTTPS (required for mobile camera access)
        SslContextFactory.Server sslContextFactory = new SslContextFactory.Server();
        sslContextFactory.setKeyStorePath("keystore.p12");
        sslContextFactory.setKeyStorePassword("changeit");
        sslContextFactory.setSniRequired(false);  // Allow IP-based access

        // HTTP Connector (development/desktop)
        ServerConnector httpConnector = new ServerConnector(server,
            new HttpConnectionFactory(httpConfig));
        httpConnector.setPort(PORT);

        // HTTPS Connector (mobile camera access)
        ServerConnector httpsConnector = new ServerConnector(server,
            new SslConnectionFactory(sslContextFactory, "http/1.1"),
            new HttpConnectionFactory(httpsConfig));
        httpsConnector.setPort(HTTPS_PORT);

        server.addConnector(httpConnector);
        server.addConnector(httpsConnector);

        // Register servlets
        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");

        // API endpoint for QR scanning
        context.addServlet(new ServletHolder(new AttendanceServlet()),
                          "/api/attendance/scan");

        // Static files (HTML, CSS, JS)
        ServletHolder staticServlet = new ServletHolder("static", DefaultServlet.class);
        staticServlet.setInitParameter("resourceBase", "src/main/webapp");
        context.addServlet(staticServlet, "/*");

        server.setHandler(context);
        server.start();

        printStartupInfo();  // Show access URLs
        server.join();       // Keep running
    }
}
```

### 7.2 Why HTTPS for Mobile?

Modern browsers **require HTTPS** for camera access via `getUserMedia()`:

```
HTTP (port 8080):
✓ Works on desktop
✗ Camera blocked on mobile ("Not secure")

HTTPS (port 8443):
✓ Works on desktop
✓ Camera works on mobile (after accepting security warning)
```

**Self-Signed Certificate:**
- For development only
- Users see security warning
- Click "Advanced" → "Proceed" to access
- Production would use proper SSL certificate

**Generating Certificate:**
```bash
keytool -genkeypair -alias jetty -keyalg RSA -keysize 2048 \
        -storetype PKCS12 -keystore keystore.p12 \
        -storepass changeit -keypass changeit \
        -dname "CN=localhost, OU=Development, O=SupplyChain" \
        -validity 365
```

### 7.3 Static File Serving

**Directory:** `src/main/webapp/`

Files:
- `scanner.html` - QR scanner UI (HTML + CSS + JavaScript)
- `index.html` - Simple welcome page

**How it works:**
1. User navigates to `https://192.168.x.x:8443/scanner.html`
2. DefaultServlet looks in `src/main/webapp/scanner.html`
3. Serves the HTML file
4. Browser renders the QR scanner interface

### 7.4 REST API Endpoint

**File:** [src/main/java/com/team/supplychain/api/AttendanceServlet.java](src/main/java/com/team/supplychain/api/AttendanceServlet.java)

**Endpoint:** `POST /api/attendance/scan`

**Request:**
```json
{
  "qrCode": "EMP-00001-ABC123",
  "action": "checkin",
  "location": "Main Entrance"
}
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Check-in successful!",
  "employeeName": "John Doe",
  "employeeId": 1,
  "department": "Finance",
  "position": "Accountant",
  "checkInTime": "8:30 AM",
  "status": "PRESENT",
  "location": "Main Entrance"
}
```

**Response (Error):**
```json
{
  "success": false,
  "message": "Employee not found",
  "employeeName": null
}
```

**Code:**
```java
@Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

    setCorsHeaders(resp);  // Allow cross-origin requests

    // Parse JSON request
    JsonObject request = JsonParser.parseReader(req.getReader()).getAsJsonObject();
    String qrCode = request.get("qrCode").getAsString();
    String action = request.has("action") ?
                    request.get("action").getAsString() : "checkin";
    String location = request.has("location") ?
                     request.get("location").getAsString() : "QR Scanner";

    // Look up employee by QR code
    Employee employee = employeeDAO.getEmployeeByQRCode(qrCode);

    if (employee == null) {
        sendErrorResponse(resp, 404, "Employee not found");
        return;
    }

    // Route to appropriate handler
    JsonObject response;
    if ("checkout".equalsIgnoreCase(action)) {
        response = handleCheckOut(employee, location);
    } else {
        response = handleCheckIn(employee, qrCode, location);
    }

    // Send JSON response
    resp.setContentType("application/json");
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.getWriter().write(gson.toJson(response));
}
```

### 7.5 QR Code Scanning Flow

**File:** `src/main/webapp/scanner.html`

**Frontend Tech:**
- **html5-qrcode** library for camera access
- **Fetch API** for REST calls
- **Vanilla JavaScript** (no frameworks)

**Flow:**

```
1. User opens https://192.168.x.x:8443/scanner.html on phone

2. Browser requests camera permission
   └─ HTTPS required for this to work!

3. Camera preview shows in browser

4. html5-qrcode library continuously scans for QR codes

5. QR code detected: "EMP-00001-ABC123"
   └─ onScanSuccess() callback triggered

6. JavaScript sends POST to /api/attendance/scan:
   {
     "qrCode": "EMP-00001-ABC123",
     "action": "checkin",
     "location": "Main Entrance"
   }

7. AttendanceServlet processes request:
   ├─ Look up employee by QR code
   ├─ Check if already checked in today
   ├─ If not, create attendance record
   └─ Return employee info

8. JavaScript receives response and displays:
   ┌────────────────────────────┐
   │  ✓ Check-In Successful!    │
   │                            │
   │  John Doe                  │
   │  Finance Department        │
   │  Accountant                │
   │                            │
   │  Status: PRESENT           │
   │  Time: 8:30 AM             │
   └────────────────────────────┘

9. Card auto-hides after 5 seconds

10. Scanner resumes, ready for next employee
```

**JavaScript Code:**
```javascript
async function processAttendance(qrCode) {
    const requestData = {
        qrCode: qrCode,
        action: currentAction,  // 'checkin' or 'checkout'
        location: "QR Scanner - Main Entrance"
    };

    try {
        const response = await fetch('/api/attendance/scan', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(requestData)
        });

        const data = await response.json();

        if (data.success) {
            // Show success message
            showEmployeeCard(data);
            playSuccessSound();
        } else {
            // Show error message
            showError(data.message);
        }

    } catch (error) {
        console.error('Error:', error);
        showError('Network error. Please try again.');
    }
}

function showEmployeeCard(data) {
    document.getElementById('employeeName').textContent = data.employeeName;
    document.getElementById('employeeDept').textContent = data.department;
    document.getElementById('employeePos').textContent = data.position;
    document.getElementById('statusText').textContent = data.status;
    document.getElementById('timeText').textContent = data.checkInTime;

    document.getElementById('employeeCard').style.display = 'block';

    // Auto-hide after 5 seconds
    setTimeout(() => {
        document.getElementById('employeeCard').style.display = 'none';
    }, 5000);
}
```

### 7.6 Integration with Desktop App

Both the desktop app and web server **share the same database**:

```
DESKTOP APP                     WEB SERVER
     │                               │
     │  Generate QR codes            │  Scan QR codes
     │  for employees                │  for attendance
     │                               │
     ├───────────────┬───────────────┤
     │               │               │
     ▼               ▼               ▼
┌────────────────────────────────────────┐
│         TiDB Cloud Database            │
│                                        │
│  employees table                       │
│  ├─ employee_id                        │
│  ├─ user_id                            │
│  └─ qr_code ◄─── Both read/write      │
│                                        │
│  attendance_records table              │
│  ├─ record_id                          │
│  ├─ employee_id                        │
│  ├─ check_in_time                      │
│  └─ check_out_time                     │
└────────────────────────────────────────┘
```

**Shared DAO Classes:**
- Both use `EmployeeDAO.getEmployeeByQRCode()`
- Both use `AttendanceDAO.checkIn()` and `checkOut()`
- Both use `DatabaseConnection.getConnection()`

---

## 8. Key Development Patterns

### 8.1 Asynchronous Operations

**Why?** Database operations can take 100-500ms. If you run them on the UI thread, the entire interface freezes.

**Solution:** JavaFX `Task` class runs code on a background thread.

```java
// BAD: Runs on UI thread - freezes interface
@FXML
private void handleLogin(ActionEvent event) {
    User user = userDAO.authenticate(username, password);  // Takes 100ms
    // UI is frozen for 100ms!
    if (user != null) {
        openDashboard(user);
    }
}

// GOOD: Runs on background thread - UI stays responsive
@FXML
private void handleLogin(ActionEvent event) {
    loginButton.setDisable(true);

    Task<User> loginTask = new Task<>() {
        @Override
        protected User call() throws Exception {
            // This runs on background thread
            return userDAO.authenticate(username, password);
        }
    };

    loginTask.setOnSucceeded(e -> {
        // This runs on UI thread after background work completes
        User user = loginTask.getValue();
        if (user != null) {
            openDashboard(user);
        } else {
            showError("Invalid credentials");
            loginButton.setDisable(false);
        }
    });

    // Start the background task
    new Thread(loginTask).start();
}
```

**Flow:**
```
UI Thread:        Background Thread:
    │
    ├─ Create Task
    ├─ Start thread ────────┐
    │                       │
    │  (UI stays            ├─ userDAO.authenticate()
    │   responsive)         │  (takes 100ms)
    │                       │
    │                       └─ Returns User object
    │
    ├─ onSucceeded callback
    ├─ Update UI with result
    │
```

### 8.2 Try-With-Resources

**Java 7+ feature** for automatic resource cleanup.

```java
// OLD WAY (Manual cleanup):
Connection conn = null;
PreparedStatement stmt = null;
try {
    conn = DatabaseConnection.getConnection();
    stmt = conn.prepareStatement(sql);
    ResultSet rs = stmt.executeQuery();
} catch (SQLException e) {
    e.printStackTrace();
} finally {
    if (stmt != null) stmt.close();
    if (conn != null) conn.close();
}

// NEW WAY (Automatic cleanup):
try (Connection conn = DatabaseConnection.getConnection();
     PreparedStatement stmt = conn.prepareStatement(sql)) {

    ResultSet rs = stmt.executeQuery();

} catch (SQLException e) {
    e.printStackTrace();
}
// conn and stmt automatically closed here, even if exception occurs!
```

### 8.3 Dependency Injection via @FXML

JavaFX automatically injects components from FXML into controller fields:

```
FXML File:                    Controller:
┌─────────────────┐          ┌──────────────────┐
│ <TextField      │          │ @FXML            │
│   fx:id="name   │  ────→   │ private TextField│
│   Field"/>      │          │   nameField;     │
└─────────────────┘          └──────────────────┘

No manual wiring needed!
JavaFX does this automatically when loading FXML.
```

---

## 9. Common Workflows

### 9.1 User Login Workflow

**Step-by-Step:**

1. **User opens application**
   - `Main.main()` executes
   - `Main.start()` loads `Login.fxml`
   - Login screen appears

2. **User enters credentials**
   - Types username in `TextField`
   - Types password in `PasswordField`

3. **User clicks "Sign In" or presses Enter**
   - Triggers `LoginController.handleLogin()`

4. **Controller validates input**
   ```java
   if (username.isEmpty() || password.isEmpty()) {
       showError("Please enter both username and password");
       return;
   }
   ```

5. **Controller disables button (prevent double-click)**
   ```java
   loginButton.setDisable(true);
   ```

6. **Controller creates background task**
   ```java
   Task<User> loginTask = new Task<>() {
       @Override
       protected User call() {
           return userDAO.authenticate(username, password);
       }
   };
   ```

7. **UserDAO queries database**
   ```sql
   SELECT * FROM users
   WHERE username = ? AND is_active = true
   ```

8. **UserDAO verifies password**
   ```java
   if (PasswordUtil.checkPassword(password, hashedPassword)) {
       return extractUserFromResultSet(rs);
   }
   ```

9. **Controller receives result**
   ```java
   loginTask.setOnSucceeded(e -> {
       User user = loginTask.getValue();
       if (user != null) {
           openDashboard(user);
       } else {
           showError("Invalid username or password");
       }
   });
   ```

10. **Controller loads role-specific dashboard**
    ```java
    switch (user.getRole()) {
        case ADMIN:
            loader = new FXMLLoader(getClass()
                .getResource("/fxml/AdminDashboard.fxml"));
            break;
        // ...
    }
    ```

11. **Dashboard appears with user info**
    - User sees "Welcome, John Doe"
    - Buttons enabled based on role

### 9.2 Employee Check-In Workflow

**Step-by-Step:**

1. **Admin generates QR code for employee** (Desktop App)
   - Uses `QRCodeService.generateQRCodeImage()`
   - Saves QR string to `employees.qr_code` column
   - Example: `"EMP-00001-ABC123"`

2. **Web server starts**
   ```bash
   mvn exec:java -Dexec.mainClass="com.team.supplychain.api.WebServerLauncher"
   ```

3. **Employee opens scanner on phone**
   - Navigates to `https://192.168.x.x:8443/scanner.html`
   - Accepts security warning (self-signed cert)
   - Grants camera permission

4. **html5-qrcode library initializes**
   ```javascript
   const html5QrCode = new Html5Qrcode("reader");
   html5QrCode.start(
       { facingMode: "environment" },  // Rear camera
       { fps: 10, qrbox: 250 },
       onScanSuccess
   );
   ```

5. **Employee holds QR code to camera**
   - Library detects QR code pattern
   - Extracts string: `"EMP-00001-ABC123"`
   - Calls `onScanSuccess("EMP-00001-ABC123")`

6. **JavaScript sends API request**
   ```javascript
   fetch('/api/attendance/scan', {
       method: 'POST',
       body: JSON.stringify({
           qrCode: "EMP-00001-ABC123",
           action: "checkin",
           location: "Main Entrance"
       })
   })
   ```

7. **AttendanceServlet receives request**
   ```java
   String qrCode = request.get("qrCode").getAsString();
   Employee employee = employeeDAO.getEmployeeByQRCode(qrCode);
   ```

8. **EmployeeDAO queries database**
   ```sql
   SELECT e.*, u.first_name, u.last_name
   FROM employees e
   JOIN users u ON e.user_id = u.user_id
   WHERE e.qr_code = ?
   ```

9. **AttendanceServlet checks if already checked in**
   ```java
   Attendance todayAttendance = attendanceDAO
       .getTodayAttendance(employee.getEmployeeId());

   if (todayAttendance != null && todayAttendance.getCheckInTime() != null) {
       return error("Already checked in");
   }
   ```

10. **AttendanceDAO creates attendance record**
    ```sql
    INSERT INTO attendance_records
    (employee_id, check_in_time, date, status, location, qr_scan_data)
    VALUES (?, NOW(), CURDATE(), ?, ?, ?)
    ```
    - Status = `PRESENT` if before 8:30 AM
    - Status = `LATE` if after 8:30 AM

11. **Servlet sends success response**
    ```json
    {
      "success": true,
      "employeeName": "John Doe",
      "checkInTime": "8:30 AM",
      "status": "PRESENT"
    }
    ```

12. **JavaScript displays confirmation**
    - Shows employee name and department
    - Shows check-in time and status
    - Plays success sound
    - Auto-hides after 5 seconds

13. **Scanner resumes** for next employee

### 9.3 Viewing Attendance in Desktop App

1. **Manager logs in** to desktop app
2. **Clicks "Employees" button** in dashboard
3. **EmployeePageController loads**
   - Queries all employees from database
   - Displays in TableView

4. **Manager clicks employee row**
5. **Controller queries attendance**
   ```java
   List<Attendance> records = attendanceDAO
       .getMonthAttendance(employeeId, LocalDate.now());
   ```

6. **Display attendance records**
   - Shows check-in/check-out times
   - Shows status (PRESENT, LATE, ABSENT)
   - Shows hours worked
   - Can export to Excel/PDF

---

## 10. Adding New Features

### Step-by-Step Guide

Let's say you want to add a new **"Suppliers"** module.

#### Step 1: Create Model Class

**File:** `src/main/java/com/team/supplychain/models/Supplier.java`

```java
package com.team.supplychain.models;

public class Supplier {
    private int supplierId;
    private String supplierName;
    private String contactPerson;
    private String email;
    private String phone;
    private String address;
    private double rating;
    private boolean isActive;

    // Constructors, getters, setters...
}
```

#### Step 2: Create DAO Class

**File:** `src/main/java/com/team/supplychain/dao/SupplierDAO.java`

```java
package com.team.supplychain.dao;

public class SupplierDAO {

    public List<Supplier> getAllSuppliers() {
        List<Supplier> suppliers = new ArrayList<>();
        String sql = "SELECT * FROM suppliers WHERE is_active = true";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                suppliers.add(extractSupplierFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    public boolean createSupplier(Supplier supplier) {
        String sql = "INSERT INTO suppliers (supplier_name, contact_person, " +
                    "email, phone, address, rating) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql,
                                     Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, supplier.getSupplierName());
            stmt.setString(2, supplier.getContactPerson());
            stmt.setString(3, supplier.getEmail());
            stmt.setString(4, supplier.getPhone());
            stmt.setString(5, supplier.getAddress());
            stmt.setDouble(6, supplier.getRating());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    supplier.setSupplierId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Supplier extractSupplierFromResultSet(ResultSet rs)
            throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(rs.getInt("supplier_id"));
        supplier.setSupplierName(rs.getString("supplier_name"));
        supplier.setContactPerson(rs.getString("contact_person"));
        supplier.setEmail(rs.getString("email"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setAddress(rs.getString("address"));
        supplier.setRating(rs.getDouble("rating"));
        supplier.setActive(rs.getBoolean("is_active"));
        return supplier;
    }
}
```

#### Step 3: Create FXML View

**File:** `src/main/resources/fxml/Supplier.fxml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<VBox xmlns="http://javafx.com/javafx/17"
      xmlns:fx="http://javafx.com/fxml/1"
      fx:controller="com.team.supplychain.controllers.SupplierController"
      spacing="20" padding="20">

    <HBox spacing="10" alignment="CENTER_LEFT">
        <Label text="Supplier Management" styleClass="page-title"/>
        <Button fx:id="addButton" text="Add Supplier"
                onAction="#handleAddSupplier"
                styleClass="primary-button"/>
    </HBox>

    <TableView fx:id="supplierTable" VBox.vgrow="ALWAYS">
        <columns>
            <TableColumn fx:id="idColumn" text="ID" prefWidth="50"/>
            <TableColumn fx:id="nameColumn" text="Supplier Name" prefWidth="150"/>
            <TableColumn fx:id="contactColumn" text="Contact Person" prefWidth="150"/>
            <TableColumn fx:id="emailColumn" text="Email" prefWidth="200"/>
            <TableColumn fx:id="phoneColumn" text="Phone" prefWidth="100"/>
            <TableColumn fx:id="ratingColumn" text="Rating" prefWidth="80"/>
        </columns>
    </TableView>

</VBox>
```

#### Step 4: Create Controller

**File:** `src/main/java/com/team/supplychain/controllers/SupplierController.java` 


```java
package com.team.supplychain.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class SupplierController {

    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Integer> idColumn;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> contactColumn;
    @FXML private TableColumn<Supplier, String> emailColumn;
    @FXML private TableColumn<Supplier, String> phoneColumn;
    @FXML private TableColumn<Supplier, Double> ratingColumn;
    @FXML private Button addButton;

    private final SupplierDAO supplierDAO = new SupplierDAO();
    private ObservableList<Supplier> supplierList;

    @FXML
    private void initialize() {
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactPerson"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        // Load data
        loadSuppliers();
    }

    private void loadSuppliers() {
        Task<List<Supplier>> loadTask = new Task<>() {
            @Override
            protected List<Supplier> call() {
                return supplierDAO.getAllSuppliers();
            }
        };

        loadTask.setOnSucceeded(e -> {
            supplierList = FXCollections.observableArrayList(loadTask.getValue());
            supplierTable.setItems(supplierList);
        });

        new Thread(loadTask).start();
    }

    @FXML
    private void handleAddSupplier(ActionEvent event) {
        // Open dialog to add new supplier
        // ...
    }
}
```

#### Step 5: Add to Dashboard Menu

**File:** `src/main/resources/fxml/Dashboard.fxml`

```xml
<VBox styleClass="sidebar-modern">
    <Button fx:id="inventoryButton" text="Inventory Management"
            onAction="#handleInventory"/>

    <!-- Add new button -->
    <Button fx:id="suppliersButton" text="Supplier Management"
            onAction="#handleSuppliers"
            styleClass="menu-button-modern"/>

    <Button fx:id="employeesButton" text="Employee Management"
            onAction="#handleEmployees"/>
</VBox>
```

**File:** `src/main/java/com/team/supplychain/controllers/DashboardController.java`

```java
@FXML private Button suppliersButton;

@FXML
private void handleSuppliers() {
    loadView("/fxml/Supplier.fxml", "Supplier Management");
}
```

#### Step 6: Configure Role-Based Access

**File:** `src/main/java/com/team/supplychain/controllers/DashboardController.java`

```java
private void configureAccessBasedOnRole() {
    switch (currentUser.getRole()) {
        case ADMIN:
        case MANAGER:
            suppliersButton.setDisable(false);  // Allow access
            break;
        case EMPLOYEE:
            suppliersButton.setDisable(true);   // Deny access
            break;
    }
}
```

---

## 11. Technical Debt & Known Issues

### Known Issues

1. **Service Layer Bypassed**
   - **Current:** Controllers call DAOs directly
   - **Should be:** Controllers → Services → DAOs
   - **Impact:** Business logic scattered in controllers
   - **Fix:** Create service layer and refactor controllers

2. **Legacy Plaintext Passwords**
   - **Current:** Some passwords in database are still plaintext
   - **Should be:** All passwords BCrypt hashed
   - **Impact:** Security vulnerability
   - **Fix:** Run `DatabasePasswordUpdater.java` migration script

3. **No SLF4J Logging**
   - **Current:** Uses `e.printStackTrace()` everywhere
   - **Should be:** Use SLF4J logger with log levels
   - **Impact:** Can't control log verbosity, poor production debugging
   - **Fix:** Add SLF4J dependency and replace printStackTrace

4. **Misplaced File**
   - **Current:** `PurchaseOrderDAO.java` is in `controllers` package
   - **Should be:** In `dao` package
   - **Impact:** Confusing package structure
   - **Fix:** Move file to correct package

5. **No Unit Tests**
   - **Current:** JUnit/TestFX/Mockito configured but no tests written
   - **Should be:** Comprehensive test coverage
   - **Impact:** Can't verify code changes don't break functionality
   - **Fix:** Write unit tests for DAOs and controllers

### Performance Considerations

1. **Connection Pooling Works Well**
   - HikariCP provides fast connection reuse
   - 10 connection limit sufficient for current load

2. **Async Operations Prevent UI Freezing**
   - All database calls run on background threads
   - UI remains responsive

3. **PreparedStatement Caching**
   - Configured in `DatabaseConnection.java`
   - Queries are compiled once and reused

---

## 12. Key Files Reference

### Project Structure

```
supply-chain-management/
│
├── src/main/java/com/team/supplychain/
│   ├── Main.java                          # Application entry point
│   │
│   ├── controllers/                       # UI Controllers
│   │   ├── LoginController.java
│   │   ├── DashboardController.java
│   │   ├── AdminDashboardController.java
│   │   ├── ManagerDashboardController.java
│   │   ├── EmployeeDashboardController.java
│   │   └── EmployeePageController.java
│   │
│   ├── dao/                               # Database Access
│   │   ├── UserDAO.java
│   │   ├── EmployeeDAO.java
│   │   ├── AttendanceDAO.java
│   │   ├── InventoryItemDAO.java
│   │   └── SupplierDAO.java
│   │
│   ├── models/                            # Data Models (POJOs)
│   │   ├── User.java
│   │   ├── Employee.java
│   │   ├── Attendance.java
│   │   ├── InventoryItem.java
│   │   └── Supplier.java
│   │
│   ├── enums/                             # Enumerations
│   │   ├── UserRole.java
│   │   └── AttendanceStatus.java
│   │
│   ├── utils/                             # Utility Classes
│   │   ├── DatabaseConnection.java
│   │   ├── PasswordUtil.java
│   │   └── AlertUtil.java
│   │
│   ├── api/                               # Web Server
│   │   ├── WebServerLauncher.java
│   │   └── AttendanceServlet.java
│   │
│   └── services/                          # Service Layer (stubs)
│       └── QRCodeService.java
│
├── src/main/resources/
│   ├── fxml/                              # FXML Views
│   │   ├── Login.fxml
│   │   ├── Dashboard.fxml
│   │   ├── AdminDashboard.fxml
│   │   ├── ManagerDashboard.fxml
│   │   ├── EmployeeDashboard.fxml
│   │   └── Employee.fxml
│   │
│   ├── css/                               # Stylesheets
│   │   ├── styles.css
│   │   ├── dashboard-admin.css
│   │   ├── dashboard-manager.css
│   │   └── dashboard-employee.css
│   │
│   └── config.properties                  # Database Config
│
├── src/main/webapp/                       # Web Application
│   ├── scanner.html                       # QR Scanner UI
│   └── index.html
│
├── keystore.p12                           # SSL Certificate
├── pom.xml                                # Maven Configuration
└── README.md                              # Project Documentation
```

---

## 13. Glossary

| Term | Definition |
|------|------------|
| **BCrypt** | Password hashing algorithm that is intentionally slow to prevent brute force attacks |
| **BorderPane** | JavaFX layout with 5 regions: top, bottom, left, right, center |
| **DAO** | Data Access Object - handles all database operations for a specific entity |
| **FXML** | XML-based language for defining JavaFX user interfaces |
| **HikariCP** | Fast connection pool library for database connections |
| **JavaFX** | Framework for building desktop GUI applications in Java |
| **Jetty** | Lightweight embedded web server |
| **POJO** | Plain Old Java Object - simple class with getters/setters |
| **PreparedStatement** | SQL query with parameters, prevents SQL injection |
| **REST API** | Web service that uses HTTP methods (GET, POST, PUT, DELETE) |
| **SNI** | Server Name Indication - TLS extension for hosting multiple SSL certificates |
| **SSL/TLS** | Encryption protocols for secure HTTPS connections |
| **Task** | JavaFX class for running operations on background threads |
| **Try-With-Resources** | Java feature for automatic resource cleanup |

---

## 14. FAQ

### Q: How do I add a new button to a screen?

1. Open the FXML file (e.g., `Dashboard.fxml`)
2. Add button with `fx:id` and `onAction`:
   ```xml
   <Button fx:id="myButton" text="My Feature" onAction="#handleMyFeature"/>
   ```
3. Open the controller (e.g., `DashboardController.java`)
4. Add `@FXML` field:
   ```java
   @FXML private Button myButton;
   ```
5. Add event handler method:
   ```java
   @FXML
   private void handleMyFeature() {
       // Your code here
   }
   ```

### Q: How do I query the database?

1. Open the appropriate DAO (e.g., `UserDAO.java`)
2. Create a new method:
   ```java
   public User getUserById(int userId) {
       String sql = "SELECT * FROM users WHERE user_id = ?";
       try (Connection conn = DatabaseConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

           stmt.setInt(1, userId);
           ResultSet rs = stmt.executeQuery();

           if (rs.next()) {
               return extractUserFromResultSet(rs);
           }
       } catch (SQLException e) {
           e.printStackTrace();
       }
       return null;
   }
   ```
3. Call from controller:
   ```java
   User user = userDAO.getUserById(123);
   ```

### Q: Why is the mobile camera not working?

**Checklist:**
1. ✓ Using HTTPS (not HTTP): `https://192.168.x.x:8443/scanner.html`
2. ✓ Accepted security warning for self-signed certificate
3. ✓ Granted camera permission in browser
4. ✓ Phone and PC on same WiFi network
5. ✓ Web server is running (`mvn exec:java ...`)

**If still not working:**
- Try restarting the web server
- Clear browser cache
- Try a different browser (Chrome recommended)
- Check firewall isn't blocking port 8443

### Q: How do I regenerate the SSL certificate?

```bash
# Delete old certificate
rm keystore.p12

# Generate new certificate
keytool -genkeypair -alias jetty -keyalg RSA -keysize 2048 \
        -storetype PKCS12 -keystore keystore.p12 \
        -storepass changeit -keypass changeit \
        -dname "CN=localhost, OU=Development, O=SupplyChain" \
        -validity 365

# Restart web server
mvn exec:java -Dexec.mainClass="com.team.supplychain.api.WebServerLauncher"
```

### Q: How do I add a new role?

1. Add to `UserRole` enum:
   ```java
   public enum UserRole {
       ADMIN, MANAGER, EMPLOYEE, SUPPLIER, WAREHOUSE_STAFF  // New role
   }
   ```
2. Create role-specific dashboard FXML
3. Update `LoginController.openDashboard()` switch statement
4. Update `DashboardController.configureAccessBasedOnRole()`

### Q: How do I debug authentication issues?

1. Check database connection:
   ```java
   DatabaseConnection.testConnection();
   ```
2. Verify user exists in database:
   ```sql
   SELECT * FROM users WHERE username = 'admin';
   ```
3. Check `is_active` flag is `true`
4. Verify password hash:
   ```java
   String hash = PasswordUtil.hashPassword("password123");
   System.out.println(hash);  // Should start with $2a$
   ```
5. Add debug logging in `UserDAO.authenticate()`:
   ```java
   System.out.println("Attempting login for: " + username);
   System.out.println("User found: " + (rs.next()));
   System.out.println("Password match: " + PasswordUtil.checkPassword(...));
   ```

### Q: How do I change the database connection?

Edit `src/main/resources/config.properties`:
```properties
db.url=jdbc:mysql://your-server:3306/your_database
db.username=your_username
db.password=your_password
```

Restart the application for changes to take effect.

---

## Summary

This codebase implements a **dual-component system**:

1. **Desktop Application (JavaFX)**
   - FXML defines UI structure
   - Controllers handle logic
   - CSS provides styling
   - DAOs abstract database operations
   - Role-based access control

2. **Mobile Web Application (HTML5 + Jetty)**
   - QR scanner with camera access
   - REST API for attendance tracking
   - Shares database with desktop app

**Key Patterns:**
- MVC architecture (FXML + Controllers + Models)
- DAO pattern for database access
- PreparedStatements prevent SQL injection
- BCrypt for password security
- Connection pooling for performance
- Asynchronous operations for responsiveness

**Everything connects through:**
- Shared TiDB Cloud database
- Consistent DAO layer
- Common model classes

For questions or issues, refer to [CLAUDE.md](CLAUDE.md) for development guidelines or [README.md](README.md) for setup instructions.

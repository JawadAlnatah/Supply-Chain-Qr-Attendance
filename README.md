# Supply Chain Management System with Employee Attendance Tracking

> A comprehensive JavaFX enterprise application featuring role-based access control, real-time inventory management, and automated attendance tracking

**Developer:** Jawad Ali Alnatah
**Technologies:** Java 17, JavaFX, Maven, MySQL/TiDB Cloud, BCrypt
**Architecture:** MVC Pattern with DAO Layer

---

## Project Overview

An enterprise-grade desktop application that streamlines supply chain operations and employee management. Built from the ground up using JavaFX and MySQL, this system demonstrates full-stack development capabilities with secure authentication, multi-role dashboards, real-time data visualization, and comprehensive audit logging.

**What it does:**
- Manages inventory across multiple categories with low-stock alerts
- Tracks employee attendance with automated status calculations
- Handles purchase orders and requisition workflows
- Provides role-based dashboards (Admin, Manager, Employee)
- Maintains complete audit trails for security compliance
- Generates exportable reports (CSV format)

**Why I built it:**
To demonstrate enterprise software development skills including secure authentication, database design, async UI programming, and implementing real-world business logic for supply chain and HR management.

---

## Key Features

### Security & Authentication
- **BCrypt password hashing** (10 salt rounds) with asynchronous verification
- **Role-based access control** (Admin, Manager, Employee, Supplier)
- **Comprehensive audit logging** for all system actions
- **Account management** with disable/enable functionality

### Multi-Role Dashboard System
- **Admin Dashboard**: System health monitoring, user management, security alerts
- **Manager Dashboard**: Inventory overview, purchase orders, employee tracking
- **Employee Dashboard**: Live attendance status, weekly calendar, personal requisitions

### Inventory Management
- Real-time stock tracking with status indicators (In Stock / Low Stock / Out of Stock)
- Category-based organization and filtering
- Supplier integration with purchase order linking
- **CSV export functionality** for reporting
- Dynamic bar charts for inventory distribution visualization

### Employee Attendance System
- Automated check-in/check-out with timestamp recording
- **Automatic LATE detection** (after 8:30 AM threshold)
- Weekly and monthly attendance statistics
- Visual calendar view with attendance indicators
- Date-range queries for historical data
- Infrastructure ready for QR code integration

### Requisition & Purchase Orders
- Employee requisition creation and tracking
- Status workflow (Pending → Approved → Completed)
- Manager approval system
- Purchase order management with supplier linking
- Real-time status updates and notifications

### Audit & Compliance
- **Immutable audit trail** for security compliance
- Track all critical actions (LOGIN, CREATE, UPDATE, DELETE)
- Advanced filtering (by action type, module, result, date range)
- Export to CSV for compliance reporting
- Log archival capability

---

## Technical Highlights

### Software Architecture
- **MVC Pattern**: Clean separation between views (FXML), controllers (business logic), and models (POJOs)
- **DAO Pattern**: Centralized database access with PreparedStatements to prevent SQL injection
- **Async Programming**: JavaFX Task pattern for non-blocking database operations
- **Observer Pattern**: Observable collections for reactive UI updates

### Key Technical Implementations

**1. Secure Authentication Pipeline**
- BCrypt hashing with configurable salt rounds
- Asynchronous authentication (prevents UI freezing during 100-500ms hash verification)
- Failed login tracking for security monitoring
- Generic error messages to prevent username enumeration

**2. Performance Optimization**
- Background thread execution for heavy database operations
- 30-second auto-refresh intervals on dashboards
- Pagination support for large datasets (100 records per page)
- Lazy loading with search/filter persistence

**3. Database Design**
- TiDB Cloud (MySQL 8.0 compatible) with SSL/TLS encryption
- Proper foreign key relationships and indexes
- Singleton connection pattern with connection pooling
- Transaction support for data consistency

**4. UI/UX Excellence**
- Custom cell factories for status badges (color-coded: green/yellow/red)
- Real-time data visualization with BarChart components
- Responsive layouts using BorderPane and VBox/HBox
- Async data loading with progress indicators

**5. Business Logic Implementation**
- Automatic LATE status calculation based on check-in time threshold
- Low stock detection (quantity ≤ reorder level)
- Work duration calculation with formatted display
- Requisition approval workflow with status transitions

### Code Quality
- Extensive Javadoc documentation for all public APIs
- Consistent naming conventions (camelCase, PascalCase)
- Try-with-resources for proper JDBC resource management
- Comprehensive error handling with user-friendly dialogs

---

## Technology Stack

| Category | Technologies |
|----------|-------------|
| **Language** | Java 17 (LTS) |
| **UI Framework** | JavaFX 21.0.1 with FXML |
| **Build Tool** | Apache Maven 3.9.11 |
| **Database** | TiDB Cloud (MySQL 8.0 compatible) |
| **Security** | BCrypt (jBCrypt 0.4) |
| **Libraries** | ZXing (QR codes), Apache POI (Excel), iText (PDF), Gson (JSON) |
| **JDBC** | MySQL Connector/J 8.2.0 |
| **Architecture** | MVC + DAO Pattern |

**Development Environment:**
- IDE: IntelliJ IDEA / Visual Studio Code
- Version Control: Git & GitHub
- Database Client: TiDB Cloud Console

---

## System Architecture

### Layered Architecture Overview

```
┌─────────────────────────────────────────┐
│     PRESENTATION LAYER (JavaFX)         │
│  ┌──────────┐  ┌──────────┐  ┌───────┐  │
│  │ FXML     │  │ CSS       │ │ Assets│  │
│  │ Views    │  │ Styles    │ │ Images│  │
│  └──────────┘  └──────────┘  └───────┘  │
└─────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────┐
│     CONTROLLER LAYER (Business Logic)   │
│  - AdminDashboardController             │
│  - ManagerInventoryController           │
│  - EmployeeAttendanceViewController     │
│  - LoginController                      │
└─────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────┐
│     DATA ACCESS LAYER (DAO Pattern)     │
│  - UserDAO         - InventoryDAO       │
│  - AttendanceDAO   - RequisitionDAO     │
│  - AuditLogDAO     - EmployeeDAO        │
└─────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────┐
│     DOMAIN MODEL LAYER (POJOs)          │
│  - User            - InventoryItem      │
│  - Employee        - Requisition        │
│  - AuditLog        - AttendanceRecord   │
└─────────────────────────────────────────┘
                    ↕
┌─────────────────────────────────────────┐
│   DATABASE LAYER (TiDB Cloud/MySQL)     │
│  - users           - inventory_items    │
│  - employees       - attendance_records │
│  - audit_logs      - requisitions       │
└─────────────────────────────────────────┘
```

**Key Design Patterns:**
- **MVC (Model-View-Controller)**: Separation of concerns between UI and logic
- **DAO (Data Access Object)**: Centralized database operations
- **Singleton**: Single database connection instance
- **Observer**: JavaFX Observable collections for reactive UI
- **Factory**: Cell factories for custom table rendering

---

## Quick Start

### Prerequisites
- **Java 17** or higher ([Download here](https://adoptium.net/temurin/releases/))
- **Apache Maven 3.9+** ([Installation guide](https://maven.apache.org/install.html))
- **Git** (for cloning the repository)
- Internet connection (for database access)

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/JawadAlnatah/Integrated-Supply-Chain-Management-System-with-QR-Based-Employee-Attendance-Tracking-.git
cd Supply-Chain-Management-System-With-QR-Based-Attendance-Employee-Tracking-

# 2. Build the project (downloads dependencies)
mvn clean install

# 3. Run the application
mvn javafx:run
```

### Database Configuration

**IMPORTANT:** This project uses TiDB Cloud (MySQL-compatible database). You need to configure your own database credentials.

**Step 1: Copy the example configuration file**
```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

**Step 2: Edit `config.properties` with your database credentials**

Open `src/main/resources/config.properties` and replace the placeholder values:
```properties
db.url=jdbc:mysql://YOUR_TIDB_HOST:4000/supply_chain_qr?sslMode=VERIFY_IDENTITY&useSSL=true
db.username=YOUR_USERNAME
db.password=YOUR_PASSWORD
```

**Step 3: Get TiDB Cloud Credentials** (Free Tier Available)
1. Sign up at [TiDB Cloud](https://tidbcloud.com/)
2. Create a new cluster (free tier available)
3. Use the database setup scripts in `/db/` directory to create tables
4. Copy connection details to `config.properties`

**Note:** The `config.properties` file is gitignored to protect your credentials. Never commit this file to GitHub.

For detailed setup instructions, see [SETUP_INSTRUCTIONS.md](SETUP_INSTRUCTIONS.md).

### Login Credentials

Test the application with these pre-configured accounts:

| Username | Password | Role | Access Level |
|----------|----------|------|--------------|
| `admin` | `password123` | Administrator | Full system access |
| `manager1` | `password123` | Manager | Inventory, employees, orders |
| `employee1` | `employee123` | Employee | Personal dashboard, requisitions |

### Database Configuration

The application connects to a cloud-hosted TiDB database (MySQL compatible). Configuration is in:
```
src/main/resources/config.properties
```

**Note:** Database credentials are pre-configured for demo purposes. For production use, implement environment-based configuration.

---

## Key Learnings & Technical Decisions

### Problem-Solving Highlights

**1. Async UI Performance**
- **Challenge**: BCrypt password verification takes 100-500ms, freezing UI
- **Solution**: Implemented JavaFX Task pattern for background authentication
- **Result**: Smooth, responsive login experience

**2. Real-Time Dashboard Updates**
- **Challenge**: Dashboard data could become stale without page refresh
- **Solution**: 30-second auto-refresh with cancellation on logout
- **Result**: Always up-to-date metrics without manual refresh

**3. CSV Export with Special Characters**
- **Challenge**: Item names and descriptions containing commas broke CSV format
- **Solution**: Implemented proper CSV escaping (wrap in quotes, escape quotes)
- **Result**: Reliable exports that open correctly in Excel

**4. Attendance Status Automation**
- **Challenge**: Manual status entry prone to human error
- **Solution**: Automatic LATE detection based on check-in timestamp vs 8:30 AM threshold
- **Result**: Consistent, accurate attendance tracking

### Why These Technologies?

- **JavaFX**: Rich desktop UI framework with native feel, perfect for enterprise applications
- **TiDB Cloud**: MySQL-compatible with serverless scaling, SSL/TLS security
- **BCrypt**: Industry-standard password hashing resistant to rainbow table attacks
- **Maven**: Simplified dependency management and consistent builds
- **DAO Pattern**: Centralized database logic, easier to maintain and test

---

## Project Statistics

- **Total Lines of Code**: ~8,500+ Java lines
- **Controllers**: 15+ FXML controllers
- **DAOs**: 7 data access objects
- **Models**: 10+ domain models
- **UI Views**: 20+ FXML layouts
- **Database Tables**: 12+ tables
- **Development Time**: 16 weeks (part-time development)
- **Dependencies**: 15 external libraries
- **Design Patterns**: MVC, DAO, Singleton, Observer, Factory

---

## Common Maven Commands

```bash
# Clean build directory
mvn clean

# Compile source code
mvn compile

# Run tests
mvn test

# Package as JAR
mvn package

# Install to local repository
mvn install

# Run the application
mvn javafx:run

# Clean, compile, and run
mvn clean compile javafx:run
```

---

## Troubleshooting

### Issue: "mvn: command not found"

**Solution:**
```bash
# Install Maven (Windows with Chocolatey)
choco install maven -y

# Verify installation
mvn -version
```

### Issue: Database Connection Failed

**Solution:**
1. Check internet connection
2. Verify credentials in `config.properties`
3. Ensure TiDB Cloud database is accessible

### Issue: JavaFX Runtime Components Missing

**Solution:**
```bash
# Always use Maven to run (not IDE run button)
mvn javafx:run
```

---

## Repository & License

**GitHub Repository**: [View Source Code](https://github.com/JawadAlnatah/Integrated-Supply-Chain-Management-System-with-QR-Based-Employee-Attendance-Tracking-)

**License**: This project was developed as part of academic coursework at Imam Abdulrahman Bin Faisal University. Free to use for educational and portfolio purposes.

**Developer**: Jawad Ali Alnatah
**Contact**: Jawad.Alnatah@gmail.com


---

## Academic Context

Developed as part of CSC 305: Software Engineering course demonstrating:
- Software Development Life Cycle (SDLC)
- Requirements analysis and system design
- Agile development practices
- Version control with Git
- Documentation (SPMP, SRS, SDD)
- Testing and quality assurance

**Advisor**: Saeed Matar Alshahrani
**Institution**: Imam Abdulrahman Bin Faisal University
**College**: Computer Science and Information Technology

---

*Last Updated: January 2025*

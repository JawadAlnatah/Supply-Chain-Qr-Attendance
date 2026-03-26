# Supply Chain Management System with QR-Based Employee Attendance Tracking

> A comprehensive JavaFX enterprise application featuring role-based access control, real-time inventory management, and QR-based attendance tracking

![Java](https://img.shields.io/badge/Java-17_LTS-007396?logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21.0.1-informational)
![Maven](https://img.shields.io/badge/Build-Maven_3.9.11-C71A36?logo=apachemaven)
![Database](https://img.shields.io/badge/Database-TiDB_Cloud_(MySQL)-4A4A55?logo=mysql)
![License](https://img.shields.io/badge/License-Academic-blue)

---

## Project Overview

An enterprise-grade desktop application that streamlines supply chain operations and employee management for mid-size organizations. Built from the ground up with JavaFX and a cloud MySQL database (TiDB), it covers secure authentication, multi-role dashboards, QR-based attendance tracking, requisition workflows, and comprehensive audit logging.

**What it does:**
- Manages inventory across multiple categories with low-stock alerts
- Tracks employee attendance via QR code scanning with automatic LATE/PRESENT detection
- Handles purchase requisition workflows with multi-step manager approval
- Provides role-based dashboards (Admin, Manager, Employee, Supplier)
- Maintains immutable audit trails for security and compliance
- Supports exportable reports (CSV, Excel, PDF)

---

## Team & My Role

This was a 6-person team project for CSC 305 (Software Engineering) at Imam Abdulrahman Bin Faisal University.

| Name | ID | Role |
|------|----|------|
| **Jawad Ali Alnatah** | 2240002923 | **Team Leader** |
| Mustafa AbdulKarim | 2240002959 | Backend Developer & Database Designer |
| Abdullah Jaffer Masiri | 2240004545 | UI/UX Designer |
| Ahmed Hussain Alghazwe | 2240002359 | Frontend Developer |
| Abdullah Abdulaziz Alhamadi | 2240003012 | Frontend Developer & GUI Designer |
| Mohammad Khalid Alqallaf | 2240005145 | Quality Assurance & Documentation |

### My Contributions as Team Leader

As team leader, I was responsible for laying the **entire technical foundation** that the rest of the team built on:

- **Architecture Design** — Designed the full 3-tier layered architecture (Presentation → Controller → DAO → Database) and established the MVC + DAO patterns used throughout the project
- **Backend Foundation** — Built the core backend infrastructure: database connection singleton, authentication system with BCrypt, DAO base pattern, session management, and the User/Employee/Audit models
- **Database Design** — Designed the full relational schema (8+ tables), wrote the DDL scripts, set up TiDB Cloud with SSL/TLS, and established all foreign key relationships
- **Frontend Foundation** — Created the main navigation shell (Dashboard), login screen, and the BorderPane-based scene-switching pattern that all other views follow
- **Security Implementation** — Implemented BCrypt password hashing, Role-Based Access Control (RBAC), SQL injection prevention via PreparedStatements, and the full audit logging system
- **Development Standards** — Set up Maven, established coding conventions, created the Git branching strategy, and wrote the developer guide so teammates could onboard quickly
- **Team Coordination** — Led weekly meetings, resolved technical blockers for teammates, and integrated everyone's work into the main branch

---

## Screenshots

### Login Screen
![Login Screen](images/Login%20Screen.png)

### Admin Dashboard
![Admin Dashboard](images/Admin%20Dashboard.png)

### Manager Dashboard
![Manager Dashboard](images/Manager%20Dashboard.png)

### Employee Attendance Dashboard
![Employee Attendance Dashboard](images/Employee%20Attendance%20Dashboard.png)

### QR Code Attendance Tracking
![QR Code Attendance Tracking](images/QR%20Code%20Attendance%20Tracking.png)

### Purchase Requisition — Employee View
![Requisition Employee POV](images/Requisition%20Employee%20pov.png)

### Purchase Requisition — Manager View
![Requisition Manager POV](images/Requisition%20manager%20pov.png)

---

## Key Features

### Security & Authentication
- **BCrypt password hashing** (10 salt rounds) with asynchronous verification
- **Role-based access control** (Admin, Manager, Employee, Supplier)
- **Comprehensive audit logging** for all system actions
- **Account management** with disable/enable functionality

### Multi-Role Dashboard System
- **Admin Dashboard**: System health monitoring, user management, security alerts, audit log viewer
- **Manager Dashboard**: Inventory overview, purchase order tracking, employee attendance
- **Employee Dashboard**: Live attendance status, weekly calendar, personal requisitions

### Inventory Management
- Real-time stock tracking with status indicators (In Stock / Low Stock / Out of Stock)
- Category-based organization and filtering
- Supplier integration with purchase order linking
- Dynamic bar charts for inventory distribution
- CSV export for reporting

### QR-Based Employee Attendance
- Unique QR code per employee encoded with their ID
- Web-based scanner interface accessible from any mobile device
- **Automatic LATE detection** (configurable threshold, default 8:30 AM)
- Work duration calculation (check-in to check-out)
- Weekly and monthly attendance statistics with calendar view

### Purchase Requisition Workflow
- Employee creates requisition with item selection and justification
- Status workflow: `PENDING → IN_REVIEW → APPROVED / REJECTED`
- Manager approval with notes and priority assignment
- Real-time status updates, full audit trail

### Audit & Compliance
- **Immutable audit trail** — every CREATE, UPDATE, DELETE, LOGIN, and SECURITY_INCIDENT logged
- Advanced filtering (action type, module, user, date range, result)
- Export to CSV for compliance reporting

---

## Architecture


![Layered Architecture](images/Layered%20arct.png)

**Key Design Patterns:**
- **MVC (Model-View-Controller):** Separation of concerns between UI and logic
- **DAO (Data Access Object):** Centralized database operations
- **Singleton:** Single database connection instance
- **Observer:** JavaFX Observable collections for reactive UI
- **Factory:** Cell factories for custom table rendering

---

## Technology Stack

| Category | Technology |
|----------|-----------|
| Language | Java 17 (LTS) |
| UI Framework | JavaFX 21.0.1 with FXML |
| Build Tool | Apache Maven 3.9.11 |
| Database | TiDB Cloud (MySQL 8.0 compatible) |
| Security | BCrypt via jBCrypt 0.4 |
| QR Codes | ZXing 3.5.3 |
| Reporting | Apache POI (Excel), iText (PDF) |
| Web Server | Jetty 11 (QR scanner endpoint) |
| Testing | JUnit 5, TestFX, Mockito |

---

## Quick Start

### Prerequisites
- **Java 17+** ([Adoptium Temurin](https://adoptium.net/temurin/releases/))
- **Apache Maven 3.9+** ([Installation guide](https://maven.apache.org/install.html))
- Internet connection (for cloud database access)

### Installation

```bash
# 1. Clone the repository
git clone https://github.com/JawadAlnatah/Supply-Chain-Qr-Attendance
cd Supply-Chain-Qr-Attendance

# 2. Build the project (downloads all dependencies)
mvn clean install

# 3. Run the application
mvn javafx:run
```

### Database Configuration

This project uses TiDB Cloud (MySQL-compatible). You need your own database credentials.

**Step 1:** Copy the example config
```bash
cp src/main/resources/config.properties.example src/main/resources/config.properties
```

**Step 2:** Edit `config.properties`
```properties
db.url=jdbc:mysql://YOUR_TIDB_HOST:4000/supply_chain_qr?sslMode=VERIFY_IDENTITY&useSSL=true
db.username=YOUR_USERNAME
db.password=YOUR_PASSWORD
```

**Step 3:** Get free TiDB Cloud credentials at [tidbcloud.com](https://tidbcloud.com/), create a cluster, run the schema scripts in `/db/`, then connect.

> `config.properties` is gitignored — never commit your credentials.

### Test Credentials

| Username | Password | Role |
|----------|----------|------|
| `admin` | `password123` | Administrator |
| `manager1` | `password123` | Manager |
| `employee1` | `employee123` | Employee |
| `supplier1` | `supplier123` | Supplier |

---

## Project Statistics

| Metric | Value |
|--------|-------|
| Java lines of code | ~12,000 |
| Controllers | 24 |
| DAOs | 8 |
| Domain models | 8 |
| FXML views | 15+ |
| Database tables | 8+ |
| Test cases | 61 (96.7% pass rate) |
| Development duration | 16 weeks |
| Team size | 6 developers |
| IEEE documentation | SDD (47+ pages), STS, SPMP |

---

## Common Maven Commands

```bash
mvn javafx:run          # Run the application (primary command)
mvn clean install       # Clean build with all dependencies
mvn compile             # Compile only
mvn test                # Run tests
mvn package             # Package as executable JAR
mvn clean compile javafx:run  # Full clean run
```

---

## Troubleshooting

**`mvn: command not found`**
```bash
choco install maven -y   # Windows (Chocolatey)
mvn -version             # Verify
```

**Database connection failed**
1. Check your internet connection
2. Verify credentials in `config.properties`
3. Ensure your TiDB Cloud cluster is active

**JavaFX runtime components missing**
```bash
# Always run via Maven, not the IDE run button
mvn javafx:run
```

---

## Academic Context

Developed for **CSC 305: Software Engineering** at Imam Abdulrahman Bin Faisal University (Fall 2024), following a 16-week Agile development cycle with IEEE-standard documentation.

**Deliverables:** IEEE 1016 Software Design Description, IEEE 829 Software Test Specification, SPMP with Gantt chart

- **Instructor:** Dr. Rahma Ahmed
- **Advisor:** Saeed Matar Alshahrani
- **Institution:** College of Computer Science and Information Technology, IAU

---

## Contact

- **Jawad Ali Alnatah** (Team Leader & Architect)
- Email: Jawad.Alnatah@gmail.com
- GitHub: [JawadAlnatah](https://github.com/JawadAlnatah)

---


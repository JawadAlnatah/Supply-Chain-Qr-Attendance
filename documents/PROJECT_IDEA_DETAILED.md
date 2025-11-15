# Fresh Dairy Co. - Supply Chain Management System
## Comprehensive Project Idea Document

**Version:** 1.1.0
**Last Updated:** November 15, 2025
**Course:** CSC 305 - Software Engineering
**Team:** Supply Chain Management Team
**Methodology:** Agile (this document will evolve as the project progresses)

---

## 1. Executive Summary

**Fresh Dairy Co.** is a dairy processing and distribution company that transforms raw milk from local farms into finished dairy products (milk, cheese, yogurt, butter, cream) for retail distribution. Our Supply Chain Management System is a hybrid desktop and web application designed to streamline operations by integrating:

- **Inventory Management** - Track finished dairy products with reorder levels and supplier connections
- **Supplier Management** - Manage relationships with dairy farms and packaging suppliers
- **Purchase Order System** - Automate procurement workflows
- **QR-Based Employee Attendance** - Modern, contactless employee time tracking
- **Reporting & Analytics** - Real-time visibility into operations

**Core Technology:** Java 17 desktop application (JavaFX 21) with cloud database (TiDB/MySQL) and HTML5 web scanner for QR attendance.

**Key Differentiator:** Unlike generic supply chain systems, ours tightly integrates workforce attendance tracking via QR codes, recognizing that employee presence is critical in time-sensitive, perishable goods operations.

---

## 2. Problem Statement & Business Case

### 2.1 Current Challenges

Dairy processing and distribution companies like Fresh Dairy Co. face several operational challenges:

1. **Manual Inventory Tracking**
   - Spreadsheet-based inventory leads to errors and outdated stock levels
   - Difficulty tracking perishable products with varying shelf lives
   - No automated alerts for low stock or reorder points

2. **Disconnected Supplier Management**
   - Supplier information scattered across documents and emails
   - No centralized view of supplier performance or reliability
   - Purchase orders created manually with high error rates

3. **Inefficient Employee Attendance**
   - Paper-based or manual punch-card systems
   - Time theft and buddy-punching issues
   - Delayed attendance data for payroll processing
   - Lack of real-time visibility into workforce availability

4. **Limited Operational Visibility**
   - No real-time reporting or dashboards
   - Difficult to identify trends or make data-driven decisions
   - Compliance and audit challenges

### 2.2 Business Impact

**Without an integrated system:**
- Inventory discrepancies lead to stockouts or overstocking (estimated 15-20% accuracy loss)
- Manual processes waste 5-10 hours per week of management time
- Delayed attendance data delays payroll by 1-2 days
- Poor supplier tracking results in missed delivery windows (critical for perishables)

**With our system:**
- **Operational Efficiency:** Reduce manual data entry by 70%
- **Inventory Accuracy:** Improve stock accuracy to >95%
- **Cost Savings:** Eliminate paper-based systems, reduce labor hours
- **Real-Time Visibility:** Instant access to inventory levels, supplier status, employee attendance
- **Compliance:** Automated record-keeping for audits and regulatory requirements

### 2.3 Why This Project?

This system addresses a **critical business need** for small-to-medium dairy processing companies that:
- Cannot afford enterprise ERP systems (SAP, Oracle)
- Need a focused solution for core supply chain operations
- Require modern workforce tracking (QR codes) for hygiene compliance
- Want a system that can be customized and maintained in-house

---

## 3. Solution Overview

### 3.1 Company Context: Fresh Dairy Co.

**Who We Are:**
Fresh Dairy Co. is a regional dairy processing and distribution company that serves as the **middle layer** in the dairy supply chain:

```
Dairy Farms → [FRESH DAIRY CO.] → Retailers/Grocers → Consumers
              (Our System Manages This)
```

**What We Do:**
1. **Receive** raw milk from local dairy farm suppliers
2. **Process** raw milk into finished products (pasteurization, packaging)
3. **Manage** inventory of finished goods in our warehouse
4. **Track** our warehouse and quality control employees
5. **Distribute** finished products to retail partners

**What We DON'T Do (Out of Scope):**
- ❌ Manage farm operations (cow milking, feeding, veterinary care)
- ❌ Control production machinery or manufacturing processes
- ❌ Handle retail sales or customer orders
- ❌ Manage delivery logistics to end customers

### 3.2 System Architecture

**Hybrid Architecture:**

1. **Desktop Application (JavaFX)**
   - Primary interface for managers, administrators, warehouse staff
   - Full CRUD operations for inventory, suppliers, purchase orders, employees
   - Role-based dashboards and reporting
   - QR code generation for employees
   - Runs on Windows/Mac/Linux workstations

2. **Web Scanner Interface (HTML5)**
   - Used by security guard at main gate entrance
   - QR code scanner using device camera (tablet or smartphone)
   - Displays employee verification screen with photo after scan
   - Guard confirms identity before recording attendance
   - Lightweight, works on any smartphone or tablet

3. **Cloud Database (TiDB/MySQL)**
   - Centralized data storage
   - Accessible from desktop app and web scanner
   - SSL/TLS encrypted connections
   - Automatic backups

### 3.3 Core Integration

The key innovation is **tight integration between supply chain operations and workforce tracking**:

- Warehouse managers see both inventory levels AND which employees are on-site
- Real-time attendance data helps explain productivity variations
- QR attendance system with security guard verification prevents time fraud
- Photo-based identity verification ensures only authorized employees enter
- All data unified in single system for comprehensive reporting

---

## 4. Scope Definition

### 4.1 What We WILL Manage

#### Suppliers
- **Dairy Farms:** Raw milk suppliers
  - Contact information, delivery schedules, milk quality ratings
  - Examples: "Green Pastures Farm", "Sunrise Dairy Farm", "Valley View Ranch"

- **Packaging Suppliers:** Bottles, labels, containers, caps
  - Product catalogs, pricing, lead times
  - Examples: "PlastiPack Industries", "EcoBottle Co.", "LabelPro Inc."

- **Equipment/Maintenance Suppliers:** Cleaning chemicals, machinery parts, safety equipment
  - Service contracts, response times
  - Examples: "Dairy Tech Solutions", "Clean Pro Supplies"

#### Inventory (Finished Products)
- **Milk:** Whole, Skim, 2%, Lactose-Free (1L, 2L bottles)
- **Cheese:** Cheddar, Mozzarella, Swiss, Cream Cheese (250g, 500g blocks)
- **Yogurt:** Plain, Greek, Strawberry, Blueberry (150g, 500g cups)
- **Butter:** Salted, Unsalted (250g, 500g packages)
- **Cream:** Heavy, Light, Whipping (250ml, 500ml cartons)

**Total SKUs:** ~15-20 products (manageable for demo/testing)

#### Purchase Orders
- Orders placed TO suppliers for raw materials and supplies
- Track: order date, expected delivery, quantity, unit price, total cost, status
- Statuses: Pending, Approved, Shipped, Received, Cancelled

#### Employees (Our Staff)
- **Warehouse Workers:** Receiving, picking, packing, shipping
- **Quality Control Inspectors:** Product testing, safety compliance
- **Inventory Managers:** Stock oversight, reordering decisions
- **Delivery Drivers:** Transport finished goods to retailers
- **Security Guards:** Gate control, employee attendance verification
- **Administrators:** System management, user accounts

#### Attendance
- QR-based check-in/check-out for all employees
- Track: employee, timestamp, status (checked in, checked out, late, absent)
- Real-time visibility into who's on-site
- Attendance history for payroll and compliance

### 4.2 What We Will NOT Manage

- ❌ **Farm Operations:** Cow milking schedules, feed management, herd health
- ❌ **Raw Materials Inventory:** Raw milk volume before processing (out of scope)
- ❌ **Production/Manufacturing:** Pasteurization process, quality control during production
- ❌ **Customer/Sales Orders:** Retail partners' orders or end-consumer sales
- ❌ **Delivery Logistics:** Route optimization, delivery tracking to retailers
- ❌ **Financial Accounting:** General ledger, accounts payable/receivable (only purchase order tracking)
- ❌ **HR Management:** Payroll calculation, benefits, performance reviews (only attendance tracking)

### 4.3 System Boundaries

```
┌─────────────────────────────────────────────────┐
│         FRESH DAIRY CO. SYSTEM (In Scope)       │
│                                                 │
│  ┌─────────────┐  ┌──────────────┐  ┌────────┐ │
│  │  Suppliers  │  │  Inventory   │  │Employee│ │
│  │ Management  │  │  Tracking    │  │Tracking│ │
│  └─────────────┘  └──────────────┘  └────────┘ │
│                                                 │
│  ┌─────────────┐  ┌──────────────┐             │
│  │  Purchase   │  │  Reporting & │             │
│  │   Orders    │  │  Dashboard   │             │
│  └─────────────┘  └──────────────┘             │
└─────────────────────────────────────────────────┘
        ↑                               ↑
   [Suppliers]                     [Employees]
   (External)                      (Our Staff)
        ↓                               ↓
  Raw Materials                  QR Attendance
```

---

## 5. User Personas

### Persona 1: Sarah - Warehouse Manager

**Demographics:**
- Age: 35
- Role: Warehouse Manager
- Experience: 8 years in dairy operations
- Tech Proficiency: Moderate

**Goals:**
- Maintain optimal inventory levels (no stockouts, minimal waste)
- Track supplier performance and delivery reliability
- Monitor employee attendance and productivity
- Generate reports for management meetings

**Pain Points:**
- Spends 2 hours daily updating spreadsheets
- Can't see real-time inventory or attendance data
- Struggles to coordinate supplier deliveries
- Misses reorder points, leading to rush orders

**How Our System Helps:**
- Real-time dashboard shows inventory, suppliers, and on-site employees
- Automated reorder alerts based on configurable thresholds
- One-click purchase order generation
- Visual reports for management presentations

### Persona 2: Ahmed - Warehouse Employee

**Demographics:**
- Age: 28
- Role: Warehouse Worker (receiving, picking, packing)
- Experience: 3 years
- Tech Proficiency: Basic (smartphone user)

**Goals:**
- Clock in/out quickly at start/end of shift
- Focus on warehouse tasks without administrative overhead
- Avoid touching shared surfaces (hygiene concerns)

**Pain Points:**
- Old punch-card system often jammed or lost cards
- Buddy-punching by coworkers created unfair situations
- Manual timesheets sometimes lost, affecting payroll

**How Our System Helps:**
- Personal QR code on printed badge
- Security guard scans badge at gate - quick verification
- Guard sees employee photo for identity confirmation
- No buddy-punching possible (guard verifies visually)
- Attendance instantly recorded after guard approval

### Persona 3: Dr. Fatima - Quality Control Inspector

**Demographics:**
- Age: 42
- Role: Quality Control Inspector
- Experience: 12 years in food safety
- Tech Proficiency: Moderate-High

**Goals:**
- Ensure only approved suppliers deliver raw materials
- Track which products are approaching expiration
- Verify compliance with food safety regulations
- Document quality issues for audit trails

**Pain Points:**
- Supplier certifications tracked in paper files
- Difficult to trace batch origins if quality issues arise
- No alerts for products nearing expiration

**How Our System Helps:**
- Supplier profiles include certifications, ratings, quality history
- Inventory items linked to supplier source
- Filterable views for high-priority items
- Audit trail for all system changes

### Persona 4: Khaled - System Administrator

**Demographics:**
- Age: 32
- Role: IT Administrator / Operations Manager
- Experience: 5 years
- Tech Proficiency: High

**Goals:**
- Manage user accounts and access permissions
- Ensure system security and data integrity
- Configure system settings (reorder levels, alert thresholds)
- Generate comprehensive reports for management

**Pain Points:**
- Previous system had no role-based access control
- Password resets required manual database edits
- No centralized configuration management

**How Our System Helps:**
- Role-based access control (Admin, Manager, Employee, Supplier)
- User management interface for account creation/deactivation
- Centralized configuration via admin dashboard
- System logs for security auditing

### Persona 5: Hassan - Security Guard

**Demographics:**
- Age: 45
- Role: Security Guard (stationed at main gate)
- Experience: 15 years in security
- Tech Proficiency: Basic-Moderate (smartphone user)

**Goals:**
- Verify employee identity before allowing entry
- Prevent unauthorized access to facility
- Quickly process employees during shift changes
- Maintain security logs

**Pain Points:**
- Manual logbook is slow and error-prone
- Can't verify if person matches the badge photo (no photos on old badges)
- Difficult to spot fake or stolen badges
- Handwriting illegible, causing payroll issues

**How Our System Helps:**
- Tablet with web scanner at gate (easy to use)
- After scanning QR, employee photo appears instantly for visual verification
- One-tap confirm or deny buttons
- Denied entries automatically notify manager
- No manual logbook needed, all digital

---

## 6. Key Features & Requirements

### 6.1 Inventory Management

**Functional Requirements:**

| Requirement ID | Description | Priority |
|---------------|-------------|----------|
| INV-001 | Add new inventory items with name, category, quantity, unit price, supplier | Must-Have |
| INV-002 | Update existing inventory (quantity adjustments, price changes) | Must-Have |
| INV-003 | Delete/deactivate inventory items | Must-Have |
| INV-004 | Set reorder level and reorder quantity per item | Must-Have |
| INV-005 | Display low-stock alerts when quantity ≤ reorder level | Must-Have |
| INV-006 | Search/filter inventory by category, supplier, stock status | Should-Have |
| INV-007 | View inventory history (quantity changes over time) | Nice-to-Have |
| INV-008 | Track expiration dates for perishable items | Nice-to-Have |

**Product Categories:**
- Milk (Whole, Skim, 2%, Lactose-Free)
- Cheese (Cheddar, Mozzarella, Swiss, Cream Cheese)
- Yogurt (Plain, Greek, Flavored)
- Butter (Salted, Unsalted)
- Cream (Heavy, Light, Whipping)

### 6.2 Supplier Management

**Functional Requirements:**

| Requirement ID | Description | Priority |
|---------------|-------------|----------|
| SUP-001 | Add suppliers with name, contact person, email, phone, address | Must-Have |
| SUP-002 | Categorize suppliers (Dairy Farm, Packaging, Equipment/Maintenance) | Must-Have |
| SUP-003 | Rate suppliers (1-5 stars based on quality, timeliness) | Should-Have |
| SUP-004 | Mark suppliers as active/inactive | Must-Have |
| SUP-005 | Link suppliers to inventory items they provide | Must-Have |
| SUP-006 | View supplier contact history and notes | Should-Have |
| SUP-007 | Search suppliers by name, category, or rating | Should-Have |

**Supplier Types:**
- **Dairy Farms:** Raw milk suppliers
- **Packaging:** Bottles, labels, containers
- **Equipment/Maintenance:** Cleaning supplies, machinery parts

### 6.3 Purchase Order Management

**Functional Requirements:**

| Requirement ID | Description | Priority |
|---------------|-------------|----------|
| PO-001 | Create purchase orders with supplier, items, quantities, prices | Must-Have |
| PO-002 | Set expected delivery date for purchase orders | Must-Have |
| PO-003 | Update PO status (Pending, Approved, Shipped, Received, Cancelled) | Must-Have |
| PO-004 | Calculate total cost automatically (quantity × unit price) | Must-Have |
| PO-005 | View all purchase orders with filtering by status, supplier, date | Must-Have |
| PO-006 | Mark PO as received and auto-update inventory quantities | Should-Have |
| PO-007 | Generate PO reports (spending by supplier, monthly totals) | Nice-to-Have |

**Purchase Order Workflow:**
1. Manager identifies low-stock item (via alert or manual check)
2. Creates PO: selects supplier, items, quantities
3. System calculates total cost
4. PO submitted (status: Pending)
5. Manager approves (status: Approved)
6. Supplier ships (status: Shipped)
7. Warehouse receives delivery (status: Received, inventory updated)

### 6.4 QR-Based Employee Attendance

**Functional Requirements:**

| Requirement ID | Description | Priority |
|---------------|-------------|----------|
| ATT-001 | Generate unique QR code for each employee | Must-Have |
| ATT-002 | Web-based QR scanner for security guard at gate (tablet/smartphone) | Must-Have |
| ATT-002b | Display employee verification screen with photo after QR scan | Must-Have |
| ATT-002c | Security guard confirmation required before attendance recorded | Must-Have |
| ATT-003 | Record check-in timestamp only after guard confirms (status: Checked In) | Must-Have |
| ATT-004 | Record check-out timestamp when guard confirms exit scan (status: Checked Out) | Must-Have |
| ATT-005 | Display current on-site employees in real-time | Must-Have |
| ATT-006 | Flag late arrivals based on configurable shift start times | Should-Have |
| ATT-007 | View attendance history per employee (date range filter) | Must-Have |
| ATT-008 | Export attendance data for payroll (CSV/Excel) | Should-Have |
| ATT-009 | Manual attendance override for admins (handle scanner issues) | Should-Have |
| ATT-010 | Deny check-in option with manager notification for security incidents | Must-Have |

**Attendance Workflow:**
1. Admin generates QR code for new employee and uploads employee photo
2. QR code printed on employee badge
3. Employee arrives at main gate with badge
4. Security guard (stationed at gate) scans employee's QR badge using tablet
5. System displays verification screen with employee photo, name, department, position
6. Guard visually verifies identity matches the photo
7. Guard taps "Confirm Check-In" → attendance recorded with timestamp
8. Employee proceeds into facility
9. At end of shift, employee exits through gate, guard scans badge again
10. Guard confirms check-out → system records exit timestamp
11. Manager views attendance dashboard showing all on-site employees in real-time

**Attendance Statuses:**
- **Checked In:** Employee on-site
- **Checked Out:** Employee left
- **Late:** Checked in after shift start time
- **Absent:** No check-in recorded for scheduled shift

### 6.5 Employee Management

**Functional Requirements:**

| Requirement ID | Description | Priority |
|---------------|-------------|----------|
| EMP-001 | Add employees with name, email, phone, department, position | Must-Have |
| EMP-002 | Link employee to user account for system login | Must-Have |
| EMP-003 | Assign roles (Admin, Manager, Employee, Supplier) | Must-Have |
| EMP-004 | Store hire date and employment status (active/inactive) | Must-Have |
| EMP-005 | View employee list with search and filter capabilities | Must-Have |
| EMP-006 | Update employee details | Must-Have |
| EMP-007 | Deactivate employees (soft delete - retain for history) | Must-Have |
| EMP-008 | Upload and store employee photo (cloud URL) for QR verification | Must-Have |

**Employee Departments:**
- Warehouse Operations
- Quality Control
- Inventory Management
- Security
- Administration

### 6.6 Reporting & Dashboard

**Functional Requirements:**

| Requirement ID | Description | Priority |
|---------------|-------------|----------|
| REP-001 | Dashboard shows key metrics (total inventory value, low-stock count, on-site employees) | Must-Have |
| REP-002 | Inventory report (current stock levels, values, categories) | Must-Have |
| REP-003 | Supplier report (list with ratings, contact info) | Should-Have |
| REP-004 | Purchase order report (spending by period, by supplier) | Should-Have |
| REP-005 | Attendance report (employee hours, late arrivals, absences) | Must-Have |
| REP-006 | Export reports to PDF and Excel | Should-Have |
| REP-007 | Customizable date ranges for all reports | Should-Have |

### 6.7 User Management & Security

**Functional Requirements:**

| Requirement ID | Description | Priority |
|---------------|-------------|----------|
| SEC-001 | User authentication with username and password | Must-Have |
| SEC-002 | Password hashing with BCrypt (10 salt rounds) | Must-Have |
| SEC-003 | Role-based access control (Admin, Manager, Employee, Supplier) | Must-Have |
| SEC-004 | Admin can create, update, deactivate user accounts | Must-Have |
| SEC-005 | Users can change their own passwords | Should-Have |
| SEC-006 | Session timeout after 30 minutes of inactivity | Nice-to-Have |
| SEC-007 | Audit log for critical operations (user changes, inventory updates) | Nice-to-Have |

**Role Permissions:**

| Feature | Admin | Manager | Employee | Supplier (Future) |
|---------|-------|---------|----------|-------------------|
| Inventory (View) | ✅ | ✅ | ✅ | ❌ |
| Inventory (Edit) | ✅ | ✅ | ❌ | ❌ |
| Suppliers (View) | ✅ | ✅ | ❌ | Own Profile Only |
| Suppliers (Edit) | ✅ | ✅ | ❌ | ❌ |
| Purchase Orders (View) | ✅ | ✅ | ❌ | Related POs Only |
| Purchase Orders (Create) | ✅ | ✅ | ❌ | ❌ |
| Employees (View) | ✅ | ✅ | ❌ | ❌ |
| Employees (Edit) | ✅ | ❌ | ❌ | ❌ |
| Attendance (View All) | ✅ | ✅ | ❌ | ❌ |
| Attendance (Scan QR) | ✅ | ✅ | ✅ | ❌ |
| Reports | ✅ | ✅ | Limited | ❌ |
| User Management | ✅ | ❌ | ❌ | ❌ |

---

## 7. Detailed Use Cases

### Use Case 1: QR-Based Employee Attendance with Security Verification

**Actors:**
- Primary: Hassan (Security Guard at main gate)
- Secondary: Ahmed (Warehouse Employee)

**Precondition:**
- Ahmed has been issued a QR code badge by admin
- Ahmed's photo uploaded to system
- Hassan has tablet with web scanner at gate entrance

**Trigger:** Ahmed arrives for his shift at main gate

**Main Flow:**

**Check-In Process:**
1. Ahmed arrives at main gate entrance at 08:02
2. Ahmed presents his QR code badge to Hassan (security guard)
3. Hassan scans Ahmed's QR badge using the tablet scanner
4. Scanner decodes QR code and sends employee ID to backend
5. System verifies employee exists and is active
6. System displays verification screen on Hassan's tablet:
   ```
   ┌─────────────────────────────────┐
   │  EMPLOYEE VERIFICATION          │
   ├─────────────────────────────────┤
   │  Photo: [Ahmed's Photo]         │
   │  Name: Ahmed Ali                │
   │  Employee ID: EMP-001           │
   │  Department: Warehouse Ops      │
   │  Position: Warehouse Worker     │
   │  Status: Active                 │
   │  Shift Start: 08:00             │
   ├─────────────────────────────────┤
   │  [✓ CONFIRM CHECK-IN]           │
   │  [✗ DENY ENTRY]                 │
   └─────────────────────────────────┘
   ```
7. Hassan visually compares Ahmed's face with the photo on screen
8. Identity confirmed - Hassan taps "Confirm Check-In"
9. System records check-in timestamp (08:02:00)
10. System determines status:
    - Time = 08:02 > Shift Start (08:00) → Status: "Late"
11. Tablet displays confirmation: "Ahmed Ali - Checked in at 08:02 (Late)"
12. Ahmed proceeds into facility
13. Manager's dashboard instantly updates to show Ahmed as on-site

**Check-Out Process:**
14. At end of shift (17:05), Ahmed exits through main gate
15. Hassan scans Ahmed's QR badge again
16. Verification screen displays (same as step 6, but with "Confirm Check-Out" button)
17. Hassan confirms identity and taps "Confirm Check-Out"
18. System records check-out timestamp (17:05:00)
19. Status changes to "Checked Out"
20. Tablet displays: "Ahmed Ali - Checked out at 17:05. Total hours: 9.05"
21. Dashboard updates to remove Ahmed from on-site list

**Alternative Flows:**

**A1: Security Denies Entry (Suspicious Activity)**
- At step 8, Hassan notices person doesn't match photo (wrong person, stolen badge)
- Hassan taps "Deny Entry" button
- System prompts: "Reason for denial?" → Hassan selects: "Identity mismatch"
- System logs security incident with timestamp and reason
- System sends instant notification to warehouse manager: "Security Alert: Check-in denied for badge EMP-001 at 08:02"
- Tablet displays: "Entry denied. Manager notified."
- Hassan prevents person from entering, follows security protocol
- Manager investigates incident

**A2: QR Code Lost/Stolen**
- Employee reports lost badge to admin
- Admin deactivates old QR code in system
- If old QR is scanned, system displays: "Badge deactivated. Contact administrator."
- Hassan denies entry, directs employee to admin office
- Admin generates new QR code after verifying employee identity

**A3: Scanner Malfunction**
- Network or scanner failure at gate
- Hassan radios warehouse manager
- Manager manually records attendance via desktop app using override function
- Hassan logs employee name in temporary backup list
- When system restored, manager reconciles manual entries

**A4: Employee Forgot Badge**
- Employee arrives without badge
- Hassan cannot scan QR code
- Employee directed to admin office
- Admin verifies identity (ID card) and manually records check-in with note
- Temporary paper pass issued for the day

**A5: Late Arrival with Valid Reason**
- At step 11, system flags Ahmed as "Late" (08:02 > 08:00)
- Ahmed explains reason to Hassan (traffic, medical appointment)
- Hassan adds note when confirming: "Late - Medical appointment"
- Manager reviews late arrivals report, sees note, no disciplinary action

**Postcondition:**
- Attendance record stored with employee ID, date, check-in time, check-out time, status, verified by security guard
- If denied, security incident logged with reason and manager notified
- Employee either granted or denied facility access based on verification

---

### Use Case 2: Creating a Purchase Order

**Actor:** Sarah (Warehouse Manager)
**Precondition:** Sarah is logged in, supplier exists in system
**Trigger:** Inventory alert shows "Whole Milk 1L" is at reorder level

**Main Flow:**
1. Sarah sees low-stock alert on dashboard: "Whole Milk 1L - Current: 50, Reorder Level: 100"
2. Clicks "Create Purchase Order" button
3. System displays PO creation form
4. Sarah selects supplier: "Green Pastures Farm" (dairy farm)
5. Adds items to order:
   - Item: Raw Milk (for processing)
   - Quantity: 5000 liters
   - Unit Price: $0.50/liter
   - System calculates: Total = 5000 × $0.50 = $2,500.00
6. Sets expected delivery date: "2025-11-18"
7. Adds note: "Urgent - low stock on whole milk products"
8. Clicks "Submit Purchase Order"
9. System creates PO with status "Pending", assigns PO number "PO-2025-1115-001"
10. System sends confirmation: "Purchase Order created successfully"
11. PO appears in Sarah's "Purchase Orders" list

**Approval Flow:**
12. Admin reviews pending POs in dashboard
13. Approves PO → Status changes to "Approved"
14. Email notification sent to supplier (future enhancement)

**Receipt Flow:**
15. Delivery arrives on 2025-11-18
16. Warehouse employee reports receipt
17. Sarah updates PO status to "Received"
18. System prompts: "Update inventory quantities?" → Sarah confirms
19. Relevant inventory items (Whole Milk 1L, Skim Milk 1L, etc.) quantities increase
20. Alert disappears from low-stock dashboard

**Alternative Flows:**
- **Supplier Not in System:** Sarah adds new supplier first, then creates PO
- **Delayed Delivery:** Sarah updates expected date, status remains "Shipped"
- **Partial Receipt:** Sarah creates second PO for remaining items (future enhancement)
- **Cancelled Order:** Sarah changes status to "Cancelled", inventory not updated

**Postcondition:** Purchase order recorded, inventory updated (if received), audit trail created

---

### Use Case 3: Managing Inventory Reorder Levels

**Actor:** Sarah (Warehouse Manager)
**Precondition:** Inventory item exists
**Trigger:** Sarah wants to adjust reorder thresholds

**Main Flow:**
1. Sarah navigates to "Inventory Management" module
2. Searches for "Greek Yogurt 500g"
3. Clicks "Edit" on the item
4. System displays edit form with current values:
   - Item Name: Greek Yogurt 500g
   - Category: Yogurt
   - Current Quantity: 150
   - Unit Price: $2.50
   - Reorder Level: 100 (alert triggers when quantity ≤ 100)
   - Reorder Quantity: 200 (suggested order amount)
   - Supplier: "Dairy Delight Packaging"
   - Location: Warehouse Aisle 3, Shelf B
5. Sarah updates Reorder Level from 100 to 120 (increased demand forecast)
6. Updates Reorder Quantity from 200 to 250
7. Clicks "Save Changes"
8. System validates inputs (non-negative numbers, reorder quantity > reorder level)
9. System updates database
10. Confirmation message: "Greek Yogurt 500g updated successfully"
11. If current quantity (150) > new reorder level (120), no alert triggered
12. When quantity drops to 120 or below, low-stock alert appears

**Alternative Flows:**
- **Invalid Input:** Sarah enters negative reorder level → System shows error, prevents save
- **Permission Denied:** Employee-role user tries to edit → System blocks with "Insufficient permissions"
- **Concurrent Edit:** Another manager edits same item → Last save wins (future: implement optimistic locking)

**Postcondition:** Inventory item reorder thresholds updated, alerts trigger based on new levels

---

### Use Case 4: Viewing Attendance Reports

**Actor:** Sarah (Warehouse Manager)
**Precondition:** Attendance data exists for employees
**Trigger:** End of week, Sarah needs attendance summary for payroll

**Main Flow:**
1. Sarah navigates to "Attendance" module
2. Clicks "Attendance Report" tab
3. System displays report filter options:
   - Date Range: [From Date] to [To Date]
   - Employee: [All Employees ▼] or specific employee
   - Status: [All Statuses ▼] or Checked In, Checked Out, Late, Absent
4. Sarah sets filters:
   - Date Range: 2025-11-11 to 2025-11-15 (this work week)
   - Employee: All Employees
   - Status: All Statuses
5. Clicks "Generate Report"
6. System queries attendance records, aggregates data
7. Report displays in table format:

| Employee Name | Total Days | On-Time | Late | Absent | Total Hours |
|--------------|------------|---------|------|--------|-------------|
| Ahmed Ali | 5 | 4 | 1 | 0 | 42.5 |
| Fatima Hassan | 5 | 5 | 0 | 0 | 40.0 |
| Khaled Omar | 4 | 3 | 1 | 1 | 32.0 |

8. Sarah reviews data, identifies Khaled's absence on 2025-11-13
9. Clicks "Export to Excel" button
10. System generates Excel file with detailed records (employee, date, check-in, check-out, hours, status)
11. Excel file downloads: "Attendance_Report_2025-11-11_to_2025-11-15.xlsx"
12. Sarah forwards file to HR for payroll processing

**Alternative Flows:**
- **Individual Employee:** Sarah selects "Ahmed Ali" from dropdown → Report shows only Ahmed's attendance
- **Late Arrivals Only:** Sarah filters Status: "Late" → Report shows all late check-ins
- **Export to PDF:** Sarah clicks "Export to PDF" → System generates printable PDF report
- **No Data:** Date range has no attendance records → System displays "No records found for selected criteria"

**Postcondition:** Attendance report viewed and/or exported for external processing

---

## 8. Success Criteria & KPIs

### 8.1 Functional Acceptance Criteria

| Criterion | Target | Measurement Method |
|-----------|--------|-------------------|
| User Authentication | 100% success rate | All test users can log in with valid credentials |
| Inventory CRUD | 100% functional | Create, read, update, delete operations work without errors |
| QR Code Generation | Unique QR per employee | No duplicate QR codes, all codes scannable |
| QR Attendance Recording | <2 second response | Time from scan to confirmation message |
| Low-Stock Alerts | 100% accuracy | Alerts trigger when quantity ≤ reorder level |
| Role-Based Access | 100% compliance | Users cannot access features outside their role |
| Report Generation | All formats supported | PDF and Excel exports work for all report types |
| Database Persistence | 100% data integrity | No data loss after application restart |

### 8.2 Performance Metrics

| Metric | Target | Rationale |
|--------|--------|-----------|
| Application Startup Time | <5 seconds | User frustration threshold |
| Database Query Response | <2 seconds for 95% of queries | Maintain responsiveness |
| Concurrent Users | Support 10+ simultaneous users | Team size + future growth |
| QR Scanner Load Time | <3 seconds on 4G connection | Mobile usability |
| Report Generation Time | <10 seconds for 1000 records | Acceptable wait time |
| System Uptime | >95% during working hours | Allow for maintenance windows |

### 8.3 Usability Goals

| Goal | Target | How We'll Measure |
|------|--------|-------------------|
| Onboarding Time | New user productive in <15 minutes | Timed training sessions |
| Error Rate | <5% user errors in common tasks | Track failed operations during testing |
| Task Completion | >90% of users complete tasks without help | Usability testing observations |
| User Satisfaction | >4/5 average rating | Post-deployment survey |
| Mobile Scanner Usability | >85% success rate on first scan | Track QR scan failures vs. successes |

### 8.4 Quality Metrics

| Metric | Target | Current Status |
|--------|--------|----------------|
| Code Test Coverage | >70% (from SPMP) | Framework ready, tests TBD |
| Critical Bugs | 0 before deployment | Active development |
| Code Review Coverage | 100% of pull requests | Git workflow enforced |
| Documentation Coverage | 100% of public APIs | Javadoc required |
| Security Vulnerabilities | 0 high/critical | BCrypt implemented, PreparedStatements used |

### 8.5 Business Impact KPIs (Estimated)

| KPI | Baseline (Manual System) | Target (Our System) | Expected Improvement |
|-----|-------------------------|---------------------|----------------------|
| Inventory Accuracy | 80% | >95% | +15% improvement |
| Time on Manual Data Entry | 10 hours/week | <3 hours/week | 70% reduction |
| Stockout Incidents | 3-4 per month | <1 per month | 75% reduction |
| Attendance Processing Time | 2 days delay | Real-time | Instant visibility |
| Supplier Lookup Time | 5-10 minutes | <30 seconds | 90% reduction |
| Report Generation Time | 2-3 hours | <5 minutes | 95% reduction |

---

## 9. Technical Architecture Overview

### 9.1 System Components

```
┌─────────────────────────────────────────────────────────────┐
│                     PRESENTATION LAYER                      │
│                                                             │
│  ┌──────────────────────┐      ┌───────────────────────┐   │
│  │   Desktop App        │      │   Web QR Scanner      │   │
│  │   (JavaFX 21)        │      │   (HTML5 + JS)        │   │
│  │                      │      │                       │   │
│  │  - Inventory UI      │      │  - Camera Access      │   │
│  │  - Supplier UI       │      │  - QR Decoder         │   │
│  │  - PO UI             │      │  - Attendance Submit  │   │
│  │  - Employee UI       │      │                       │   │
│  │  - Reports UI        │      │                       │   │
│  └──────────────────────┘      └───────────────────────┘   │
│           ↓                              ↓                  │
└───────────┼──────────────────────────────┼──────────────────┘
            │                              │
            ↓                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                        │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌─────────────────┐   │
│  │ Controllers  │  │  Services    │  │  DAOs           │   │
│  │              │  │  (Future)    │  │                 │   │
│  │ - Login      │  │              │  │ - UserDAO       │   │
│  │ - Dashboard  │  │ - Auth       │  │ - EmployeeDAO   │   │
│  │ - Inventory  │  │ - QRCode     │  │ - InventoryDAO  │   │
│  │ - Supplier   │  │ - Attendance │  │ - SupplierDAO   │   │
│  │ - Employee   │  │ - Inventory  │  │ - PurchaseDAO   │   │
│  │ - Attendance │  │              │  │ - AttendanceDAO │   │
│  └──────────────┘  └──────────────┘  └─────────────────┘   │
│           ↓                                  ↓              │
└───────────┼──────────────────────────────────┼──────────────┘
            │                                  │
            ↓                                  ↓
┌─────────────────────────────────────────────────────────────┐
│                      DATA LAYER                             │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │         TiDB Cloud (MySQL-compatible)                 │  │
│  │                                                       │  │
│  │  Tables: users, employees, inventory_items,          │  │
│  │          suppliers, purchase_orders, attendance      │  │
│  │                                                       │  │
│  │  Connection: SSL/TLS, Singleton Pattern              │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 9.2 Technology Stack

**Frontend (Desktop):**
- Java 17 (LTS) - Modern language features
- JavaFX 21 - Rich desktop UI framework
- FXML - Declarative UI layout
- CSS - Styling and theming

**Frontend (Web Scanner):**
- HTML5 - Structure
- JavaScript - QR decoding logic
- ZXing library (JavaScript port) - QR code reading
- Responsive design for mobile devices

**Backend:**
- Java 17 - Business logic
- JDBC (MySQL Connector/J 8.2.0) - Database connectivity
- DAO Pattern - Data access abstraction
- Service Layer (planned) - Business logic separation

**Database:**
- TiDB Cloud (MySQL 8.0 compatible) - Serverless cloud database
- SSL/TLS encryption - Secure connections
- Automated backups - Data safety

**Security:**
- BCrypt (jBCrypt 0.4) - Password hashing with 10 salt rounds
- PreparedStatements - SQL injection prevention
- Role-Based Access Control (RBAC) - Feature-level permissions

**Reporting:**
- Apache POI 5.2.5 - Excel generation (.xlsx)
- iText PDF 5.5.13.3 - PDF generation

**Libraries:**
- ZXing 3.5.3 - QR code generation/scanning
- Gson 2.10.1 - JSON processing
- Apache Commons Lang3 3.14.0 - Utility functions
- SLF4J 2.0.9 - Logging framework

**Build & Development:**
- Apache Maven 3.9.11 - Build automation
- Git - Version control
- JUnit 5 - Unit testing
- TestFX - JavaFX UI testing
- Mockito - Mocking framework

### 9.3 Security Architecture

**Authentication Flow:**
1. User enters username/password in LoginController
2. UserDAO queries database for user by username
3. PasswordUtil.checkPassword() verifies BCrypt hash
4. If valid, User object returned with role information
5. User object stored in session, passed to DashboardController
6. Dashboard configures UI based on user role

**Password Storage:**
- Plaintext passwords NEVER stored
- BCrypt hash with 10 salt rounds (configurable)
- Backward compatibility: Legacy plaintext check (migration support)
- Database column: `password_hash` (VARCHAR 60)

**Authorization (Role-Based Access Control):**
- Roles: ADMIN, MANAGER, EMPLOYEE, SUPPLIER
- Dashboard buttons enabled/disabled based on role
- DAO methods check user permissions (future enhancement)
- Session timeout after inactivity (future enhancement)

**Data Protection:**
- Database connection uses SSL/TLS (mandatory for TiDB Cloud)
- PreparedStatements prevent SQL injection
- Input validation on all user inputs
- Audit logging for critical operations (future enhancement)

**Attendance Fraud Prevention:**
- Security guard-controlled QR scanning (not employee self-scan)
- Photo verification required before check-in approval
- Physical presence at gate required (prevents remote scanning)
- Guard discretion to deny suspicious check-ins
- Security incidents logged and manager notified immediately

### 9.4 Database Schema

**Core Tables:**

```sql
users (
  user_id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(60) NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  role ENUM('ADMIN', 'MANAGER', 'EMPLOYEE', 'SUPPLIER'),
  first_name VARCHAR(50),
  last_name VARCHAR(50),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  last_login TIMESTAMP
)

employees (
  employee_id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT FOREIGN KEY REFERENCES users(user_id),
  department VARCHAR(50),
  position VARCHAR(50),
  phone VARCHAR(20),
  qr_code VARCHAR(100) UNIQUE,
  photo_url VARCHAR(255), -- Cloud storage URL for employee photo
  hire_date DATE,
  is_active BOOLEAN DEFAULT TRUE
)

inventory_items (
  item_id INT PRIMARY KEY AUTO_INCREMENT,
  item_name VARCHAR(100) NOT NULL,
  description TEXT,
  category VARCHAR(50),
  quantity INT NOT NULL DEFAULT 0,
  unit_price DECIMAL(10,2),
  reorder_level INT,
  reorder_quantity INT,
  supplier_id INT FOREIGN KEY REFERENCES suppliers(supplier_id),
  location VARCHAR(100),
  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)

suppliers (
  supplier_id INT PRIMARY KEY AUTO_INCREMENT,
  supplier_name VARCHAR(100) NOT NULL,
  contact_person VARCHAR(100),
  email VARCHAR(100),
  phone VARCHAR(20),
  address TEXT,
  category VARCHAR(50), -- Dairy Farm, Packaging, Equipment/Maintenance
  rating DECIMAL(2,1), -- 1.0 to 5.0
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)

purchase_orders (
  po_id INT PRIMARY KEY AUTO_INCREMENT,
  po_number VARCHAR(50) UNIQUE,
  supplier_id INT FOREIGN KEY REFERENCES suppliers(supplier_id),
  order_date DATE NOT NULL,
  expected_delivery DATE,
  status ENUM('PENDING', 'APPROVED', 'SHIPPED', 'RECEIVED', 'CANCELLED'),
  total_amount DECIMAL(10,2),
  notes TEXT,
  created_by INT FOREIGN KEY REFERENCES users(user_id),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)

attendance (
  attendance_id INT PRIMARY KEY AUTO_INCREMENT,
  employee_id INT FOREIGN KEY REFERENCES employees(employee_id),
  attendance_date DATE NOT NULL,
  check_in_time TIMESTAMP,
  check_out_time TIMESTAMP,
  status ENUM('CHECKED_IN', 'CHECKED_OUT', 'LATE', 'ABSENT'),
  notes TEXT
)
```

---

## 10. Non-Functional Requirements

### 10.1 Performance

| Requirement | Target | Priority |
|------------|--------|----------|
| Application startup time | <5 seconds | High |
| Database query response | <2 seconds for 95% of queries | High |
| QR scanner page load | <3 seconds on 4G connection | High |
| Report generation (1000 records) | <10 seconds | Medium |
| Concurrent users supported | 10+ simultaneous users | Medium |
| Data refresh interval | Real-time for attendance, <30s for inventory | Medium |

### 10.2 Security

| Requirement | Implementation | Priority |
|------------|----------------|----------|
| Password hashing | BCrypt with 10 salt rounds | Critical |
| SQL injection prevention | PreparedStatements for all queries | Critical |
| Database connection encryption | SSL/TLS (TiDB Cloud enforced) | Critical |
| Role-based access control | Enum-based roles, UI restrictions | High |
| Session timeout | 30 minutes inactivity (future) | Medium |
| Audit logging | Critical operations logged (future) | Medium |
| XSS prevention | Input sanitization, output encoding | High |

### 10.3 Usability

| Requirement | Target | Priority |
|------------|--------|----------|
| User onboarding | <15 minutes to productivity | High |
| Error messages | Clear, actionable guidance | High |
| Mobile scanner compatibility | Works on iOS Safari, Android Chrome | Critical |
| Keyboard shortcuts | Common operations (Ctrl+S save, etc.) | Low |
| Accessibility | WCAG 2.1 Level A compliance (future) | Low |
| Consistent UI patterns | Same layout across modules | Medium |

### 10.4 Scalability

| Requirement | Target | Priority |
|------------|--------|----------|
| Data volume | Support 10,000+ inventory items | Medium |
| User growth | Scale to 50+ concurrent users (future) | Low |
| Attendance records | Handle 5 years of daily attendance | Medium |
| Database connection pooling | Implement for >20 concurrent users | Future |

### 10.5 Reliability

| Requirement | Target | Priority |
|------------|--------|----------|
| System uptime | >95% during working hours (8am-6pm) | High |
| Data backup frequency | Daily automated backups (TiDB Cloud) | Critical |
| Recovery time objective (RTO) | <4 hours | Medium |
| Recovery point objective (RPO) | <24 hours (daily backups) | Medium |
| Error handling | Graceful degradation, no crashes | High |

### 10.6 Maintainability

| Requirement | Target | Priority |
|------------|--------|----------|
| Code documentation | Javadoc for all public APIs | High |
| Code test coverage | >70% (from SPMP) | High |
| Modular architecture | Layered MVC with clear separation | High |
| Naming conventions | Consistent Java/database conventions | Medium |
| Version control | Git with feature branches, PR reviews | High |

### 10.7 Compatibility

| Requirement | Target | Priority |
|------------|--------|----------|
| Java version | Java 17+ (LTS) | Critical |
| Operating systems | Windows 10+, macOS 11+, Ubuntu 20.04+ | High |
| Database | MySQL 8.0+ (TiDB Cloud compatible) | Critical |
| Mobile browsers (QR scanner) | iOS Safari 14+, Chrome 90+, Firefox 88+ | High |
| Screen resolutions | 1366×768 minimum, responsive up to 4K | Medium |

---

## 11. Sample Data Structure

### 11.1 Product Categories

**Milk Products (5 SKUs):**
| Item Name | Category | Typical Quantity | Unit Price | Reorder Level | Reorder Qty |
|-----------|----------|------------------|------------|---------------|-------------|
| Whole Milk 1L | Milk | 200 bottles | $2.50 | 100 | 200 |
| Skim Milk 1L | Milk | 150 bottles | $2.50 | 80 | 150 |
| 2% Milk 2L | Milk | 100 bottles | $4.00 | 50 | 100 |
| Lactose-Free Milk 1L | Milk | 80 bottles | $3.50 | 40 | 80 |
| Whole Milk 2L | Milk | 120 bottles | $4.50 | 60 | 120 |

**Cheese Products (4 SKUs):**
| Item Name | Category | Typical Quantity | Unit Price | Reorder Level | Reorder Qty |
|-----------|----------|------------------|------------|---------------|-------------|
| Cheddar Cheese 500g | Cheese | 100 blocks | $5.00 | 50 | 100 |
| Mozzarella 250g | Cheese | 120 packages | $3.50 | 60 | 120 |
| Swiss Cheese 500g | Cheese | 60 blocks | $6.00 | 30 | 60 |
| Cream Cheese 200g | Cheese | 150 tubs | $2.50 | 75 | 150 |

**Yogurt Products (4 SKUs):**
| Item Name | Category | Typical Quantity | Unit Price | Reorder Level | Reorder Qty |
|-----------|----------|------------------|------------|---------------|-------------|
| Plain Yogurt 500g | Yogurt | 100 cups | $3.00 | 50 | 100 |
| Greek Yogurt 200g | Yogurt | 150 cups | $2.50 | 75 | 150 |
| Strawberry Yogurt 150g | Yogurt | 200 cups | $1.50 | 100 | 200 |
| Blueberry Yogurt 150g | Yogurt | 180 cups | $1.50 | 90 | 180 |

**Butter Products (2 SKUs):**
| Item Name | Category | Typical Quantity | Unit Price | Reorder Level | Reorder Qty |
|-----------|----------|------------------|------------|---------------|-------------|
| Salted Butter 250g | Butter | 120 packages | $4.00 | 60 | 120 |
| Unsalted Butter 500g | Butter | 80 packages | $7.00 | 40 | 80 |

**Cream Products (3 SKUs):**
| Item Name | Category | Typical Quantity | Unit Price | Reorder Level | Reorder Qty |
|-----------|----------|------------------|------------|---------------|-------------|
| Heavy Cream 500ml | Cream | 80 cartons | $3.50 | 40 | 80 |
| Light Cream 250ml | Cream | 100 cartons | $2.00 | 50 | 100 |
| Whipping Cream 500ml | Cream | 60 cartons | $4.50 | 30 | 60 |

**Total: 18 SKUs across 5 categories**

### 11.2 Sample Suppliers

**Dairy Farms (Raw Milk Suppliers):**
1. **Green Pastures Farm**
   - Contact: Ahmed Al-Rashid
   - Phone: +966-555-1234
   - Email: ahmed@greenpastures.sa
   - Rating: 4.5/5
   - Supplies: Raw milk (5000L per week)

2. **Sunrise Dairy Farm**
   - Contact: Fatima Hassan
   - Phone: +966-555-5678
   - Email: fatima@sunrisedairy.sa
   - Rating: 4.8/5
   - Supplies: Raw milk (3000L per week)

3. **Valley View Ranch**
   - Contact: Khaled Omar
   - Phone: +966-555-9012
   - Email: khaled@valleyview.sa
   - Rating: 4.2/5
   - Supplies: Raw milk (2000L per week)

**Packaging Suppliers:**
4. **PlastiPack Industries**
   - Contact: Sarah Ahmed
   - Phone: +966-555-3456
   - Email: sales@plastipack.sa
   - Rating: 4.7/5
   - Supplies: Plastic bottles (1L, 2L), yogurt cups, butter containers

5. **EcoBottle Co.**
   - Contact: Mohammed Ali
   - Phone: +966-555-7890
   - Email: info@ecobottle.sa
   - Rating: 4.3/5
   - Supplies: Eco-friendly bottles, labels, caps

**Equipment/Maintenance:**
6. **Dairy Tech Solutions**
   - Contact: Dr. Layla Hassan
   - Phone: +966-555-2345
   - Email: support@dairytech.sa
   - Rating: 4.9/5
   - Supplies: Machinery parts, maintenance services, cleaning chemicals

### 11.3 Sample Employees

| Name | Department | Position | Role | QR Code | Photo URL | Hire Date |
|------|------------|----------|------|---------|-----------|-----------|
| Khaled Admin | Administration | System Administrator | ADMIN | QR-ADM-001 | https://storage.tidbcloud.com/fresh-dairy/employees/ADM-001.jpg | 2023-01-15 |
| Sarah Manager | Warehouse Operations | Warehouse Manager | MANAGER | QR-MGR-001 | https://storage.tidbcloud.com/fresh-dairy/employees/MGR-001.jpg | 2023-03-10 |
| Ahmed Ali | Warehouse Operations | Warehouse Worker | EMPLOYEE | QR-EMP-001 | https://storage.tidbcloud.com/fresh-dairy/employees/EMP-001.jpg | 2024-02-01 |
| Fatima Hassan | Quality Control | QC Inspector | EMPLOYEE | QR-EMP-002 | https://storage.tidbcloud.com/fresh-dairy/employees/EMP-002.jpg | 2023-06-20 |
| Mohammed Omar | Warehouse Operations | Warehouse Worker | EMPLOYEE | QR-EMP-003 | https://storage.tidbcloud.com/fresh-dairy/employees/EMP-003.jpg | 2024-05-15 |
| Layla Abdullah | Inventory Management | Inventory Specialist | MANAGER | QR-MGR-002 | https://storage.tidbcloud.com/fresh-dairy/employees/MGR-002.jpg | 2023-08-01 |
| Hassan Khalid | Security | Security Guard | EMPLOYEE | QR-EMP-004 | https://storage.tidbcloud.com/fresh-dairy/employees/EMP-004.jpg | 2022-11-01 |

---

## 12. Agile Development Notes

This document is a **living document** that will evolve as the project progresses. We are using **Agile/Scrum methodology** with 2-week sprints.

### 12.1 Current Sprint Focus (Week 10-11)
- Complete inventory management CRUD operations
- Implement supplier management UI
- Begin QR code generation functionality

### 12.2 Upcoming Features (Subject to Change)
- Purchase order workflow
- QR attendance web scanner
- Reporting and dashboards
- Data import/export utilities

### 12.3 Known Pending Decisions
- [ ] Exact expiration date tracking requirements for perishable items
- [ ] Security guard user role (SECURITY vs EMPLOYEE role) - needs team discussion
- [ ] Cloud storage provider for employee photos (TiDB Cloud storage vs AWS S3 vs Azure Blob)
- [ ] Integration with external payroll systems (future consideration)
- [ ] Barcode support in addition to QR codes (future enhancement)

### 12.4 Recent Decisions Made
- ✅ **QR Attendance Security (Nov 15, 2025):** Changed from employee self-scan to security guard-controlled scanning with photo verification to prevent time fraud
- ✅ **Employee Photos (Nov 15, 2025):** Store as cloud URLs in `photo_url` field, not local files
- ✅ **Single Gate (Nov 15, 2025):** System designed for single main gate entrance (no multi-gate tracking needed)
- ✅ **Manager Notification (Nov 15, 2025):** Denied check-ins automatically notify warehouse manager
- ✅ **No Manual Logbook (Nov 15, 2025):** Digital-only system, manual override only for scanner failures

### 12.5 Document Update Log

| Date | Version | Changes | Updated By |
|------|---------|---------|------------|
| 2025-11-15 | 1.0 | Initial comprehensive project idea document created | Team |
| 2025-11-15 | 1.1 | Updated QR attendance workflow: security guard-controlled scanning with photo verification, added security guard persona, updated database schema with photo_url field, added fraud prevention measures | Team |

---

## 13. References & Resources

- **Project Idea Form** - `documents/Project Idea Form.pdf` (Week 3)
- **Project Proposal Form** - `documents/Project Proposal Form.pdf` (Week 4)
- **SPMP** - `documents/SPMP Template.pdf` (Week 8)
- **README.md** - Setup instructions, credentials, troubleshooting
- **CLAUDE.md** - Technical guidance for development

---

## Questions or Clarifications?

This document is maintained by the team. For questions, suggestions, or updates:
- Discuss in team meetings (weekly Scrum)
- Update via pull requests with team review
- Tag issues in GitHub/project management tool

**Last Reviewed:** November 15, 2025
**Next Review:** December 1, 2025 (or as needed during sprints)

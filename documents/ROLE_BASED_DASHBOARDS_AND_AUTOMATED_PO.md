# Role-Based Dashboards & Automated Purchase Order System
## Technical Specification Document

**Version:** 1.0.0
**Last Updated:** November 15, 2025
**Course:** CSC 305 - Software Engineering
**Team:** Supply Chain Management Team
**Related Documents:** PROJECT_IDEA_DETAILED.md, CLAUDE.md

---

## 1. Executive Summary

This document specifies the design and implementation of **role-based dashboards** and the **automated purchase order progression system** for the Fresh Dairy Co. Supply Chain Management System.

### Key Features:
- **Three distinct role-based dashboards** (Admin, Manager, Employee)
- **Automated PO status progression** with configurable timers
- **Auto-inventory updates** when orders are received
- **Toast notification system** for real-time user feedback
- **Modern JavaFX UI** with charts, graphs, and animations

### Core Decisions:
1. ✅ **Manager approves own POs** - No admin approval workflow needed
2. ✅ **All inventory is public** - No department restrictions
3. ✅ **Supplier as data only** - No login capability for current scope
4. ✅ **Security guard = EMPLOYEE role** - Uses web scanner, not desktop app
5. ✅ **Automated PO progression** - Simulates supplier processing with timers
6. ✅ **Fixed dashboards** - Role-specific layouts, not customizable widgets

---

## 2. User Role Definitions & Permissions

### 2.1 ADMIN Role
**Example User:** Khaled Admin - System Administrator

**Primary Responsibilities:**
- System configuration and settings management
- User account creation and management
- Employee management (create, edit, generate QR codes)
- System oversight and audit logging
- Manual attendance override (scanner failures)

**What Admin CAN Do:**
- ✅ Create, edit, deactivate user accounts
- ✅ Create, edit, deactivate employees
- ✅ Generate QR codes for employees
- ✅ Upload employee photos
- ✅ Configure system settings (shift times, alert thresholds, PO timers)
- ✅ View all purchase orders (oversight)
- ✅ Create purchase orders (but managers typically do this)
- ✅ View/edit all inventory items
- ✅ View/edit all suppliers
- ✅ View all attendance records
- ✅ Manual attendance override
- ✅ Generate all types of reports
- ✅ Access audit logs

**What Admin CANNOT Do:**
- ❌ Does NOT need to approve POs (managers self-approve)

**Dashboard Focus:**
- System-wide metrics and health monitoring
- User management tools
- Configuration settings
- Critical alerts and security incidents

---

### 2.2 MANAGER Role
**Example Users:** Sarah Manager - Warehouse Manager, Layla Abdullah - Inventory Specialist

**Primary Responsibilities:**
- Day-to-day warehouse operations
- Inventory management and reordering
- Purchase order creation and approval (self-approve)
- Supplier coordination and rating
- Employee attendance monitoring
- Operational reporting

**What Manager CAN Do:**
- ✅ View/edit all inventory items
- ✅ Adjust inventory quantities
- ✅ Set reorder levels and reorder quantities
- ✅ Create purchase orders
- ✅ **Approve own purchase orders** (new: no admin approval needed)
- ✅ Update PO status (Preparing, Shipped, Received)
- ✅ View/add/edit suppliers
- ✅ Rate suppliers (quality, timeliness)
- ✅ View all employee list (read-only)
- ✅ View all attendance records
- ✅ Perform QR verification at gate (if acting as security)
- ✅ Generate operational reports (inventory, suppliers, attendance, POs)
- ✅ Export reports (PDF, Excel)

**What Manager CANNOT Do:**
- ❌ Create or edit employee records
- ❌ Generate QR codes
- ❌ Create or manage user accounts
- ❌ Access system settings
- ❌ Manual attendance override (admin only)

**Dashboard Focus:**
- Operational alerts (low stock, pending deliveries)
- Real-time inventory and attendance status
- PO tracking with live status updates
- Quick action buttons for daily tasks

---

### 2.3 EMPLOYEE Role
**Example Users:** Ahmed Ali - Warehouse Worker, Dr. Fatima Hassan - QC Inspector, Hassan Khalid - Security Guard

**Primary Responsibilities:**
- Operational tasks (warehouse work, quality control, security)
- Personal attendance tracking
- Viewing inventory for operational needs

**What Employee CAN Do:**
- ✅ View all inventory items (read-only)
- ✅ Search inventory for warehouse picking
- ✅ View own attendance history
- ✅ Access personal QR code (if needed)

**Special Case - Security Guard (Hassan):**
- ✅ Use web scanner tablet at gate (separate interface)
- ✅ Scan employee QR badges
- ✅ View employee verification screens (photos, names, departments)
- ✅ Confirm or deny check-ins
- ⚠️ Note: This is a **functional capability** of the web scanner, not a desktop app permission

**What Employee CANNOT Do:**
- ❌ Edit inventory
- ❌ View or edit suppliers
- ❌ View or create purchase orders
- ❌ View other employees' information
- ❌ View other employees' attendance records
- ❌ Generate reports
- ❌ Access any settings

**Dashboard Focus:**
- Personal attendance status (checked in/out)
- Personal shift schedule
- Weekly attendance history (last 7 days)
- Simple, focused interface

---

### 2.4 SUPPLIER Role (Future - Data Only)
**Status:** Not implemented as login role for current scope

**Current Implementation:**
- Suppliers are **contact records** in the database
- Admin/Manager add and edit supplier information
- No login capability for suppliers
- No user_id foreign key in suppliers table

**Future Enhancement (Post-Week 16):**
- Supplier portal for viewing their own POs
- Update delivery status capability
- View their performance ratings

---

## 3. Complete Permission Matrix

| Module/Feature | Admin | Manager | Employee | Supplier (Future) |
|----------------|-------|---------|----------|-------------------|
| **Inventory Management** | | | | |
| View all items | ✅ Full | ✅ Full | ✅ Read-only | ❌ |
| Create items | ✅ | ✅ | ❌ | ❌ |
| Edit items | ✅ All | ✅ All | ❌ | ❌ |
| Adjust quantities | ✅ | ✅ | ❌ | ❌ |
| Set reorder levels | ✅ | ✅ | ❌ | ❌ |
| Deactivate items | ✅ | ✅ | ❌ | ❌ |
| **Supplier Management** | | | | |
| View suppliers | ✅ All | ✅ All | ❌ | Own profile only |
| Create suppliers | ✅ | ✅ | ❌ | ❌ |
| Edit suppliers | ✅ All | ✅ All | ❌ | Own (future) |
| Rate suppliers | ✅ | ✅ | ❌ | ❌ |
| Deactivate suppliers | ✅ | ✅ | ❌ | ❌ |
| **Purchase Orders** | | | | |
| View POs | ✅ All | ✅ All | ❌ | Related only |
| Create POs | ✅ | ✅ | ❌ | ❌ |
| **Approve POs** | ✅ | ✅ **Self-approve** | ❌ | ❌ |
| Update PO status | ✅ | ✅ | ❌ | Delivery status |
| Cancel POs | ✅ | ✅ | ❌ | ❌ |
| **Employee Management** | | | | |
| View employees | ✅ All | ✅ All (read-only) | ❌ | ❌ |
| Create employees | ✅ | ❌ | ❌ | ❌ |
| Edit employees | ✅ All | ❌ | ❌ | ❌ |
| Generate QR codes | ✅ | ❌ | ❌ | ❌ |
| Upload photos | ✅ | ❌ | ❌ | ❌ |
| Deactivate employees | ✅ | ❌ | ❌ | ❌ |
| **Attendance** | | | | |
| View all records | ✅ | ✅ | ❌ | ❌ |
| View own history | ✅ | ✅ | ✅ | ❌ |
| QR verification (gate) | ✅ | ✅ | ✅ (web scanner) | ❌ |
| Manual override | ✅ | ❌ | ❌ | ❌ |
| Export attendance data | ✅ | ✅ | ❌ | ❌ |
| **Reports** | | | | |
| Inventory reports | ✅ | ✅ | ❌ | ❌ |
| Supplier reports | ✅ | ✅ | ❌ | ❌ |
| PO reports | ✅ | ✅ | ❌ | ❌ |
| Attendance reports | ✅ | ✅ | ❌ | ❌ |
| Audit logs | ✅ | ❌ | ❌ | ❌ |
| Export PDF/Excel | ✅ | ✅ | ❌ | ❌ |
| **User Management** | | | | |
| View users | ✅ | ❌ | ❌ | ❌ |
| Create users | ✅ | ❌ | ❌ | ❌ |
| Edit users | ✅ | ❌ | ❌ | ❌ |
| Assign roles | ✅ | ❌ | ❌ | ❌ |
| Reset passwords | ✅ | ❌ | ❌ | ❌ |
| Deactivate users | ✅ | ❌ | ❌ | ❌ |
| **Settings** | | | | |
| System configuration | ✅ | ❌ | ❌ | ❌ |
| PO timer settings | ✅ | ❌ | ❌ | ❌ |
| Shift time settings | ✅ | ❌ | ❌ | ❌ |
| Alert thresholds | ✅ | ❌ | ❌ | ❌ |

---

## 4. Dashboard Specifications

### 4.1 Admin Dashboard - "Command Center"

**Layout Mockup:**

```
┌──────────────────────────────────────────────────────────────────────┐
│  Fresh Dairy Co. - Supply Chain System    [User: Khaled Admin] [⚙️] │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐          │
│  │  System Health │ │  Active Users  │ │  Total Items   │          │
│  │      🟢 OK     │ │      24 👥     │ │     342 📦     │          │
│  │   Uptime: 99%  │ │  Last 24h: 18  │ │  Value: $45K   │          │
│  └────────────────┘ └────────────────┘ └────────────────┘          │
│                                                                      │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐          │
│  │  Low Stock     │ │  Security      │ │  Pending Tasks │          │
│  │    8 items ⚠️  │ │  Incidents: 0  │ │     3 tasks    │          │
│  │  [View Details]│ │  Status: Safe  │ │  [View Tasks]  │          │
│  └────────────────┘ └────────────────┘ └────────────────┘          │
│                                                                      │
│  ┌──────────────────────────────────────┐  ┌─────────────────────┐ │
│  │  Inventory Value Trend (Last 7 Days) │  │  Critical Alerts    │ │
│  │  📈 [LineChart]                       │  │  🔴 Milk: 50 units  │ │
│  │      $50K ┤                           │  │  🟡 Late: 3 emps    │ │
│  │      $45K ┤    ╱─╲                    │  │  🟢 All OK: Cheese  │ │
│  │      $40K ┤   ╱   ╲   ╱─╲             │  │                     │ │
│  │      $35K ┤  ╱     ╲ ╱   ╲            │  │  [View All Alerts]  │ │
│  │           └────────────────           │  │                     │ │
│  │           Mon Tue Wed Thu Fri         │  │                     │ │
│  └──────────────────────────────────────┘  └─────────────────────┘ │
│                                                                      │
│  Quick Actions:                                                     │
│  [➕ Create User] [👥 Manage Employees] [⚙️ Settings] [📊 Reports]  │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

**Widgets:**

1. **System Health Card**
   - Status: OK / Warning / Critical (color-coded: green/yellow/red)
   - Uptime percentage
   - Last restart time

2. **Active Users Card**
   - Count of logged-in users in last 24 hours
   - Current active sessions
   - Click to view user activity log

3. **Total Items Card**
   - Total inventory item count
   - Total inventory value ($)
   - Quick link to inventory module

4. **Low Stock Alert Card**
   - Count of items at or below reorder level
   - Red/yellow indicator
   - "View Details" button → Opens inventory with low-stock filter

5. **Security Incidents Card**
   - Count of denied attendance entries today
   - "Status: Safe" or "Attention Needed"
   - Click to view incident log

6. **Pending Tasks Card**
   - Items requiring admin attention
   - Example: New user requests, system updates
   - Click to view task list

7. **Inventory Value Trend Chart**
   - JavaFX LineChart
   - Shows total inventory value over last 7 days
   - X-axis: Days (Mon-Sun)
   - Y-axis: Value ($)
   - Animated line with smooth curves

8. **Critical Alerts Panel**
   - Scrollable list of prioritized alerts
   - Color-coded by severity
   - Click alert to navigate to relevant module

**Quick Actions:**
- Create User → Opens user creation dialog
- Manage Employees → Opens employee management module
- Settings → Opens system settings
- Reports → Opens report generation module

**Auto-Refresh:**
- Widgets refresh every 30 seconds
- Charts update every 60 seconds
- Real-time alerts push immediately

**Visual Design:**
- Gradient background: #e3f2fd (light blue) to #ffffff (white)
- Card shadows: -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2)
- Font: Segoe UI / San Francisco (system default)
- Accent color: #1976d2 (Material Blue 700)

---

### 4.2 Manager Dashboard - "Operations Hub"

**Layout Mockup:**

```
┌──────────────────────────────────────────────────────────────────────┐
│  Fresh Dairy Co. - Warehouse Operations  [User: Sarah Manager] [📊] │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌────────────────┐ ┌────────────────┐ ┌────────────────┐          │
│  │  Inventory     │ │  On-Site Now   │ │  POs In Transit│          │
│  │  342 items 📦  │ │    12 👤       │ │     2 🚚       │          │
│  │  8 need reorder│ │  Ahmed, Fatima │ │  [View Details]│          │
│  └────────────────┘ └────────────────┘ └────────────────┘          │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Stock Levels by Category                                    │   │
│  │  ┌─────────────────────────────────────────────────────────┐ │   │
│  │  │ Milk    ████████░░ 80% (200/250)                        │ │   │
│  │  │ Cheese  ████░░░░░░ 40% (60/150) ⚠️ REORDER               │ │   │
│  │  │ Yogurt  ██████████ 95% (190/200)                        │ │   │
│  │  │ Butter  ███████░░░ 70% (84/120)                         │ │   │
│  │  │ Cream   ██████░░░░ 65% (52/80)                          │ │   │
│  │  └─────────────────────────────────────────────────────────┘ │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌──────────────────────────────────────┐  ┌─────────────────────┐ │
│  │  Recent Alerts & Actions             │  │  POs In Progress    │ │
│  │  🔴 Cheddar 500g - REORDER (35 left) │  │                     │ │
│  │     [Create PO] [Adjust Levels]      │  │  PO-1115-001:       │ │
│  │                                       │  │  🟡 PREPARING       │ │
│  │  🟡 Ahmed Ali - Late at 08:15         │  │  Milk from Green    │ │
│  │     [View Attendance] [Add Note]     │  │  Pastures Farm      │ │
│  │                                       │  │  ETA: 4 min         │ │
│  │  🟢 Delivery from PlastiPack - Today  │  │                     │ │
│  │     [Mark Received] [View Details]   │  │  PO-1115-002:       │ │
│  │                                       │  │  🔵 SHIPPED         │ │
│  └──────────────────────────────────────┘  │  Bottles from Eco   │ │
│                                             │  ETA: 8 min         │ │
│  Quick Actions:                             │  [View All POs]     │ │
│  [📋 Create PO] [📊 Update Inventory]       └─────────────────────┘ │
│  [👥 View Attendance] [📈 Generate Report]                          │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

**Widgets:**

1. **Inventory Summary Card**
   - Total item count
   - Items needing reorder (below reorder level)
   - Click to open inventory module

2. **On-Site Employees Card**
   - Real-time count of checked-in employees
   - Names of currently on-site staff (scrollable if many)
   - Updates immediately when QR scan occurs
   - Click to view full attendance list

3. **POs In Transit Card**
   - Count of active POs (Preparing, Shipped statuses)
   - Click to view PO list filtered by in-progress statuses

4. **Stock Levels by Category Bar Chart**
   - JavaFX HorizontalBarChart
   - Each category (Milk, Cheese, Yogurt, Butter, Cream)
   - Shows: Current / Max capacity percentage
   - Color coding:
     - Green: >70%
     - Yellow: 40-70%
     - Red: <40% with ⚠️ warning icon
   - Click bar to filter inventory by category

5. **Recent Alerts & Actions Panel**
   - Scrollable list of actionable items
   - Low-stock alerts with quick action buttons
   - Late arrivals with links to attendance
   - Expected deliveries with status update buttons
   - Auto-refresh every 30 seconds

6. **POs In Progress Panel**
   - Real-time status of active purchase orders
   - Shows:
     - PO number
     - Status badge (color-coded)
     - Supplier name
     - Item description
     - ETA countdown (for automated progression)
   - Status colors:
     - 🟡 Yellow: PREPARING
     - 🔵 Blue: SHIPPED
     - 🟢 Green: RECEIVED
   - Auto-updates when status changes (no refresh needed)
   - Animated countdown timer
   - "View All POs" link to full PO module

**Quick Actions:**
- Create PO → Opens PO creation form
- Update Inventory → Opens inventory module
- View Attendance → Opens attendance module
- Generate Report → Opens report module with preset filters

**Auto-Refresh:**
- On-Site Employees: Real-time (WebSocket or polling every 5 sec)
- POs In Progress: Every 10 seconds (to show ETA countdown)
- Stock Levels Chart: Every 60 seconds
- Alerts Panel: Every 30 seconds

**Visual Design:**
- Gradient background: #f3e5f5 (light purple) to #ffffff
- Accent color: #7b1fa2 (Purple 700)
- Progress bars: Gradient fills
- Animated transitions when data updates

---

### 4.3 Employee Dashboard - "Personal Portal"

**Layout Mockup:**

```
┌──────────────────────────────────────────────────────────────────────┐
│  Fresh Dairy Co.                         [User: Ahmed Ali] [👤]     │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                 Your Attendance Status                       │   │
│  │                                                              │   │
│  │                     ✅ CHECKED IN                            │   │
│  │                                                              │   │
│  │  Check-In:  08:05 AM          Status: On-Time               │   │
│  │  Shift:     08:00 AM - 05:00 PM                             │   │
│  │  Location:  Main Gate (verified by Hassan)                  │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Your Weekly Attendance (Last 7 Days)                       │   │
│  │                                                              │   │
│  │  Mon   Tue   Wed   Thu   Fri   Sat   Sun                   │   │
│  │   ✅    ✅    ✅    ⚠️    ✅    ✅    ⚠️                      │   │
│  │  8:00  8:05  8:00  8:15  8:00  8:00  8:20                   │   │
│  │                                                              │   │
│  │  Total Hours This Week: 42.5 hours                          │   │
│  │  On-Time: 5 days  |  Late: 2 days                           │   │
│  │                                                              │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Current On-Site Employees: 12                              │   │
│  │  (Ahmed, Fatima, Mohammed, Sarah, Layla, Hassan...)         │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
│  Quick Access:                                                      │
│  [📋 View Inventory] [📅 My Attendance History]                     │
│                                                                      │
└──────────────────────────────────────────────────────────────────────┘
```

**Widgets:**

1. **Your Attendance Status Card (Large)**
   - Current status: Checked In / Checked Out
   - Large visual indicator:
     - ✅ Green checkmark if checked in
     - ⭕ Gray circle if checked out
   - Today's check-in time
   - Current shift schedule
   - Verification location (e.g., "Main Gate")
   - Guard who verified (e.g., "verified by Hassan")

2. **Weekly Attendance Calendar**
   - Simple 7-day view (last 7 days or current week)
   - Each day shows:
     - ✅ Green checkmark: On-time
     - ⚠️ Yellow warning: Late
     - ❌ Red X: Absent
   - Time of check-in displayed below day
   - Summary stats:
     - Total hours worked
     - On-time count
     - Late count

3. **Current On-Site Employees**
   - Simple count
   - Names list (for coordination purposes)
   - Helps employees know who's available

**Quick Access Buttons:**
- View Inventory → Opens inventory module (read-only)
- My Attendance History → Opens full attendance history with date range

**Auto-Refresh:**
- Attendance Status: Real-time (updates on check-in/out)
- On-Site Employees: Every 30 seconds

**Visual Design:**
- Clean, simple layout
- Large, easy-to-read text
- Minimal visual clutter
- Focus on personal information
- Accent color: #43a047 (Green 600)

---

## 5. Automated Purchase Order Progression

### 5.1 Overview

The automated PO progression system simulates realistic supplier processing by automatically transitioning purchase orders through status stages using configurable timers. This feature provides:

- **Demo Value:** Live status changes during presentations
- **Realism:** Simulates actual supplier processing times
- **Automation:** Reduces manual status updates
- **Flexibility:** Configurable timers for different scenarios (fast demo vs realistic)

### 5.2 PO Status Workflow

**Status Stages:**

```
1. PENDING     → Manager creates PO, awaiting supplier acceptance
                 ⬇️ [Timer 1: pending_to_preparing_seconds]

2. PREPARING   → Supplier accepted and is preparing order
                 ⬇️ [Timer 2: preparing_to_shipped_seconds]

3. SHIPPED     → Order shipped, in transit
                 ⬇️ [Timer 3: shipped_to_received_seconds]

4. RECEIVED    → Order arrived at warehouse
                 ⬇️ [Auto-trigger: inventory update]

5. COMPLETE    → Inventory quantities updated, PO archived
```

**Status Definitions:**

| Status | Description | Manager Actions | System Actions |
|--------|-------------|----------------|----------------|
| PENDING | PO created, awaiting supplier | Can edit/cancel | Start Timer 1 |
| PREPARING | Supplier accepted, preparing items | View only | Start Timer 2, Send notification |
| SHIPPED | Items shipped, in transit | View tracking (future) | Start Timer 3, Send notification |
| RECEIVED | Items arrived at warehouse | Confirm receipt (auto) | Update inventory, Send notification |
| COMPLETE | Inventory updated, PO closed | View for reference | Archive, Update metrics |

### 5.3 Database Schema Updates

#### New Table: `settings`

```sql
CREATE TABLE settings (
  setting_key VARCHAR(50) PRIMARY KEY,
  setting_value VARCHAR(100) NOT NULL,
  description TEXT,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by INT,
  FOREIGN KEY (updated_by) REFERENCES users(user_id)
);

-- Default Configuration
INSERT INTO settings (setting_key, setting_value, description) VALUES
  ('po.auto_progression_enabled', 'true', 'Enable/disable automatic PO status progression'),
  ('po.pending_to_preparing_seconds', '60', 'Time (seconds) from PENDING to PREPARING'),
  ('po.preparing_to_shipped_seconds', '180', 'Time (seconds) from PREPARING to SHIPPED'),
  ('po.shipped_to_received_seconds', '300', 'Time (seconds) from SHIPPED to RECEIVED'),
  ('po.auto_inventory_update', 'true', 'Auto-update inventory when PO reaches RECEIVED'),
  ('notification.toast_duration_seconds', '5', 'Duration (seconds) for toast notifications'),
  ('dashboard.refresh_interval_seconds', '30', 'Dashboard widget auto-refresh interval');
```

#### Updates to `purchase_orders` Table (Optional)

```sql
ALTER TABLE purchase_orders ADD COLUMN expected_preparing_time TIMESTAMP NULL;
ALTER TABLE purchase_orders ADD COLUMN expected_shipping_time TIMESTAMP NULL;
ALTER TABLE purchase_orders ADD COLUMN expected_delivery_time TIMESTAMP NULL;
ALTER TABLE purchase_orders ADD COLUMN auto_progression_enabled BOOLEAN DEFAULT TRUE;
```

*Note: These columns are optional. If not used, timers are relative to current time, not absolute timestamps.*

### 5.4 Java Implementation

#### Class: `PurchaseOrderSimulator.java`

**Location:** `com.team.supplychain.services.PurchaseOrderSimulator`

```java
package com.team.supplychain.services;

import com.team.supplychain.dao.PurchaseOrderDAO;
import com.team.supplychain.dao.SettingsDAO;
import com.team.supplychain.dao.InventoryDAO;
import com.team.supplychain.models.PurchaseOrder;
import com.team.supplychain.enums.POStatus;
import com.team.supplychain.utils.ToastNotification;
import javafx.application.Platform;

public class PurchaseOrderSimulator {

    private static final PurchaseOrderDAO poDAO = new PurchaseOrderDAO();
    private static final SettingsDAO settingsDAO = new SettingsDAO();
    private static final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * Start automated progression for a purchase order
     * @param poId Purchase order ID
     */
    public static void simulateDelivery(int poId) {
        // Check if auto-progression is enabled
        boolean enabled = settingsDAO.getBooleanSetting("po.auto_progression_enabled", true);
        if (!enabled) {
            return; // Manual mode, skip automation
        }

        // Get configured durations (in seconds)
        int pendingDuration = settingsDAO.getIntSetting("po.pending_to_preparing_seconds", 60);
        int preparingDuration = settingsDAO.getIntSetting("po.preparing_to_shipped_seconds", 180);
        int shippingDuration = settingsDAO.getIntSetting("po.shipped_to_received_seconds", 300);

        // Start background thread
        Thread simulationThread = new Thread(() -> {
            try {
                // Stage 1: PENDING → PREPARING
                Thread.sleep(pendingDuration * 1000L);
                updatePOStatus(poId, POStatus.PREPARING, "Supplier is preparing your order");

                // Stage 2: PREPARING → SHIPPED
                Thread.sleep(preparingDuration * 1000L);
                updatePOStatus(poId, POStatus.SHIPPED, "Order shipped! In transit to warehouse");

                // Stage 3: SHIPPED → RECEIVED
                Thread.sleep(shippingDuration * 1000L);
                updatePOStatus(poId, POStatus.RECEIVED, "Order arrived at warehouse!");

                // Stage 4: Auto-update inventory
                boolean autoUpdate = settingsDAO.getBooleanSetting("po.auto_inventory_update", true);
                if (autoUpdate) {
                    autoUpdateInventory(poId);
                }

            } catch (InterruptedException e) {
                // Thread interrupted (app closing or PO cancelled)
                Thread.currentThread().interrupt();
                System.out.println("PO simulation interrupted for PO: " + poId);
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() ->
                    ToastNotification.showError("Error updating PO status: " + e.getMessage())
                );
            }
        });

        // Set as daemon thread (won't prevent app shutdown)
        simulationThread.setDaemon(true);
        simulationThread.setName("PO-Simulator-" + poId);
        simulationThread.start();
    }

    /**
     * Update PO status and show notification
     */
    private static void updatePOStatus(int poId, POStatus newStatus, String message) {
        try {
            // Update database
            poDAO.updateStatus(poId, newStatus);

            // Show toast notification on JavaFX thread
            Platform.runLater(() -> {
                PurchaseOrder po = poDAO.getById(poId);
                if (po != null) {
                    ToastNotification.showInfo(
                        "PO-" + po.getPoNumber() + ": " + message
                    );
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Auto-update inventory when PO is received
     */
    private static void autoUpdateInventory(int poId) {
        try {
            PurchaseOrder po = poDAO.getById(poId);
            if (po == null) return;

            // Get PO items (assuming you have PurchaseOrderItem table)
            // For each item in the PO:
            //   1. Get current inventory quantity
            //   2. Add ordered quantity
            //   3. Update inventory

            // Example (simplified):
            // inventoryDAO.increaseQuantity(itemId, quantity);

            Platform.runLater(() -> {
                ToastNotification.showSuccess(
                    "Inventory updated for PO-" + po.getPoNumber() + "!"
                );
            });

            // Update PO status to COMPLETE
            poDAO.updateStatus(poId, POStatus.COMPLETE);

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() ->
                ToastNotification.showError("Error updating inventory: " + e.getMessage())
            );
        }
    }

    /**
     * Cancel ongoing simulation (if PO is cancelled)
     */
    public static void cancelSimulation(int poId) {
        // In practice, need to track active threads and interrupt them
        // For simplicity, threads are daemon and will stop on app close
    }
}
```

#### Class: `SettingsDAO.java`

**Location:** `com.team.supplychain.dao.SettingsDAO`

```java
package com.team.supplychain.dao;

import com.team.supplychain.utils.DatabaseConnection;
import java.sql.*;

public class SettingsDAO {

    /**
     * Get string setting value
     */
    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM settings WHERE setting_key = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, key);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("setting_value");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return defaultValue;
    }

    /**
     * Get integer setting value
     */
    public int getIntSetting(String key, int defaultValue) {
        String value = getSetting(key, String.valueOf(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Get boolean setting value
     */
    public boolean getBooleanSetting(String key, boolean defaultValue) {
        String value = getSetting(key, String.valueOf(defaultValue));
        return "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    /**
     * Update setting value
     */
    public boolean updateSetting(String key, String value, int updatedBy) {
        String sql = "UPDATE settings SET setting_value = ?, updated_by = ?, updated_at = CURRENT_TIMESTAMP WHERE setting_key = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, value);
            stmt.setInt(2, updatedBy);
            stmt.setString(3, key);

            int rows = stmt.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
```

#### Class: `ToastNotification.java`

**Location:** `com.team.supplychain.utils.ToastNotification`

```java
package com.team.supplychain.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class ToastNotification {

    public enum Type {
        INFO("#2196F3"),    // Blue
        SUCCESS("#4CAF50"), // Green
        WARNING("#FF9800"), // Orange
        ERROR("#F44336");   // Red

        private final String color;
        Type(String color) { this.color = color; }
        public String getColor() { return color; }
    }

    /**
     * Show toast notification
     */
    public static void show(String message, Type type, int durationSeconds) {
        Stage toastStage = new Stage();
        toastStage.initStyle(StageStyle.TRANSPARENT);
        toastStage.setAlwaysOnTop(true);

        Text text = new Text(message);
        text.setFill(Color.WHITE);
        text.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        StackPane root = new StackPane(text);
        root.setStyle(
            "-fx-background-color: " + type.getColor() + ";" +
            "-fx-background-radius: 10;" +
            "-fx-padding: 15 25;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 15, 0, 0, 5);"
        );
        root.setAlignment(Pos.CENTER);
        root.setOpacity(0);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);

        // Position: Bottom-right corner
        toastStage.setX(javafx.stage.Screen.getPrimary().getVisualBounds().getWidth() - 350);
        toastStage.setY(javafx.stage.Screen.getPrimary().getVisualBounds().getHeight() - 100);

        toastStage.show();

        // Fade-in animation
        Timeline fadeIn = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(root.opacityProperty(), 0)),
            new KeyFrame(Duration.millis(300), new KeyValue(root.opacityProperty(), 1))
        );

        // Fade-out animation
        Timeline fadeOut = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(root.opacityProperty(), 1)),
            new KeyFrame(Duration.millis(300), new KeyValue(root.opacityProperty(), 0))
        );

        fadeOut.setOnFinished(e -> toastStage.close());

        // Show for duration, then fade out
        Timeline showDuration = new Timeline(
            new KeyFrame(Duration.seconds(durationSeconds), e -> fadeOut.play())
        );

        fadeIn.play();
        fadeIn.setOnFinished(e -> showDuration.play());
    }

    // Convenience methods
    public static void showInfo(String message) {
        show(message, Type.INFO, 5);
    }

    public static void showSuccess(String message) {
        show(message, Type.SUCCESS, 5);
    }

    public static void showWarning(String message) {
        show(message, Type.WARNING, 5);
    }

    public static void showError(String message) {
        show(message, Type.ERROR, 5);
    }
}
```

### 5.5 Integration with Purchase Order Creation

**In `PurchaseOrderController.java`:**

```java
public void handleCreatePO() {
    try {
        // Create PO object
        PurchaseOrder po = new PurchaseOrder();
        po.setPoNumber(generatePONumber());
        po.setSupplierId(selectedSupplier.getSupplierId());
        po.setOrderDate(LocalDate.now());
        po.setExpectedDelivery(expectedDeliveryDate);
        po.setStatus(POStatus.PENDING);
        po.setTotalAmount(calculateTotal());
        po.setCreatedBy(currentUser.getUserId());

        // Save to database
        int poId = purchaseOrderDAO.create(po);

        if (poId > 0) {
            // Start automated progression
            PurchaseOrderSimulator.simulateDelivery(poId);

            // Show success message
            ToastNotification.showSuccess(
                "Purchase Order created! Supplier will process automatically."
            );

            // Refresh PO list
            loadPurchaseOrders();

            // Close dialog
            closeDialog();
        }

    } catch (Exception e) {
        e.printStackTrace();
        ToastNotification.showError("Failed to create PO: " + e.getMessage());
    }
}
```

### 5.6 Configuration UI for Admin

**Settings Module - PO Timer Configuration:**

```
┌─────────────────────────────────────────────────────────┐
│  Purchase Order Automation Settings                    │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  [✓] Enable automatic PO progression                   │
│  [✓] Auto-update inventory when order received         │
│                                                         │
│  ┌────────────────────────────────────────────────────┐ │
│  │  Timer Configuration                               │ │
│  ├────────────────────────────────────────────────────┤ │
│  │  Pending → Preparing:  [  60  ] seconds           │ │
│  │  Preparing → Shipped:  [ 180  ] seconds           │ │
│  │  Shipped → Received:   [ 300  ] seconds           │ │
│  │                                                    │ │
│  │  Total Time: 540 seconds (9 minutes)              │ │
│  └────────────────────────────────────────────────────┘ │
│                                                         │
│  Quick Presets:                                         │
│  [Fast Demo (10-20-30s)] [Normal (1-3-5min)]           │
│  [Realistic (1hr-3hr-1day)]                            │
│                                                         │
│  [Apply Settings] [Reset to Defaults]                  │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

**Preset Values:**

| Preset | Pending→Preparing | Preparing→Shipped | Shipped→Received | Total Time |
|--------|-------------------|-------------------|------------------|------------|
| **Fast Demo** | 10 sec | 20 sec | 30 sec | 60 sec (1 min) |
| **Normal** | 60 sec | 180 sec | 300 sec | 540 sec (9 min) |
| **Realistic** | 3600 sec (1 hr) | 10800 sec (3 hr) | 86400 sec (24 hr) | ~28.5 hours |

### 5.7 Manager Dashboard - PO Status Display

**Real-Time PO Status Card:**

```
┌───────────────────────────────────┐
│  POs In Progress                  │
├───────────────────────────────────┤
│                                   │
│  PO-2025-1115-001                 │
│  🟡 PREPARING                      │
│  ├─ Supplier: Green Pastures Farm │
│  ├─ Item: Raw Milk 5000L          │
│  └─ ETA: 2 min 35 sec             │
│     [View Details]                │
│                                   │
│  PO-2025-1115-002                 │
│  🔵 SHIPPED                        │
│  ├─ Supplier: PlastiPack          │
│  ├─ Item: Bottles 10,000 units    │
│  └─ ETA: 7 min 12 sec             │
│     [View Details]                │
│                                   │
│  [View All POs]                   │
└───────────────────────────────────┘
```

**Implementation - ETA Countdown:**

```java
// In Manager Dashboard Controller
private void updatePOCountdowns() {
    Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
        for (PurchaseOrder po : activePOs) {
            // Calculate time until next status change
            long secondsRemaining = calculateSecondsRemaining(po);

            // Update label
            String eta = formatTime(secondsRemaining);
            poLabels.get(po.getPoId()).setText("ETA: " + eta);
        }
    }));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
}
```

---

## 6. Toast Notification System

### 6.1 Specifications

**Position:** Bottom-right corner of screen
**Duration:** 5 seconds (configurable)
**Animation:** Fade-in (300ms), Display (5s), Fade-out (300ms)
**Stacking:** New toasts appear above older ones (vertical stack)

**Types & Colors:**

| Type | Color | Use Case |
|------|-------|----------|
| INFO | Blue (#2196F3) | General notifications (PO status updates) |
| SUCCESS | Green (#4CAF50) | Successful operations (PO created, inventory updated) |
| WARNING | Orange (#FF9800) | Warnings (low stock, late arrival) |
| ERROR | Red (#F44336) | Errors (failed to create PO, database error) |

**Examples:**

```
┌────────────────────────────────┐
│  ℹ️ PO-001: Supplier is        │
│     preparing your order       │
└────────────────────────────────┘
   (Blue background)

┌────────────────────────────────┐
│  ✅ Inventory updated for      │
│     PO-001! (+200 Milk 1L)     │
└────────────────────────────────┘
   (Green background)

┌────────────────────────────────┐
│  ⚠️ Cheddar Cheese: Only 35    │
│     units left. Reorder now!   │
└────────────────────────────────┘
   (Orange background)

┌────────────────────────────────┐
│  ❌ Failed to create PO:       │
│     Database connection error  │
└────────────────────────────────┘
   (Red background)
```

### 6.2 Usage Examples

```java
// In PO Simulator
ToastNotification.showInfo("Order is being prepared");

// In Inventory Controller
ToastNotification.showSuccess("Inventory updated: +200 Whole Milk 1L");

// In Dashboard
ToastNotification.showWarning("Low stock alert: Cheddar Cheese (35 units)");

// In DAO
ToastNotification.showError("Database connection failed");
```

---

## 7. JavaFX Visual Design System

### 7.1 Color Palette - "Fresh Dairy Co." Theme

**Primary Colors:**

| Color Name | Hex Code | Usage |
|------------|----------|-------|
| Primary Blue | #1976d2 | Admin accent, buttons, links |
| Primary Purple | #7b1fa2 | Manager accent, highlights |
| Primary Green | #43a047 | Employee accent, success states |
| Light Blue BG | #e3f2fd | Admin dashboard background (gradient start) |
| Light Purple BG | #f3e5f5 | Manager dashboard background (gradient start) |
| Light Green BG | #e8f5e9 | Employee dashboard background (gradient start) |
| White | #ffffff | Background (gradient end), cards |

**Semantic Colors:**

| Purpose | Hex Code |
|---------|----------|
| Success | #4CAF50 (Green) |
| Warning | #FF9800 (Orange) |
| Error | #F44336 (Red) |
| Info | #2196F3 (Blue) |
| Neutral Gray | #757575 |
| Light Gray | #BDBDBD |

### 7.2 Typography

**Font Family:**
- Primary: System default (Segoe UI on Windows, San Francisco on macOS)
- Fallback: Arial, Helvetica, sans-serif

**Font Sizes:**

| Element | Size | Weight |
|---------|------|--------|
| Dashboard Title | 24px | Bold |
| Widget Title | 18px | Semi-Bold |
| Metric Value | 32px | Bold |
| Body Text | 14px | Regular |
| Small Text | 12px | Regular |
| Button Text | 14px | Medium |

### 7.3 CSS Styling Guidelines

**Dashboard Root:**

```css
.dashboard-root {
    -fx-background-color: linear-gradient(to bottom, #e3f2fd, #ffffff);
}

.dashboard-root-manager {
    -fx-background-color: linear-gradient(to bottom, #f3e5f5, #ffffff);
}

.dashboard-root-employee {
    -fx-background-color: linear-gradient(to bottom, #e8f5e9, #ffffff);
}
```

**Widget Cards:**

```css
.dashboard-card {
    -fx-background-color: white;
    -fx-background-radius: 10;
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
    -fx-padding: 20;
}

.dashboard-card:hover {
    -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 3);
    -fx-cursor: hand;
}
```

**Metric Cards:**

```css
.metric-card-value {
    -fx-font-size: 32px;
    -fx-font-weight: bold;
    -fx-text-fill: #1976d2;
}

.metric-card-label {
    -fx-font-size: 14px;
    -fx-text-fill: #757575;
}

.metric-card-icon {
    -fx-font-size: 48px;
}
```

**Alert Cards:**

```css
.alert-card-critical {
    -fx-background-color: #ffebee;
    -fx-border-color: #f44336;
    -fx-border-radius: 5;
    -fx-border-width: 2;
}

.alert-card-warning {
    -fx-background-color: #fff3e0;
    -fx-border-color: #ff9800;
    -fx-border-radius: 5;
    -fx-border-width: 2;
}

.alert-card-success {
    -fx-background-color: #e8f5e9;
    -fx-border-color: #4caf50;
    -fx-border-radius: 5;
    -fx-border-width: 2;
}
```

**Buttons:**

```css
.quick-action-button {
    -fx-background-color: #1976d2;
    -fx-text-fill: white;
    -fx-background-radius: 5;
    -fx-padding: 10 20;
    -fx-font-size: 14px;
    -fx-font-weight: medium;
    -fx-cursor: hand;
}

.quick-action-button:hover {
    -fx-background-color: #1565c0;
}

.quick-action-button:pressed {
    -fx-background-color: #0d47a1;
}
```

### 7.4 Animation Specifications

**Card Fade-In:**

```java
FadeTransition fadeIn = new FadeTransition(Duration.millis(500), card);
fadeIn.setFromValue(0.0);
fadeIn.setToValue(1.0);
fadeIn.play();
```

**Chart Update Transition:**

```java
// When chart data updates
Timeline timeline = new Timeline(
    new KeyFrame(Duration.ZERO, new KeyValue(chart.opacityProperty(), 0.5)),
    new KeyFrame(Duration.millis(300), new KeyValue(chart.opacityProperty(), 1.0))
);
timeline.play();
```

**PO Status Change Pulse:**

```java
ScaleTransition pulse = new ScaleTransition(Duration.millis(200), statusBadge);
pulse.setFromX(1.0);
pulse.setFromY(1.0);
pulse.setToX(1.2);
pulse.setToY(1.2);
pulse.setAutoReverse(true);
pulse.setCycleCount(2);
pulse.play();
```

### 7.5 Chart Specifications

**Inventory Value Trend (LineChart):**

```java
LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
chart.setTitle("Inventory Value Trend");
chart.setAnimated(true);
chart.setLegendVisible(false);

XYChart.Series<String, Number> series = new XYChart.Series<>();
series.setName("Value");

// Add data points (last 7 days)
series.getData().add(new XYChart.Data<>("Mon", 42000));
series.getData().add(new XYChart.Data<>("Tue", 43500));
// ... more data

chart.getData().add(series);
```

**Stock Levels (HorizontalBarChart):**

```java
CategoryAxis yAxis = new CategoryAxis();
NumberAxis xAxis = new NumberAxis();
BarChart<Number, String> chart = new BarChart<>(xAxis, yAxis);
chart.setTitle("Stock Levels by Category");
chart.setBarGap(5);
chart.setCategoryGap(10);

XYChart.Series<Number, String> series = new XYChart.Series<>();

// Add categories with percentage fill
series.getData().add(new XYChart.Data<>(80, "Milk"));
series.getData().add(new XYChart.Data<>(40, "Cheese")); // Low - color red
series.getData().add(new XYChart.Data<>(95, "Yogurt"));
// ... more data

chart.getData().add(series);

// Custom color based on percentage
for (XYChart.Data<Number, String> data : series.getData()) {
    double value = data.getXValue().doubleValue();
    String color;
    if (value < 40) color = "#f44336"; // Red
    else if (value < 70) color = "#ff9800"; // Orange
    else color = "#4caf50"; // Green

    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
}
```

---

## 8. Demo Scenario - Professor Presentation

### 8.1 Setup (Before Demo)

**Configuration:**
- Admin logs in
- Opens Settings → PO Automation
- Selects "Fast Demo" preset (10-20-30 seconds)
- Clicks "Apply Settings"
- Logs out, Manager logs in

**Test Data:**
- Inventory: Whole Milk 1L at 45 units (reorder level: 100)
- Supplier: Green Pastures Farm (active, 4.5★ rating)

### 8.2 Live Demo Timeline (2-Minute Full Workflow)

**0:00 - Manager Login**
- Sarah Manager logs in
- Dashboard loads with low-stock alert:
  - 🔴 "Whole Milk 1L - REORDER (45 units left)"

**0:15 - Create Purchase Order**
- Sarah clicks "Create PO" button
- Fills form:
  - Supplier: Green Pastures Farm
  - Item: Raw Milk for processing
  - Quantity: 5000 liters
  - Unit Price: $0.50/liter
  - Total: $2,500.00 (auto-calculated)
  - Expected Delivery: Tomorrow
- Clicks "Submit"
- Toast notification: ✅ "Purchase Order created! Supplier will process automatically."

**0:30 - PO Appears in Dashboard**
- PO-2025-1115-001 appears in "POs In Progress" widget
- Status: 🟡 PENDING
- ETA: 10 seconds

**0:40 - Status Changes to PREPARING**
- Toast notification: ℹ️ "PO-001: Supplier is preparing your order"
- Status badge pulses (animation)
- Status: 🟡 PREPARING
- ETA: 20 seconds

**1:00 - Status Changes to SHIPPED**
- Toast notification: ℹ️ "PO-001: Order shipped! In transit to warehouse"
- Status badge animates again
- Status: 🔵 SHIPPED
- ETA: 30 seconds

**1:30 - Status Changes to RECEIVED**
- Toast notification: ✅ "PO-001: Order arrived at warehouse!"
- Status: 🟢 RECEIVED

**1:35 - Inventory Auto-Updates**
- Toast notification: ✅ "Inventory updated for PO-001! (+200 Whole Milk 1L)"
- Inventory widget updates: 45 → 245 units
- Low-stock alert disappears
- Stock level bar chart updates (animation shows bar growing)

**1:45 - Review Results**
- Sarah navigates to Inventory module
- Shows Whole Milk 1L: 245 units (green status)
- Navigates to Purchase Orders module
- Shows PO-001: Status = COMPLETE

**2:00 - Demo Complete**
- Professor sees full workflow in 2 minutes
- Emphasize automation, real-time updates, modern UI

### 8.3 Talking Points

**During Demo:**
1. "Notice the low-stock alert - our system proactively identifies reorder needs"
2. "Manager creates PO with automatic approval - no bottleneck waiting for admin"
3. "Watch as the status changes automatically - simulating real supplier processing"
4. "Toast notifications keep manager informed without being intrusive"
5. "Inventory updates automatically when order arrives - no manual data entry"
6. "Modern JavaFX UI with charts, animations, and real-time updates"

**Key Features to Highlight:**
- ✅ Automated PO progression (unique feature)
- ✅ Real-time dashboard updates
- ✅ Toast notification system
- ✅ Role-based access (show different dashboards)
- ✅ Auto-inventory update (eliminates manual work)
- ✅ Modern, professional UI design

---

## 9. Implementation Priorities

### Phase 1: Must-Have (Week 11-12)

**Dashboard Layouts:**
- [ ] Admin dashboard FXML layout
- [ ] Manager dashboard FXML layout
- [ ] Employee dashboard FXML layout
- [ ] Basic widget cards (metric cards)

**PO Auto-Progression:**
- [ ] Create `settings` table
- [ ] Implement `SettingsDAO.java`
- [ ] Implement `PurchaseOrderSimulator.java`
- [ ] Integrate with PO creation workflow
- [ ] Basic toast notifications (info, success only)

**Role Enforcement:**
- [ ] Update `DashboardController.java` with new role logic
- [ ] Disable buttons based on updated permission matrix
- [ ] Test all three role dashboards

### Phase 2: Should-Have (Week 13)

**Dashboard Enhancements:**
- [ ] Stock levels bar chart (Manager dashboard)
- [ ] Inventory trend line chart (Admin dashboard)
- [ ] Real-time on-site employees widget
- [ ] POs in progress widget with ETA countdown

**Notifications:**
- [ ] Complete `ToastNotification.java` with all types
- [ ] Implement stacking for multiple toasts
- [ ] Add configurable duration

**Settings UI:**
- [ ] Admin settings module for PO timers
- [ ] Quick preset buttons (Fast Demo, Normal, Realistic)
- [ ] Enable/disable toggle for auto-progression

**Auto-Inventory Update:**
- [ ] Implement inventory quantity increase on PO receipt
- [ ] Link PO items to inventory items (many-to-many)
- [ ] Success notification when inventory updated

### Phase 3: Nice-to-Have (Week 14+)

**Advanced Animations:**
- [ ] Card fade-in on dashboard load
- [ ] Chart transition animations
- [ ] PO status badge pulse effect

**Enhanced Charts:**
- [ ] Pie chart for supplier distribution
- [ ] Area chart for attendance trends
- [ ] Interactive chart tooltips

**Additional Features:**
- [ ] Dashboard widget auto-refresh (configurable interval)
- [ ] Export dashboard as PDF report
- [ ] Customizable alert thresholds per manager

---

## 10. Technical Requirements Summary

### 10.1 Database Schema Changes

**New Tables:**
```sql
CREATE TABLE settings (
  setting_key VARCHAR(50) PRIMARY KEY,
  setting_value VARCHAR(100) NOT NULL,
  description TEXT,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  updated_by INT,
  FOREIGN KEY (updated_by) REFERENCES users(user_id)
);
```

**Seed Data:**
```sql
INSERT INTO settings (setting_key, setting_value, description) VALUES
  ('po.auto_progression_enabled', 'true', 'Enable/disable automatic PO status progression'),
  ('po.pending_to_preparing_seconds', '60', 'Time from PENDING to PREPARING'),
  ('po.preparing_to_shipped_seconds', '180', 'Time from PREPARING to SHIPPED'),
  ('po.shipped_to_received_seconds', '300', 'Time from SHIPPED to RECEIVED'),
  ('po.auto_inventory_update', 'true', 'Auto-update inventory when PO received'),
  ('notification.toast_duration_seconds', '5', 'Toast notification duration'),
  ('dashboard.refresh_interval_seconds', '30', 'Dashboard auto-refresh interval');
```

### 10.2 New Java Classes

| Class | Package | Purpose |
|-------|---------|---------|
| `SettingsDAO.java` | `com.team.supplychain.dao` | Settings CRUD operations |
| `PurchaseOrderSimulator.java` | `com.team.supplychain.services` | PO auto-progression logic |
| `ToastNotification.java` | `com.team.supplychain.utils` | Toast notification utility |
| `AdminDashboardController.java` | `com.team.supplychain.controllers` | Admin dashboard controller |
| `ManagerDashboardController.java` | `com.team.supplychain.controllers` | Manager dashboard controller |
| `EmployeeDashboardController.java` | `com.team.supplychain.controllers` | Employee dashboard controller |
| `SettingsController.java` | `com.team.supplychain.controllers` | Settings module controller |

### 10.3 FXML Files

| File | Purpose |
|------|---------|
| `AdminDashboard.fxml` | Admin dashboard layout |
| `ManagerDashboard.fxml` | Manager dashboard layout |
| `EmployeeDashboard.fxml` | Employee dashboard layout |
| `Settings.fxml` | Settings module layout |

### 10.4 CSS Files

| File | Purpose |
|------|---------|
| `dashboard-admin.css` | Admin dashboard styling |
| `dashboard-manager.css` | Manager dashboard styling |
| `dashboard-employee.css` | Employee dashboard styling |
| `toast-notifications.css` | Toast notification styling |

### 10.5 Dependencies

**No new Maven dependencies required!**

All features use built-in JavaFX components:
- `javafx.scene.chart.*` - Charts (LineChart, BarChart, PieChart)
- `javafx.animation.*` - Animations (FadeTransition, Timeline)
- `javafx.stage.*` - Toast notification Stage

---

## 11. Summary of Key Decisions

### Role-Based Access:
1. ✅ **Security guard = EMPLOYEE role** - Uses web scanner (separate interface), not desktop app
2. ✅ **Manager approves own POs** - No admin approval workflow, faster operations
3. ✅ **All inventory is public** - No department restrictions, all employees can view
4. ✅ **Supplier as data only** - No login capability for current scope, future enhancement

### Dashboard Design:
5. ✅ **Fixed role-based dashboards** - Not customizable widgets, optimized per role
6. ✅ **Modern JavaFX UI** - Charts, gradients, animations, Material Design inspired
7. ✅ **Real-time updates** - Auto-refresh, WebSocket/polling for live data

### Automated PO System:
8. ✅ **Background thread simulation** - Configurable timers for status progression
9. ✅ **Auto-inventory update** - Inventory quantities increase automatically on PO receipt
10. ✅ **Toast notifications** - Non-intrusive, bottom-right corner, 5-second duration
11. ✅ **Configurable timers** - Admin can set durations, presets for demo/realistic modes
12. ✅ **Timers restart on app restart** - Simple approach, acceptable for project scope

### PO Workflow:
```
Manager creates PO → Auto-approved (PENDING)
   ↓ [10-60 sec]
PREPARING (supplier preparing order)
   ↓ [20-180 sec]
SHIPPED (order in transit)
   ↓ [30-300 sec]
RECEIVED (order arrived)
   ↓ [Auto-trigger]
COMPLETE (inventory updated)
```

---

## 12. Next Steps

### For Development Team:

1. **Week 11:** Implement Phase 1 (Must-Have)
   - Create dashboard layouts
   - Implement PO simulator
   - Basic toast notifications
   - Update role enforcement

2. **Week 12:** Implement Phase 2 (Should-Have)
   - Add charts to dashboards
   - Complete toast notification system
   - Admin settings UI
   - Auto-inventory update

3. **Week 13:** Implement Phase 3 (Nice-to-Have)
   - Animations and polish
   - Advanced charts
   - Dashboard auto-refresh

4. **Week 14-15:** Testing & Demo Preparation
   - Test all roles thoroughly
   - Prepare demo scenario
   - Create presentation slides
   - Practice live demo timing

### For Documentation:

- [ ] Update PROJECT_IDEA_DETAILED.md with final role decisions
- [ ] Update CLAUDE.md with new architecture patterns
- [ ] Create user manual for each role
- [ ] Document API for future enhancements

---

**Document Status:** ✅ Complete and ready for implementation

**Last Updated:** November 15, 2025
**Version:** 1.0.0
**Approved By:** Team (Agile approval)

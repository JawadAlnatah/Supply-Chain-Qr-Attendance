-- ============================================
-- DATABASE MIGRATION SCRIPT
-- Supply Chain Management System
-- Version: 1.1.0
-- Date: 2025-12-06
--
-- This script updates the existing database to support
-- all implemented UI features (Admin, Manager, Employee dashboards)
--
-- IMPORTANT: This script uses conditional checks to avoid errors
-- if columns/tables already exist from previous partial runs.
-- ============================================

-- Use the database
USE supply_chain_qr;

-- Set variables for conditional DDL
SET @db_name = 'supply_chain_qr';

-- ============================================
-- PHASE 1: MODIFY EXISTING TABLES
-- ============================================

-- 1.1 Update PURCHASE_ORDERS table
-- Add missing columns needed by ManagerPurchaseOrders page
-- Add po_number column (conditional)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE table_schema = @db_name AND table_name = 'purchase_orders' AND column_name = 'po_number');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE purchase_orders ADD COLUMN po_number VARCHAR(20) AFTER order_id',
    'SELECT "Column po_number already exists" AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add items_count column (conditional)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE table_schema = @db_name AND table_name = 'purchase_orders' AND column_name = 'items_count');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE purchase_orders ADD COLUMN items_count INT DEFAULT 0 AFTER total_amount',
    'SELECT "Column items_count already exists" AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add requested_by column (conditional)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE table_schema = @db_name AND table_name = 'purchase_orders' AND column_name = 'requested_by');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE purchase_orders ADD COLUMN requested_by INT AFTER created_by',
    'SELECT "Column requested_by already exists" AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Modify status enum
ALTER TABLE purchase_orders
    MODIFY status ENUM('PENDING', 'APPROVED', 'IN_TRANSIT', 'DELIVERED', 'REJECTED', 'CANCELLED') DEFAULT 'PENDING';

-- Add UNIQUE constraint on po_number (conditional)
SET @idx_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.STATISTICS
    WHERE table_schema = @db_name AND table_name = 'purchase_orders' AND index_name = 'unique_po_number');
SET @sql = IF(@idx_exists = 0,
    'ALTER TABLE purchase_orders ADD UNIQUE KEY unique_po_number (po_number)',
    'SELECT "Index unique_po_number already exists" AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add FOREIGN KEY constraint (conditional)
SET @fk_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
    WHERE table_schema = @db_name AND table_name = 'purchase_orders' AND constraint_name = 'fk_po_requested_by');
SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE purchase_orders ADD CONSTRAINT fk_po_requested_by FOREIGN KEY (requested_by) REFERENCES users(user_id)',
    'SELECT "Foreign key fk_po_requested_by already exists" AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1.2 Update ATTENDANCE_RECORDS table
-- Add columns needed by ManagerAttendance and EmployeeAttendance pages
-- Add hours_worked column (conditional)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE table_schema = @db_name AND table_name = 'attendance_records' AND column_name = 'hours_worked');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE attendance_records ADD COLUMN hours_worked DECIMAL(4,2) AFTER check_out_time',
    'SELECT "Column hours_worked already exists" AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add department column (conditional)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE table_schema = @db_name AND table_name = 'attendance_records' AND column_name = 'department');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE attendance_records ADD COLUMN department VARCHAR(100) AFTER employee_id',
    'SELECT "Column department already exists" AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Modify status enum
ALTER TABLE attendance_records
    MODIFY status ENUM('PRESENT', 'ABSENT', 'LATE', 'CHECKED_IN', 'CHECKED_OUT') DEFAULT 'PRESENT';

-- 1.3 Update EMPLOYEES table
-- Add columns needed by ManagerEmployees page
-- Add is_active column (conditional)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE table_schema = @db_name AND table_name = 'employees' AND column_name = 'is_active');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE employees ADD COLUMN is_active BOOLEAN DEFAULT TRUE AFTER hire_date',
    'SELECT "Column is_active already exists" AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add status column (conditional)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE table_schema = @db_name AND table_name = 'employees' AND column_name = 'status');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE employees ADD COLUMN status ENUM(\'ACTIVE\', \'ON_LEAVE\', \'INACTIVE\', \'TERMINATED\') DEFAULT \'ACTIVE\' AFTER is_active',
    'SELECT "Column status already exists" AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Add email column (conditional)
SET @col_exists = (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE table_schema = @db_name AND table_name = 'employees' AND column_name = 'email');
SET @sql = IF(@col_exists = 0,
    'ALTER TABLE employees ADD COLUMN email VARCHAR(100) AFTER phone',
    'SELECT "Column email already exists" AS msg');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 1.4 Drop old supply_requests/requisitions table if exists (will recreate with proper structure)
-- This is necessary because the old structure is incompatible with the new schema
DROP TABLE IF EXISTS requisition_items;
DROP TABLE IF EXISTS supply_requests;
DROP TABLE IF EXISTS requisitions;

-- 1.5 Create REQUISITIONS table with proper structure
-- Supports multiple items per requisition via requisition_items table
CREATE TABLE IF NOT EXISTS requisitions (
    requisition_id INT AUTO_INCREMENT PRIMARY KEY,
    requisition_code VARCHAR(50) UNIQUE NOT NULL,
    requested_by INT NOT NULL,
    supplier_id INT NULL,
    category VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    justification TEXT,
    status VARCHAR(50) DEFAULT 'Pending',
    total_amount DECIMAL(15, 2) DEFAULT 0.00,
    total_items INT DEFAULT 0,
    request_date DATETIME NOT NULL,
    reviewed_by INT NULL,
    review_date DATETIME NULL,
    review_notes TEXT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (requested_by) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL,
    INDEX idx_requested_by (requested_by),
    INDEX idx_status (status),
    INDEX idx_request_date (request_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1.6 Create REQUISITION_ITEMS table
-- Stores multiple items per requisition
CREATE TABLE IF NOT EXISTS requisition_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    requisition_id INT NOT NULL,
    inventory_item_id INT NULL,
    item_name VARCHAR(200) NOT NULL,
    category VARCHAR(100),
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    subtotal DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (requisition_id) REFERENCES requisitions(requisition_id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_item_id) REFERENCES inventory_items(item_id) ON DELETE SET NULL,
    INDEX idx_requisition_id (requisition_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================
-- PHASE 2: CREATE NEW TABLES
-- ============================================

-- 2.1 Create AUDIT_LOGS table
-- For AdminAuditLogs page - tracks all system activities
CREATE TABLE IF NOT EXISTS audit_logs (
    log_id INT PRIMARY KEY AUTO_INCREMENT,
    log_code VARCHAR(20) UNIQUE,  -- LOG0001247, LOG0001248, etc.
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id INT,
    username VARCHAR(50),
    action_type ENUM('CREATE', 'UPDATE', 'DELETE', 'LOGIN', 'LOGOUT', 'BACKUP', 'READ') NOT NULL,
    module VARCHAR(50) NOT NULL,  -- Users, Inventory, Settings, Authentication, Database, etc.
    description TEXT,
    ip_address VARCHAR(45),  -- IPv6 support
    result ENUM('SUCCESS', 'FAILED', 'WARNING') DEFAULT 'SUCCESS',
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_timestamp (timestamp),
    INDEX idx_user (user_id),
    INDEX idx_action_type (action_type),
    INDEX idx_module (module),
    INDEX idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.2 Create SECURITY_INCIDENTS table
-- For AdminSecurity page - tracks security events and threats
CREATE TABLE IF NOT EXISTS security_incidents (
    incident_id INT PRIMARY KEY AUTO_INCREMENT,
    incident_code VARCHAR(20) UNIQUE,  -- SEC001, SEC002, etc.
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    event_type ENUM('FAILED_LOGIN', 'UNAUTHORIZED_ACCESS', 'SUSPICIOUS_ACTIVITY', 'BRUTE_FORCE', 'SQL_INJECTION') NOT NULL,
    username VARCHAR(50),
    user_id INT,
    ip_address VARCHAR(45) NOT NULL,
    location VARCHAR(100),
    severity ENUM('CRITICAL', 'HIGH', 'MEDIUM', 'LOW') NOT NULL,
    status ENUM('OPEN', 'INVESTIGATING', 'RESOLVED') DEFAULT 'OPEN',
    notes TEXT,
    resolved_by INT,
    resolved_at TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE SET NULL,
    FOREIGN KEY (resolved_by) REFERENCES users(user_id),
    INDEX idx_timestamp (timestamp),
    INDEX idx_event_type (event_type),
    INDEX idx_severity (severity),
    INDEX idx_status (status),
    INDEX idx_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.3 Create SYSTEM_SETTINGS table
-- For AdminSystemSettings page - stores system configuration
CREATE TABLE IF NOT EXISTS system_settings (
    setting_id INT PRIMARY KEY AUTO_INCREMENT,
    category ENUM('DATABASE', 'EMAIL', 'SECURITY', 'APPLICATION', 'NOTIFICATION') NOT NULL,
    setting_name VARCHAR(100) NOT NULL,
    setting_key VARCHAR(100) UNIQUE NOT NULL,  -- For programmatic access
    current_value VARCHAR(255) NOT NULL,
    default_value VARCHAR(255) NOT NULL,
    data_type ENUM('STRING', 'INTEGER', 'BOOLEAN', 'DECIMAL') DEFAULT 'STRING',
    description TEXT,
    modified_by INT,
    modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_editable BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (modified_by) REFERENCES users(user_id) ON DELETE SET NULL,
    INDEX idx_category (category),
    INDEX idx_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.4 Create GENERATED_REPORTS table
-- For AdminReports page - tracks generated report files
CREATE TABLE IF NOT EXISTS generated_reports (
    report_id INT PRIMARY KEY AUTO_INCREMENT,
    report_code VARCHAR(20) UNIQUE,  -- RPT001, RPT002, etc.
    report_name VARCHAR(255) NOT NULL,
    report_type ENUM('INVENTORY', 'USERS', 'FINANCIAL', 'ATTENDANCE', 'SECURITY', 'AUDIT', 'PURCHASE_ORDERS', 'SUPPLIERS', 'EMPLOYEES') NOT NULL,
    generated_by INT NOT NULL,
    generated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    file_format ENUM('PDF', 'EXCEL', 'CSV', 'JSON') NOT NULL,
    file_path VARCHAR(500),  -- Server file path or cloud URL
    file_size VARCHAR(20),  -- "2.3 MB", "890 KB", etc.
    status ENUM('READY', 'GENERATING', 'FAILED') DEFAULT 'READY',
    parameters JSON,  -- Store filter params used (date range, filters, etc.)
    download_count INT DEFAULT 0,
    FOREIGN KEY (generated_by) REFERENCES users(user_id),
    INDEX idx_report_type (report_type),
    INDEX idx_generated_date (generated_date),
    INDEX idx_status (status),
    INDEX idx_generated_by (generated_by)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2.5 Create PURCHASE_ORDER_ITEMS table
-- For detailed PO line items (needed by ManagerPurchaseOrders)
CREATE TABLE IF NOT EXISTS purchase_order_items (
    po_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    item_id INT,
    item_name VARCHAR(100),  -- Store name in case item is deleted
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2),
    total_price DECIMAL(10,2),
    FOREIGN KEY (order_id) REFERENCES purchase_orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id) ON DELETE SET NULL,
    INDEX idx_order_id (order_id),
    INDEX idx_item_id (item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- PHASE 3: ADD PERFORMANCE INDEXES
-- ============================================

-- 3.1 Inventory table indexes
CREATE INDEX IF NOT EXISTS idx_inventory_name ON inventory_items(item_name);
CREATE INDEX IF NOT EXISTS idx_inventory_category ON inventory_items(category);

-- 3.2 Purchase order indexes
CREATE INDEX IF NOT EXISTS idx_po_status ON purchase_orders(status);
CREATE INDEX IF NOT EXISTS idx_po_order_date ON purchase_orders(order_date);

-- 3.3 Attendance indexes
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance_records(date);
CREATE INDEX IF NOT EXISTS idx_attendance_employee_date ON attendance_records(employee_id, date);

-- 3.4 User indexes
CREATE INDEX IF NOT EXISTS idx_user_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_user_is_active ON users(is_active);

-- ============================================
-- PHASE 4: INSERT INITIAL DATA
-- ============================================

-- 4.1 Populate SYSTEM_SETTINGS table with default configuration
-- Use INSERT IGNORE to skip if settings already exist
INSERT IGNORE INTO system_settings (category, setting_name, setting_key, current_value, default_value, data_type, description) VALUES
-- Database Settings
('DATABASE', 'Max Connections', 'db.max_connections', '100', '50', 'INTEGER', 'Maximum number of concurrent database connections'),
('DATABASE', 'Connection Timeout', 'db.connection_timeout', '30s', '10s', 'STRING', 'Database connection timeout duration'),
('DATABASE', 'Auto Backup Interval', 'db.auto_backup_interval', '24h', '168h', 'STRING', 'Automatic backup frequency'),

-- Email Settings
('EMAIL', 'SMTP Server', 'email.smtp_server', 'smtp.gmail.com', 'localhost', 'STRING', 'SMTP server hostname'),
('EMAIL', 'SMTP Port', 'email.smtp_port', '587', '25', 'INTEGER', 'SMTP server port number'),
('EMAIL', 'Use TLS', 'email.use_tls', 'Enabled', 'Disabled', 'BOOLEAN', 'Enable TLS encryption for email'),

-- Security Settings
('SECURITY', 'Session Timeout', 'security.session_timeout', '30min', '15min', 'STRING', 'User session inactivity timeout'),
('SECURITY', 'Max Login Attempts', 'security.max_login_attempts', '5', '3', 'INTEGER', 'Maximum failed login attempts before account lock'),
('SECURITY', 'Password Expiry Days', 'security.password_expiry_days', '90', '60', 'INTEGER', 'Number of days before password expires'),

-- Application Settings
('APPLICATION', 'Default Language', 'app.default_language', 'English', 'English', 'STRING', 'Default application language'),
('APPLICATION', 'Date Format', 'app.date_format', 'YYYY-MM-DD', 'MM/DD/YYYY', 'STRING', 'Date display format'),
('APPLICATION', 'Timezone', 'app.timezone', 'Asia/Riyadh', 'UTC', 'STRING', 'Application timezone');

-- ============================================
-- PHASE 5: DATA INTEGRITY CHECKS
-- ============================================

-- 5.1 Generate PO numbers for existing purchase orders (if any)
UPDATE purchase_orders
SET po_number = CONCAT('PO-', YEAR(CURRENT_DATE), '-', LPAD(order_id, 3, '0'))
WHERE po_number IS NULL;

-- 5.2 Set requested_by to created_by for existing POs
UPDATE purchase_orders
SET requested_by = created_by
WHERE requested_by IS NULL AND created_by IS NOT NULL;

-- ============================================
-- MIGRATION COMPLETE
-- ============================================

-- Verify table counts
SELECT
    'users' as table_name, COUNT(*) as record_count FROM users
UNION ALL
SELECT 'employees', COUNT(*) FROM employees
UNION ALL
SELECT 'suppliers', COUNT(*) FROM suppliers
UNION ALL
SELECT 'inventory_items', COUNT(*) FROM inventory_items
UNION ALL
SELECT 'purchase_orders', COUNT(*) FROM purchase_orders
UNION ALL
SELECT 'purchase_order_items', COUNT(*) FROM purchase_order_items
UNION ALL
SELECT 'requisitions', COUNT(*) FROM requisitions
UNION ALL
SELECT 'requisition_items', COUNT(*) FROM requisition_items
UNION ALL
SELECT 'attendance_records', COUNT(*) FROM attendance_records
UNION ALL
SELECT 'audit_logs', COUNT(*) FROM audit_logs
UNION ALL
SELECT 'security_incidents', COUNT(*) FROM security_incidents
UNION ALL
SELECT 'system_settings', COUNT(*) FROM system_settings
UNION ALL
SELECT 'generated_reports', COUNT(*) FROM generated_reports
ORDER BY table_name;

-- Show all tables
SHOW TABLES;

-- Migration complete message
SELECT 'Database migration completed successfully!' as status,
       NOW() as completed_at;

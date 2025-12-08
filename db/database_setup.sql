-- Supply Chain Management System Database Setup
-- Database: supply_chain_qr

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS supply_chain_qr;
USE supply_chain_qr;

-- Drop tables if they exist (for clean setup)
DROP TABLE IF EXISTS attendance_records;
DROP TABLE IF EXISTS supply_requests;
DROP TABLE IF EXISTS purchase_order_items;
DROP TABLE IF EXISTS purchase_orders;
DROP TABLE IF EXISTS inventory_items;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS employees;
DROP TABLE IF EXISTS users;

-- Create Users table
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    role ENUM('ADMIN', 'MANAGER', 'EMPLOYEE', 'SUPPLIER') NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Create Employees table
CREATE TABLE employees (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT UNIQUE NOT NULL,
    department VARCHAR(100),
    position VARCHAR(100),
    phone VARCHAR(20),
    qr_code VARCHAR(255) UNIQUE,
    hire_date DATE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- Create Suppliers table  
CREATE TABLE suppliers (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address TEXT,
    rating DECIMAL(2,1) CHECK (rating >= 0 AND rating <= 5),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Inventory Items table
CREATE TABLE inventory_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    item_name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    quantity INT DEFAULT 0 CHECK (quantity >= 0),
    unit_price DECIMAL(10,2),
    reorder_level INT DEFAULT 10,
    reorder_quantity INT DEFAULT 50,
    supplier_id INT,
    location VARCHAR(100),
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id) ON DELETE SET NULL
);

-- Create Purchase Orders table
CREATE TABLE purchase_orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_id INT NOT NULL,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expected_delivery DATE,
    status ENUM('PENDING', 'APPROVED', 'SHIPPED', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    total_amount DECIMAL(12,2),
    created_by INT,
    approved_by INT,
    notes TEXT,
    FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id),
    FOREIGN KEY (approved_by) REFERENCES users(user_id)
);

-- Create Purchase Order Items table
CREATE TABLE purchase_order_items (
    order_item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2),
    total_price DECIMAL(12,2),
    FOREIGN KEY (order_id) REFERENCES purchase_orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id)
);

-- Create Supply Requests table
CREATE TABLE supply_requests (
    request_id INT PRIMARY KEY AUTO_INCREMENT,
    requester_id INT NOT NULL,
    item_id INT NOT NULL,
    quantity_requested INT NOT NULL CHECK (quantity_requested > 0),
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    needed_by DATE,
    status ENUM('PENDING', 'APPROVED', 'REJECTED', 'FULFILLED') DEFAULT 'PENDING',
    priority ENUM('LOW', 'MEDIUM', 'HIGH') DEFAULT 'MEDIUM',
    reason TEXT,
    approved_by INT,
    approval_date TIMESTAMP NULL,
    notes TEXT,
    FOREIGN KEY (requester_id) REFERENCES users(user_id),
    FOREIGN KEY (item_id) REFERENCES inventory_items(item_id),
    FOREIGN KEY (approved_by) REFERENCES users(user_id)
);

-- Create Attendance Records table
CREATE TABLE attendance_records (
    record_id INT PRIMARY KEY AUTO_INCREMENT,
    employee_id INT NOT NULL,
    check_in_time TIMESTAMP NULL,
    check_out_time TIMESTAMP NULL,
    date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'HALF_DAY') DEFAULT 'PRESENT',
    location VARCHAR(100),
    qr_scan_data TEXT,
    notes TEXT,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id),
    UNIQUE KEY unique_attendance (employee_id, date)
);

-- Insert sample data for testing

-- Insert sample users (password is 'password123' for all)
INSERT INTO users (username, password_hash, email, role, first_name, last_name) VALUES
('admin', 'password123', 'admin@company.com', 'ADMIN', 'System', 'Administrator'),
('manager1', 'password123', 'manager1@company.com', 'MANAGER', 'John', 'Smith'),
('employee1', 'password123', 'emp1@company.com', 'EMPLOYEE', 'Jane', 'Doe'),
('employee2', 'password123', 'emp2@company.com', 'EMPLOYEE', 'Bob', 'Johnson');

-- Insert sample employees
INSERT INTO employees (user_id, department, position, phone, qr_code, hire_date) VALUES
(1, 'IT', 'System Administrator', '555-0001', 'QR_ADMIN_001', '2020-01-15'),
(2, 'Operations', 'Operations Manager', '555-0002', 'QR_MGR_001', '2019-06-20'),
(3, 'Warehouse', 'Warehouse Staff', '555-0003', 'QR_EMP_001', '2021-03-10'),
(4, 'Warehouse', 'Inventory Clerk', '555-0004', 'QR_EMP_002', '2022-01-05');

-- Insert sample suppliers
INSERT INTO suppliers (supplier_name, contact_person, email, phone, address, rating) VALUES
('Tech Supplies Inc.', 'Mike Wilson', 'mike@techsupplies.com', '555-1001', '123 Tech Street', 4.5),
('Office World', 'Sarah Brown', 'sarah@officeworld.com', '555-1002', '456 Office Ave', 4.0),
('Hardware Plus', 'Tom Davis', 'tom@hardwareplus.com', '555-1003', '789 Hardware Blvd', 3.8);

-- Insert sample inventory items
INSERT INTO inventory_items (item_name, description, category, quantity, unit_price, reorder_level, supplier_id, location) VALUES
('Desktop Computer', 'Dell OptiPlex 3080', 'Electronics', 25, 599.99, 5, 1, 'Warehouse A-1'),
('Office Chair', 'Ergonomic office chair', 'Furniture', 50, 149.99, 10, 2, 'Warehouse B-2'),
('Printer Paper', 'A4 80gsm white paper (500 sheets)', 'Stationery', 200, 4.99, 50, 2, 'Storage Room 1'),
('USB Mouse', 'Wireless optical mouse', 'Electronics', 75, 12.99, 20, 1, 'Warehouse A-2'),
('Desk Lamp', 'LED desk lamp', 'Furniture', 30, 29.99, 10, 3, 'Warehouse B-1');

-- Create indexes for better performance
CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_employee_qr ON employees(qr_code);
CREATE INDEX idx_item_name ON inventory_items(item_name);
CREATE INDEX idx_attendance_date ON attendance_records(date);
CREATE INDEX idx_attendance_employee ON attendance_records(employee_id);

COMMIT;

-- Display confirmation
SELECT 'Database setup completed successfully!' AS Status;

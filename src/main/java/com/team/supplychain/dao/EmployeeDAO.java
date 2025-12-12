package com.team.supplychain.dao;

import com.team.supplychain.models.Employee;
import com.team.supplychain.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDAO {
    
    public boolean createEmployee(Employee employee) {
        String sql = "INSERT INTO employees (user_id, department, position, phone, " +
                    "qr_code, hire_date) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, employee.getUserId());
            stmt.setString(2, employee.getDepartment());
            stmt.setString(3, employee.getPosition());
            stmt.setString(4, employee.getPhone());
            stmt.setString(5, employee.getQrCode());
            stmt.setDate(6, Date.valueOf(employee.getHireDate()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    employee.setEmployeeId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public Employee getEmployeeById(int employeeId) {
        String sql = "SELECT e.*, u.first_name, u.last_name, u.email " +
                    "FROM employees e " +
                    "JOIN users u ON e.user_id = u.user_id " +
                    "WHERE e.employee_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return extractEmployeeFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Employee getEmployeeByQRCode(String qrCode) {
        String sql = "SELECT e.*, u.first_name, u.last_name, u.email " +
                    "FROM employees e " +
                    "JOIN users u ON e.user_id = u.user_id " +
                    "WHERE e.qr_code = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, qrCode);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractEmployeeFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Employee getEmployeeByUserId(int userId) {
        String sql = "SELECT e.*, u.first_name, u.last_name, u.email " +
                    "FROM employees e " +
                    "JOIN users u ON e.user_id = u.user_id " +
                    "WHERE e.user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractEmployeeFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> employees = new ArrayList<>();
        String sql = "SELECT e.*, u.first_name, u.last_name, u.email " +
                    "FROM employees e " +
                    "JOIN users u ON e.user_id = u.user_id " +
                    "ORDER BY u.last_name, u.first_name";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                employees.add(extractEmployeeFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return employees;
    }
    
    public boolean updateEmployee(Employee employee) {
        String sql = "UPDATE employees SET department = ?, position = ?, " +
                    "phone = ?, qr_code = ? WHERE employee_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, employee.getDepartment());
            stmt.setString(2, employee.getPosition());
            stmt.setString(3, employee.getPhone());
            stmt.setString(4, employee.getQrCode());
            stmt.setInt(5, employee.getEmployeeId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean deleteEmployee(int employeeId) {
        String sql = "DELETE FROM employees WHERE employee_id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, employeeId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private Employee extractEmployeeFromResultSet(ResultSet rs) throws SQLException {
        Employee employee = new Employee();
        employee.setEmployeeId(rs.getInt("employee_id"));
        employee.setUserId(rs.getInt("user_id"));
        employee.setDepartment(rs.getString("department"));
        employee.setPosition(rs.getString("position"));
        employee.setPhone(rs.getString("phone"));
        employee.setQrCode(rs.getString("qr_code"));
        
        Date hireDate = rs.getDate("hire_date");
        if (hireDate != null) {
            employee.setHireDate(hireDate.toLocalDate());
        }
        
        // User info from joined query
        employee.setFirstName(rs.getString("first_name"));
        employee.setLastName(rs.getString("last_name"));
        employee.setEmail(rs.getString("email"));
        
        return employee;
    }
}
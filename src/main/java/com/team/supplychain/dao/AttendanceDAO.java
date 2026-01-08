package com.team.supplychain.dao;

import com.team.supplychain.enums.AttendanceStatus;
import com.team.supplychain.models.Attendance;
import com.team.supplychain.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Attendance Records
 * Handles all database operations related to employee attendance tracking
 */
public class AttendanceDAO {

    // Work day start time for determining LATE status
    private static final LocalTime WORK_START_TIME = LocalTime.of(8, 30); // 8:30 AM

    /**
     * Record employee check-in
     * Creates new attendance record for today with current timestamp
     * Determines status (PRESENT or LATE) based on check-in time
     *
     * @param employeeId Employee's ID
     * @param qrCode QR code scanned
     * @param location Check-in location (e.g., "Main Entrance")
     * @return Created Attendance object with record ID, or null if failed
     */
    public Attendance checkIn(int employeeId, String qrCode, String location) {
        String sql = "INSERT INTO attendance_records (employee_id, check_in_time, date, status, location, qr_scan_data) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            LocalDateTime now = LocalDateTime.now();
            LocalDate today = LocalDate.now();
            
            // Determine status: LATE if after 8:30 AM, otherwise PRESENT
            LocalTime checkInTime = now.toLocalTime();
            AttendanceStatus status = checkInTime.isAfter(WORK_START_TIME) ?
                    AttendanceStatus.LATE : AttendanceStatus.PRESENT;

            stmt.setInt(1, employeeId);
            stmt.setTimestamp(2, Timestamp.valueOf(now));
            stmt.setDate(3, Date.valueOf(today));
            stmt.setString(4, status.name());
            stmt.setString(5, location);
            stmt.setString(6, qrCode);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    int recordId = rs.getInt(1);

                    // Create and return Attendance object
                    Attendance attendance = new Attendance();
                    attendance.setRecordId(recordId);
                    attendance.setEmployeeId(employeeId);
                    attendance.setCheckInTime(now);
                    attendance.setDate(today);
                    attendance.setStatus(status);
                    attendance.setLocation(location);
                    attendance.setQrScanData(qrCode);

                    System.out.println("Check-in successful: Employee " + employeeId + " at " + now);
                    return attendance;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Record employee check-out
     * Updates today's attendance record with check-out timestamp
     *
     * @param employeeId Employee's ID
     * @return true if successful, false otherwise
     */
    public boolean checkOut(int employeeId) {
        String sql = "UPDATE attendance_records SET check_out_time = ? " +
                    "WHERE employee_id = ? AND date = CURDATE() AND check_out_time IS NULL";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            LocalDateTime now = LocalDateTime.now();
            stmt.setTimestamp(1, Timestamp.valueOf(now));
            stmt.setInt(2, employeeId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Check-out successful: Employee " + employeeId + " at " + now);
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Get today's attendance record for an employee
     *
     * @param employeeId Employee's ID
     * @return Attendance object for today, or null if not checked in
     */
    public Attendance getTodayAttendance(int employeeId) {
        String sql = "SELECT * FROM attendance_records WHERE employee_id = ? AND date = CURDATE()";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return extractAttendanceFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get attendance records for a specific week
     *
     * @param employeeId Employee's ID
     * @param weekStartDate Start date of the week (typically Monday)
     * @return List of Attendance records for the week (max 7 days)
     */
    public List<Attendance> getWeekAttendance(int employeeId, LocalDate weekStartDate) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance_records " +
                    "WHERE employee_id = ? AND date >= ? AND date < DATE_ADD(?, INTERVAL 7 DAY) " +
                    "ORDER BY date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(weekStartDate));
            stmt.setDate(3, Date.valueOf(weekStartDate));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    /**
     * Get attendance records for a specific month
     *
     * @param employeeId Employee's ID
     * @param year Year (e.g., 2025)
     * @param month Month (1-12)
     * @return List of Attendance records for the month
     */
    public List<Attendance> getMonthAttendance(int employeeId, int year, int month) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance_records " +
                    "WHERE employee_id = ? AND YEAR(date) = ? AND MONTH(date) = ? " +
                    "ORDER BY date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setInt(2, year);
            stmt.setInt(3, month);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    /**
     * Get attendance records within a date range
     *
     * @param employeeId Employee's ID
     * @param startDate Start date (inclusive)
     * @param endDate End date (inclusive)
     * @return List of Attendance records ordered by date descending
     */
    public List<Attendance> getAttendanceByDateRange(int employeeId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance_records " +
                    "WHERE employee_id = ? AND date BETWEEN ? AND ? " +
                    "ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            stmt.setDate(2, Date.valueOf(startDate));
            stmt.setDate(3, Date.valueOf(endDate));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    /**
     * Get all attendance records for an employee (for admin/manager view)
     *
     * @param employeeId Employee's ID
     * @return List of all Attendance records ordered by date descending
     */
    public List<Attendance> getAllAttendance(int employeeId) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT * FROM attendance_records WHERE employee_id = ? ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, employeeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                attendanceList.add(extractAttendanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attendanceList;
    }

    /**
     * Check if employee has already checked in today
     *
     * @param employeeId Employee's ID
     * @return true if already checked in (no check-out yet), false otherwise
     */
    public boolean isCheckedInToday(int employeeId) {
        Attendance today = getTodayAttendance(employeeId);
        return today != null && today.isCheckedIn();
    }

    /**
     * Get attendance statistics for a date range
     * Useful for generating reports and analytics
     *
     * @param employeeId Employee's ID
     * @param startDate Start date
     * @param endDate End date
     * @return AttendanceStatistics object with counts and averages
     */
    public AttendanceStatistics getStatistics(int employeeId, LocalDate startDate, LocalDate endDate) {
        List<Attendance> records = getAttendanceByDateRange(employeeId, startDate, endDate);

        long presentDays = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
                .count();

        long lateDays = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.LATE)
                .count();

        long absentDays = records.stream()
                .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
                .count();

        double totalMinutes = records.stream()
                .mapToLong(a -> a.getWorkDuration().toMinutes())
                .sum();

        double avgHoursPerDay = records.isEmpty() ? 0 : (totalMinutes / 60.0) / records.size();

        return new AttendanceStatistics(presentDays, lateDays, absentDays, avgHoursPerDay, totalMinutes / 60.0);
    }

    /**
     * Extract Attendance object from ResultSet
     * Helper method for mapping database rows to Attendance objects
     *
     * @param rs ResultSet positioned at a valid row
     * @return Attendance object
     * @throws SQLException if database access error occurs
     */
    private Attendance extractAttendanceFromResultSet(ResultSet rs) throws SQLException {
        Attendance attendance = new Attendance();

        attendance.setRecordId(rs.getInt("record_id"));
        attendance.setEmployeeId(rs.getInt("employee_id"));

        Timestamp checkInTs = rs.getTimestamp("check_in_time");
        if (checkInTs != null) {
            attendance.setCheckInTime(checkInTs.toLocalDateTime());
        }

        Timestamp checkOutTs = rs.getTimestamp("check_out_time");
        if (checkOutTs != null) {
            attendance.setCheckOutTime(checkOutTs.toLocalDateTime());
        }

        Date date = rs.getDate("date");
        if (date != null) {
            attendance.setDate(date.toLocalDate());
        }

        String statusStr = rs.getString("status");
        if (statusStr != null) {
            attendance.setStatus(AttendanceStatus.valueOf(statusStr));
        }

        attendance.setLocation(rs.getString("location"));
        attendance.setQrScanData(rs.getString("qr_scan_data"));
        attendance.setNotes(rs.getString("notes"));

        return attendance;
    }

    /**
     * Get all attendance records across all employees with employee details
     * Joins with employees and users tables to get employee names and departments
     *
     * @return List of all attendance records with employee information
     */
    public List<Attendance> getAllAttendanceWithEmployeeDetails() {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT a.*, e.department, e.position, u.first_name, u.last_name " +
                    "FROM attendance_records a " +
                    "JOIN employees e ON a.employee_id = e.employee_id " +
                    "JOIN users u ON e.user_id = u.user_id " +
                    "ORDER BY a.date DESC, a.check_in_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = extractAttendanceFromResultSet(rs);
                // Set employee details from joined query
                attendance.setEmployeeFirstName(rs.getString("first_name"));
                attendance.setEmployeeLastName(rs.getString("last_name"));
                attendance.setDepartment(rs.getString("department"));
                attendance.setPosition(rs.getString("position"));
                attendanceList.add(attendance);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return attendanceList;
    }

    /**
     * Get all attendance records for a specific date with employee details
     * Used by managers to view attendance for a particular day
     *
     * @param date The date to query attendance records for
     * @return List of attendance records for the specified date
     */
    public List<Attendance> getAttendanceForDate(LocalDate date) {
        List<Attendance> attendanceList = new ArrayList<>();
        String sql = "SELECT a.*, e.department, e.position, u.first_name, u.last_name " +
                    "FROM attendance_records a " +
                    "JOIN employees e ON a.employee_id = e.employee_id " +
                    "JOIN users u ON e.user_id = u.user_id " +
                    "WHERE a.date = ? " +
                    "ORDER BY a.check_in_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Attendance attendance = extractAttendanceFromResultSet(rs);
                // Set employee details from joined query
                attendance.setEmployeeFirstName(rs.getString("first_name"));
                attendance.setEmployeeLastName(rs.getString("last_name"));
                attendance.setDepartment(rs.getString("department"));
                attendance.setPosition(rs.getString("position"));
                attendanceList.add(attendance);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return attendanceList;
    }

    /**
     * Inner class for attendance statistics
     * Used by getStatistics() method
     */
    public static class AttendanceStatistics {
        public final long presentDays;
        public final long lateDays;
        public final long absentDays;
        public final double avgHoursPerDay;
        public final double totalHours;

        public AttendanceStatistics(long presentDays, long lateDays, long absentDays,
                                   double avgHoursPerDay, double totalHours) {
            this.presentDays = presentDays;
            this.lateDays = lateDays;
            this.absentDays = absentDays;
            this.avgHoursPerDay = avgHoursPerDay;
            this.totalHours = totalHours;
        }
    }
}

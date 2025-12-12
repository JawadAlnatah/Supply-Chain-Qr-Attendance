package com.team.supplychain.models;

import com.team.supplychain.enums.AttendanceStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;

/**
 * Model class for attendance records
 * Maps to attendance_records table in database
 */
public class Attendance {

    // Database columns
    private int recordId;
    private int employeeId;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDate date;
    private AttendanceStatus status;
    private String location;
    private String qrScanData;
    private String notes;

    // Joined query fields (from employees + users tables)
    private String employeeFirstName;
    private String employeeLastName;
    private String department;
    private String position;

    // Constructors
    public Attendance() {
    }

    public Attendance(int employeeId, LocalDateTime checkInTime, LocalDate date, AttendanceStatus status) {
        this.employeeId = employeeId;
        this.checkInTime = checkInTime;
        this.date = date;
        this.status = status;
    }

    // Calculated/Helper methods

    /**
     * Calculate work duration between check-in and check-out
     * @return Duration of work, or ZERO if not checked out yet
     */
    public Duration getWorkDuration() {
        if (checkInTime != null && checkOutTime != null) {
            return Duration.between(checkInTime, checkOutTime);
        }
        return Duration.ZERO;
    }

    /**
     * Get formatted work hours (e.g., "8h 30m")
     * @return Formatted string of hours and minutes worked
     */
    public String getFormattedHours() {
        Duration duration = getWorkDuration();
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours == 0 && minutes == 0) {
            return "—";
        }
        return String.format("%dh %dm", hours, minutes);
    }

    /**
     * Check if employee is currently checked in (not checked out yet)
     * @return true if checked in but not checked out
     */
    public boolean isCheckedIn() {
        return checkInTime != null && checkOutTime == null;
    }

    /**
     * Get full employee name
     * @return First name + Last name
     */
    public String getEmployeeFullName() {
        if (employeeFirstName != null && employeeLastName != null) {
            return employeeFirstName + " " + employeeLastName;
        }
        return "";
    }

    /**
     * Format check-in time for display
     * @return Formatted time string (e.g., "8:30 AM")
     */
    public String getFormattedCheckInTime() {
        if (checkInTime != null) {
            return checkInTime.format(DateTimeFormatter.ofPattern("h:mm a"));
        }
        return "—";
    }

    /**
     * Format check-out time for display
     * @return Formatted time string (e.g., "5:00 PM")
     */
    public String getFormattedCheckOutTime() {
        if (checkOutTime != null) {
            return checkOutTime.format(DateTimeFormatter.ofPattern("h:mm a"));
        }
        return "In Progress";
    }

    // Getters and Setters

    public int getRecordId() {
        return recordId;
    }

    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getCheckInTime() {
        return checkInTime;
    }

    public void setCheckInTime(LocalDateTime checkInTime) {
        this.checkInTime = checkInTime;
    }

    public LocalDateTime getCheckOutTime() {
        return checkOutTime;
    }

    public void setCheckOutTime(LocalDateTime checkOutTime) {
        this.checkOutTime = checkOutTime;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public AttendanceStatus getStatus() {
        return status;
    }

    public void setStatus(AttendanceStatus status) {
        this.status = status;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getQrScanData() {
        return qrScanData;
    }

    public void setQrScanData(String qrScanData) {
        this.qrScanData = qrScanData;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getEmployeeFirstName() {
        return employeeFirstName;
    }

    public void setEmployeeFirstName(String employeeFirstName) {
        this.employeeFirstName = employeeFirstName;
    }

    public String getEmployeeLastName() {
        return employeeLastName;
    }

    public void setEmployeeLastName(String employeeLastName) {
        this.employeeLastName = employeeLastName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "Attendance{" +
                "recordId=" + recordId +
                ", employeeId=" + employeeId +
                ", date=" + date +
                ", checkInTime=" + checkInTime +
                ", checkOutTime=" + checkOutTime +
                ", status=" + status +
                ", location='" + location + '\'' +
                '}';
    }
}

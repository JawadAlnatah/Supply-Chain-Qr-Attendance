package com.team.supplychain.api;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.team.supplychain.dao.AttendanceDAO;
import com.team.supplychain.dao.EmployeeDAO;
import com.team.supplychain.models.Attendance;
import com.team.supplychain.models.Employee;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * REST API Servlet for QR Code Attendance Scanning
 * Handles employee check-in and check-out via QR code
 *
 * Endpoint: POST /api/attendance/scan
 * Request Body: {"qrCode": "EMP-00001-ABC123", "action": "checkin" or "checkout", "location": "Main Entrance"}
 * Response: JSON with employee details and attendance status
 */
public class AttendanceServlet extends HttpServlet {

    private AttendanceDAO attendanceDAO;
    private EmployeeDAO employeeDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        attendanceDAO = new AttendanceDAO();
        employeeDAO = new EmployeeDAO();
        gson = new Gson();
        System.out.println("AttendanceServlet initialized");
    }

    /**
     * Handle OPTIONS request for CORS preflight
     */
    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Handle POST request for QR code scanning
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter out = resp.getWriter();
        JsonObject response = new JsonObject();

        try {
            // Parse request body
            BufferedReader reader = req.getReader();
            JsonObject requestData = gson.fromJson(reader, JsonObject.class);

            if (requestData == null || !requestData.has("qrCode")) {
                response.addProperty("success", false);
                response.addProperty("message", "QR code is required");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(response));
                return;
            }

            String qrCode = requestData.get("qrCode").getAsString();
            String action = requestData.has("action") ? requestData.get("action").getAsString() : "checkin";
            String location = requestData.has("location") ? requestData.get("location").getAsString() : "Main Entrance";

            // Validate inputs
            if (qrCode == null || qrCode.trim().isEmpty()) {
                response.addProperty("success", false);
                response.addProperty("message", "Invalid QR code");
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(response));
                return;
            }

            // Lookup employee by QR code
            Employee employee = employeeDAO.getEmployeeByQRCode(qrCode.trim());

            if (employee == null) {
                response.addProperty("success", false);
                response.addProperty("message", "Employee not found. Invalid QR code.");
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(response));
                return;
            }

            // Perform check-in or check-out
            if ("checkout".equalsIgnoreCase(action)) {
                handleCheckOut(employee, response);
            } else {
                handleCheckIn(employee, qrCode, location, response);
            }

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(gson.toJson(response));

        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("success", false);
            response.addProperty("message", "Server error: " + e.getMessage());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(gson.toJson(response));
        } finally {
            out.flush();
        }
    }

    /**
     * Handle employee check-in
     */
    private void handleCheckIn(Employee employee, String qrCode, String location, JsonObject response) {
        // Check if already checked in today
        Attendance todayAttendance = attendanceDAO.getTodayAttendance(employee.getEmployeeId());

        if (todayAttendance != null && todayAttendance.isCheckedIn()) {
            response.addProperty("success", false);
            response.addProperty("message", "Already checked in today at " + todayAttendance.getFormattedCheckInTime());
            response.addProperty("employeeName", employee.getFullName());
            response.addProperty("employeeId", employee.getEmployeeId());
            response.addProperty("department", employee.getDepartment());
            response.addProperty("checkInTime", todayAttendance.getFormattedCheckInTime());
            return;
        }

        if (todayAttendance != null && !todayAttendance.isCheckedIn()) {
            response.addProperty("success", false);
            response.addProperty("message", "Already completed attendance for today. Checked out at " +
                todayAttendance.getFormattedCheckOutTime());
            response.addProperty("employeeName", employee.getFullName());
            response.addProperty("employeeId", employee.getEmployeeId());
            return;
        }

        // Perform check-in
        Attendance attendance = attendanceDAO.checkIn(employee.getEmployeeId(), qrCode, location);

        if (attendance != null) {
            response.addProperty("success", true);
            response.addProperty("message", "Check-in successful!");
            response.addProperty("employeeName", employee.getFullName());
            response.addProperty("employeeId", employee.getEmployeeId());
            response.addProperty("department", employee.getDepartment());
            response.addProperty("position", employee.getPosition());
            response.addProperty("checkInTime", attendance.getFormattedCheckInTime());
            response.addProperty("status", attendance.getStatus().toString());
            response.addProperty("location", location);

            System.out.println("Check-in: " + employee.getFullName() + " at " + attendance.getFormattedCheckInTime());
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Failed to record check-in. Please try again.");
        }
    }

    /**
     * Handle employee check-out
     */
    private void handleCheckOut(Employee employee, JsonObject response) {
        // Check if employee is checked in today
        Attendance todayAttendance = attendanceDAO.getTodayAttendance(employee.getEmployeeId());

        if (todayAttendance == null) {
            response.addProperty("success", false);
            response.addProperty("message", "Not checked in today. Please check in first.");
            response.addProperty("employeeName", employee.getFullName());
            response.addProperty("employeeId", employee.getEmployeeId());
            return;
        }

        if (!todayAttendance.isCheckedIn()) {
            response.addProperty("success", false);
            response.addProperty("message", "Already checked out at " + todayAttendance.getFormattedCheckOutTime());
            response.addProperty("employeeName", employee.getFullName());
            response.addProperty("employeeId", employee.getEmployeeId());
            response.addProperty("hoursWorked", todayAttendance.getFormattedHours());
            return;
        }

        // Perform check-out
        boolean success = attendanceDAO.checkOut(employee.getEmployeeId());

        if (success) {
            // Reload to get updated data
            Attendance updatedAttendance = attendanceDAO.getTodayAttendance(employee.getEmployeeId());

            response.addProperty("success", true);
            response.addProperty("message", "Check-out successful!");
            response.addProperty("employeeName", employee.getFullName());
            response.addProperty("employeeId", employee.getEmployeeId());
            response.addProperty("department", employee.getDepartment());
            response.addProperty("position", employee.getPosition());
            response.addProperty("checkInTime", updatedAttendance.getFormattedCheckInTime());
            response.addProperty("checkOutTime", updatedAttendance.getFormattedCheckOutTime());
            response.addProperty("hoursWorked", updatedAttendance.getFormattedHours());

            System.out.println("Check-out: " + employee.getFullName() + " - " + updatedAttendance.getFormattedHours());
        } else {
            response.addProperty("success", false);
            response.addProperty("message", "Failed to record check-out. Please try again.");
        }
    }

    /**
     * Set CORS headers to allow cross-origin requests
     */
    private void setCorsHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        resp.setHeader("Access-Control-Max-Age", "3600");
    }

    /**
     * Handle GET request for testing
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setCorsHeaders(resp);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        JsonObject response = new JsonObject();
        response.addProperty("status", "online");
        response.addProperty("message", "Attendance Scanner API is running");
        response.addProperty("endpoint", "POST /api/attendance/scan");
        response.addProperty("version", "1.0");

        PrintWriter out = resp.getWriter();
        out.print(gson.toJson(response));
        out.flush();
    }
}

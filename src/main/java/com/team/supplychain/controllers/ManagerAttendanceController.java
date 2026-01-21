package com.team.supplychain.controllers;

import com.team.supplychain.dao.AttendanceDAO;
import com.team.supplychain.enums.AttendanceStatus;
import com.team.supplychain.models.Attendance;
import com.team.supplychain.models.User;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for the Manager Attendance Tracking view
 * Displays all attendance records from database with statistics for selected date
 */
public class ManagerAttendanceController {

    @FXML private Label totalEmployeesLabel;
    @FXML private Label presentLabel;
    @FXML private Label absentLabel;
    @FXML private Label lateLabel;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private TextField searchField;
    @FXML private Button todayButton;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;

    @FXML private TableView<Attendance> attendanceTable;
    @FXML private TableColumn<Attendance, Integer> employeeIdColumn;
    @FXML private TableColumn<Attendance, String> employeeNameColumn;
    @FXML private TableColumn<Attendance, String> departmentColumn;
    @FXML private TableColumn<Attendance, String> checkInColumn;
    @FXML private TableColumn<Attendance, String> checkOutColumn;
    @FXML private TableColumn<Attendance, String> hoursWorkedColumn;
    @FXML private TableColumn<Attendance, String> statusColumn;

    private User currentUser;
    private ObservableList<Attendance> attendanceData;
    private final AttendanceDAO attendanceDAO = new AttendanceDAO();

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        System.out.println("ManagerAttendanceController initialized");
        attendanceData = FXCollections.observableArrayList();
        setupTable();
        setupFilters();

        // Initialize DatePicker to today
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
            // Wire DatePicker to reload stats when date changes
            datePicker.setOnAction(e -> {
                LocalDate selectedDate = datePicker.getValue();
                if (selectedDate != null) {
                    loadStatsForDate(selectedDate);
                }
            });
        }

        // Load all attendance records and stats for today
        loadAllAttendanceFromDatabase();
        loadStatsForDate(LocalDate.now());
    }

    /**
     * Set up table columns to use Attendance model fields
     */
    private void setupTable() {
        if (attendanceTable == null) return;

        // Employee ID column
        employeeIdColumn.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getEmployeeId()).asObject());

        // Employee Name column - use getEmployeeFullName() helper method
        employeeNameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getEmployeeFullName()));

        // Department column
        departmentColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDepartment()));

        // Check In column - use formatted time
        checkInColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getFormattedCheckInTime()));

        // Check Out column - use formatted time
        checkOutColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getFormattedCheckOutTime()));

        // Hours Worked column - use formatted hours
        hoursWorkedColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getFormattedHours()));

        // Status column with color-coded labels
        statusColumn.setCellValueFactory(cellData -> {
            AttendanceStatus status = cellData.getValue().getStatus();
            return new SimpleStringProperty(status != null ? status.name() : "N/A");
        });

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    label.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; " +
                                 "-fx-font-size: 11px; -fx-font-weight: bold; " +
                                 getStatusStyle(status));
                    setGraphic(label);
                    setAlignment(Pos.CENTER);
                }
            }

            private String getStatusStyle(String status) {
                switch (status) {
                    case "PRESENT":
                        return "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
                    case "LATE":
                        return "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
                    case "ABSENT":
                        return "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
                    default:
                        return "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
                }
            }
        });

        attendanceTable.setItems(attendanceData);
    }

    /**
     * Set up filter dropdowns
     */
    private void setupFilters() {
        if (departmentFilter != null) {
            departmentFilter.getItems().addAll("All Departments", "Production", "Quality Control",
                "Packaging", "Warehouse", "Procurement", "Administration", "Finance");
            departmentFilter.setValue("All Departments");
        }
    }

    /**
     * Load all attendance records from database using background thread
     * Shows ALL attendance records across all dates
     */
    private void loadAllAttendanceFromDatabase() {
        Task<List<Attendance>> loadTask = new Task<>() {
            @Override
            protected List<Attendance> call() {
                // Query database on background thread
                return attendanceDAO.getAllAttendanceWithEmployeeDetails();
            }
        };

        loadTask.setOnSucceeded(event -> {
            // Update UI on JavaFX thread
            List<Attendance> records = loadTask.getValue();
            if (records != null) {
                attendanceData.clear();
                attendanceData.addAll(records);
                System.out.println("Loaded " + records.size() + " attendance records from database");
            } else {
                System.err.println("Failed to load attendance records - null result");
                showError("Database Error", "Failed to load attendance records from database");
            }
        });

        loadTask.setOnFailed(event -> {
            // Handle errors
            Throwable error = loadTask.getException();
            error.printStackTrace();
            showError("Database Error", "Failed to load attendance: " + error.getMessage());
        });

        // Start background task
        new Thread(loadTask).start();
    }

    /**
     * Load statistics for a specific date
     * Stats show ONLY the selected date's data
     *
     * @param date The date to calculate stats for
     */
    private void loadStatsForDate(LocalDate date) {
        Task<List<Attendance>> statsTask = new Task<>() {
            @Override
            protected List<Attendance> call() {
                // Query attendance records for the selected date only
                return attendanceDAO.getAttendanceForDate(date);
            }
        };

        statsTask.setOnSucceeded(event -> {
            // Calculate stats on JavaFX thread
            List<Attendance> dateRecords = statsTask.getValue();
            if (dateRecords != null) {
                updateStats(dateRecords);
                System.out.println("Loaded stats for " + date + ": " + dateRecords.size() + " records");
            } else {
                System.err.println("Failed to load stats for date: " + date);
                clearStats();
            }
        });

        statsTask.setOnFailed(event -> {
            // Handle errors
            Throwable error = statsTask.getException();
            error.printStackTrace();
            showError("Database Error", "Failed to load statistics: " + error.getMessage());
            clearStats();
        });

        // Start background task
        new Thread(statsTask).start();
    }

    /**
     * Update statistics labels based on attendance records
     *
     * @param records List of attendance records for the selected date
     */
    private void updateStats(List<Attendance> records) {
        int total = records.size();

        // Count PRESENT status (excludes LATE and ABSENT)
        long present = records.stream()
            .filter(a -> a.getStatus() == AttendanceStatus.PRESENT)
            .count();

        // Count LATE status
        long late = records.stream()
            .filter(a -> a.getStatus() == AttendanceStatus.LATE)
            .count();

        // Count ABSENT status
        long absent = records.stream()
            .filter(a -> a.getStatus() == AttendanceStatus.ABSENT)
            .count();

        // Update labels
        if (totalEmployeesLabel != null) totalEmployeesLabel.setText(String.valueOf(total));
        if (presentLabel != null) presentLabel.setText(String.valueOf(present));
        if (absentLabel != null) absentLabel.setText(String.valueOf(absent));
        if (lateLabel != null) lateLabel.setText(String.valueOf(late));
    }

    /**
     * Clear all statistics labels
     */
    private void clearStats() {
        if (totalEmployeesLabel != null) totalEmployeesLabel.setText("0");
        if (presentLabel != null) presentLabel.setText("0");
        if (absentLabel != null) absentLabel.setText("0");
        if (lateLabel != null) lateLabel.setText("0");
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Set DatePicker to today's date
     */
    @FXML
    private void handleToday() {
        if (datePicker != null) {
            datePicker.setValue(LocalDate.now());
            loadStatsForDate(LocalDate.now());
        }
    }

    /**
     * Export attendance report to CSV
     */
    @FXML
    private void handleExport() {
        if (attendanceData == null || attendanceData.isEmpty()) {
            showError("No Data", "There is no attendance data to export.");
            return;
        }

        // File chooser for save location
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Export Attendance Report");
        fileChooser.setInitialFileName("attendance_report_" + java.time.LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        java.io.File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (file != null) {
            try {
                exportToCSV(file);
                showInfo("Export Successful", "Attendance report exported to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Export Failed", "Failed to export attendance report: " + e.getMessage());
            }
        }
    }

    /**
     * Export attendance data to CSV file
     */
    private void exportToCSV(java.io.File file) throws Exception {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
            // Write CSV header
            writer.println("Employee ID,Employee Name,Department,Date,Check In,Check Out,Hours Worked,Status");

            // Write attendance records
            for (Attendance record : attendanceData) {
                writer.printf("%s,%s,%s,%s,%s,%s,%s,%s%n",
                    escapeCSV(String.valueOf(record.getEmployeeId())),
                    escapeCSV(record.getEmployeeFullName()),
                    escapeCSV(record.getDepartment()),
                    escapeCSV(record.getDate() != null ? record.getDate().toString() : ""),
                    escapeCSV(record.getFormattedCheckInTime()),
                    escapeCSV(record.getFormattedCheckOutTime()),
                    escapeCSV(record.getFormattedHours()),
                    escapeCSV(record.getStatus() != null ? record.getStatus().toString() : "")
                );
            }
        }
    }

    /**
     * Escape CSV values properly (handle commas, quotes, newlines)
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Refresh both table data and statistics
     */
    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing attendance data...");
        loadAllAttendanceFromDatabase();

        LocalDate selectedDate = datePicker != null ? datePicker.getValue() : LocalDate.now();
        if (selectedDate != null) {
            loadStatsForDate(selectedDate);
        }
    }

    // ==================== UTILITY METHODS ====================

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

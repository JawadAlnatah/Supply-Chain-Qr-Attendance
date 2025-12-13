package com.team.supplychain.controllers;

import com.team.supplychain.dao.AttendanceDAO;
import com.team.supplychain.dao.EmployeeDAO;
import com.team.supplychain.models.Attendance;
import com.team.supplychain.models.Employee;
import com.team.supplychain.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Employee Attendance View
 * Displays attendance history, weekly/monthly calendars, and statistics
 */
public class EmployeeAttendanceViewController {

    // ==================== SUMMARY CARDS ====================
    @FXML private Label weekAttendanceValue;
    @FXML private Label monthAttendanceValue;
    @FXML private Label avgHoursValue;

    // ==================== CALENDAR TOGGLE ====================
    @FXML private Button weeklyViewButton;
    @FXML private Button monthlyViewButton;

    // ==================== CALENDAR VIEWS ====================
    @FXML private HBox weeklyCalendarPane;
    @FXML private VBox monthlyCalendarPane;
    @FXML private Label weekRangeLabel;
    @FXML private Label monthLabel;

    private static final DateTimeFormatter MONTH_ABBREV_FORMATTER = DateTimeFormatter.ofPattern("MMM");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // ==================== ATTENDANCE HISTORY TABLE ====================
    @FXML private TableView<AttendanceRecord> attendanceHistoryTable;
    @FXML private TableColumn<AttendanceRecord, String> dateColumn;
    @FXML private TableColumn<AttendanceRecord, String> dayColumn;
    @FXML private TableColumn<AttendanceRecord, String> checkInColumn;
    @FXML private TableColumn<AttendanceRecord, String> checkOutColumn;
    @FXML private TableColumn<AttendanceRecord, String> hoursColumn;
    @FXML private TableColumn<AttendanceRecord, String> statusColumn;

    private User currentUser;
    private Employee currentEmployee;
    private AttendanceDAO attendanceDAO;
    private EmployeeDAO employeeDAO;

    // Current viewing period
    private LocalDate currentWeekStart;
    private LocalDate currentMonthStart;

    /**
     * Helper class to hold attendance data loaded in background
     */
    private static class AttendanceData {
        Employee employee;
        List<Attendance> weekAttendance;
        List<Attendance> monthAttendance;

        AttendanceData(Employee employee, List<Attendance> weekAttendance, List<Attendance> monthAttendance) {
            this.employee = employee;
            this.weekAttendance = weekAttendance;
            this.monthAttendance = monthAttendance;
        }
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("EEEE");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter WEEK_FORMATTER = DateTimeFormatter.ofPattern("MMM dd");

    /**
     * Set the current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadAttendanceData();
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        System.out.println("EmployeeAttendanceViewController initialized");
        attendanceDAO = new AttendanceDAO();
        employeeDAO = new EmployeeDAO();

        // Initialize current viewing period to current week/month
        LocalDate today = LocalDate.now();
        currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        currentMonthStart = today.withDayOfMonth(1);

        // Configure table columns
        setupTableColumns();

        // Show weekly view by default
        showWeeklyView();
    }

    // ==================== VIEW TOGGLE HANDLERS ====================

    @FXML
    private void handleWeeklyView() {
        System.out.println("Weekly view clicked");
        showWeeklyView();
    }

    @FXML
    private void handleMonthlyView() {
        System.out.println("Monthly view clicked");
        showMonthlyView();
    }

    private void showWeeklyView() {
        if (weeklyCalendarPane != null && monthlyCalendarPane != null) {
            weeklyCalendarPane.setVisible(true);
            weeklyCalendarPane.setManaged(true);
            monthlyCalendarPane.setVisible(false);
            monthlyCalendarPane.setManaged(false);

            // Update button styles
            updateButtonStyles(true);
        }
    }

    private void showMonthlyView() {
        if (weeklyCalendarPane != null && monthlyCalendarPane != null) {
            weeklyCalendarPane.setVisible(false);
            weeklyCalendarPane.setManaged(false);
            monthlyCalendarPane.setVisible(true);
            monthlyCalendarPane.setManaged(true);

            // Update button styles
            updateButtonStyles(false);
        }
    }

    private void updateButtonStyles(boolean weeklyActive) {
        if (weeklyViewButton != null && monthlyViewButton != null) {
            if (weeklyActive) {
                // Weekly button active
                if (!weeklyViewButton.getStyleClass().contains("action-button-primary")) {
                    weeklyViewButton.getStyleClass().add("action-button-primary");
                }
                weeklyViewButton.getStyleClass().remove("action-button-secondary");

                if (!monthlyViewButton.getStyleClass().contains("action-button-secondary")) {
                    monthlyViewButton.getStyleClass().add("action-button-secondary");
                }
                monthlyViewButton.getStyleClass().remove("action-button-primary");
            } else {
                // Monthly button active
                if (!monthlyViewButton.getStyleClass().contains("action-button-primary")) {
                    monthlyViewButton.getStyleClass().add("action-button-primary");
                }
                monthlyViewButton.getStyleClass().remove("action-button-secondary");

                if (!weeklyViewButton.getStyleClass().contains("action-button-secondary")) {
                    weeklyViewButton.getStyleClass().add("action-button-secondary");
                }
                weeklyViewButton.getStyleClass().remove("action-button-primary");
            }
        }
    }

    // ==================== NAVIGATION HANDLERS ====================

    @FXML
    private void handlePreviousWeek() {
        System.out.println("Previous week clicked");
        currentWeekStart = currentWeekStart.minusWeeks(1);
        loadAttendanceData();
    }

    @FXML
    private void handleNextWeek() {
        System.out.println("Next week clicked");
        currentWeekStart = currentWeekStart.plusWeeks(1);
        loadAttendanceData();
    }

    @FXML
    private void handlePreviousMonth() {
        System.out.println("Previous month clicked");
        currentMonthStart = currentMonthStart.minusMonths(1);
        loadAttendanceData();
    }

    @FXML
    private void handleNextMonth() {
        System.out.println("Next month clicked");
        currentMonthStart = currentMonthStart.plusMonths(1);
        loadAttendanceData();
    }

    // ==================== ACTION HANDLERS ====================

    @FXML
    private void handleExportExcel() {
        System.out.println("Export to Excel clicked");
        // TODO: Export attendance data to Excel using Apache POI
    }

    @FXML
    private void handleViewRecord() {
        System.out.println("View attendance record clicked");
        // TODO: Show attendance record details dialog
    }

    // ==================== HELPER METHODS ====================

    /**
     * Setup table column bindings
     */
    private void setupTableColumns() {
        if (attendanceHistoryTable != null) {
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            dayColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
            checkInColumn.setCellValueFactory(new PropertyValueFactory<>("checkIn"));
            checkOutColumn.setCellValueFactory(new PropertyValueFactory<>("checkOut"));
            hoursColumn.setCellValueFactory(new PropertyValueFactory<>("hours"));
            statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        }
    }

    /**
     * Load attendance data from database asynchronously and update UI
     */
    private void loadAttendanceData() {
        if (currentUser == null) {
            System.out.println("No user session - cannot load attendance data");
            return;
        }

        // Update calendar labels immediately (no database needed)
        updateCalendarLabels();

        // Load data in background thread
        Task<AttendanceData> loadTask = new Task<>() {
            @Override
            protected AttendanceData call() throws Exception {
                // Fetch employee record
                Employee employee = employeeDAO.getEmployeeByUserId(currentUser.getUserId());
                if (employee == null) {
                    throw new Exception("No employee profile found for user: " + currentUser.getUsername());
                }

                int employeeId = employee.getEmployeeId();
                System.out.println("Loading attendance data for employee: " + employee.getFullName());

                // Get current month's attendance for table and statistics
                LocalDate today = LocalDate.now();
                LocalDate monthStart = today.withDayOfMonth(1);
                LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
                List<Attendance> monthAttendance = attendanceDAO.getAttendanceByDateRange(employeeId, monthStart, monthEnd);

                // Get current week's attendance
                List<Attendance> weekAttendance = attendanceDAO.getWeekAttendance(employeeId, currentWeekStart);

                return new AttendanceData(employee, weekAttendance, monthAttendance);
            }
        };

        // Update UI when data is loaded
        loadTask.setOnSucceeded(e -> {
            AttendanceData data = loadTask.getValue();

            // Update current employee
            currentEmployee = data.employee;

            // Update statistics cards
            updateStatistics(data.weekAttendance, data.monthAttendance);

            // Update weekly calendar visual
            updateWeeklyCalendarView(data.weekAttendance);

            // Populate attendance history table
            populateAttendanceTable(data.monthAttendance);

            System.out.println("Loaded " + data.monthAttendance.size() + " attendance records");
        });

        // Handle errors
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            exception.printStackTrace();
            System.err.println("Failed to load attendance data: " + exception.getMessage());
        });

        // Start background task
        new Thread(loadTask).start();
    }

    /**
     * Update statistics cards (week attendance, month attendance, avg hours)
     */
    private void updateStatistics(List<Attendance> weekAttendance, List<Attendance> monthAttendance) {
        // Week attendance: count present/late days
        long weekDaysPresent = weekAttendance.stream()
                .filter(a -> a.getCheckInTime() != null)
                .count();
        if (weekAttendanceValue != null) {
            weekAttendanceValue.setText(weekDaysPresent + "/7 days");
        }

        // Month attendance: count present/late days
        long monthDaysPresent = monthAttendance.stream()
                .filter(a -> a.getCheckInTime() != null)
                .count();
        if (monthAttendanceValue != null) {
            monthAttendanceValue.setText(monthDaysPresent + " days");
        }

        // Average hours worked per day (only count completed days)
        List<Attendance> completedDays = monthAttendance.stream()
                .filter(a -> a.getCheckInTime() != null && a.getCheckOutTime() != null)
                .toList();

        if (!completedDays.isEmpty()) {
            Duration totalHours = completedDays.stream()
                    .map(Attendance::getWorkDuration)
                    .reduce(Duration.ZERO, Duration::plus);

            long avgMinutes = totalHours.toMinutes() / completedDays.size();
            long avgHours = avgMinutes / 60;
            long avgMins = avgMinutes % 60;

            if (avgHoursValue != null) {
                avgHoursValue.setText(String.format("%dh %dm", avgHours, avgMins));
            }
        } else {
            if (avgHoursValue != null) {
                avgHoursValue.setText("0h 0m");
            }
        }
    }

    /**
     * Update calendar range/month labels
     */
    private void updateCalendarLabels() {
        if (weekRangeLabel != null) {
            LocalDate weekEnd = currentWeekStart.plusDays(6);
            String rangeText = currentWeekStart.format(WEEK_FORMATTER) + " - " + weekEnd.format(WEEK_FORMATTER);
            weekRangeLabel.setText(rangeText);
        }

        if (monthLabel != null) {
            monthLabel.setText(currentMonthStart.format(MONTH_FORMATTER));
        }
    }

    /**
     * Update weekly calendar visual with real attendance data
     */
    private void updateWeeklyCalendarView(List<Attendance> weekAttendance) {
        if (weeklyCalendarPane == null) {
            return;
        }

        // Clear existing calendar
        weeklyCalendarPane.getChildren().clear();

        // Create map of attendance by date for quick lookup
        Map<LocalDate, Attendance> attendanceMap = new HashMap<>();
        if (weekAttendance != null) {
            for (Attendance attendance : weekAttendance) {
                attendanceMap.put(attendance.getDate(), attendance);
            }
        }

        LocalDate today = LocalDate.now();
        String[] dayAbbreviations = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

        // Create 7 day boxes (Monday to Sunday)
        for (int i = 0; i < 7; i++) {
            LocalDate date = currentWeekStart.plusDays(i);
            Attendance attendance = attendanceMap.get(date);

            boolean isToday = date.equals(today);
            boolean isFuture = date.isAfter(today);
            boolean isWeekend = (i == 5 || i == 6); // Saturday or Sunday

            // Create day box
            VBox dayBox = new VBox(8);
            dayBox.setAlignment(Pos.TOP_CENTER);
            dayBox.setPadding(new Insets(15, 15, 15, 15));
            HBox.setHgrow(dayBox, Priority.ALWAYS);

            // Determine style class based on status
            if (isToday) {
                dayBox.getStyleClass().add("day-today");
            } else if (attendance != null) {
                if (attendance.getStatus() == com.team.supplychain.enums.AttendanceStatus.LATE) {
                    dayBox.getStyleClass().add("day-late");
                } else {
                    dayBox.getStyleClass().add("day-present");
                }
            } else if (isFuture) {
                dayBox.getStyleClass().add("day-future");
            } else {
                dayBox.getStyleClass().add("day-absent");
            }

            // Day abbreviation label (MON, TUE, etc.)
            Label dayLabel = new Label(dayAbbreviations[i]);
            dayLabel.setStyle("-fx-text-fill: #808080; -fx-font-weight: bold; -fx-font-size: 11px;");

            // Date number label
            Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
            String dateColor = isToday ? "#ffffff" : (isFuture ? "#2c3e50" : "#2c3e50");
            dateLabel.setStyle("-fx-text-fill: " + dateColor + "; -fx-font-weight: bold; -fx-font-size: 24px;");

            // Month label
            Label monthLabel = new Label(date.format(MONTH_ABBREV_FORMATTER));
            String monthColor = isToday ? "#ffffff" : "#808080";
            monthLabel.setStyle("-fx-text-fill: " + monthColor + "; -fx-font-size: 11px;");

            // Spacer
            Region spacer = new Region();
            VBox.setVgrow(spacer, Priority.ALWAYS);

            // Status and time labels
            if (isWeekend && isFuture) {
                // Future weekend
                Label statusIcon = new Label("—");
                statusIcon.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 28px;");
                Label statusText = new Label("Weekend");
                statusText.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 11px;");
                dayBox.getChildren().addAll(dayLabel, dateLabel, monthLabel, spacer, statusIcon, statusText);
            } else if (isToday && attendance != null) {
                // Today with attendance
                Label statusIcon = new Label("✓");
                String iconColor = "#ffffff";
                statusIcon.setStyle("-fx-text-fill: " + iconColor + "; -fx-font-size: 28px; -fx-font-weight: bold;");

                Label checkInLabel = new Label(attendance.getFormattedCheckInTime());
                checkInLabel.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold; -fx-font-size: 12px;");

                Label statusText = new Label(attendance.isCheckedIn() ? "In Progress" : attendance.getFormattedCheckOutTime());
                statusText.setStyle("-fx-text-fill: #ffffff; -fx-font-size: 11px;");

                dayBox.getChildren().addAll(dayLabel, dateLabel, monthLabel, spacer, statusIcon, checkInLabel, statusText);
            } else if (attendance != null) {
                // Past day with attendance
                boolean isLate = attendance.getStatus() == com.team.supplychain.enums.AttendanceStatus.LATE;
                Label statusIcon = new Label(isLate ? "⚠" : "✓");
                String iconColor = isLate ? "#ff9800" : "#43a047";
                statusIcon.setStyle("-fx-text-fill: " + iconColor + "; -fx-font-size: 28px; " + (isLate ? "" : "-fx-font-weight: bold;"));

                Label checkInLabel = new Label(attendance.getFormattedCheckInTime());
                checkInLabel.setStyle("-fx-text-fill: " + iconColor + "; -fx-font-weight: bold; -fx-font-size: 12px;");

                Label checkOutLabel = new Label(attendance.getFormattedCheckOutTime());
                checkOutLabel.setStyle("-fx-text-fill: " + iconColor + "; -fx-font-size: 11px;");

                dayBox.getChildren().addAll(dayLabel, dateLabel, monthLabel, spacer, statusIcon, checkInLabel, checkOutLabel);
            } else if (isFuture) {
                // Future day (no data yet)
                Label statusIcon = new Label("—");
                statusIcon.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 28px;");
                dayBox.getChildren().addAll(dayLabel, dateLabel, monthLabel, spacer, statusIcon);
            } else {
                // Past day absent
                Label statusIcon = new Label("✗");
                statusIcon.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 28px;");
                Label statusText = new Label("Absent");
                statusText.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 11px;");
                dayBox.getChildren().addAll(dayLabel, dateLabel, monthLabel, spacer, statusIcon, statusText);
            }

            weeklyCalendarPane.getChildren().add(dayBox);
        }
    }

    /**
     * Populate attendance history table with records
     */
    private void populateAttendanceTable(List<Attendance> attendanceList) {
        if (attendanceHistoryTable == null) {
            return;
        }

        ObservableList<AttendanceRecord> records = FXCollections.observableArrayList();

        for (Attendance attendance : attendanceList) {
            String date = attendance.getDate().format(DATE_FORMATTER);
            String day = attendance.getDate().format(DAY_FORMATTER);
            String checkIn = attendance.getFormattedCheckInTime();
            String checkOut = attendance.getFormattedCheckOutTime();
            String hours = attendance.getFormattedHours();
            String status = attendance.getStatus().toString();

            records.add(new AttendanceRecord(date, day, checkIn, checkOut, hours, status));
        }

        attendanceHistoryTable.setItems(records);
    }

    // ==================== INNER CLASS FOR TABLE ====================

    /**
     * Simple POJO for attendance table display
     */
    public static class AttendanceRecord {
        private final String date;
        private final String day;
        private final String checkIn;
        private final String checkOut;
        private final String hours;
        private final String status;

        public AttendanceRecord(String date, String day, String checkIn,
                                String checkOut, String hours, String status) {
            this.date = date;
            this.day = day;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.hours = hours;
            this.status = status;
        }

        public String getDate() { return date; }
        public String getDay() { return day; }
        public String getCheckIn() { return checkIn; }
        public String getCheckOut() { return checkOut; }
        public String getHours() { return hours; }
        public String getStatus() { return status; }
    }
}

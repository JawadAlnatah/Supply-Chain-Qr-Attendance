package com.team.supplychain.controllers;

import com.team.supplychain.dao.AttendanceDAO;
import com.team.supplychain.dao.EmployeeDAO;
import com.team.supplychain.dao.RequisitionDAO;
import com.team.supplychain.enums.AttendanceStatus;
import com.team.supplychain.models.Attendance;
import com.team.supplychain.models.Employee;
import com.team.supplychain.models.Requisition;
import com.team.supplychain.models.User;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for the Employee Dashboard view
 * Displays personal attendance information, team status, and requisition management
 */
public class EmployeeDashboardController {

    // ==================== LAYOUT ELEMENTS ====================
    @FXML private BorderPane rootPane;
    @FXML private ScrollPane centerScrollPane;

    // ==================== HEADER ELEMENTS ====================
    @FXML private Label userNameLabel;
    @FXML private Label profileNameLabel;
    @FXML private Label profilePositionLabel;
    @FXML private Button refreshButton;
    @FXML private Button logoutButton;

    // ==================== SIDEBAR NAVIGATION ====================
    @FXML private Button dashboardButton;
    @FXML private Button attendanceButton;
    @FXML private Button requisitionsButton;
    @FXML private Button createRequisitionButton;
    @FXML private Button profileButton;

    // ==================== WELCOME SECTION ====================
    @FXML private Label dateTimeLabel;

    // ==================== ATTENDANCE STATUS CARDS ====================
    @FXML private Label todayStatusLabel;
    @FXML private Label weekDaysLabel;
    @FXML private Label monthDaysLabel;
    @FXML private HBox weekCalendarContainer;

    // ==================== TEAM INFO ====================
    @FXML private Label teamOnSiteLabel;

    // ==================== STATUS BAR ====================
    @FXML private Label currentDateLabel;

    // ==================== RECENT REQUISITIONS ====================
    @FXML private VBox recentRequisitionsContainer;

    private User currentUser;
    private Employee currentEmployee;
    private Node originalDashboardContent;
    private RequisitionDAO requisitionDAO;
    private AttendanceDAO attendanceDAO;
    private EmployeeDAO employeeDAO;
    private Timeline autoRefreshTimeline;

    /**
     * Helper class to hold all dashboard data loaded in background
     */
    private static class DashboardData {
        Employee employee;
        Attendance todayAttendance;
        List<Attendance> weekAttendance;
        List<Attendance> monthAttendance;
        List<Requisition> recentRequisitions;

        DashboardData(Employee employee, Attendance todayAttendance,
                     List<Attendance> weekAttendance, List<Attendance> monthAttendance,
                     List<Requisition> recentRequisitions) {
            this.employee = employee;
            this.todayAttendance = todayAttendance;
            this.weekAttendance = weekAttendance;
            this.monthAttendance = monthAttendance;
            this.recentRequisitions = recentRequisitions;
        }
    }

    /**
     * Set the current logged-in employee user and load dashboard data asynchronously
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInterface(); // Update UI labels immediately

        // Load all data in background thread
        Task<DashboardData> loadTask = new Task<>() {
            @Override
            protected DashboardData call() throws Exception {
                // Load employee profile
                Employee employee = employeeDAO.getEmployeeByUserId(user.getUserId());

                if (employee == null) {
                    throw new Exception("No employee record found for user: " + user.getUsername());
                }

                int employeeId = employee.getEmployeeId();

                // Load today's attendance
                Attendance todayAttendance = attendanceDAO.getTodayAttendance(employeeId);

                // Load week's attendance
                LocalDate today = LocalDate.now();
                LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                List<Attendance> weekAttendance = attendanceDAO.getWeekAttendance(employeeId, weekStart);

                // Load month's attendance
                LocalDate monthStart = today.withDayOfMonth(1);
                LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());
                List<Attendance> monthAttendance = attendanceDAO.getAttendanceByDateRange(employeeId, monthStart, monthEnd);

                // Load recent requisitions
                List<Requisition> requisitions = requisitionDAO.getRequisitionsByUser(user.getUserId());

                return new DashboardData(employee, todayAttendance, weekAttendance, monthAttendance, requisitions);
            }
        };

        // Handle successful data loading
        loadTask.setOnSucceeded(e -> {
            DashboardData data = loadTask.getValue();

            // Update current employee
            currentEmployee = data.employee;
            System.out.println("Employee profile loaded: " + currentEmployee.getFullName());

            // Update UI with loaded data
            updateTodayStatus(data.todayAttendance);
            updateWeekStatistics(data.weekAttendance);
            updateMonthStatistics(data.monthAttendance);
            updateRecentRequisitionsUI(data.recentRequisitions);

            System.out.println("Dashboard data loaded successfully");
        });

        // Handle errors
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            exception.printStackTrace();
            System.err.println("Failed to load dashboard data: " + exception.getMessage());
            showError("Loading Error", "Failed to load dashboard data. Please try again.");
        });

        // Start background task
        new Thread(loadTask).start();
    }

    /**
     * Refresh dashboard data manually (triggered by refresh button)
     */
    @FXML
    private void handleRefreshDashboard() {
        System.out.println("Manual dashboard refresh triggered");

        if (currentUser == null || currentEmployee == null) {
            System.err.println("Cannot refresh: User or employee not loaded");
            return;
        }

        // Show visual feedback - rotate the refresh icon
        if (refreshButton != null) {
            refreshButton.setDisable(true);
        }

        // Load all data in background thread
        Task<DashboardData> refreshTask = new Task<>() {
            @Override
            protected DashboardData call() throws Exception {
                int employeeId = currentEmployee.getEmployeeId();

                // Load today's attendance
                Attendance todayAttendance = attendanceDAO.getTodayAttendance(employeeId);

                // Load week's attendance
                LocalDate today = LocalDate.now();
                LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                List<Attendance> weekAttendance = attendanceDAO.getWeekAttendance(employeeId, weekStart);

                // Load month's attendance
                LocalDate monthStart = today.withDayOfMonth(1);
                LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());
                List<Attendance> monthAttendance = attendanceDAO.getAttendanceByDateRange(employeeId, monthStart, monthEnd);

                // Load recent requisitions
                List<Requisition> requisitions = requisitionDAO.getRequisitionsByUser(currentUser.getUserId());

                return new DashboardData(currentEmployee, todayAttendance, weekAttendance, monthAttendance, requisitions);
            }
        };

        // Handle successful data loading
        refreshTask.setOnSucceeded(e -> {
            DashboardData data = refreshTask.getValue();

            // Update UI with loaded data
            updateTodayStatus(data.todayAttendance);
            updateWeekStatistics(data.weekAttendance);
            updateMonthStatistics(data.monthAttendance);
            updateRecentRequisitionsUI(data.recentRequisitions);

            // Re-enable refresh button
            if (refreshButton != null) {
                refreshButton.setDisable(false);
            }

            System.out.println("Dashboard refreshed successfully");
        });

        // Handle errors
        refreshTask.setOnFailed(e -> {
            Throwable exception = refreshTask.getException();
            exception.printStackTrace();
            System.err.println("Failed to refresh dashboard: " + exception.getMessage());

            // Re-enable refresh button
            if (refreshButton != null) {
                refreshButton.setDisable(false);
            }
        });

        // Start background task
        new Thread(refreshTask).start();
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Initialize DAOs
        requisitionDAO = new RequisitionDAO();
        attendanceDAO = new AttendanceDAO();
        employeeDAO = new EmployeeDAO();

        // Store original dashboard content for navigation back
        if (centerScrollPane != null) {
            originalDashboardContent = centerScrollPane.getContent();
        }

        // Set dashboard button as active by default
        setActiveMenuButton(dashboardButton);

        // Start auto-refresh timer (refresh every 30 seconds)
        startAutoRefresh();

        System.out.println("EmployeeDashboardController initialized");
    }

    /**
     * Start automatic dashboard refresh every 30 seconds
     */
    private void startAutoRefresh() {
        // Create timeline that runs every 30 seconds
        autoRefreshTimeline = new Timeline(new KeyFrame(Duration.seconds(30), event -> {
            System.out.println("Auto-refresh triggered");
            handleRefreshDashboard();
        }));

        // Set to repeat indefinitely
        autoRefreshTimeline.setCycleCount(Animation.INDEFINITE);

        // Start the timeline
        autoRefreshTimeline.play();

        System.out.println("Auto-refresh started (every 30 seconds)");
    }

    /**
     * Stop automatic dashboard refresh
     */
    private void stopAutoRefresh() {
        if (autoRefreshTimeline != null) {
            autoRefreshTimeline.stop();
            System.out.println("Auto-refresh stopped");
        }
    }

    /**
     * Update the user interface with current user information
     */
    private void updateUserInterface() {
        if (currentUser != null) {
            String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();

            // Update header name label
            if (userNameLabel != null) {
                userNameLabel.setText(fullName);
            }

            // Update profile card name label
            if (profileNameLabel != null) {
                profileNameLabel.setText(fullName);
            }

            // Update profile position/department label
            if (profilePositionLabel != null && currentEmployee != null) {
                String position = currentEmployee.getPosition() != null ? currentEmployee.getPosition() : "Employee";
                profilePositionLabel.setText(position);
            }
        }

        // Update current date label
        if (currentDateLabel != null) {
            LocalDate today = LocalDate.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
            currentDateLabel.setText(today.format(dateFormatter));
        }

        // Update date/time label in welcome section
        if (dateTimeLabel != null) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEEE, MMM dd, yyyy - h:mm a");
            dateTimeLabel.setText(now.format(dateTimeFormatter));
        }
    }

    /**
     * Load employee profile from database
     */
    private void loadEmployeeProfile() {
        if (currentUser == null) {
            return;
        }

        try {
            currentEmployee = employeeDAO.getEmployeeByUserId(currentUser.getUserId());

            if (currentEmployee == null) {
                System.err.println("No employee record found for user: " + currentUser.getUsername());
            } else {
                System.out.println("Employee profile loaded: " + currentEmployee.getFullName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load employee profile: " + e.getMessage());
        }
    }

    /**
     * Load attendance data from database and update UI
     */
    private void loadAttendanceData() {
        if (currentEmployee == null) {
            System.err.println("Cannot load attendance data: Employee profile not loaded");
            return;
        }

        try {
            int employeeId = currentEmployee.getEmployeeId();

            // Get today's attendance
            Attendance todayAttendance = attendanceDAO.getTodayAttendance(employeeId);
            updateTodayStatus(todayAttendance);

            // Get this week's attendance (Monday to Sunday)
            LocalDate today = LocalDate.now();
            LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            List<Attendance> weekAttendance = attendanceDAO.getWeekAttendance(employeeId, weekStart);
            updateWeekStatistics(weekAttendance);

            // Get this month's attendance
            LocalDate monthStart = today.withDayOfMonth(1);
            LocalDate monthEnd = today.with(TemporalAdjusters.lastDayOfMonth());
            List<Attendance> monthAttendance = attendanceDAO.getAttendanceByDateRange(employeeId, monthStart, monthEnd);
            updateMonthStatistics(monthAttendance);

            System.out.println("Attendance data loaded successfully");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to load attendance data: " + e.getMessage());
        }
    }

    /**
     * Update today's attendance status on the dashboard
     */
    private void updateTodayStatus(Attendance todayAttendance) {
        if (todayStatusLabel == null) {
            return;
        }

        if (todayAttendance == null) {
            todayStatusLabel.setText("Not Checked In");
            todayStatusLabel.setStyle("-fx-text-fill: #fb8c00;"); // Orange for not checked in
        } else if (todayAttendance.isCheckedIn()) {
            // Still checked in (no checkout yet)
            String checkInTime = todayAttendance.getFormattedCheckInTime();
            todayStatusLabel.setText("Checked In at " + checkInTime);

            if (todayAttendance.getStatus() == AttendanceStatus.LATE) {
                todayStatusLabel.setStyle("-fx-text-fill: #fb8c00;"); // Orange for late
            } else {
                todayStatusLabel.setStyle("-fx-text-fill: #43a047;"); // Green for on time
            }
        } else {
            // Checked out
            String hours = todayAttendance.getFormattedHours();
            todayStatusLabel.setText("Completed - " + hours);
            todayStatusLabel.setStyle("-fx-text-fill: #43a047;"); // Green for completed
        }
    }

    /**
     * Update this week's attendance statistics
     */
    private void updateWeekStatistics(List<Attendance> weekAttendance) {
        if (weekDaysLabel == null) {
            return;
        }

        if (weekAttendance == null || weekAttendance.isEmpty()) {
            weekDaysLabel.setText("0 days");
            updateWeekCalendar(weekAttendance);
            return;
        }

        long presentDays = weekAttendance.stream()
            .filter(a -> a.getStatus() == AttendanceStatus.PRESENT ||
                        a.getStatus() == AttendanceStatus.LATE)
            .count();

        weekDaysLabel.setText(presentDays + " days");

        // Update visual calendar
        updateWeekCalendar(weekAttendance);
    }

    /**
     * Build dynamic week calendar with real attendance data
     */
    private void updateWeekCalendar(List<Attendance> weekAttendance) {
        if (weekCalendarContainer == null) {
            return;
        }

        // Clear existing calendar
        weekCalendarContainer.getChildren().clear();

        // Create map of attendance by date for quick lookup
        Map<LocalDate, Attendance> attendanceMap = new HashMap<>();
        if (weekAttendance != null) {
            for (Attendance attendance : weekAttendance) {
                attendanceMap.put(attendance.getDate(), attendance);
            }
        }

        // Get current week's Monday
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Day abbreviations
        String[] dayAbbreviations = {"MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN"};

        // Create 7 day boxes (Monday to Sunday)
        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            Attendance attendance = attendanceMap.get(date);

            boolean isToday = date.equals(today);
            boolean isFuture = date.isAfter(today);
            boolean isPresent = attendance != null &&
                               (attendance.getStatus() == AttendanceStatus.PRESENT ||
                                attendance.getStatus() == AttendanceStatus.LATE);

            // Create day box
            VBox dayBox = new VBox(8);
            dayBox.setAlignment(javafx.geometry.Pos.CENTER);
            dayBox.setPadding(new Insets(15, 10, 15, 10));
            HBox.setHgrow(dayBox, Priority.ALWAYS);

            // Set style class based on status
            if (isToday) {
                dayBox.getStyleClass().addAll("day-box", "day-today");
            } else if (isPresent) {
                dayBox.getStyleClass().addAll("day-box", "day-present");
            } else if (isFuture) {
                dayBox.getStyleClass().addAll("day-box", "day-future");
            } else {
                dayBox.getStyleClass().addAll("day-box", "day-absent");
            }

            // Day abbreviation label
            Label dayLabel = new Label(dayAbbreviations[i]);
            dayLabel.getStyleClass().add("day-label");
            dayLabel.setStyle("-fx-font-size: 11px;");

            // Date number label
            Label dateLabel = new Label(String.valueOf(date.getDayOfMonth()));
            dateLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                             (isToday ? "-fx-text-fill: #2e7d32;" :
                              isFuture ? "-fx-text-fill: #9e9e9e;" :
                              "-fx-text-fill: #424242;"));

            // Status indicator
            Label statusLabel = new Label();
            statusLabel.setStyle("-fx-font-size: 16px;");

            if (isToday) {
                statusLabel.setText("•");
                statusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 20px;");
            } else if (isPresent) {
                statusLabel.setText("✓");
                statusLabel.setStyle("-fx-text-fill: #43a047; -fx-font-size: 16px;");
            } else if (isFuture) {
                statusLabel.setText("—");
                statusLabel.setStyle("-fx-text-fill: #bdbdbd; -fx-font-size: 16px;");
            } else {
                statusLabel.setText("✗");
                statusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 16px;");
            }

            dayBox.getChildren().addAll(dayLabel, dateLabel, statusLabel);
            weekCalendarContainer.getChildren().add(dayBox);
        }
    }

    /**
     * Update this month's attendance statistics
     */
    private void updateMonthStatistics(List<Attendance> monthAttendance) {
        if (monthDaysLabel == null) {
            return;
        }

        if (monthAttendance == null || monthAttendance.isEmpty()) {
            monthDaysLabel.setText("0 days");
            return;
        }

        long presentDays = monthAttendance.stream()
            .filter(a -> a.getStatus() == AttendanceStatus.PRESENT ||
                        a.getStatus() == AttendanceStatus.LATE)
            .count();

        monthDaysLabel.setText(presentDays + " days");
    }

    // ==================== NAVIGATION EVENT HANDLERS ====================

    @FXML
    private void handleDashboard() {
        System.out.println("Dashboard navigation clicked");
        // Restore original dashboard content
        if (centerScrollPane != null && originalDashboardContent != null) {
            centerScrollPane.setContent(originalDashboardContent);
        }
        setActiveMenuButton(dashboardButton);
    }

    @FXML
    private void handleMyAttendance() {
        System.out.println("My Attendance clicked");
        loadContentView("/fxml/EmployeeAttendanceView.fxml", attendanceButton);
    }

    @FXML
    private void handleMyRequisitions() {
        System.out.println("My Requisitions clicked");
        loadContentView("/fxml/EmployeeRequisitionsView.fxml", requisitionsButton);
    }

    @FXML
    private void handleCreateRequisition() {
        System.out.println("Create Requisition clicked");
        loadContentView("/fxml/EmployeeCreateRequisitionView.fxml", createRequisitionButton);
    }

    @FXML
    private void handleProfile() {
        System.out.println("My Profile clicked");
        loadContentView("/fxml/EmployeeProfileView.fxml", profileButton);
    }

    @FXML
    private void handleInventory() {
        System.out.println("Inventory clicked");
        loadContentView("/fxml/ManagerInventory.fxml", null);
    }

    // ==================== ACTION HANDLERS ====================

    @FXML
    private void handleCheckOut() {
        System.out.println("Check Out clicked");

        if (currentEmployee == null) {
            showError("Check-Out Error", "Employee profile not loaded. Please try logging in again.");
            return;
        }

        try {
            // Check if employee is checked in today
            Attendance todayAttendance = attendanceDAO.getTodayAttendance(currentEmployee.getEmployeeId());

            if (todayAttendance == null) {
                showError("Check-Out Error", "You haven't checked in today. Please check in first.");
                return;
            }

            if (!todayAttendance.isCheckedIn()) {
                showError("Already Checked Out", "You have already checked out for today.");
                return;
            }

            // Perform check-out
            boolean success = attendanceDAO.checkOut(currentEmployee.getEmployeeId());

            if (success) {
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Check-Out Successful");
                alert.setHeaderText(null);
                alert.setContentText("You have successfully checked out at " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a")));
                alert.showAndWait();

                // Reload attendance data to update UI
                loadAttendanceData();
            } else {
                showError("Check-Out Failed", "Failed to record check-out. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Check-Out Error", "An error occurred while checking out: " + e.getMessage());
        }
    }

    @FXML
    private void handleViewRequisition() {
        System.out.println("View Requisition clicked");
        // TODO: Show requisition details dialog
    }

    @FXML
    private void handleLogout() {
        try {
            // Stop auto-refresh to prevent memory leaks
            stopAutoRefresh();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent loginView = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();

            // Reset window state before switching to login
            stage.setMaximized(false);
            stage.setResizable(false);

            // Set login scene
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.setTitle("Supply Chain Management System - Login");

            // Center the window on screen
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError("Logout Error", "Failed to return to login screen");
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Load a new content view into the center ScrollPane
     */
    private void loadContentView(String fxmlPath, Button menuButton) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            // Get the controller and pass the current user if it has setCurrentUser method
            Object controller = loader.getController();
            if (controller instanceof EmployeeAttendanceViewController) {
                ((EmployeeAttendanceViewController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof EmployeeRequisitionsViewController) {
                ((EmployeeRequisitionsViewController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof EmployeeCreateRequisitionViewController) {
                ((EmployeeCreateRequisitionViewController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof EmployeeProfileViewController) {
                ((EmployeeProfileViewController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof ManagerInventoryController) {
                ((ManagerInventoryController) controller).setCurrentUser(currentUser);
            }

            // Replace the center content
            if (centerScrollPane != null) {
                centerScrollPane.setContent(content);
            }

            // Update active menu button
            setActiveMenuButton(menuButton);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Failed to load the requested page: " + fxmlPath);
        }
    }

    /**
     * Set the active menu button style
     */
    private void setActiveMenuButton(Button activeButton) {
        // Remove active style from all menu buttons
        Button[] menuButtons = {dashboardButton, attendanceButton, requisitionsButton,
                                createRequisitionButton, profileButton};

        for (Button button : menuButtons) {
            if (button != null) {
                button.getStyleClass().remove("menu-button-active");
            }
        }

        // Add active style to the clicked button
        if (activeButton != null && !activeButton.getStyleClass().contains("menu-button-active")) {
            activeButton.getStyleClass().add("menu-button-active");
        }
    }

    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== REQUISITION LOADING METHODS ====================

    /**
     * Load recent requisitions from database for the current user
     */
    private void loadRecentRequisitions() {
        if (recentRequisitionsContainer == null || currentUser == null) {
            return;
        }

        // Clear container
        recentRequisitionsContainer.getChildren().clear();

        try {
            // Fetch user's requisitions from database
            List<Requisition> requisitions = requisitionDAO.getRequisitionsByUser(currentUser.getUserId());
            updateRecentRequisitionsUI(requisitions);

        } catch (Exception e) {
            e.printStackTrace();
            showErrorState();
        }
    }

    /**
     * Update recent requisitions UI with provided data
     */
    private void updateRecentRequisitionsUI(List<Requisition> requisitions) {
        if (recentRequisitionsContainer == null) {
            return;
        }

        // Clear container
        recentRequisitionsContainer.getChildren().clear();

        if (requisitions == null || requisitions.isEmpty()) {
            // Show empty state
            showEmptyRequisitionsState();
            return;
        }

        // Limit to 5 most recent
        List<Requisition> recentReqs = requisitions.stream()
                .limit(5)
                .collect(Collectors.toList());

        // Create UI for each requisition
        for (Requisition req : recentReqs) {
            HBox reqCard = createRequisitionCard(req);
            recentRequisitionsContainer.getChildren().add(reqCard);
        }
    }

    /**
     * Create a UI card for a single requisition
     */
    private HBox createRequisitionCard(Requisition requisition) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("po-item");
        card.setStyle("-fx-padding: 16 20;");

        // Left section - requisition info
        VBox leftSection = new VBox(4);
        HBox.setHgrow(leftSection, Priority.ALWAYS);

        // Top row: code + category
        HBox topRow = new HBox(8);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label codeLabel = new Label(requisition.getRequisitionCode());
        codeLabel.getStyleClass().add("po-number");
        codeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        Label dotLabel = new Label("•");
        dotLabel.getStyleClass().add("po-separator");

        Label categoryLabel = new Label(requisition.getCategory());
        categoryLabel.getStyleClass().add("po-supplier");

        topRow.getChildren().addAll(codeLabel, dotLabel, categoryLabel);

        // Bottom row: item count + date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        String dateStr = requisition.getRequestDate().format(formatter);
        String detailsText = requisition.getTotalItems() + " items • Requested on " + dateStr;

        Label detailsLabel = new Label(detailsText);
        detailsLabel.getStyleClass().add("po-items");
        detailsLabel.setStyle("-fx-font-size: 11px;");

        leftSection.getChildren().addAll(topRow, detailsLabel);

        // Status badge
        Label statusBadge = new Label(requisition.getStatus().toUpperCase());
        String statusClass = getStatusStyleClass(requisition.getStatus());
        statusBadge.getStyleClass().add(statusClass);
        statusBadge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 6 12; -fx-background-radius: 15;");

        // View button
        Button viewButton = new Button("View");
        viewButton.getStyleClass().add("action-button-secondary");
        viewButton.setStyle("-fx-font-size: 11px;");
        viewButton.setOnAction(e -> handleViewRequisitionClick(requisition));

        card.getChildren().addAll(leftSection, statusBadge, viewButton);

        return card;
    }

    /**
     * Get the CSS style class for a requisition status
     */
    private String getStatusStyleClass(String status) {
        if (status == null) return "po-status-pending";

        switch (status.toLowerCase()) {
            case "approved":
            case "shipped":
                return "po-status-shipped";
            case "rejected":
                return "po-status-rejected";
            case "pending":
            default:
                return "po-status-pending";
        }
    }

    /**
     * Show empty state when no requisitions exist
     */
    private void showEmptyRequisitionsState() {
        VBox emptyState = new VBox(10);
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setStyle("-fx-padding: 40 20;");

        Label iconLabel = new Label("📋");
        iconLabel.setStyle("-fx-font-size: 36px; -fx-opacity: 0.5;");

        Label titleLabel = new Label("No Requisitions Yet");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #757575;");

        Label subtitleLabel = new Label("Create your first requisition to get started");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9e9e9e;");

        emptyState.getChildren().addAll(iconLabel, titleLabel, subtitleLabel);
        recentRequisitionsContainer.getChildren().add(emptyState);
    }

    /**
     * Show error state when database loading fails
     */
    private void showErrorState() {
        Label errorLabel = new Label("Unable to load requisitions. Please try again later.");
        errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px; -fx-padding: 20 0;");
        recentRequisitionsContainer.getChildren().add(errorLabel);
    }

    /**
     * Handle click on View button for a specific requisition
     */
    private void handleViewRequisitionClick(Requisition requisition) {
        // Navigate to full requisitions view
        // The requisitions view will handle loading and displaying all user requisitions
        handleMyRequisitions();
    }
}

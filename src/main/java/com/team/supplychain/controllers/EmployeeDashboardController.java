package com.team.supplychain.controllers;

import com.team.supplychain.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

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

    // ==================== TEAM INFO ====================
    @FXML private Label teamOnSiteLabel;

    // ==================== STATUS BAR ====================
    @FXML private Label currentDateLabel;

    private User currentUser;
    private Node originalDashboardContent;

    /**
     * Set the current logged-in employee user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInterface();
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        // Store original dashboard content for navigation back
        if (centerScrollPane != null) {
            originalDashboardContent = centerScrollPane.getContent();
        }

        // Set dashboard button as active by default
        setActiveMenuButton(dashboardButton);

        // TODO: Load employee's attendance data from database
        // TODO: Load weekly attendance calendar
        // TODO: Calculate hours worked today
        // TODO: Load team status (on-site colleagues count)
        // TODO: Load monthly and weekly statistics
        // TODO: Load recent requisitions
        System.out.println("EmployeeDashboardController initialized with dummy data");
    }

    /**
     * Update the user interface with current user information
     */
    private void updateUserInterface() {
        if (currentUser != null && userNameLabel != null) {
            userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        }

        // TODO: Load employee details from EmployeeDAO
        // TODO: Update date/time label with current date and time
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
        // TODO: Navigate to employee profile view
        setActiveMenuButton(profileButton);
    }

    // ==================== ACTION HANDLERS ====================

    @FXML
    private void handleCheckOut() {
        System.out.println("Check Out clicked");
        // TODO: Record check-out time in database
        // TODO: Update UI to show checked-out status
    }

    @FXML
    private void handleViewRequisition() {
        System.out.println("View Requisition clicked");
        // TODO: Show requisition details dialog
    }

    @FXML
    private void handleLogout() {
        try {
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
}

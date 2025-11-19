package com.team.supplychain.controllers;

import com.team.supplychain.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the Employee Dashboard view
 * Displays personal attendance information and basic team status
 */
public class EmployeeDashboardController {

    // Header elements
    @FXML private Label currentUserLabel;
    @FXML private Button logoutButton;

    // Welcome section
    @FXML private Label welcomeLabel;
    @FXML private Label dateTimeLabel;

    // Attendance status
    @FXML private Label attendanceStatusLabel;
    @FXML private Label checkInTimeLabel;
    @FXML private Label hoursWorkedLabel;
    @FXML private Label checkInLocationLabel;

    // Team info
    @FXML private Label teamOnSiteLabel;
    @FXML private Label departmentLabel;
    @FXML private Label positionLabel;

    // Stats
    @FXML private Label daysThisMonthLabel;
    @FXML private Label hoursThisWeekLabel;
    @FXML private Label avgCheckInLabel;

    private User currentUser;

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
        // TODO: Load employee's attendance data from database
        // TODO: Load weekly attendance calendar
        // TODO: Calculate hours worked today
        // TODO: Load team status (on-site colleagues count)
        // TODO: Load monthly and weekly statistics
    }

    /**
     * Update the user interface with current user information
     */
    private void updateUserInterface() {
        if (currentUser != null) {
            currentUserLabel.setText("Employee: " + currentUser.getFirstName() + " " + currentUser.getLastName());
            welcomeLabel.setText("Welcome, " + currentUser.getFirstName() + "!");

            // TODO: Load employee details from EmployeeDAO
            // departmentLabel.setText(employee.getDepartment());
            // positionLabel.setText(employee.getPosition());
        }

        // TODO: Update date/time label with current date and time
        // dateTimeLabel.setText(formattedDateTime);
    }

    // ==================== EVENT HANDLERS ====================

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent loginView = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(loginView);
            stage.setScene(scene);
            stage.setTitle("Fresh Dairy Co. - Login");
        } catch (IOException e) {
            e.printStackTrace();
            showError("Logout Error", "Failed to return to login screen");
        }
    }

    @FXML
    private void handleViewHistory() {
        // TODO: Navigate to full attendance history view
        System.out.println("View attendance history clicked");
    }

    @FXML
    private void handleViewReport() {
        // TODO: Navigate to personal performance report view
        System.out.println("View performance report clicked");
    }

    // ==================== HELPER METHODS ====================

    private void showError(String title, String message) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

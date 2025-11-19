package com.team.supplychain.controllers;

import com.team.supplychain.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the Manager Dashboard view
 * Displays operations metrics, purchase order tracking, and team activity
 */
public class ManagerDashboardController {

    // Header elements
    @FXML private Label currentUserLabel;
    @FXML private Button notificationsButton;
    @FXML private Button logoutButton;

    // Metric cards
    @FXML private Label inventorySummaryLabel;
    @FXML private ProgressBar inventoryHealthBar;
    @FXML private Label onSiteEmployeesLabel;
    @FXML private Label posInTransitLabel;

    // Charts
    @FXML private BarChart<String, Number> stockLevelsChart;

    // Activity and PO containers
    @FXML private VBox recentActivityContainer;
    @FXML private VBox posInProgressContainer;

    private User currentUser;

    /**
     * Set the current logged-in manager user
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
        // TODO: Load real-time operations data from database
        // TODO: Populate stock levels chart
        // TODO: Load recent activity/alerts
        // TODO: Load purchase orders in progress with ETA countdown
    }

    /**
     * Update the user interface with current user information
     */
    private void updateUserInterface() {
        if (currentUser != null) {
            currentUserLabel.setText("Manager: " + currentUser.getFirstName() + " " + currentUser.getLastName());
        }
    }

    // ==================== EVENT HANDLERS ====================

    @FXML
    private void handleNotifications() {
        // TODO: Show notifications panel or navigate to notifications view
        System.out.println("Notifications clicked");
    }

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
    private void handleViewAttendance() {
        // TODO: Navigate to attendance view
        System.out.println("View attendance clicked");
    }

    @FXML
    private void handleTrackOrders() {
        // TODO: Navigate to purchase orders tracking view
        System.out.println("Track orders clicked");
    }

    @FXML
    private void handleViewAllActivity() {
        // TODO: Navigate to full activity log
        System.out.println("View all activity clicked");
    }

    @FXML
    private void handleCreatePO() {
        // TODO: Open create purchase order dialog/form
        System.out.println("Create new PO clicked");
    }

    @FXML
    private void handleInventory() {
        // TODO: Navigate to inventory view
        System.out.println("Inventory clicked");
    }

    @FXML
    private void handlePurchaseOrders() {
        // TODO: Navigate to purchase orders view
        System.out.println("Purchase orders clicked");
    }

    @FXML
    private void handleAttendance() {
        // TODO: Navigate to attendance management view
        System.out.println("Attendance clicked");
    }

    @FXML
    private void handleEmployees() {
        // TODO: Navigate to employees view
        System.out.println("Employees clicked");
    }

    @FXML
    private void handleReports() {
        // TODO: Navigate to reports view
        System.out.println("Reports clicked");
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

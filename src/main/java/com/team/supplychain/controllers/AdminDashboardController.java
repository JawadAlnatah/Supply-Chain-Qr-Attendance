package com.team.supplychain.controllers;

import com.team.supplychain.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the Admin Dashboard view
 * Displays system-wide metrics, alerts, and management tools
 */
public class AdminDashboardController {

    // Header elements
    @FXML private Label currentUserLabel;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;

    // Metric cards
    @FXML private Label activeUsersLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label lowStockCountLabel;
    @FXML private Label securityIncidentsLabel;
    @FXML private Label pendingTasksLabel;

    // Chart
    @FXML private LineChart<String, Number> inventoryTrendChart;

    // Alerts panel
    @FXML private VBox criticalAlertsContainer;

    private User currentUser;

    /**
     * Set the current logged-in admin user
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
        // TODO: Load real-time data from database
        // TODO: Populate inventory trend chart
        // TODO: Load critical alerts
    }

    /**
     * Update the user interface with current user information
     */
    private void updateUserInterface() {
        if (currentUser != null) {
            currentUserLabel.setText("Admin: " + currentUser.getFirstName() + " " + currentUser.getLastName());
        }
    }

    // ==================== EVENT HANDLERS ====================

    @FXML
    private void handleSettings() {
        // TODO: Navigate to settings view
        System.out.println("Settings clicked");
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
    private void handleViewLowStock() {
        // TODO: Navigate to inventory view filtered by low stock
        System.out.println("View low stock clicked");
    }

    @FXML
    private void handleReviewSecurity() {
        // TODO: Navigate to security incidents view
        System.out.println("Review security incidents clicked");
    }

    @FXML
    private void handleViewTasks() {
        // TODO: Navigate to tasks/notifications view
        System.out.println("View pending tasks clicked");
    }

    @FXML
    private void handleViewAllAlerts() {
        // TODO: Navigate to full alerts view
        System.out.println("View all alerts clicked");
    }

    @FXML
    private void handleManageUsers() {
        // TODO: Navigate to user management view
        System.out.println("Manage users clicked");
    }

    @FXML
    private void handleViewInventory() {
        // TODO: Navigate to inventory view
        System.out.println("View inventory clicked");
    }

    @FXML
    private void handlePurchaseOrders() {
        // TODO: Navigate to purchase orders view
        System.out.println("Purchase orders clicked");
    }

    @FXML
    private void handleReports() {
        // TODO: Navigate to reports view
        System.out.println("Reports clicked");
    }

    @FXML
    private void handleSystemSettings() {
        // TODO: Navigate to system settings (PO automation, etc.)
        System.out.println("System settings clicked");
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

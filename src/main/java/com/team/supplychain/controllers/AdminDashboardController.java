package com.team.supplychain.controllers;

import com.team.supplychain.models.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the Admin Dashboard view
 * Displays system-wide metrics, alerts, and management tools
 */
public class AdminDashboardController {

    // ==================== HEADER ELEMENTS ====================
    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    @FXML private Button notificationsButton;

    // ==================== MAIN CONTENT AREA ====================
    @FXML private ScrollPane centerScrollPane;

    // ==================== SIDEBAR NAVIGATION ====================
    @FXML private Button dashboardButton;
    @FXML private Button usersButton;
    @FXML private Button systemButton;
    @FXML private Button securityButton;
    @FXML private Button auditButton;
    @FXML private Button reportsButton;

    // ==================== METRIC CARDS ====================
    @FXML private Label systemHealthLabel;
    @FXML private Label activeUsersLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label lowStockCountLabel;
    @FXML private Label securityIncidentsLabel;
    @FXML private Label pendingTasksLabel;

    // ==================== CHARTS & PANELS ====================
    @FXML private LineChart<String, Number> inventoryTrendChart;
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
        System.out.println("AdminDashboardController initialized with dummy data");
    }

    /**
     * Update the user interface with current user information
     */
    private void updateUserInterface() {
        if (currentUser != null && userNameLabel != null) {
            userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        }
    }

    // ==================== NAVIGATION EVENT HANDLERS ====================

    @FXML
    private void handleDashboard() {
        System.out.println("Dashboard navigation clicked");
        // TODO: Refresh dashboard view
    }

    @FXML
    private void handleManageUsers() {
        System.out.println("Manage Users clicked");
        loadContentView("/fxml/AdminUserManagement.fxml");
    }

    @FXML
    private void handleSystemSettings() {
        System.out.println("System Settings clicked");
        loadContentView("/fxml/AdminSystemSettings.fxml");
    }

    @FXML
    private void handleSecurity() {
        System.out.println("Security & Access clicked");
        loadContentView("/fxml/AdminSecurity.fxml");
    }

    @FXML
    private void handleAuditLogs() {
        System.out.println("Audit Logs clicked");
        loadContentView("/fxml/AdminAuditLogs.fxml");
    }

    @FXML
    private void handleReports() {
        System.out.println("Reports & Analytics clicked");
        loadContentView("/fxml/AdminReports.fxml");
    }

    // ==================== ACTION HANDLERS ====================

    @FXML
    private void handleViewLowStock() {
        System.out.println("View low stock clicked");
        // TODO: Navigate to inventory view filtered by low stock
    }

    @FXML
    private void handleReviewSecurity() {
        System.out.println("Review security incidents clicked");
        // TODO: Navigate to security incidents view
    }

    @FXML
    private void handleViewTasks() {
        System.out.println("View pending tasks clicked");
        // TODO: Navigate to tasks/notifications view
    }

    @FXML
    private void handleViewAlert() {
        System.out.println("View alert details clicked");
        // TODO: Show alert detail dialog
    }

    @FXML
    private void handleNotifications() {
        System.out.println("Notifications clicked");
        // TODO: Show notifications panel/dropdown
    }

    @FXML
    private void handleViewAllAlerts() {
        System.out.println("View all alerts clicked");
        // TODO: Navigate to full alerts view
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
     * Load a content view into the center scroll pane
     */
    private void loadContentView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent content = loader.load();

            // Get the controller and set current user if it has setCurrentUser method
            Object controller = loader.getController();
            if (controller instanceof AdminUserManagementController) {
                ((AdminUserManagementController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof AdminSystemSettingsController) {
                ((AdminSystemSettingsController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof AdminSecurityController) {
                ((AdminSecurityController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof AdminAuditLogsController) {
                ((AdminAuditLogsController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof AdminReportsController) {
                ((AdminReportsController) controller).setCurrentUser(currentUser);
            }

            // Replace content in center scroll pane
            if (centerScrollPane != null) {
                centerScrollPane.setContent(content);
            }

        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Failed to load page: " + fxmlPath);
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

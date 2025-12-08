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
import javafx.scene.control.ScrollPane;
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

    // Main content area
    @FXML private ScrollPane centerScrollPane;

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

    @FXML
    private void handleViewAttendance() {
        System.out.println("View attendance clicked");
        loadContentView("/fxml/ManagerAttendance.fxml");
    }

    @FXML
    private void handleTrackOrders() {
        System.out.println("Track orders clicked");
        loadContentView("/fxml/ManagerPurchaseOrders.fxml");
    }

    @FXML
    private void handleViewAllActivity() {
        System.out.println("View all activity clicked");
        // TODO: Create activity log page
    }

    @FXML
    private void handleCreatePO() {
        System.out.println("Create new PO clicked");
        // TODO: Open create purchase order dialog/form
    }

    @FXML
    private void handleInventory() {
        System.out.println("Inventory clicked");
        loadContentView("/fxml/ManagerInventory.fxml");
    }

    @FXML
    private void handlePurchaseOrders() {
        System.out.println("Purchase orders clicked");
        loadContentView("/fxml/ManagerPurchaseOrders.fxml");
    }

    @FXML
    private void handleAttendance() {
        System.out.println("Attendance clicked");
        loadContentView("/fxml/ManagerAttendance.fxml");
    }

    @FXML
    private void handleEmployees() {
        System.out.println("Employees clicked");
        loadContentView("/fxml/ManagerEmployees.fxml");
    }

    @FXML
    private void handleReports() {
        System.out.println("Reports clicked");
        loadContentView("/fxml/ManagerReports.fxml");
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
            if (controller instanceof ManagerInventoryController) {
                ((ManagerInventoryController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof ManagerPurchaseOrdersController) {
                ((ManagerPurchaseOrdersController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof ManagerAttendanceController) {
                ((ManagerAttendanceController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof ManagerEmployeesController) {
                ((ManagerEmployeesController) controller).setCurrentUser(currentUser);
            } else if (controller instanceof ManagerReportsController) {
                ((ManagerReportsController) controller).setCurrentUser(currentUser);
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

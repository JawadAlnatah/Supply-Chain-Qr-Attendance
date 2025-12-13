package com.team.supplychain.controllers;

import com.team.supplychain.dao.AuditLogDAO;
import com.team.supplychain.dao.InventoryDAO;
import com.team.supplychain.dao.RequisitionDAO;
import com.team.supplychain.dao.UserDAO;
import com.team.supplychain.models.User;
import javafx.concurrent.Task;
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
    private Parent originalDashboardContent;

    // ==================== DAOs ====================
    private UserDAO userDAO;
    private InventoryDAO inventoryDAO;
    private RequisitionDAO requisitionDAO;
    private AuditLogDAO auditLogDAO;

    /**
     * Helper class to hold all dashboard metrics loaded in background
     */
    private static class DashboardMetrics {
        int activeUsers;
        int totalItems;
        int lowStockCount;
        int outOfStockCount;
        int pendingRequisitions;
        int securityIncidents;
        String systemHealth;

        DashboardMetrics(int activeUsers, int totalItems, int lowStockCount,
                        int outOfStockCount, int pendingRequisitions, int securityIncidents) {
            this.activeUsers = activeUsers;
            this.totalItems = totalItems;
            this.lowStockCount = lowStockCount;
            this.outOfStockCount = outOfStockCount;
            this.pendingRequisitions = pendingRequisitions;
            this.securityIncidents = securityIncidents;
            this.systemHealth = calculateSystemHealth(lowStockCount, activeUsers, totalItems);
        }

        private static String calculateSystemHealth(int lowStockCount, int activeUsers, int totalItems) {
            int healthScore = 100;

            if (totalItems > 0) {
                double lowStockPercentage = (lowStockCount * 100.0) / totalItems;
                if (lowStockPercentage > 30) {
                    healthScore -= 40;
                } else if (lowStockPercentage > 15) {
                    healthScore -= 20;
                } else if (lowStockPercentage > 5) {
                    healthScore -= 10;
                }
            }

            if (activeUsers < 5) {
                healthScore -= 10;
            }

            if (healthScore >= 90) {
                return "Excellent";
            } else if (healthScore >= 75) {
                return "Good";
            } else if (healthScore >= 60) {
                return "Fair";
            } else if (healthScore >= 40) {
                return "Poor";
            } else {
                return "Critical";
            }
        }
    }

    /**
     * Set the current logged-in admin user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInterface();
    }

    /**
     * Initialize the controller - loads all data asynchronously
     */
    @FXML
    private void initialize() {
        // Save the original dashboard content so we can restore it later
        if (centerScrollPane != null && centerScrollPane.getContent() != null) {
            originalDashboardContent = (Parent) centerScrollPane.getContent();
        }

        // Initialize DAOs
        userDAO = new UserDAO();
        inventoryDAO = new InventoryDAO();
        requisitionDAO = new RequisitionDAO();
        auditLogDAO = new AuditLogDAO();

        // Load all data asynchronously
        loadDashboardDataAsync();

        System.out.println("AdminDashboardController initialized - loading data in background");
    }

    /**
     * Load all dashboard data in a single background task
     */
    private void loadDashboardDataAsync() {
        Task<DashboardMetrics> loadTask = new Task<>() {
            @Override
            protected DashboardMetrics call() throws Exception {
                // Load all metrics in background thread
                // Note: Each query is called ONCE (no duplicates)
                int activeUsers = userDAO.getActiveUserCount();
                int totalItems = inventoryDAO.getTotalItemsCount();
                int lowStockCount = inventoryDAO.getLowStockCount();
                int outOfStockCount = inventoryDAO.getOutOfStockCount();
                int pendingRequisitions = requisitionDAO.getPendingRequisitionsCount();
                int securityIncidents = auditLogDAO.getRecentAuditLogCount();

                return new DashboardMetrics(
                    activeUsers, totalItems, lowStockCount,
                    outOfStockCount, pendingRequisitions, securityIncidents
                );
            }
        };

        // Update UI when data is loaded
        loadTask.setOnSucceeded(e -> {
            DashboardMetrics metrics = loadTask.getValue();

            // Update metric cards
            if (activeUsersLabel != null) {
                activeUsersLabel.setText(String.valueOf(metrics.activeUsers));
            }
            if (totalItemsLabel != null) {
                totalItemsLabel.setText(String.valueOf(metrics.totalItems));
            }
            if (lowStockCountLabel != null) {
                lowStockCountLabel.setText(String.valueOf(metrics.lowStockCount));
            }
            if (pendingTasksLabel != null) {
                pendingTasksLabel.setText(String.valueOf(metrics.pendingRequisitions));
            }
            if (securityIncidentsLabel != null) {
                securityIncidentsLabel.setText(String.valueOf(metrics.securityIncidents));
            }
            if (systemHealthLabel != null) {
                systemHealthLabel.setText(metrics.systemHealth);
            }

            // Update critical alerts using already-loaded metrics
            updateCriticalAlerts(metrics);

            System.out.println("Dashboard data loaded successfully");
        });

        // Handle errors
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            exception.printStackTrace();
            System.err.println("Error loading dashboard metrics: " + exception.getMessage());
        });

        // Start background task
        new Thread(loadTask).start();
    }

    /**
     * Update the user interface with current user information
     */
    private void updateUserInterface() {
        if (currentUser != null && userNameLabel != null) {
            userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        }
    }

    /**
     * Load dashboard metrics from database
     */
    private void loadDashboardMetrics() {
        try {
            // Active Users Count
            int activeUsers = userDAO.getActiveUserCount();
            if (activeUsersLabel != null) {
                activeUsersLabel.setText(String.valueOf(activeUsers));
            }

            // Total Inventory Items
            int totalItems = inventoryDAO.getTotalItemsCount();
            if (totalItemsLabel != null) {
                totalItemsLabel.setText(String.valueOf(totalItems));
            }

            // Low Stock Count
            int lowStockCount = inventoryDAO.getLowStockCount();
            if (lowStockCountLabel != null) {
                lowStockCountLabel.setText(String.valueOf(lowStockCount));
            }

            // Pending Tasks (Requisitions)
            int pendingTasks = requisitionDAO.getPendingRequisitionsCount();
            if (pendingTasksLabel != null) {
                pendingTasksLabel.setText(String.valueOf(pendingTasks));
            }

            // Security Incidents (count from audit logs)
            int securityIncidents = auditLogDAO.getRecentAuditLogCount();
            if (securityIncidentsLabel != null) {
                securityIncidentsLabel.setText(String.valueOf(securityIncidents));
            }

            // System Health - based on low stock and other metrics
            String systemHealth = calculateSystemHealth(lowStockCount, activeUsers, totalItems);
            if (systemHealthLabel != null) {
                systemHealthLabel.setText(systemHealth);
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading dashboard metrics: " + e.getMessage());
        }
    }

    /**
     * Calculate system health based on various metrics
     */
    private String calculateSystemHealth(int lowStockCount, int activeUsers, int totalItems) {
        // Calculate health percentage based on multiple factors
        int healthScore = 100;

        // Deduct points for low stock issues
        if (totalItems > 0) {
            double lowStockPercentage = (lowStockCount * 100.0) / totalItems;
            if (lowStockPercentage > 30) {
                healthScore -= 40; // Critical
            } else if (lowStockPercentage > 15) {
                healthScore -= 20; // Warning
            } else if (lowStockPercentage > 5) {
                healthScore -= 10; // Minor
            }
        }

        // Deduct points for low active users
        if (activeUsers < 5) {
            healthScore -= 10;
        }

        // Return health status
        if (healthScore >= 90) {
            return "Excellent";
        } else if (healthScore >= 75) {
            return "Good";
        } else if (healthScore >= 60) {
            return "Fair";
        } else if (healthScore >= 40) {
            return "Poor";
        } else {
            return "Critical";
        }
    }

    /**
     * Update critical alerts using already-loaded metrics (no duplicate queries)
     */
    private void updateCriticalAlerts(DashboardMetrics metrics) {
        if (criticalAlertsContainer == null) {
            return;
        }

        // Clear existing alerts
        criticalAlertsContainer.getChildren().clear();

        // Check for low stock items
        if (metrics.lowStockCount > 0) {
            addAlertToContainer(
                "Low Stock Alert",
                metrics.lowStockCount + " items are below reorder level",
                "warning"
            );
        }

        // Check for out of stock items
        if (metrics.outOfStockCount > 0) {
            addAlertToContainer(
                "Out of Stock",
                metrics.outOfStockCount + " items are completely out of stock",
                "critical"
            );
        }

        // Check for pending requisitions
        if (metrics.pendingRequisitions > 5) {
            addAlertToContainer(
                "Pending Requisitions",
                metrics.pendingRequisitions + " requisitions awaiting review",
                "info"
            );
        }

        // If no alerts, show a positive message
        if (criticalAlertsContainer.getChildren().isEmpty()) {
            addAlertToContainer(
                "All Systems Normal",
                "No critical alerts at this time",
                "success"
            );
        }
    }

    /**
     * Load critical alerts from database (deprecated - use updateCriticalAlerts instead)
     */
    @Deprecated
    private void loadCriticalAlerts() {
        try {
            if (criticalAlertsContainer != null) {
                // Clear existing alerts
                criticalAlertsContainer.getChildren().clear();

                // Check for low stock items
                int lowStockCount = inventoryDAO.getLowStockCount();
                if (lowStockCount > 0) {
                    addAlertToContainer(
                        "Low Stock Alert",
                        lowStockCount + " items are below reorder level",
                        "warning"
                    );
                }

                // Check for out of stock items
                int outOfStockCount = inventoryDAO.getOutOfStockCount();
                if (outOfStockCount > 0) {
                    addAlertToContainer(
                        "Out of Stock",
                        outOfStockCount + " items are completely out of stock",
                        "critical"
                    );
                }

                // Check for pending requisitions
                int pendingRequisitions = requisitionDAO.getPendingRequisitionsCount();
                if (pendingRequisitions > 5) {
                    addAlertToContainer(
                        "Pending Requisitions",
                        pendingRequisitions + " requisitions awaiting review",
                        "info"
                    );
                }

                // If no alerts, show a positive message
                if (criticalAlertsContainer.getChildren().isEmpty()) {
                    addAlertToContainer(
                        "All Systems Normal",
                        "No critical alerts at this time",
                        "success"
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading critical alerts: " + e.getMessage());
        }
    }

    /**
     * Add an alert to the alerts container
     */
    private void addAlertToContainer(String title, String message, String type) {
        Label alertLabel = new Label(title + ": " + message);
        alertLabel.setWrapText(true);
        alertLabel.setMaxWidth(Double.MAX_VALUE);
        alertLabel.setStyle(getAlertStyle(type));
        criticalAlertsContainer.getChildren().add(alertLabel);
    }

    /**
     * Get CSS style for alert type
     */
    private String getAlertStyle(String type) {
        switch (type.toLowerCase()) {
            case "critical":
                return "-fx-background-color: #fee; -fx-padding: 10; -fx-background-radius: 5; " +
                       "-fx-border-color: #f88; -fx-border-width: 1; -fx-border-radius: 5; " +
                       "-fx-text-fill: #c00; -fx-font-weight: bold;";
            case "warning":
                return "-fx-background-color: #ffc; -fx-padding: 10; -fx-background-radius: 5; " +
                       "-fx-border-color: #fc0; -fx-border-width: 1; -fx-border-radius: 5; " +
                       "-fx-text-fill: #840;";
            case "info":
                return "-fx-background-color: #def; -fx-padding: 10; -fx-background-radius: 5; " +
                       "-fx-border-color: #8cf; -fx-border-width: 1; -fx-border-radius: 5; " +
                       "-fx-text-fill: #048;";
            case "success":
                return "-fx-background-color: #dfd; -fx-padding: 10; -fx-background-radius: 5; " +
                       "-fx-border-color: #8d8; -fx-border-width: 1; -fx-border-radius: 5; " +
                       "-fx-text-fill: #040;";
            default:
                return "-fx-background-color: #eee; -fx-padding: 10; -fx-background-radius: 5;";
        }
    }

    // ==================== NAVIGATION EVENT HANDLERS ====================

    @FXML
    private void handleDashboard() {
        System.out.println("Dashboard navigation clicked - restoring main dashboard view");

        // Restore the original dashboard content
        if (centerScrollPane != null && originalDashboardContent != null) {
            centerScrollPane.setContent(originalDashboardContent);
        }
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

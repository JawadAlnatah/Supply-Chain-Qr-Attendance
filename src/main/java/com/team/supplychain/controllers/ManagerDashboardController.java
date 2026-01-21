package com.team.supplychain.controllers;

import com.team.supplychain.dao.InventoryDAO;
import com.team.supplychain.models.InventoryItem;
import com.team.supplychain.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Manager Dashboard view
 * Displays operations metrics, purchase order tracking, and team activity
 */
public class ManagerDashboardController {

    // Header elements
    @FXML private Label currentUserLabel;
    @FXML private Button notificationsButton;
    @FXML private Button logoutButton;
    @FXML private Button dashboardButton;

    // Main content area
    @FXML private ScrollPane centerScrollPane;
    private javafx.scene.Node initialDashboardContent;

    // Metric cards
    @FXML private Label inventorySummaryLabel;
    @FXML private ProgressBar inventoryHealthBar;
    @FXML private Label onSiteEmployeesLabel;
    @FXML private Label posInTransitLabel;

    // Charts
    @FXML private PieChart stockLevelsPieChart;

    // Activity and PO containers
    @FXML private VBox recentActivityContainer;
    @FXML private VBox posInProgressContainer;

    private User currentUser;
    private final InventoryDAO inventoryDAO = new InventoryDAO();

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
        // Save the initial dashboard content so we can restore it later
        if (centerScrollPane != null) {
            initialDashboardContent = centerScrollPane.getContent();
        }

        // Load inventory distribution pie chart
        loadInventoryPieChart();

        // TODO: Load real-time operations data from database
        // TODO: Load recent activity/alerts
        // TODO: Load purchase orders in progress with ETA countdown
    }

    /**
     * Load and display inventory distribution pie chart
     */
    private void loadInventoryPieChart() {
        if (stockLevelsPieChart == null) return;

        // Fetch inventory data from database
        List<InventoryItem> items = inventoryDAO.getAllInventoryItems();
        if (items == null || items.isEmpty()) return;

        // Group items by category and sum quantities
        Map<String, Integer> categoryQuantities = new HashMap<>();
        for (InventoryItem item : items) {
            String category = item.getCategory();
            int quantity = item.getQuantity();
            categoryQuantities.put(category, categoryQuantities.getOrDefault(category, 0) + quantity);
        }

        // Create pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        // Define vibrant solid colors for different categories
        String[] colors = {
            "#8b5cf6",  // Purple
            "#3b82f6",  // Blue
            "#10b981",  // Green
            "#f59e0b",  // Amber
            "#ef4444",  // Red
            "#7c3aed"   // Deep Purple
        };

        // Add data for each category
        for (Map.Entry<String, Integer> entry : categoryQuantities.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            pieChartData.add(slice);
        }

        // Set data to pie chart
        stockLevelsPieChart.setData(pieChartData);

        // Apply individual colors to each slice with enhanced styling
        javafx.application.Platform.runLater(() -> {
            int colorIndex = 0;
            for (PieChart.Data data : pieChartData) {
                javafx.scene.Node node = data.getNode();
                if (node != null) {
                    String color = colors[colorIndex % colors.length];

                    // Base style with shadow
                    String baseStyle =
                        "-fx-pie-color: " + color + "; " +
                        "-fx-border-width: 2px; " +
                        "-fx-border-color: white; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.2), 8, 0.1, 0, 2);";

                    node.setStyle(baseStyle);

                    // Enhanced hover effect
                    node.setOnMouseEntered(e -> {
                        node.setStyle(
                            "-fx-pie-color: " + color + "; " +
                            "-fx-border-width: 3px; " +
                            "-fx-border-color: white; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0, 0, 0, 0.4), 15, 0.3, 0, 4);"
                        );
                        node.setScaleX(1.08);
                        node.setScaleY(1.08);
                    });

                    node.setOnMouseExited(e -> {
                        node.setStyle(baseStyle);
                        node.setScaleX(1.0);
                        node.setScaleY(1.0);
                    });
                }
                colorIndex++;
            }
        });
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
    private void handleDashboard() {
        System.out.println("Dashboard clicked");
        // Restore the initial dashboard content
        if (centerScrollPane != null && initialDashboardContent != null) {
            centerScrollPane.setContent(initialDashboardContent);
        }
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

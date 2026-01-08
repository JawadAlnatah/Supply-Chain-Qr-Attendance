package com.team.supplychain.controllers;

import com.team.supplychain.dao.InventoryDAO;
import com.team.supplychain.models.InventoryItem;
import com.team.supplychain.models.User;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Manager Inventory View
 * Manages inventory items, stock levels, and item operations
 */
public class ManagerInventoryController {

    // ==================== STATS LABELS ====================
    @FXML private Label totalItemsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Label totalValueLabel;

    // ==================== FILTERS & SEARCH ====================
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextField searchField;

    // ==================== BUTTONS ====================
    @FXML private Button exportButton;
    @FXML private Button refreshButton;

    // ==================== CHART ====================
    @FXML private BarChart<String, Number> inventoryChart;
    @FXML private CategoryAxis categoryAxis;
    @FXML private NumberAxis quantityAxis;

    // ==================== TABLE ====================
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, Integer> itemIdColumn;
    @FXML private TableColumn<InventoryItem, String> itemNameColumn;
    @FXML private TableColumn<InventoryItem, String> categoryColumn;
    @FXML private TableColumn<InventoryItem, Integer> quantityColumn;
    @FXML private TableColumn<InventoryItem, Integer> reorderLevelColumn;
    @FXML private TableColumn<InventoryItem, String> unitPriceColumn;
    @FXML private TableColumn<InventoryItem, String> totalValueColumn;
    @FXML private TableColumn<InventoryItem, String> statusColumn;
    @FXML private TableColumn<InventoryItem, Void> actionsColumn;

    private User currentUser;
    private ObservableList<InventoryItem> inventoryData;
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    /**
     * Set the current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        System.out.println("ManagerInventoryController initialized");

        // Initialize data
        inventoryData = FXCollections.observableArrayList();

        // Setup table
        setupInventoryTable();

        // Setup filters
        setupFilters();

        // Load data from database
        loadInventoryFromDatabase();
    }

    /**
     * Setup inventory table columns
     */
    private void setupInventoryTable() {
        if (inventoryTable == null) return;

        // Item ID Column
        itemIdColumn.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getItemId()).asObject());

        // Item Name Column
        itemNameColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getItemName()));

        // Category Column
        categoryColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCategory()));

        // Quantity Column
        quantityColumn.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());

        // Reorder Level Column
        reorderLevelColumn.setCellValueFactory(cellData ->
            new SimpleIntegerProperty(cellData.getValue().getReorderLevel()).asObject());

        // Unit Price Column
        unitPriceColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty("SAR " + String.format("%.2f", cellData.getValue().getUnitPrice())));

        // Total Value Column
        totalValueColumn.setCellValueFactory(cellData -> {
            BigDecimal total = cellData.getValue().getTotalValue();
            return new SimpleStringProperty("SAR " + String.format("%.2f", total));
        });

        // Status Column with colored badges (calculated from needsReorder)
        statusColumn.setCellValueFactory(cellData -> {
            InventoryItem item = cellData.getValue();
            String status;
            if (item.getQuantity() == 0) {
                status = "Out of Stock";
            } else if (item.needsReorder()) {
                status = "Low Stock";
            } else {
                status = "In Stock";
            }
            return new SimpleStringProperty(status);
        });

        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label statusLabel = new Label(status);
                    statusLabel.setStyle(
                        "-fx-background-radius: 12px; " +
                        "-fx-padding: 5px 12px; " +
                        "-fx-font-size: 11px; " +
                        "-fx-font-weight: bold; " +
                        getStatusStyle(status)
                    );
                    setGraphic(statusLabel);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }

            private String getStatusStyle(String status) {
                switch (status) {
                    case "In Stock":
                        return "-fx-background-color: rgba(34,197,94,0.15); -fx-text-fill: #16a34a;";
                    case "Low Stock":
                        return "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
                    case "Out of Stock":
                        return "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
                    default:
                        return "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
                }
            }
        });

        // Actions Column - Disabled for view-only mode
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(null); // Disabled - view only mode
            }
        });

        inventoryTable.setItems(inventoryData);
    }

    /**
     * Setup filter combo box
     */
    private void setupFilters() {
        if (categoryFilter != null) {
            categoryFilter.getItems().addAll("All Categories", "Electronics", "Furniture",
                "Office Supplies", "Raw Materials", "Packaging");
            categoryFilter.setValue("All Categories");

            // Wire category filter to reload data
            categoryFilter.setOnAction(e -> {
                String selected = categoryFilter.getValue();
                if ("All Categories".equals(selected)) {
                    loadInventoryFromDatabase();
                } else {
                    loadInventoryByCategory(selected);
                }
            });
        }

        // Wire search field to filter data
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue == null || newValue.trim().isEmpty()) {
                    loadInventoryFromDatabase();
                } else {
                    searchInventory(newValue.trim());
                }
            });
        }
    }

    /**
     * Load all inventory items from database using background thread
     */
    private void loadInventoryFromDatabase() {
        Task<List<InventoryItem>> loadTask = new Task<>() {
            @Override
            protected List<InventoryItem> call() {
                return inventoryDAO.getAllInventoryItems();
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<InventoryItem> items = loadTask.getValue();
            if (items != null) {
                inventoryData.clear();
                inventoryData.addAll(items);
                updateStats();
                System.out.println("Loaded " + items.size() + " inventory items from database");
            } else {
                System.err.println("Failed to load inventory - null result");
                showError("Database Error", "Failed to load inventory items from database");
            }
        });

        loadTask.setOnFailed(event -> {
            Throwable error = loadTask.getException();
            error.printStackTrace();
            showError("Database Error", "Failed to load inventory: " + error.getMessage());
        });

        new Thread(loadTask).start();
    }

    /**
     * Load inventory items by category using background thread
     */
    private void loadInventoryByCategory(String category) {
        Task<List<InventoryItem>> loadTask = new Task<>() {
            @Override
            protected List<InventoryItem> call() {
                return inventoryDAO.getInventoryItemsByCategory(category);
            }
        };

        loadTask.setOnSucceeded(event -> {
            List<InventoryItem> items = loadTask.getValue();
            if (items != null) {
                inventoryData.clear();
                inventoryData.addAll(items);
                updateStats();
                System.out.println("Loaded " + items.size() + " items in category: " + category);
            }
        });

        loadTask.setOnFailed(event -> {
            Throwable error = loadTask.getException();
            error.printStackTrace();
            showError("Database Error", "Failed to filter by category: " + error.getMessage());
        });

        new Thread(loadTask).start();
    }

    /**
     * Search inventory items using background thread
     */
    private void searchInventory(String searchTerm) {
        Task<List<InventoryItem>> searchTask = new Task<>() {
            @Override
            protected List<InventoryItem> call() {
                return inventoryDAO.searchInventoryItems(searchTerm);
            }
        };

        searchTask.setOnSucceeded(event -> {
            List<InventoryItem> items = searchTask.getValue();
            if (items != null) {
                inventoryData.clear();
                inventoryData.addAll(items);
                updateStats();
                System.out.println("Found " + items.size() + " items matching: " + searchTerm);
            }
        });

        searchTask.setOnFailed(event -> {
            Throwable error = searchTask.getException();
            error.printStackTrace();
            showError("Database Error", "Failed to search inventory: " + error.getMessage());
        });

        new Thread(searchTask).start();
    }

    /**
     * Update statistics labels based on current inventory data
     */
    private void updateStats() {
        int totalItems = inventoryData.size();

        // Count items needing reorder (low stock)
        long lowStock = inventoryData.stream()
            .filter(InventoryItem::needsReorder)
            .filter(item -> item.getQuantity() > 0) // Not out of stock
            .count();

        // Count out of stock items
        long outOfStock = inventoryData.stream()
            .filter(item -> item.getQuantity() == 0)
            .count();

        // Calculate total inventory value
        BigDecimal totalValue = inventoryData.stream()
            .map(InventoryItem::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Update UI labels
        if (totalItemsLabel != null) totalItemsLabel.setText(String.valueOf(totalItems));
        if (lowStockLabel != null) lowStockLabel.setText(String.valueOf(lowStock));
        if (outOfStockLabel != null) outOfStockLabel.setText(String.valueOf(outOfStock));
        if (totalValueLabel != null) totalValueLabel.setText("SAR " + String.format("%,.2f", totalValue));

        // Update chart
        updateChart();
    }

    /**
     * Update the inventory distribution chart by category
     */
    private void updateChart() {
        if (inventoryChart == null) return;

        // Group items by category and sum quantities
        Map<String, Integer> categoryQuantities = new HashMap<>();
        for (InventoryItem item : inventoryData) {
            String category = item.getCategory();
            int quantity = item.getQuantity();
            categoryQuantities.put(category, categoryQuantities.getOrDefault(category, 0) + quantity);
        }

        // Create chart data series
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantity");

        // Add data points for each category
        for (Map.Entry<String, Integer> entry : categoryQuantities.entrySet()) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
            series.getData().add(data);
        }

        // Clear existing data and add new series
        inventoryChart.getData().clear();
        inventoryChart.getData().add(series);

        // Style the bars with purple gradient
        inventoryChart.setStyle("-fx-bar-fill: linear-gradient(to bottom, #a78bfa, #8b5cf6);");
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Handle export button - Export inventory data to CSV file
     */
    @FXML
    private void handleExport() {
        System.out.println("Export clicked");

        // Create file chooser dialog
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Save Inventory Report");
        fileChooser.setInitialFileName("inventory_report_" + java.time.LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        // Show save dialog
        java.io.File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());

        if (file != null) {
            try {
                exportToCSV(file);
                showInfo("Export Successful", "Inventory report has been exported to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Export Failed", "Failed to export inventory report:\n" + e.getMessage());
            }
        }
    }

    /**
     * Export inventory data to CSV file
     */
    private void exportToCSV(java.io.File file) throws Exception {
        try (java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(file))) {
            // Write CSV header
            writer.write("ID,Item Name,Category,Quantity,Reorder Level,Unit Price,Total Value,Status");
            writer.newLine();

            // Write data rows
            for (InventoryItem item : inventoryData) {
                // Calculate total value
                BigDecimal totalValue = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));

                // Determine status
                String status;
                if (item.getQuantity() == 0) {
                    status = "Out of Stock";
                } else if (item.getQuantity() <= item.getReorderLevel()) {
                    status = "Low Stock";
                } else {
                    status = "In Stock";
                }

                // Escape and write fields
                writer.write(String.format("%d,%s,%s,%d,%d,%s,%s,%s",
                    item.getItemId(),
                    escapeCSV(item.getItemName()),
                    escapeCSV(item.getCategory()),
                    item.getQuantity(),
                    item.getReorderLevel(),
                    item.getUnitPrice().toString(),
                    totalValue.toString(),
                    status
                ));
                writer.newLine();
            }
        }
    }

    /**
     * Escape special characters in CSV fields
     */
    private String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        // If the value contains comma, quote, or newline, wrap it in quotes and escape quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Handle refresh button - reload all data from database
     */
    @FXML
    private void handleRefresh() {
        System.out.println("Refreshing inventory data...");
        loadInventoryFromDatabase();
    }

    // ==================== UTILITY METHODS ====================

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

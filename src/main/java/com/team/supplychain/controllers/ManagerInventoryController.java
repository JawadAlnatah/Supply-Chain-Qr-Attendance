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
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
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
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;
    @FXML private Label itemCountLabel;
    @FXML private Label paginationLabel;

    // ==================== BUTTONS ====================
    @FXML private Button exportButton;
    @FXML private Button refreshButton;

    // ==================== CHART ====================
    @FXML private PieChart inventoryPieChart;

    // ==================== TABLE ====================
    @FXML private TableView<InventoryItem> inventoryTable;
    @FXML private TableColumn<InventoryItem, Integer> itemIdColumn;
    @FXML private TableColumn<InventoryItem, String> itemNameColumn;
    @FXML private TableColumn<InventoryItem, String> categoryColumn;
    @FXML private TableColumn<InventoryItem, String> locationColumn;
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

        // Location Column
        locationColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getLocation()));

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

        // Actions Column with View and Edit buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            private final Button editBtn = new Button("Edit");
            private final HBox container = new HBox(8, viewBtn, editBtn);

            {
                // Style View button
                viewBtn.setStyle(
                    "-fx-background-color: rgba(167,139,250,0.15); " +
                    "-fx-text-fill: #a78bfa; " +
                    "-fx-background-radius: 8px; " +
                    "-fx-padding: 6px 14px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;"
                );

                // Style Edit button
                editBtn.setStyle(
                    "-fx-background-color: rgba(34,197,94,0.15); " +
                    "-fx-text-fill: #16a34a; " +
                    "-fx-background-radius: 8px; " +
                    "-fx-padding: 6px 14px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;"
                );

                // Hover effects
                viewBtn.setOnMouseEntered(e -> viewBtn.setStyle(
                    "-fx-background-color: rgba(167,139,250,0.25); " +
                    "-fx-text-fill: #a78bfa; " +
                    "-fx-background-radius: 8px; " +
                    "-fx-padding: 6px 14px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;"
                ));
                viewBtn.setOnMouseExited(e -> viewBtn.setStyle(
                    "-fx-background-color: rgba(167,139,250,0.15); " +
                    "-fx-text-fill: #a78bfa; " +
                    "-fx-background-radius: 8px; " +
                    "-fx-padding: 6px 14px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;"
                ));

                editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                    "-fx-background-color: rgba(34,197,94,0.25); " +
                    "-fx-text-fill: #16a34a; " +
                    "-fx-background-radius: 8px; " +
                    "-fx-padding: 6px 14px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;"
                ));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(
                    "-fx-background-color: rgba(34,197,94,0.15); " +
                    "-fx-text-fill: #16a34a; " +
                    "-fx-background-radius: 8px; " +
                    "-fx-padding: 6px 14px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-cursor: hand;"
                ));

                // Button actions
                viewBtn.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleViewItem(item);
                });

                editBtn.setOnAction(e -> {
                    InventoryItem item = getTableView().getItems().get(getIndex());
                    handleEditItem(item);
                });

                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
                setAlignment(Pos.CENTER);
            }
        });

        inventoryTable.setItems(inventoryData);
    }

    /**
     * Setup filter combo boxes and search field
     */
    private void setupFilters() {
        if (categoryFilter != null) {
            categoryFilter.getItems().addAll("All Categories", "Electronics", "Furniture",
                "Office Supplies", "Raw Materials", "Packaging");
            categoryFilter.setValue("All Categories");

            // Wire category filter to apply filters
            categoryFilter.setOnAction(e -> applyFilters());
        }

        if (statusFilter != null) {
            statusFilter.getItems().addAll("All Stock Status", "In Stock", "Low Stock", "Out of Stock");
            statusFilter.setValue("All Stock Status");

            // Wire status filter to apply filters
            statusFilter.setOnAction(e -> applyFilters());
        }

        // Wire search field to filter data
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        }
    }

    /**
     * Apply all filters (category, status, search) to inventory data
     */
    private void applyFilters() {
        Task<List<InventoryItem>> filterTask = new Task<>() {
            @Override
            protected List<InventoryItem> call() {
                List<InventoryItem> items = inventoryDAO.getAllInventoryItems();
                if (items == null) return FXCollections.observableArrayList();

                String selectedCategory = categoryFilter != null ? categoryFilter.getValue() : "All Categories";
                String selectedStatus = statusFilter != null ? statusFilter.getValue() : "All Stock Status";
                String searchText = searchField != null ? searchField.getText() : "";

                return items.stream()
                    .filter(item -> {
                        // Filter by category
                        if (!"All Categories".equals(selectedCategory) && !item.getCategory().equals(selectedCategory)) {
                            return false;
                        }

                        // Filter by status
                        if (!"All Stock Status".equals(selectedStatus)) {
                            String itemStatus = getItemStatus(item);
                            if (!itemStatus.equals(selectedStatus)) {
                                return false;
                            }
                        }

                        // Filter by search text
                        if (searchText != null && !searchText.trim().isEmpty()) {
                            String search = searchText.toLowerCase();
                            return item.getItemName().toLowerCase().contains(search) ||
                                   item.getCategory().toLowerCase().contains(search) ||
                                   (item.getLocation() != null && item.getLocation().toLowerCase().contains(search));
                        }

                        return true;
                    })
                    .collect(java.util.stream.Collectors.toList());
            }
        };

        filterTask.setOnSucceeded(event -> {
            List<InventoryItem> filteredItems = filterTask.getValue();
            inventoryData.clear();
            inventoryData.addAll(filteredItems);
            updateStats();
        });

        filterTask.setOnFailed(event -> {
            Throwable error = filterTask.getException();
            error.printStackTrace();
            showError("Filter Error", "Failed to apply filters: " + error.getMessage());
        });

        new Thread(filterTask).start();
    }

    /**
     * Get status string for an inventory item
     */
    private String getItemStatus(InventoryItem item) {
        if (item.getQuantity() == 0) {
            return "Out of Stock";
        } else if (item.needsReorder()) {
            return "Low Stock";
        } else {
            return "In Stock";
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

        // Update item count and pagination labels
        if (itemCountLabel != null) {
            itemCountLabel.setText("(" + totalItems + (totalItems == 1 ? " item)" : " items)"));
        }
        if (paginationLabel != null) {
            paginationLabel.setText(totalItems + (totalItems == 1 ? " item" : " items"));
        }

        // Update chart
        updateChart();
    }

    /**
     * Update the inventory distribution pie chart by category with enhanced styling
     */
    private void updateChart() {
        if (inventoryPieChart == null) return;

        // Group items by category and sum quantities
        Map<String, Integer> categoryQuantities = new HashMap<>();
        for (InventoryItem item : inventoryData) {
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
        int index = 0;
        for (Map.Entry<String, Integer> entry : categoryQuantities.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")", entry.getValue());
            pieChartData.add(slice);
            index++;
        }

        // Set data to pie chart
        inventoryPieChart.setData(pieChartData);

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

        // Style the chart background
        inventoryPieChart.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-padding: 20px;"
        );
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

    /**
     * Handle clear filters button
     */
    @FXML
    private void handleClearFilters() {
        System.out.println("Clearing all filters...");
        if (categoryFilter != null) categoryFilter.setValue("All Categories");
        if (statusFilter != null) statusFilter.setValue("All Stock Status");
        if (searchField != null) searchField.clear();
        loadInventoryFromDatabase();
    }

    /**
     * Handle view item details
     */
    private void handleViewItem(InventoryItem item) {
        System.out.println("View item: " + item.getItemName());

        StringBuilder details = new StringBuilder();
        details.append("ITEM DETAILS\n");
        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        details.append("Item ID: ").append(item.getItemId()).append("\n");
        details.append("Item Name: ").append(item.getItemName()).append("\n");
        details.append("Category: ").append(item.getCategory()).append("\n");
        details.append("Location: ").append(item.getLocation() != null ? item.getLocation() : "N/A").append("\n\n");

        details.append("STOCK INFORMATION\n");
        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        details.append("Quantity: ").append(item.getQuantity()).append(" units\n");
        details.append("Reorder Level: ").append(item.getReorderLevel()).append(" units\n");
        details.append("Status: ").append(getItemStatus(item)).append("\n\n");

        details.append("PRICING\n");
        details.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        details.append("Unit Price: SAR ").append(String.format("%.2f", item.getUnitPrice())).append("\n");
        details.append("Total Value: SAR ").append(String.format("%.2f", item.getTotalValue())).append("\n");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inventory Item Details");
        alert.setHeaderText(item.getItemName());
        alert.setContentText(details.toString());
        alert.getDialogPane().setMinWidth(500);
        alert.showAndWait();
    }

    /**
     * Handle edit item
     */
    private void handleEditItem(InventoryItem item) {
        System.out.println("Edit item: " + item.getItemName());
        showInfo("Edit Item", "Edit functionality is not yet implemented.\n\nItem: " + item.getItemName());
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

package com.team.supplychain.controllers;

import com.team.supplychain.models.User;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;

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
    @FXML private Button addItemButton;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;

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

        // Load dummy data
        loadDummyInventoryData();

        // Setup filters
        setupFilters();

        // Update stats
        updateStats();
    }

    /**
     * Setup inventory table columns
     */
    private void setupInventoryTable() {
        if (inventoryTable == null) return;

        // Item ID Column
        itemIdColumn.setCellValueFactory(new PropertyValueFactory<>("itemId"));

        // Item Name Column
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));

        // Category Column
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Quantity Column
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        // Reorder Level Column
        reorderLevelColumn.setCellValueFactory(new PropertyValueFactory<>("reorderLevel"));

        // Unit Price Column
        unitPriceColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty("$" + cellData.getValue().getUnitPrice().toString()));

        // Total Value Column
        totalValueColumn.setCellValueFactory(cellData -> {
            BigDecimal total = cellData.getValue().getUnitPrice()
                .multiply(BigDecimal.valueOf(cellData.getValue().getQuantity()));
            return new SimpleStringProperty("$" + total.toString());
        });

        // Status Column with colored badges
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
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
                return switch (status) {
                    case "In Stock" -> "-fx-background-color: rgba(34,197,94,0.15); -fx-text-fill: #16a34a;";
                    case "Low Stock" -> "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
                    case "Out of Stock" -> "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
                    default -> "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
                };
            }
        });

        // Actions Column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox container = new HBox(8);

            {
                editButton.setStyle(
                    "-fx-background-color: #a78bfa; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-padding: 5px 12px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand;"
                );
                deleteButton.setStyle(
                    "-fx-background-color: #ef4444; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-padding: 5px 12px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand;"
                );

                editButton.setOnAction(event -> handleEditItem(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDeleteItem(getTableView().getItems().get(getIndex())));

                container.getChildren().addAll(editButton, deleteButton);
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(container);
                }
            }
        });

        inventoryTable.setItems(inventoryData);
    }

    /**
     * Setup filter combo box
     */
    private void setupFilters() {
        if (categoryFilter != null) {
            categoryFilter.getItems().addAll("All Categories", "Raw Materials", "Packaging", "Office Supplies", "Equipment");
            categoryFilter.setValue("All Categories");
        }
    }

    /**
     * Load dummy inventory data for dairy company
     */
    private void loadDummyInventoryData() {
        inventoryData.add(new InventoryItem(1, "Whole Milk - Bulk (1000L)", "Raw Materials", 45, 50, new BigDecimal("2500.00"), "Low Stock"));
        inventoryData.add(new InventoryItem(2, "Fresh Cream (200L)", "Raw Materials", 120, 30, new BigDecimal("800.00"), "In Stock"));
        inventoryData.add(new InventoryItem(3, "Skimmed Milk Powder (50kg)", "Raw Materials", 80, 40, new BigDecimal("450.00"), "In Stock"));
        inventoryData.add(new InventoryItem(4, "Butter (25kg blocks)", "Raw Materials", 0, 20, new BigDecimal("320.00"), "Out of Stock"));
        inventoryData.add(new InventoryItem(5, "Milk Bottles 1L (1000 units)", "Packaging", 250, 100, new BigDecimal("850.00"), "In Stock"));
        inventoryData.add(new InventoryItem(6, "Plastic Bottle Caps (5000 units)", "Packaging", 15, 50, new BigDecimal("120.00"), "Low Stock"));
        inventoryData.add(new InventoryItem(7, "Product Labels - Roll (2000)", "Packaging", 180, 80, new BigDecimal("220.00"), "In Stock"));
        inventoryData.add(new InventoryItem(8, "Office Chair - Ergonomic", "Office Supplies", 24, 10, new BigDecimal("250.00"), "In Stock"));
        inventoryData.add(new InventoryItem(9, "Whiteboard Markers (Set of 12)", "Office Supplies", 5, 15, new BigDecimal("8.50"), "Low Stock"));
        inventoryData.add(new InventoryItem(10, "Hand Sanitizer (500ml)", "Office Supplies", 0, 25, new BigDecimal("8.00"), "Out of Stock"));
    }

    /**
     * Update statistics labels
     */
    private void updateStats() {
        int totalItems = inventoryData.size();
        long lowStock = inventoryData.stream().filter(item -> item.getStatus().equals("Low Stock")).count();
        long outOfStock = inventoryData.stream().filter(item -> item.getStatus().equals("Out of Stock")).count();

        BigDecimal totalValue = inventoryData.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalItemsLabel != null) totalItemsLabel.setText(String.valueOf(totalItems));
        if (lowStockLabel != null) lowStockLabel.setText(String.valueOf(lowStock));
        if (outOfStockLabel != null) outOfStockLabel.setText(String.valueOf(outOfStock));
        if (totalValueLabel != null) totalValueLabel.setText("$" + String.format("%,.2f", totalValue));
    }

    // ==================== EVENT HANDLERS ====================

    @FXML
    private void handleAddItem() {
        System.out.println("Add Item clicked");
        showInfo("Add Item", "Add new item functionality will be implemented here.");
    }

    @FXML
    private void handleExport() {
        System.out.println("Export clicked");
        showInfo("Export Report", "Export functionality will be implemented here.");
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refresh clicked");
        loadDummyInventoryData();
        updateStats();
        showInfo("Refreshed", "Inventory data has been refreshed.");
    }

    private void handleEditItem(InventoryItem item) {
        System.out.println("Edit item: " + item.getItemName());
        showInfo("Edit Item", "Editing: " + item.getItemName());
    }

    private void handleDeleteItem(InventoryItem item) {
        System.out.println("Delete item: " + item.getItemName());
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete " + item.getItemName() + "?");
        alert.setContentText("This action cannot be undone.");
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                inventoryData.remove(item);
                updateStats();
            }
        });
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== INNER CLASS ====================

    /**
     * Inventory Item Model
     */
    public static class InventoryItem {
        private final IntegerProperty itemId;
        private final StringProperty itemName;
        private final StringProperty category;
        private final IntegerProperty quantity;
        private final IntegerProperty reorderLevel;
        private final ObjectProperty<BigDecimal> unitPrice;
        private final StringProperty status;

        public InventoryItem(int itemId, String itemName, String category, int quantity,
                           int reorderLevel, BigDecimal unitPrice, String status) {
            this.itemId = new SimpleIntegerProperty(itemId);
            this.itemName = new SimpleStringProperty(itemName);
            this.category = new SimpleStringProperty(category);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.reorderLevel = new SimpleIntegerProperty(reorderLevel);
            this.unitPrice = new SimpleObjectProperty<>(unitPrice);
            this.status = new SimpleStringProperty(status);
        }

        public int getItemId() { return itemId.get(); }
        public IntegerProperty itemIdProperty() { return itemId; }

        public String getItemName() { return itemName.get(); }
        public StringProperty itemNameProperty() { return itemName; }

        public String getCategory() { return category.get(); }
        public StringProperty categoryProperty() { return category; }

        public int getQuantity() { return quantity.get(); }
        public IntegerProperty quantityProperty() { return quantity; }

        public int getReorderLevel() { return reorderLevel.get(); }
        public IntegerProperty reorderLevelProperty() { return reorderLevel; }

        public BigDecimal getUnitPrice() { return unitPrice.get(); }
        public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }

        public String getStatus() { return status.get(); }
        public StringProperty statusProperty() { return status; }
    }
}

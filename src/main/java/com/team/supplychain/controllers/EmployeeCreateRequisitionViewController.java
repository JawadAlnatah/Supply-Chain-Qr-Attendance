package com.team.supplychain.controllers;

import com.team.supplychain.dao.InventoryDAO;
import com.team.supplychain.dao.RequisitionDAO;
import com.team.supplychain.models.InventoryItem;
import com.team.supplychain.models.Requisition;
import com.team.supplychain.models.User;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.util.converter.IntegerStringConverter;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controller for the Employee Create Requisition View
 * Allows employees to create new purchase requisition requests
 */
public class EmployeeCreateRequisitionViewController {

    // ==================== BASIC INFORMATION ====================
    @FXML private ComboBox<String> supplierCombo;
    @FXML private ComboBox<String> categoryCombo;
    @FXML private ComboBox<String> priorityCombo;
    @FXML private ComboBox<String> departmentCombo;
    @FXML private TextArea justificationArea;

    // ==================== SUMMARY CARD ====================
    @FXML private Label requisitionIdLabel;
    @FXML private Label supplierNameLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label requestedByLabel;
    @FXML private Label requestDateLabel;

    // ==================== ITEMS TABLE ====================
    @FXML private TableView<RequisitionItem> itemsTable;
    @FXML private TableColumn<RequisitionItem, String> itemNameColumn;
    @FXML private TableColumn<RequisitionItem, String> categoryColumn;
    @FXML private TableColumn<RequisitionItem, Integer> quantityColumn;
    @FXML private TableColumn<RequisitionItem, String> unitPriceColumn;
    @FXML private TableColumn<RequisitionItem, String> subtotalColumn;
    @FXML private TableColumn<RequisitionItem, Void> actionsColumn;

    // ==================== BUTTONS ====================
    @FXML private Button addItemButton;
    @FXML private Button cancelButton;
    @FXML private Button submitButton;

    private User currentUser;
    private ObservableList<RequisitionItem> itemsList;
    private ObservableList<String> inventoryItems;
    private ObservableList<String> suppliers;
    private Map<String, InventoryItemData> inventoryMap;
    private Map<String, String> supplierCategories;
    private int requisitionCounter = 1;
    private RequisitionDAO requisitionDAO;
    private InventoryDAO inventoryDAO;

    // Filtering data structures
    private Map<String, Set<String>> supplierToCategories; // supplier → categories
    private ObservableList<String> filteredItems; // Items filtered by supplier+category
    private String currentSelectedSupplier;
    private String currentSelectedCategory;

    /**
     * Set the current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateUserInfo();
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        System.out.println("EmployeeCreateRequisitionViewController initialized");

        // Initialize DAO
        requisitionDAO = new RequisitionDAO();
        inventoryDAO = new InventoryDAO();

        // Initialize collections
        itemsList = FXCollections.observableArrayList();
        inventoryItems = FXCollections.observableArrayList();
        inventoryMap = new HashMap<>();
        suppliers = FXCollections.observableArrayList();
        supplierCategories = new HashMap<>();
        supplierToCategories = new HashMap<>();
        filteredItems = FXCollections.observableArrayList();

        // Load inventory data from database first, then extract suppliers
        loadInventoryItemsFromDatabase();
        loadSuppliersFromInventory();
        loadCategories();
        loadPriorities();
        loadDepartments();

        // Set up supplier combo listener
        setupSupplierListener();

        // Set up category combo listener
        setupCategoryListener();

        // Set up table
        setupItemsTable();

        // Generate and set initial requisition ID
        String initialReqId = generateRequisitionId();
        if (requisitionIdLabel != null) {
            requisitionIdLabel.setText(initialReqId);
        }

        // Set current date
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");
        if (requestDateLabel != null) {
            requestDateLabel.setText(today.format(formatter));
        }

        // Initialize totals
        updateTotals();
    }

    /**
     * Load suppliers from the inventory data that was already loaded from database
     */
    private void loadSuppliersFromInventory() {
        // Extract unique suppliers from the supplierToCategories map (populated during inventory loading)
        if (!supplierToCategories.isEmpty()) {
            suppliers.addAll(supplierToCategories.keySet());
            suppliers.sort(String::compareTo);

            // Build supplierCategories map (supplier -> first category, for backward compatibility)
            for (Map.Entry<String, Set<String>> entry : supplierToCategories.entrySet()) {
                String supplier = entry.getKey();
                Set<String> categories = entry.getValue();
                if (!categories.isEmpty()) {
                    supplierCategories.put(supplier, categories.iterator().next());
                }
            }
        }

        if (supplierCombo != null) {
            supplierCombo.setItems(suppliers);
        }

        System.out.println("Loaded " + suppliers.size() + " suppliers from inventory data");
    }

    /**
     * Setup listener for supplier selection to filter categories and update UI
     */
    private void setupSupplierListener() {
        if (supplierCombo != null) {
            supplierCombo.setOnAction(event -> {
                String selectedSupplier = supplierCombo.getValue();

                if (selectedSupplier != null) {
                    currentSelectedSupplier = selectedSupplier;

                    // Update summary card
                    if (supplierNameLabel != null) {
                        supplierNameLabel.setText(selectedSupplier);
                    }

                    // Filter and update categories for this supplier
                    Set<String> availableCategories = supplierToCategories.get(selectedSupplier);
                    if (availableCategories != null && categoryCombo != null) {
                        categoryCombo.getItems().clear();
                        categoryCombo.getItems().addAll(availableCategories);
                        categoryCombo.setValue(null); // Clear selection

                        // Enable category dropdown now that supplier is selected
                        categoryCombo.setDisable(false);
                        categoryCombo.setPromptText("Select category...");
                    }

                    // Clear current category and items
                    currentSelectedCategory = null;
                    clearRequisitionItems();
                }
            });
        }
    }

    /**
     * Setup listener for category selection to filter items
     */
    private void setupCategoryListener() {
        if (categoryCombo != null) {
            categoryCombo.setOnAction(event -> {
                String selectedCategory = categoryCombo.getValue();

                if (selectedCategory != null && currentSelectedSupplier != null) {
                    currentSelectedCategory = selectedCategory;

                    // Filter items by supplier AND category
                    filterItemsBySupplierAndCategory();
                }
            });
        }
    }

    /**
     * Filter items by selected supplier AND category
     */
    private void filterItemsBySupplierAndCategory() {
        filteredItems.clear();

        if (currentSelectedSupplier == null || currentSelectedCategory == null) {
            return;
        }

        // Filter inventory items by supplier AND category
        for (Map.Entry<String, InventoryItemData> entry : inventoryMap.entrySet()) {
            String itemName = entry.getKey();
            InventoryItemData data = entry.getValue();

            if (data.supplier.equals(currentSelectedSupplier) &&
                data.category.equals(currentSelectedCategory)) {
                filteredItems.add(itemName);
            }
        }

        // Sort filtered items
        filteredItems.sort(String::compareTo);

        // Refresh table to update ComboBoxes
        if (itemsTable != null) {
            itemsTable.refresh();
        }
    }

    /**
     * Clear all requisition items from the table
     */
    private void clearRequisitionItems() {
        // Clear all items from the table
        itemsList.clear();

        // Add fresh empty rows
        for (int i = 0; i < 5; i++) {
            itemsList.add(new RequisitionItem());
        }

        updateTotals();
    }

    /**
     * Generate unique requisition ID in format REQ-XXX
     */
    private String generateRequisitionId() {
        return String.format("REQ-%03d", requisitionCounter++);
    }

    /**
     * Load inventory items from the database
     */
    private void loadInventoryItemsFromDatabase() {
        // Fetch all inventory items from database
        List<InventoryItem> dbItems = inventoryDAO.getAllInventoryItems();

        if (dbItems == null || dbItems.isEmpty()) {
            System.out.println("No inventory items found in database");
            return;
        }

        System.out.println("Loaded " + dbItems.size() + " inventory items from database");

        // Populate inventoryMap with database items
        for (InventoryItem item : dbItems) {
            String itemName = item.getItemName();
            String category = item.getCategory() != null ? item.getCategory() : "Uncategorized";
            String supplierName = item.getSupplierName() != null ? item.getSupplierName() : "Unknown Supplier";
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;

            // Add to inventory map
            inventoryMap.put(itemName, new InventoryItemData(category, supplierName, unitPrice));

            // Build supplier → categories reverse lookup for filtering
            supplierToCategories
                .computeIfAbsent(supplierName, k -> new HashSet<>())
                .add(category);
        }

        // Populate inventoryItems list and sort
        inventoryItems.addAll(inventoryMap.keySet());
        inventoryItems.sort(String::compareTo);
    }

    /**
     * Load category options for dairy company
     * Categories will be dynamically populated based on selected supplier
     */
    private void loadCategories() {
        if (categoryCombo != null) {
            // Don't pre-populate - categories will be loaded when supplier is selected
            categoryCombo.setPromptText("Select supplier first...");
            categoryCombo.setDisable(true); // Disabled until supplier selected
        }
    }

    /**
     * Load priority options
     */
    private void loadPriorities() {
        if (priorityCombo != null) {
            priorityCombo.getItems().addAll("Low", "Medium", "High", "Urgent");
            priorityCombo.setValue("Medium");
        }
    }

    /**
     * Load department options for dairy company
     */
    private void loadDepartments() {
        if (departmentCombo != null) {
            departmentCombo.getItems().addAll(
                "Production",
                "Quality Control",
                "Packaging",
                "Warehouse",
                "Procurement",
                "Logistics",
                "Administration"
            );
            departmentCombo.setValue("Production");
        }
    }

    /**
     * Set up the items table with columns and cell factories
     */
    private void setupItemsTable() {
        if (itemsTable == null) return;

        // Item Name Column with ComboBox
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        itemNameColumn.setCellFactory(column -> {
            return new TableCell<RequisitionItem, String>() {
                private final ComboBox<String> comboBox = new ComboBox<>(filteredItems);

                {
                    comboBox.setPromptText("Select item...");
                    comboBox.setPrefWidth(250);

                    // Clean, professional ComboBox styling
                    comboBox.setStyle(
                        "-fx-background-color: white;" +
                        "-fx-border-color: #d1d5da;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-padding: 6 12;" +
                        "-fx-font-size: 12px;" +
                        "-fx-cursor: hand;"
                    );

                    comboBox.setOnAction(event -> {
                        String selected = comboBox.getValue();
                        if (selected != null && !selected.isEmpty()) {
                            RequisitionItem item = getTableView().getItems().get(getIndex());
                            item.setItemName(selected);

                            // Auto-fill category and unit price
                            InventoryItemData data = inventoryMap.get(selected);
                            if (data != null) {
                                item.setCategory(data.category);
                                item.setUnitPrice(data.price);
                                item.recalculateSubtotal();
                                updateTotals();
                            }
                            getTableView().refresh();
                        }
                    });
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        comboBox.setValue(item);
                        setGraphic(comboBox);
                    }
                }
            };
        });

        // Category Column
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));

        // Quantity Column with editable TextField
        quantityColumn.setCellValueFactory(cellData -> cellData.getValue().quantityProperty().asObject());
        quantityColumn.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantityColumn.setOnEditCommit(event -> {
            RequisitionItem item = event.getRowValue();
            item.setQuantity(event.getNewValue());
            item.recalculateSubtotal();
            updateTotals();
        });

        // Unit Price Column
        unitPriceColumn.setCellValueFactory(cellData -> {
            BigDecimal price = cellData.getValue().getUnitPrice();
            return new SimpleStringProperty(price != null ? "$" + price.toString() : "$0.00");
        });

        // Subtotal Column
        subtotalColumn.setCellValueFactory(cellData -> {
            BigDecimal subtotal = cellData.getValue().getSubtotal();
            return new SimpleStringProperty(subtotal != null ? "$" + subtotal.toString() : "$0.00");
        });

        // Actions Column (Remove button)
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.getStyleClass().add("action-button-secondary");
                removeButton.setOnAction(event -> {
                    RequisitionItem item = getTableView().getItems().get(getIndex());
                    handleRemoveItem(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Set table items
        itemsTable.setItems(itemsList);
        itemsTable.setEditable(true);

        // Add initial empty rows
        for (int i = 0; i < 5; i++) {
            itemsList.add(new RequisitionItem());
        }
    }

    /**
     * Update user info in summary card
     */
    private void updateUserInfo() {
        if (currentUser != null && requestedByLabel != null) {
            requestedByLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
        }
    }

    /**
     * Update totals in summary card
     */
    private void updateTotals() {
        int totalItems = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (RequisitionItem item : itemsList) {
            if (item.getItemName() != null && !item.getItemName().isEmpty()) {
                totalItems += item.getQuantity();
                if (item.getSubtotal() != null) {
                    totalAmount = totalAmount.add(item.getSubtotal());
                }
            }
        }

        if (totalItemsLabel != null) {
            totalItemsLabel.setText(String.valueOf(totalItems));
        }

        if (totalAmountLabel != null) {
            totalAmountLabel.setText("$" + totalAmount.toString());
        }
    }

    // ==================== EVENT HANDLERS ====================

    /**
     * Handle adding a new item row
     */
    @FXML
    private void handleAddItem() {
        System.out.println("Add Item clicked");
        itemsList.add(new RequisitionItem());
        itemsTable.scrollTo(itemsList.size() - 1);
    }

    /**
     * Handle removing an item from the table
     */
    private void handleRemoveItem(RequisitionItem item) {
        itemsList.remove(item);
        updateTotals();
    }

    /**
     * Handle form submission
     */
    @FXML
    private void handleSubmit() {
        System.out.println("Submit clicked");

        if (!validateForm()) {
            return;
        }

        try {
            // Create Requisition object
            Requisition requisition = new Requisition();

            // Generate requisition code
            String reqCode = requisitionDAO.generateRequisitionCode();
            requisition.setRequisitionCode(reqCode);

            // Set user info
            if (currentUser != null) {
                requisition.setRequestedBy(currentUser.getUserId());
                requisition.setRequesterName(currentUser.getFirstName() + " " + currentUser.getLastName());
            }

            // Set requisition details
            requisition.setCategory(categoryCombo.getValue());
            requisition.setDepartment(departmentCombo.getValue());
            requisition.setPriority(priorityCombo.getValue());
            requisition.setJustification(justificationArea.getText().trim());
            requisition.setStatus("Pending");
            requisition.setRequestDate(LocalDateTime.now());

            // Calculate totals and add items
            List<com.team.supplychain.models.RequisitionItem> validItems = itemsList.stream()
                .filter(item -> item.getItemName() != null && !item.getItemName().isEmpty())
                .map(this::convertToModelItem)
                .collect(Collectors.toList());

            requisition.setItems(validItems);
            requisition.setTotalItems(validItems.stream().mapToInt(item -> item.getQuantity()).sum());

            BigDecimal totalAmount = validItems.stream()
                .map(com.team.supplychain.models.RequisitionItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            requisition.setTotalAmount(totalAmount);

            // Save to database
            Integer requisitionId = requisitionDAO.createRequisition(requisition);

            if (requisitionId != null) {
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Requisition Submitted");
                alert.setHeaderText("✓ Success!");
                alert.setContentText("Your requisition has been submitted successfully.\n\n" +
                                    "Requisition Code: " + reqCode + "\n" +
                                    "Category: " + requisition.getCategory() + "\n" +
                                    "Total Items: " + requisition.getTotalItems() + "\n" +
                                    "Total Amount: SAR " + requisition.getTotalAmount() + "\n" +
                                    "Status: Pending Approval\n\n" +
                                    "A manager will review your request shortly.");
                alert.showAndWait();

                // Navigate to My Requisitions page
                navigateToMyRequisitions();
            } else {
                showError("Submission Error", "Failed to save requisition to database. Please try again.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showError("Submission Error", "An error occurred while submitting the requisition: " + e.getMessage());
        }
    }

    /**
     * Convert controller RequisitionItem to model RequisitionItem
     */
    private com.team.supplychain.models.RequisitionItem convertToModelItem(RequisitionItem controllerItem) {
        com.team.supplychain.models.RequisitionItem modelItem = new com.team.supplychain.models.RequisitionItem();
        modelItem.setItemName(controllerItem.getItemName());
        modelItem.setCategory(controllerItem.getCategory());
        modelItem.setQuantity(controllerItem.getQuantity());
        modelItem.setUnitPrice(controllerItem.getUnitPrice());
        modelItem.calculateSubtotal();
        return modelItem;
    }

    /**
     * Handle cancel button
     */
    @FXML
    private void handleCancel() {
        System.out.println("Cancel clicked");

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancel Requisition");
        alert.setHeaderText("Discard changes?");
        alert.setContentText("Are you sure you want to cancel? All entered data will be lost.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            navigateToDashboard();
        }
    }

    // ==================== VALIDATION ====================

    /**
     * Validate the form before submission
     */
    private boolean validateForm() {
        // Check supplier selected
        if (supplierCombo.getValue() == null || supplierCombo.getValue().isEmpty()) {
            showError("Validation Error", "Please select a supplier");
            supplierCombo.requestFocus();
            return false;
        }

        // Check category selected
        if (categoryCombo.getValue() == null || categoryCombo.getValue().isEmpty()) {
            showError("Validation Error", "Please select a category");
            categoryCombo.requestFocus();
            return false;
        }

        // Check priority selected
        if (priorityCombo.getValue() == null || priorityCombo.getValue().isEmpty()) {
            showError("Validation Error", "Please select a priority level");
            priorityCombo.requestFocus();
            return false;
        }

        // Check at least one item added
        boolean hasItems = itemsList.stream().anyMatch(item -> item.getItemName() != null && !item.getItemName().isEmpty());
        if (!hasItems) {
            showError("Validation Error", "Please add at least one item to the requisition");
            return false;
        }

        // Check all items have quantity > 0
        for (RequisitionItem item : itemsList) {
            if (item.getItemName() != null && !item.getItemName().isEmpty()) {
                if (item.getQuantity() <= 0) {
                    showError("Validation Error", "Please enter valid quantities (greater than 0) for all items");
                    return false;
                }
            }
        }

        // Check justification not empty
        if (justificationArea.getText().trim().isEmpty()) {
            showError("Validation Error", "Please provide a justification for this requisition");
            justificationArea.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Show error dialog
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ==================== NAVIGATION ====================

    /**
     * Navigate to My Requisitions page
     */
    private void navigateToMyRequisitions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/EmployeeRequisitionsView.fxml"));
            Parent content = loader.load();

            // Get the controller and set user
            EmployeeRequisitionsViewController controller = loader.getController();
            controller.setCurrentUser(currentUser);

            // Replace current view
            replaceView(content);

        } catch (IOException e) {
            e.printStackTrace();
            showError("Navigation Error", "Failed to load My Requisitions page");
        }
    }

    /**
     * Navigate back to dashboard
     */
    private void navigateToDashboard() {
        // This will be handled by the parent controller
        // For now, just print a message
        System.out.println("Navigating back to dashboard");
    }

    /**
     * Replace the current view with new content
     */
    private void replaceView(Parent newContent) {
        // Get the ScrollPane from parent hierarchy
        if (supplierCombo != null && supplierCombo.getScene() != null && supplierCombo.getScene().getRoot() != null) {
            // This is a placeholder - actual implementation depends on parent structure
            System.out.println("Replacing view with My Requisitions");
        }
    }

    // ==================== INNER CLASSES ====================

    /**
     * Data class for inventory items with supplier information
     */
    private static class InventoryItemData {
        String category;
        String supplier;
        BigDecimal price;

        InventoryItemData(String category, String supplier, BigDecimal price) {
            this.category = category;
            this.supplier = supplier;
            this.price = price;
        }
    }

    /**
     * POJO for requisition line items
     */
    public static class RequisitionItem {
        private final StringProperty itemName;
        private final StringProperty category;
        private final IntegerProperty quantity;
        private final ObjectProperty<BigDecimal> unitPrice;
        private final ObjectProperty<BigDecimal> subtotal;

        public RequisitionItem() {
            this.itemName = new SimpleStringProperty("");
            this.category = new SimpleStringProperty("");
            this.quantity = new SimpleIntegerProperty(1);
            this.unitPrice = new SimpleObjectProperty<>(BigDecimal.ZERO);
            this.subtotal = new SimpleObjectProperty<>(BigDecimal.ZERO);
        }

        public void recalculateSubtotal() {
            if (unitPrice.get() != null && quantity.get() > 0) {
                BigDecimal calculatedSubtotal = unitPrice.get().multiply(new BigDecimal(quantity.get()));
                subtotal.set(calculatedSubtotal);
            } else {
                subtotal.set(BigDecimal.ZERO);
            }
        }

        // Getters and Setters
        public String getItemName() { return itemName.get(); }
        public void setItemName(String value) { itemName.set(value); }
        public StringProperty itemNameProperty() { return itemName; }

        public String getCategory() { return category.get(); }
        public void setCategory(String value) { category.set(value); }
        public StringProperty categoryProperty() { return category; }

        public int getQuantity() { return quantity.get(); }
        public void setQuantity(int value) {
            quantity.set(value);
            recalculateSubtotal();
        }
        public IntegerProperty quantityProperty() { return quantity; }

        public BigDecimal getUnitPrice() { return unitPrice.get(); }
        public void setUnitPrice(BigDecimal value) {
            unitPrice.set(value);
            recalculateSubtotal();
        }
        public ObjectProperty<BigDecimal> unitPriceProperty() { return unitPrice; }

        public BigDecimal getSubtotal() { return subtotal.get(); }
        public void setSubtotal(BigDecimal value) { subtotal.set(value); }
        public ObjectProperty<BigDecimal> subtotalProperty() { return subtotal; }
    }
}

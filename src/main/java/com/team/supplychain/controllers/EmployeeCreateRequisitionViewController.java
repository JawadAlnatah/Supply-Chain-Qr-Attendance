package com.team.supplychain.controllers;

import com.team.supplychain.dao.RequisitionDAO;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        // Initialize collections
        itemsList = FXCollections.observableArrayList();
        inventoryItems = FXCollections.observableArrayList();
        inventoryMap = new HashMap<>();
        suppliers = FXCollections.observableArrayList();
        supplierCategories = new HashMap<>();

        // Load dummy data
        loadDummySuppliers();
        loadDummyInventoryItems();
        loadCategories();
        loadPriorities();
        loadDepartments();

        // Set up supplier combo listener
        setupSupplierListener();

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
     * Load dummy supplier data for dairy company
     */
    private void loadDummySuppliers() {
        suppliers.addAll(
            "Al-Safi Dairy Farm - Riyadh",
            "Nadec Dairy Farm - Kharj",
            "Almarai Farm - Eastern Province",
            "Gulf Packaging Supplies - Dammam",
            "Saudi Paper & Plastic Co. - Jeddah",
            "Modern Office Equipment - Riyadh"
        );

        supplierCategories.put("Al-Safi Dairy Farm - Riyadh", "Raw Materials");
        supplierCategories.put("Nadec Dairy Farm - Kharj", "Raw Materials");
        supplierCategories.put("Almarai Farm - Eastern Province", "Raw Materials");
        supplierCategories.put("Gulf Packaging Supplies - Dammam", "Packaging");
        supplierCategories.put("Saudi Paper & Plastic Co. - Jeddah", "Packaging");
        supplierCategories.put("Modern Office Equipment - Riyadh", "Office Supplies");

        if (supplierCombo != null) {
            supplierCombo.setItems(suppliers);
        }
    }

    /**
     * Setup listener for supplier selection to update summary card
     */
    private void setupSupplierListener() {
        if (supplierCombo != null) {
            supplierCombo.setOnAction(event -> {
                String selectedSupplier = supplierCombo.getValue();
                if (selectedSupplier != null && supplierNameLabel != null) {
                    supplierNameLabel.setText(selectedSupplier);
                }
            });
        }
    }

    /**
     * Generate unique requisition ID in format REQ-XXX
     */
    private String generateRequisitionId() {
        return String.format("REQ-%03d", requisitionCounter++);
    }

    /**
     * Load dummy inventory items with prices, categories, and suppliers for dairy company
     */
    private void loadDummyInventoryItems() {
        // RAW MATERIALS
        inventoryMap.put("Whole Milk - Bulk (1000L)",
            new InventoryItemData("Raw Materials", "Al-Safi Dairy Farm - Riyadh", new BigDecimal("2500.00")));
        inventoryMap.put("Fresh Cream (200L)",
            new InventoryItemData("Raw Materials", "Nadec Dairy Farm - Kharj", new BigDecimal("800.00")));
        inventoryMap.put("Skimmed Milk Powder (50kg)",
            new InventoryItemData("Raw Materials", "Almarai Farm - Eastern Province", new BigDecimal("450.00")));
        inventoryMap.put("Butter (25kg blocks)",
            new InventoryItemData("Raw Materials", "Al-Safi Dairy Farm - Riyadh", new BigDecimal("320.00")));
        inventoryMap.put("Cheese Cultures (5kg)",
            new InventoryItemData("Raw Materials", "Nadec Dairy Farm - Kharj", new BigDecimal("180.00")));

        // PACKAGING
        inventoryMap.put("Milk Bottles 1L (1000 units)",
            new InventoryItemData("Packaging", "Gulf Packaging Supplies - Dammam", new BigDecimal("850.00")));
        inventoryMap.put("Plastic Bottle Caps (5000 units)",
            new InventoryItemData("Packaging", "Gulf Packaging Supplies - Dammam", new BigDecimal("120.00")));
        inventoryMap.put("Product Labels - Roll (2000 units)",
            new InventoryItemData("Packaging", "Saudi Paper & Plastic Co. - Jeddah", new BigDecimal("220.00")));
        inventoryMap.put("Cardboard Boxes - Large (500 units)",
            new InventoryItemData("Packaging", "Saudi Paper & Plastic Co. - Jeddah", new BigDecimal("380.00")));
        inventoryMap.put("Shrink Wrap Film (100m roll)",
            new InventoryItemData("Packaging", "Gulf Packaging Supplies - Dammam", new BigDecimal("95.00")));
        inventoryMap.put("Yogurt Cups 200ml (2000 units)",
            new InventoryItemData("Packaging", "Gulf Packaging Supplies - Dammam", new BigDecimal("420.00")));

        // OFFICE SUPPLIES
        inventoryMap.put("Office Chair - Ergonomic",
            new InventoryItemData("Office Supplies", "Modern Office Equipment - Riyadh", new BigDecimal("250.00")));
        inventoryMap.put("Whiteboard Markers (Set of 12)",
            new InventoryItemData("Office Supplies", "Modern Office Equipment - Riyadh", new BigDecimal("8.50")));
        inventoryMap.put("Printer Paper A4 (10 reams)",
            new InventoryItemData("Office Supplies", "Modern Office Equipment - Riyadh", new BigDecimal("45.00")));
        inventoryMap.put("Hand Sanitizer (500ml)",
            new InventoryItemData("Office Supplies", "Modern Office Equipment - Riyadh", new BigDecimal("8.00")));

        inventoryItems.addAll(inventoryMap.keySet());
        inventoryItems.sort(String::compareTo);
    }

    /**
     * Load category options for dairy company
     */
    private void loadCategories() {
        if (categoryCombo != null) {
            categoryCombo.getItems().addAll(
                "Raw Materials",
                "Packaging",
                "Office Supplies",
                "Equipment"
            );
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
                private final ComboBox<String> comboBox = new ComboBox<>(inventoryItems);

                {
                    comboBox.setPromptText("Select item...");
                    comboBox.setPrefWidth(250);
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

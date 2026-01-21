package com.team.supplychain.controllers;

import com.team.supplychain.dao.InventoryDAO;
import com.team.supplychain.dao.RequisitionDAO;
import com.team.supplychain.models.InventoryItem;
import com.team.supplychain.models.Requisition;
import com.team.supplychain.models.RequisitionItem;
import com.team.supplychain.models.User;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for the Manager Purchase Orders View
 * Manages purchase order approvals, tracking, and supplier deliveries
 */
public class ManagerPurchaseOrdersController {

    // ==================== STATS LABELS ====================
    @FXML private Label pendingLabel;
    @FXML private Label approvedLabel;
    @FXML private Label inTransitLabel;
    @FXML private Label deliveredLabel;
    @FXML private Label totalValueLabel;

    // ==================== FILTERS & SEARCH ====================
    @FXML private ComboBox<String> statusFilter;
    @FXML private TextField searchField;

    // ==================== BUTTONS ====================
    @FXML private Button approveButton;
    @FXML private Button rejectButton;
    @FXML private Button refreshButton;

    // ==================== TABLE ====================
    @FXML private TableView<PurchaseOrder> purchaseOrdersTable;
    @FXML private TableColumn<PurchaseOrder, Boolean> selectColumn;
    @FXML private TableColumn<PurchaseOrder, String> poNumberColumn;
    @FXML private TableColumn<PurchaseOrder, String> supplierColumn;
    @FXML private TableColumn<PurchaseOrder, String> requestedByColumn;
    @FXML private TableColumn<PurchaseOrder, String> dateColumn;
    @FXML private TableColumn<PurchaseOrder, Integer> itemsCountColumn;
    @FXML private TableColumn<PurchaseOrder, String> totalAmountColumn;
    @FXML private TableColumn<PurchaseOrder, String> statusColumn;
    @FXML private TableColumn<PurchaseOrder, Void> actionsColumn;

    private User currentUser;
    private ObservableList<PurchaseOrder> poData;
    private RequisitionDAO requisitionDAO;
    // Map to track requisition ID for each PO number
    private Map<String, Integer> poNumberToRequisitionId;

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
        System.out.println("ManagerPurchaseOrdersController initialized");

        // Initialize DAO and data
        requisitionDAO = new RequisitionDAO();
        poData = FXCollections.observableArrayList();
        poNumberToRequisitionId = new HashMap<>();

        // Setup table
        setupPOTable();

        // Load data from database
        loadRequisitionsFromDatabase();

        // Setup filters
        setupFilters();

        // Update stats
        updateStats();
    }

    /**
     * Setup purchase orders table columns
     */
    private void setupPOTable() {
        if (purchaseOrdersTable == null) return;

        // Select Column with checkboxes
        selectColumn.setCellValueFactory(new PropertyValueFactory<>("selected"));
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));

        // PO Number Column
        poNumberColumn.setCellValueFactory(new PropertyValueFactory<>("poNumber"));

        // Supplier Column
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplier"));

        // Requested By Column
        requestedByColumn.setCellValueFactory(new PropertyValueFactory<>("requestedBy"));

        // Date Column
        dateColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))));

        // Items Count Column
        itemsCountColumn.setCellValueFactory(new PropertyValueFactory<>("itemsCount"));

        // Total Amount Column
        totalAmountColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty("$" + String.format("%,.2f", cellData.getValue().getTotalAmount())));

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
                        "-fx-padding: 6px 14px; " +
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
                    case "Pending":
                        return "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
                    case "Approved":
                        return "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
                    case "In Transit":
                        return "-fx-background-color: rgba(59,130,246,0.15); -fx-text-fill: #3b82f6;";
                    case "Delivered":
                        return "-fx-background-color: rgba(167,139,250,0.15); -fx-text-fill: #a78bfa;";
                    case "Rejected":
                        return "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
                    default:
                        return "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
                }
            }
        });

        // Actions Column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            private final Button approveBtn = new Button("Approve");
            private final Button rejectBtn = new Button("Reject");
            private final HBox container = new HBox(6);

            {
                viewButton.setStyle(
                    "-fx-background-color: #a78bfa; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-padding: 5px 10px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand;"
                );
                approveBtn.setStyle(
                    "-fx-background-color: #10b981; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-padding: 5px 10px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand;"
                );
                rejectBtn.setStyle(
                    "-fx-background-color: #ef4444; " +
                    "-fx-text-fill: white; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-padding: 5px 10px; " +
                    "-fx-font-size: 11px; " +
                    "-fx-cursor: hand;"
                );

                viewButton.setOnAction(event -> handleViewPO(getTableView().getItems().get(getIndex())));
                approveBtn.setOnAction(event -> handleApprovePO(getTableView().getItems().get(getIndex())));
                rejectBtn.setOnAction(event -> handleRejectPO(getTableView().getItems().get(getIndex())));

                container.getChildren().addAll(viewButton, approveBtn, rejectBtn);
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    PurchaseOrder po = getTableView().getItems().get(getIndex());
                    // Show approve/reject only for pending orders
                    if ("Pending".equals(po.getStatus())) {
                        container.getChildren().setAll(viewButton, approveBtn, rejectBtn);
                    } else {
                        container.getChildren().setAll(viewButton);
                    }
                    setGraphic(container);
                }
            }
        });

        purchaseOrdersTable.setItems(poData);
        purchaseOrdersTable.setEditable(true);
    }

    /**
     * Setup filter combo box
     */
    private void setupFilters() {
        if (statusFilter != null) {
            statusFilter.getItems().addAll("All Status", "Pending", "Approved", "In Transit", "Delivered", "Rejected");
            statusFilter.setValue("All Status");

            // Add listener for filter changes
            statusFilter.setOnAction(event -> {
                String selectedStatus = statusFilter.getValue();
                loadRequisitionsFromDatabase(selectedStatus);
                updateStats();
            });
        }
    }

    /**
     * Load requisitions from database
     */
    private void loadRequisitionsFromDatabase() {
        loadRequisitionsFromDatabase(null);
    }

    /**
     * Load requisitions from database with optional status filter (asynchronously)
     */
    private void loadRequisitionsFromDatabase(String statusFilter) {
        poData.clear();
        poNumberToRequisitionId.clear();

        // Show loading indicator
        if (purchaseOrdersTable != null) {
            purchaseOrdersTable.setPlaceholder(new Label("Loading purchase orders..."));
        }

        // Load data in background thread
        javafx.concurrent.Task<List<Requisition>> loadTask = new javafx.concurrent.Task<>() {
            @Override
            protected List<Requisition> call() throws Exception {
                // Fetch requisitions from database
                if (statusFilter == null || "All Status".equals(statusFilter)) {
                    // Load all requisitions (pending, approved, rejected)
                    List<Requisition> pending = requisitionDAO.getRequisitionsByStatus("Pending");
                    List<Requisition> approved = requisitionDAO.getRequisitionsByStatus("Approved");
                    List<Requisition> rejected = requisitionDAO.getRequisitionsByStatus("Rejected");

                    List<Requisition> all = new java.util.ArrayList<>();
                    all.addAll(pending);
                    all.addAll(approved);
                    all.addAll(rejected);
                    return all;
                } else {
                    return requisitionDAO.getRequisitionsByStatus(statusFilter);
                }
            }
        };

        loadTask.setOnSucceeded(event -> {
            try {
                List<Requisition> requisitions = loadTask.getValue();

                // Convert to PurchaseOrder objects on UI thread
                for (Requisition req : requisitions) {
                    PurchaseOrder po = mapRequisitionToPurchaseOrder(req);
                    poData.add(po);

                    // Track requisition ID for database updates
                    poNumberToRequisitionId.put(req.getRequisitionCode(), req.getRequisitionId());
                }

                System.out.println("Loaded " + poData.size() + " requisitions from database");

                // Reset placeholder
                if (purchaseOrdersTable != null) {
                    purchaseOrdersTable.setPlaceholder(new Label("No purchase orders found"));
                }

                updateStats();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Loading Error", "Failed to process requisitions: " + e.getMessage());
            }
        });

        loadTask.setOnFailed(event -> {
            Throwable ex = loadTask.getException();
            ex.printStackTrace();
            showError("Database Error", "Failed to load requisitions from database: " + ex.getMessage());

            if (purchaseOrdersTable != null) {
                purchaseOrdersTable.setPlaceholder(new Label("Failed to load data"));
            }
        });

        // Start background thread
        Thread thread = new Thread(loadTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Map Requisition model to PurchaseOrder table model
     */
    private PurchaseOrder mapRequisitionToPurchaseOrder(Requisition req) {
        String poNumber = req.getRequisitionCode();
        String supplier = req.getSupplierName() != null ? req.getSupplierName() : "N/A";
        String requestedBy = req.getRequesterName();
        LocalDate date = req.getRequestDate().toLocalDate();
        int itemsCount = req.getTotalItems();
        BigDecimal totalAmount = req.getTotalAmount();
        String status = req.getStatus();

        return new PurchaseOrder(poNumber, supplier, requestedBy, date, itemsCount, totalAmount, status);
    }

    /**
     * Update statistics labels
     */
    private void updateStats() {
        long pending = poData.stream().filter(po -> po.getStatus().equals("Pending")).count();
        long approved = poData.stream().filter(po -> po.getStatus().equals("Approved")).count();
        long inTransit = poData.stream().filter(po -> po.getStatus().equals("In Transit")).count();
        long delivered = poData.stream().filter(po -> po.getStatus().equals("Delivered")).count();

        BigDecimal totalValue = poData.stream()
            .map(PurchaseOrder::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (pendingLabel != null) pendingLabel.setText(String.valueOf(pending));
        if (approvedLabel != null) approvedLabel.setText(String.valueOf(approved));
        if (inTransitLabel != null) inTransitLabel.setText(String.valueOf(inTransit));
        if (deliveredLabel != null) deliveredLabel.setText(String.valueOf(delivered));
        if (totalValueLabel != null) totalValueLabel.setText("$" + String.format("%,.0f", totalValue));
    }

    // ==================== EVENT HANDLERS ====================

    @FXML
    private void handleApprove() {
        List<PurchaseOrder> selectedPOs = poData.stream()
            .filter(PurchaseOrder::isSelected)
            .collect(java.util.stream.Collectors.toList());

        if (selectedPOs.isEmpty()) {
            showError("No Selection", "Please select at least one purchase order to approve.");
            return;
        }

        // Prompt for approval notes
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Approve Selected Requisitions");
        dialog.setHeaderText("Approve " + selectedPOs.size() + " requisition(s)");
        dialog.setContentText("Approval notes (optional):");
        String notes = dialog.showAndWait().orElse("Approved by manager");

        int successCount = 0;
        for (PurchaseOrder po : selectedPOs) {
            Integer requisitionId = poNumberToRequisitionId.get(po.getPoNumber());
            if (requisitionId != null) {
                try {
                    boolean success = requisitionDAO.updateRequisitionStatus(
                        requisitionId,
                        "Approved",
                        currentUser != null ? currentUser.getUserId() : null,
                        notes
                    );

                    if (success) {
                        // Update inventory quantities after approval
                        updateInventoryFromApprovedRequisition(requisitionId);

                        po.setStatus("Approved");
                        po.setSelected(false);
                        successCount++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        purchaseOrdersTable.refresh();
        updateStats();
        showInfo("Approve", successCount + " purchase order(s) approved successfully.");
    }

    @FXML
    private void handleReject() {
        List<PurchaseOrder> selectedPOs = poData.stream()
            .filter(PurchaseOrder::isSelected)
            .collect(java.util.stream.Collectors.toList());

        if (selectedPOs.isEmpty()) {
            showError("No Selection", "Please select at least one purchase order to reject.");
            return;
        }

        // Prompt for rejection reason
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Selected Requisitions");
        dialog.setHeaderText("Reject " + selectedPOs.size() + " requisition(s)");
        dialog.setContentText("Rejection reason:");
        String notes = dialog.showAndWait().orElse("");

        if (notes.isEmpty()) {
            showError("Rejection Reason Required", "Please provide a reason for rejection.");
            return;
        }

        int successCount = 0;
        for (PurchaseOrder po : selectedPOs) {
            Integer requisitionId = poNumberToRequisitionId.get(po.getPoNumber());
            if (requisitionId != null) {
                try {
                    boolean success = requisitionDAO.updateRequisitionStatus(
                        requisitionId,
                        "Rejected",
                        currentUser != null ? currentUser.getUserId() : null,
                        notes
                    );

                    if (success) {
                        po.setStatus("Rejected");
                        po.setSelected(false);
                        successCount++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        purchaseOrdersTable.refresh();
        updateStats();
        showInfo("Reject", successCount + " purchase order(s) rejected.");
    }

    @FXML
    private void handleRefresh() {
        System.out.println("Refresh clicked");
        String currentFilter = statusFilter != null ? statusFilter.getValue() : null;
        loadRequisitionsFromDatabase(currentFilter);
        updateStats();
        showInfo("Refreshed", "Purchase orders have been refreshed from database.");
    }

    private void handleViewPO(PurchaseOrder po) {
        System.out.println("View PO: " + po.getPoNumber());

        // Get requisition ID from map
        Integer requisitionId = poNumberToRequisitionId.get(po.getPoNumber());
        if (requisitionId == null) {
            showError("Error", "Could not find requisition details for " + po.getPoNumber());
            return;
        }

        try {
            // Fetch full requisition details from database
            Requisition requisition = requisitionDAO.getRequisitionById(requisitionId);
            if (requisition == null) {
                showError("Error", "Could not load requisition details.");
                return;
            }

            // Build detailed message
            StringBuilder details = new StringBuilder();
            details.append("PO Number: ").append(po.getPoNumber()).append("\n");
            details.append("Status: ").append(po.getStatus()).append("\n");
            details.append("Requested By: ").append(po.getRequestedBy()).append("\n");
            details.append("Date: ").append(po.getDate().format(DateTimeFormatter.ofPattern("MMM dd, yyyy"))).append("\n");
            details.append("Category: ").append(requisition.getCategory() != null ? requisition.getCategory() : "N/A").append("\n");
            details.append("Priority: ").append(requisition.getPriority() != null ? requisition.getPriority() : "N/A").append("\n");
            details.append("Department: ").append(requisition.getDepartment() != null ? requisition.getDepartment() : "N/A").append("\n\n");

            details.append("Items (" + po.getItemsCount() + "):\n");
            details.append("─".repeat(50)).append("\n");

            if (requisition.getItems() != null && !requisition.getItems().isEmpty()) {
                for (RequisitionItem item : requisition.getItems()) {
                    details.append(String.format("• %s\n", item.getItemName()));
                    details.append(String.format("  Qty: %d | Unit Price: $%.2f | Total: $%.2f\n",
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()));
                }
            } else {
                details.append("No items found.\n");
            }

            details.append("─".repeat(50)).append("\n");
            details.append(String.format("Total Amount: $%,.2f", po.getTotalAmount()));

            if (requisition.getJustification() != null && !requisition.getJustification().isEmpty()) {
                details.append("\n\nJustification:\n").append(requisition.getJustification());
            }

            if (requisition.getReviewNotes() != null && !requisition.getReviewNotes().isEmpty()) {
                details.append("\n\nReview Notes:\n").append(requisition.getReviewNotes());
            }

            // Show in alert dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Purchase Order Details");
            alert.setHeaderText(po.getPoNumber() + " - Details");
            alert.setContentText(details.toString());
            alert.getDialogPane().setMinWidth(600);
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to load PO details: " + e.getMessage());
        }
    }

    private void handleApprovePO(PurchaseOrder po) {
        // Get requisition ID from map
        Integer requisitionId = poNumberToRequisitionId.get(po.getPoNumber());
        if (requisitionId == null) {
            showError("Error", "Could not find requisition ID for " + po.getPoNumber());
            return;
        }

        // Prompt for approval notes
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Approve Requisition");
        dialog.setHeaderText("Approve " + po.getPoNumber());
        dialog.setContentText("Approval notes (optional):");
        String notes = dialog.showAndWait().orElse("Approved by manager");

        try {
            // Update database
            boolean success = requisitionDAO.updateRequisitionStatus(
                requisitionId,
                "Approved",
                currentUser != null ? currentUser.getUserId() : null,
                notes
            );

            if (success) {
                // Update inventory quantities after approval
                updateInventoryFromApprovedRequisition(requisitionId);

                // Update UI
                po.setStatus("Approved");
                purchaseOrdersTable.refresh();
                updateStats();
                showInfo("Approved", po.getPoNumber() + " has been approved.");
            } else {
                showError("Error", "Failed to approve requisition in database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to approve requisition: " + e.getMessage());
        }
    }

    /**
     * Increases inventory quantities based on approved requisition items
     * This is called after a requisition is approved to update stock levels
     *
     * @param requisitionId The ID of the approved requisition
     */
    private void updateInventoryFromApprovedRequisition(Integer requisitionId) {
        try {
            // Get requisition with all items
            Requisition requisition = requisitionDAO.getRequisitionById(requisitionId);
            if (requisition == null || requisition.getItems() == null) {
                System.err.println("Could not retrieve requisition " + requisitionId + " for inventory update");
                return;
            }

            InventoryDAO inventoryDAO = new InventoryDAO();
            int itemsUpdated = 0;
            int itemsNotFound = 0;

            // Process each requisition item
            for (RequisitionItem reqItem : requisition.getItems()) {
                // Try to find matching inventory item by name
                InventoryItem inventoryItem = inventoryDAO.findInventoryItemByName(reqItem.getItemName());

                if (inventoryItem != null) {
                    // Item exists in inventory - increase quantity
                    boolean increased = inventoryDAO.increaseInventoryQuantity(
                        inventoryItem.getItemId(),
                        reqItem.getQuantity()
                    );

                    if (increased) {
                        System.out.println("Increased inventory: " + reqItem.getItemName() +
                                         " by " + reqItem.getQuantity() + " units (New quantity: " +
                                         (inventoryItem.getQuantity() + reqItem.getQuantity()) + ")");
                        itemsUpdated++;
                    } else {
                        System.err.println("Failed to increase inventory for: " + reqItem.getItemName());
                    }
                } else {
                    // Item doesn't exist in inventory yet
                    System.out.println("Note: '" + reqItem.getItemName() +
                                     "' not found in inventory. Requisition approved but inventory not updated for this item.");
                    itemsNotFound++;
                }
            }

            System.out.println("Inventory update complete: " + itemsUpdated + " items updated, " +
                             itemsNotFound + " items not found in inventory");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error updating inventory from requisition " + requisitionId + ": " + e.getMessage());
            // Don't fail the approval if inventory update fails - just log the error
        }
    }

    private void handleRejectPO(PurchaseOrder po) {
        // Get requisition ID from map
        Integer requisitionId = poNumberToRequisitionId.get(po.getPoNumber());
        if (requisitionId == null) {
            showError("Error", "Could not find requisition ID for " + po.getPoNumber());
            return;
        }

        // Prompt for rejection reason
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reject Requisition");
        dialog.setHeaderText("Reject " + po.getPoNumber());
        dialog.setContentText("Rejection reason:");
        String notes = dialog.showAndWait().orElse("");

        if (notes.isEmpty()) {
            showError("Rejection Reason Required", "Please provide a reason for rejection.");
            return;
        }

        try {
            // Update database
            boolean success = requisitionDAO.updateRequisitionStatus(
                requisitionId,
                "Rejected",
                currentUser != null ? currentUser.getUserId() : null,
                notes
            );

            if (success) {
                // Update UI
                po.setStatus("Rejected");
                purchaseOrdersTable.refresh();
                updateStats();
                showInfo("Rejected", po.getPoNumber() + " has been rejected.");
            } else {
                showError("Error", "Failed to reject requisition in database.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to reject requisition: " + e.getMessage());
        }
    }

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

    // ==================== INNER CLASS ====================

    /**
     * Purchase Order Model
     */
    public static class PurchaseOrder {
        private final BooleanProperty selected;
        private final StringProperty poNumber;
        private final StringProperty supplier;
        private final StringProperty requestedBy;
        private final ObjectProperty<LocalDate> date;
        private final IntegerProperty itemsCount;
        private final ObjectProperty<BigDecimal> totalAmount;
        private final StringProperty status;

        public PurchaseOrder(String poNumber, String supplier, String requestedBy, LocalDate date,
                           int itemsCount, BigDecimal totalAmount, String status) {
            this.selected = new SimpleBooleanProperty(false);
            this.poNumber = new SimpleStringProperty(poNumber);
            this.supplier = new SimpleStringProperty(supplier);
            this.requestedBy = new SimpleStringProperty(requestedBy);
            this.date = new SimpleObjectProperty<>(date);
            this.itemsCount = new SimpleIntegerProperty(itemsCount);
            this.totalAmount = new SimpleObjectProperty<>(totalAmount);
            this.status = new SimpleStringProperty(status);
        }

        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean value) { selected.set(value); }
        public BooleanProperty selectedProperty() { return selected; }

        public String getPoNumber() { return poNumber.get(); }
        public StringProperty poNumberProperty() { return poNumber; }

        public String getSupplier() { return supplier.get(); }
        public StringProperty supplierProperty() { return supplier; }

        public String getRequestedBy() { return requestedBy.get(); }
        public StringProperty requestedByProperty() { return requestedBy; }

        public LocalDate getDate() { return date.get(); }
        public ObjectProperty<LocalDate> dateProperty() { return date; }

        public int getItemsCount() { return itemsCount.get(); }
        public IntegerProperty itemsCountProperty() { return itemsCount; }

        public BigDecimal getTotalAmount() { return totalAmount.get(); }
        public ObjectProperty<BigDecimal> totalAmountProperty() { return totalAmount; }

        public String getStatus() { return status.get(); }
        public void setStatus(String value) { status.set(value); }
        public StringProperty statusProperty() { return status; }
    }
}

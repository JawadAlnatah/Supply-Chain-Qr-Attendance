package com.team.supplychain.controllers;

import com.team.supplychain.dao.RequisitionDAO;
import com.team.supplychain.models.Requisition;
import com.team.supplychain.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the Employee Requisitions View
 * Displays requisition history, filters, and management tools
 */
public class EmployeeRequisitionsViewController {

    // ==================== SUMMARY CARDS ====================
    @FXML private Label totalRequestsValue;
    @FXML private Label pendingValue;
    @FXML private Label approvedValue;
    @FXML private Label rejectedValue;

    // ==================== FILTERS ====================
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> dateFilter;

    // ==================== LIST VIEW ====================
    @FXML private VBox requisitionsContainer;
    @FXML private Label totalCountLabel;

    // ==================== TABLE VIEW ====================
    @FXML private TableView<RequisitionRecord> requisitionsTable;
    @FXML private TableColumn<RequisitionRecord, String> reqIdColumn;
    @FXML private TableColumn<RequisitionRecord, String> categoryColumn;
    @FXML private TableColumn<RequisitionRecord, Integer> itemsColumn;
    @FXML private TableColumn<RequisitionRecord, String> dateRequestedColumn;
    @FXML private TableColumn<RequisitionRecord, String> statusColumn;
    @FXML private TableColumn<RequisitionRecord, String> reviewerColumn;

    // ==================== VIEW TOGGLE ====================
    @FXML private Button cardViewButton;
    @FXML private Button tableViewButton;
    @FXML private ScrollPane cardViewPane;
    @FXML private VBox tableViewPane;

    private User currentUser;
    private RequisitionDAO requisitionDAO;
    private ObservableList<RequisitionRecord> requisitionsData;
    private List<Requisition> allRequisitions;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

    /**
     * Set the current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadRequisitionsData();
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        System.out.println("EmployeeRequisitionsViewController initialized");

        // Initialize DAO and data
        requisitionDAO = new RequisitionDAO();
        requisitionsData = FXCollections.observableArrayList();

        // Initialize filter ComboBoxes
        if (statusFilter != null) {
            statusFilter.getItems().addAll("All Status", "Pending", "Approved", "Rejected");
            statusFilter.setValue("All Status");
        }

        if (dateFilter != null) {
            dateFilter.getItems().addAll("All Time", "This Week", "This Month", "This Quarter", "This Year");
            dateFilter.setValue("All Time");
        }

        // Initialize table columns
        setupTableColumns();

        // Set default view to card view
        showCardView();
    }

    /**
     * Setup table columns with cell value factories
     */
    private void setupTableColumns() {
        if (requisitionsTable == null) return;

        reqIdColumn.setCellValueFactory(new PropertyValueFactory<>("reqId"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        itemsColumn.setCellValueFactory(new PropertyValueFactory<>("items"));
        dateRequestedColumn.setCellValueFactory(new PropertyValueFactory<>("dateRequested"));
        reviewerColumn.setCellValueFactory(new PropertyValueFactory<>("reviewer"));

        // Setup status column with badge styling
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<RequisitionRecord, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(status);
                    badge.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; -fx-font-size: 11px; -fx-font-weight: bold; " +
                        getStatusBadgeStyle(status));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        requisitionsTable.setItems(requisitionsData);
    }

    /**
     * Get badge style based on status
     */
    private String getStatusBadgeStyle(String status) {
        switch (status) {
            case "Pending":
                return "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            case "Approved":
                return "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
            case "Rejected":
                return "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
            default:
                return "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        }
    }

    // ==================== ACTION HANDLERS ====================

    @FXML
    private void handleCreateRequisition() {
        System.out.println("Create new requisition clicked");
        // TODO: Open create requisition dialog/form
    }

    @FXML
    private void handleViewRequisition() {
        System.out.println("View requisition details clicked");
        // TODO: Show requisition details dialog
    }

    @FXML
    private void handleCancelRequisition() {
        System.out.println("Cancel requisition clicked");
        // TODO: Confirm and cancel requisition via DAO
    }

    @FXML
    private void handleExportList() {
        System.out.println("Export list clicked");
        // TODO: Export requisitions to Excel/PDF
    }

    // ==================== VIEW TOGGLE HANDLERS ====================

    @FXML
    private void handleCardView() {
        System.out.println("Card view toggled");
        showCardView();
    }

    @FXML
    private void handleTableView() {
        System.out.println("Table view toggled");
        showTableView();
    }

    private void showCardView() {
        if (cardViewPane != null && tableViewPane != null) {
            cardViewPane.setVisible(true);
            cardViewPane.setManaged(true);
            tableViewPane.setVisible(false);
            tableViewPane.setManaged(false);
            updateViewButtonStyles(true);
        }
    }

    private void showTableView() {
        if (cardViewPane != null && tableViewPane != null) {
            cardViewPane.setVisible(false);
            cardViewPane.setManaged(false);
            tableViewPane.setVisible(true);
            tableViewPane.setManaged(true);
            updateViewButtonStyles(false);
        }
    }

    private void updateViewButtonStyles(boolean cardActive) {
        if (cardViewButton != null && tableViewButton != null) {
            if (cardActive) {
                cardViewButton.getStyleClass().remove("action-button-secondary");
                if (!cardViewButton.getStyleClass().contains("action-button-primary")) {
                    cardViewButton.getStyleClass().add("action-button-primary");
                }
                tableViewButton.getStyleClass().remove("action-button-primary");
                if (!tableViewButton.getStyleClass().contains("action-button-secondary")) {
                    tableViewButton.getStyleClass().add("action-button-secondary");
                }
            } else {
                cardViewButton.getStyleClass().remove("action-button-primary");
                if (!cardViewButton.getStyleClass().contains("action-button-secondary")) {
                    cardViewButton.getStyleClass().add("action-button-secondary");
                }
                tableViewButton.getStyleClass().remove("action-button-secondary");
                if (!tableViewButton.getStyleClass().contains("action-button-primary")) {
                    tableViewButton.getStyleClass().add("action-button-primary");
                }
            }
        }
    }

    // ==================== FILTER HANDLERS ====================

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        System.out.println("Search: " + query);
        // TODO: Filter requisitions by search query
    }

    @FXML
    private void handleStatusFilter() {
        String status = statusFilter.getValue();
        System.out.println("Filter by status: " + status);
        // TODO: Filter requisitions by status
    }

    @FXML
    private void handleDateFilter() {
        String dateRange = dateFilter.getValue();
        System.out.println("Filter by date: " + dateRange);
        // TODO: Filter requisitions by date range
    }

    // ==================== HELPER METHODS ====================

    /**
     * Load requisitions from database for current user
     */
    private void loadRequisitionsData() {
        if (currentUser == null) {
            System.out.println("Cannot load requisitions - no user set");
            return;
        }

        System.out.println("Loading requisitions data for user: " + currentUser.getUsername());

        try {
            // Load requisitions from database
            allRequisitions = requisitionDAO.getRequisitionsByUser(currentUser.getUserId());

            System.out.println("Loaded " + allRequisitions.size() + " requisitions from database");

            // Update statistics
            updateStatistics();

            // Populate table view
            populateTableView(allRequisitions);

            // Populate card view
            populateCardView(allRequisitions);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error loading requisitions: " + e.getMessage());
            showError("Load Error", "Failed to load requisitions from database.");
        }
    }

    /**
     * Update statistics cards
     */
    private void updateStatistics() {
        if (allRequisitions == null) return;

        int total = allRequisitions.size();
        long pending = allRequisitions.stream().filter(r -> "Pending".equals(r.getStatus())).count();
        long approved = allRequisitions.stream().filter(r -> "Approved".equals(r.getStatus())).count();
        long rejected = allRequisitions.stream().filter(r -> "Rejected".equals(r.getStatus())).count();

        if (totalRequestsValue != null) totalRequestsValue.setText(String.valueOf(total));
        if (pendingValue != null) pendingValue.setText(String.valueOf(pending));
        if (approvedValue != null) approvedValue.setText(String.valueOf(approved));
        if (rejectedValue != null) rejectedValue.setText(String.valueOf(rejected));
    }

    /**
     * Populate table view with requisitions
     */
    private void populateTableView(List<Requisition> requisitions) {
        requisitionsData.clear();

        for (Requisition req : requisitions) {
            String dateStr = req.getRequestDate() != null ?
                req.getRequestDate().format(DATE_FORMATTER) : "N/A";

            String reviewer = req.getReviewerName() != null ?
                req.getReviewerName() : "Pending Review";

            RequisitionRecord record = new RequisitionRecord(
                req.getRequisitionCode(),
                req.getCategory(),
                req.getTotalItems(),
                dateStr,
                req.getStatus(),
                reviewer
            );

            requisitionsData.add(record);
        }

        if (totalCountLabel != null) {
            totalCountLabel.setText("Total: " + requisitions.size() + " requisition" +
                (requisitions.size() != 1 ? "s" : ""));
        }
    }

    /**
     * Populate card view with requisitions
     */
    private void populateCardView(List<Requisition> requisitions) {
        if (requisitionsContainer == null) return;

        requisitionsContainer.getChildren().clear();

        for (Requisition req : requisitions) {
            VBox card = createRequisitionCard(req);
            requisitionsContainer.getChildren().add(card);
        }
    }

    /**
     * Create a card for displaying a requisition
     */
    private VBox createRequisitionCard(Requisition req) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12px; " +
                     "-fx-padding: 20px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        card.setPrefWidth(Double.MAX_VALUE);

        // Header row with requisition code and status
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label codeLabel = new Label(req.getRequisitionCode());
        codeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label statusBadge = new Label(req.getStatus());
        statusBadge.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; " +
                           "-fx-font-size: 11px; -fx-font-weight: bold; " +
                           getStatusBadgeStyle(req.getStatus()));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        header.getChildren().addAll(codeLabel, spacer, statusBadge);

        // Details grid
        VBox details = new VBox(8);

        details.getChildren().add(createDetailRow("Category:", req.getCategory()));
        details.getChildren().add(createDetailRow("Department:", req.getDepartment()));
        details.getChildren().add(createDetailRow("Total Items:", String.valueOf(req.getTotalItems())));
        details.getChildren().add(createDetailRow("Total Amount:", "SAR " + req.getTotalAmount()));
        details.getChildren().add(createDetailRow("Date Requested:",
            req.getRequestDate() != null ? req.getRequestDate().format(DATE_FORMATTER) : "N/A"));

        if (req.getReviewerName() != null) {
            details.getChildren().add(createDetailRow("Reviewed By:", req.getReviewerName()));
        }

        card.getChildren().addAll(header, details);

        return card;
    }

    /**
     * Create a detail row for card view
     */
    private HBox createDetailRow(String label, String value) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-min-width: 120px;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 13px; -fx-text-fill: #1e293b; -fx-font-weight: 500;");

        row.getChildren().addAll(labelNode, valueNode);
        return row;
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

    // ==================== INNER CLASS FOR TABLE ====================

    /**
     * Simple POJO for requisition table display
     */
    public static class RequisitionRecord {
        private final String reqId;
        private final String category;
        private final int items;
        private final String dateRequested;
        private final String status;
        private final String reviewer;

        public RequisitionRecord(String reqId, String category, int items,
                                 String dateRequested, String status, String reviewer) {
            this.reqId = reqId;
            this.category = category;
            this.items = items;
            this.dateRequested = dateRequested;
            this.status = status;
            this.reviewer = reviewer;
        }

        public String getReqId() { return reqId; }
        public String getCategory() { return category; }
        public int getItems() { return items; }
        public String getDateRequested() { return dateRequested; }
        public String getStatus() { return status; }
        public String getReviewer() { return reviewer; }
    }
}

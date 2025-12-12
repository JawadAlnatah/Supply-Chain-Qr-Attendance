package com.team.supplychain.controllers;

import com.team.supplychain.dao.AuditLogDAO;
import com.team.supplychain.models.AuditLog;
import com.team.supplychain.models.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminAuditLogsController {

    @FXML private Label todayActivitiesLabel, dbChangesLabel, userActionsLabel, systemEventsLabel;
    @FXML private ComboBox<String> actionTypeFilter, moduleFilter, resultFilter;
    @FXML private TextField searchField;
    @FXML private Button exportButton, generateReportButton, archiveButton, refreshButton;
    @FXML private TableView<AuditLog> logsTable;
    @FXML private TableColumn<AuditLog, String> logIdColumn, timestampColumn, userColumn, actionTypeColumn, moduleColumn, descriptionColumn, resultColumn;
    @FXML private TableColumn<AuditLog, Void> actionsColumn;

    private User currentUser;
    private ObservableList<AuditLog> logsData;
    private AuditLogDAO auditLogDAO;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int PAGE_SIZE = 100;
    private int currentOffset = 0;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        System.out.println("AdminAuditLogsController initialized");
        auditLogDAO = new AuditLogDAO();
        logsData = FXCollections.observableArrayList();
        setupTable();
        setupFilters();
        setupSearchAndFilters();
        loadLogsFromDatabase();
        updateStats();
    }

    private void setupTable() {
        if (logsTable == null) return;

        // Set cell value factories with proper field names from AuditLog model
        logIdColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getLogCode()));

        timestampColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getTimestamp().format(dateFormatter)));

        userColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getUsername()));

        actionTypeColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getActionType()));

        moduleColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getModule()));

        descriptionColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDescription()));

        resultColumn.setCellValueFactory(cellData ->
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getResult()));

        // Custom cell factory for result column with badges
        resultColumn.setCellFactory(column -> new TableCell<AuditLog, String>() {
            @Override
            protected void updateItem(String result, boolean empty) {
                super.updateItem(result, empty);
                if (empty || result == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(result.toUpperCase());
                    badge.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; -fx-font-size: 11px; -fx-font-weight: bold; " +
                        getResultBadgeStyle(result));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Actions column with View button
        actionsColumn.setCellFactory(param -> new TableCell<AuditLog, Void>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle("-fx-background-color: #22d3ee; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 12px; -fx-font-size: 11px; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> {
                    AuditLog log = getTableView().getItems().get(getIndex());
                    showLogDetailsDialog(log);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
                if (!empty) {
                    setAlignment(Pos.CENTER);
                }
            }
        });

        logsTable.setItems(logsData);
    }

    private void setupFilters() {
        if (actionTypeFilter != null) {
            actionTypeFilter.getItems().addAll("All Actions", "CREATE", "UPDATE", "DELETE", "LOGIN", "LOGOUT", "SECURITY_INCIDENT", "BACKUP", "READ");
            actionTypeFilter.setValue("All Actions");
        }
        if (moduleFilter != null) {
            moduleFilter.getItems().addAll("All Modules", "Users", "Inventory", "Settings", "Authentication", "Security", "Database", "Purchase Orders", "Requisitions", "Attendance", "Reports");
            moduleFilter.setValue("All Modules");
        }
        if (resultFilter != null) {
            resultFilter.getItems().addAll("All Results", "SUCCESS", "FAILED", "WARNING");
            resultFilter.setValue("All Results");
        }
    }

    /**
     * Setup listeners for search and filter fields
     */
    private void setupSearchAndFilters() {
        // Add listeners to filters - reload data when filter changes
        if (actionTypeFilter != null) {
            actionTypeFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentOffset = 0;
                loadLogsFromDatabase();
                updateStats();
            });
        }
        if (moduleFilter != null) {
            moduleFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentOffset = 0;
                loadLogsFromDatabase();
                updateStats();
            });
        }
        if (resultFilter != null) {
            resultFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
                currentOffset = 0;
                loadLogsFromDatabase();
                updateStats();
            });
        }
        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                currentOffset = 0;
                loadLogsFromDatabase();
                updateStats();
            });
        }
    }

    /**
     * Load audit logs from database with current filters
     */
    private void loadLogsFromDatabase() {
        if (logsData == null) return;

        logsData.clear();

        try {
            // Get current filter values
            String actionType = actionTypeFilter != null ? actionTypeFilter.getValue() : "All Actions";
            String module = moduleFilter != null ? moduleFilter.getValue() : "All Modules";
            String result = resultFilter != null ? resultFilter.getValue() : "All Results";
            String searchText = searchField != null ? searchField.getText() : "";

            // Convert "All X" to null for DAO
            if ("All Actions".equals(actionType)) actionType = null;
            if ("All Modules".equals(module)) module = null;
            if ("All Results".equals(result)) result = null;
            if (searchText != null && searchText.trim().isEmpty()) searchText = null;

            // Fetch filtered logs from database
            List<AuditLog> logs = auditLogDAO.getFilteredAuditLogs(
                actionType, module, result, searchText, PAGE_SIZE, currentOffset);

            // Add to observable list
            logsData.addAll(logs);

            System.out.println("Loaded " + logs.size() + " audit logs from database");

        } catch (Exception e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load audit logs: " + e.getMessage());
        }
    }

    /**
     * Update statistics cards from database
     */
    private void updateStats() {
        try {
            // Today's Activities
            int todayCount = auditLogDAO.getTodayActivityCount();
            if (todayActivitiesLabel != null) {
                todayActivitiesLabel.setText(String.format("%,d", todayCount));
            }

            // Database Changes (Database + Settings + Inventory modules)
            int dbChanges = auditLogDAO.getCountByModule("Database") +
                           auditLogDAO.getCountByModule("Settings") +
                           auditLogDAO.getCountByModule("Inventory");
            if (dbChangesLabel != null) {
                dbChangesLabel.setText(String.format("%,d", dbChanges));
            }

            // User Actions (non-system users)
            int userActions = auditLogDAO.getCountByUserType(false);
            if (userActionsLabel != null) {
                userActionsLabel.setText(String.format("%,d", userActions));
            }

            // System Events (system user logs)
            int systemEvents = auditLogDAO.getCountByUserType(true);
            if (systemEventsLabel != null) {
                systemEventsLabel.setText(String.format("%,d", systemEvents));
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to update statistics: " + e.getMessage());
        }
    }

    private String getResultBadgeStyle(String result) {
        if (result == null) return "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";

        switch (result.toUpperCase()) {
            case "SUCCESS":
                return "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
            case "FAILED":
                return "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
            case "WARNING":
                return "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            default:
                return "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        }
    }

    /**
     * Show detailed dialog for a single audit log
     */
    private void showLogDetailsDialog(AuditLog log) {
        if (log == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Audit Log Details");
        dialog.setHeaderText("Log Code: " + log.getLogCode());

        // Create grid for log details
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(12);
        grid.setPadding(new Insets(20, 30, 20, 30));

        int row = 0;

        // Timestamp
        grid.add(createLabel("Timestamp:", true), 0, row);
        grid.add(createLabel(log.getTimestamp().format(dateFormatter), false), 1, row++);

        // User
        String userText = log.getUsername();
        if (log.getUserId() != null) {
            userText += " (ID: " + log.getUserId() + ")";
        }
        grid.add(createLabel("User:", true), 0, row);
        grid.add(createLabel(userText, false), 1, row++);

        // Action Type
        grid.add(createLabel("Action Type:", true), 0, row);
        grid.add(createLabel(log.getActionType(), false), 1, row++);

        // Module
        grid.add(createLabel("Module:", true), 0, row);
        grid.add(createLabel(log.getModule(), false), 1, row++);

        // Result with badge
        grid.add(createLabel("Result:", true), 0, row);
        Label resultBadge = new Label(log.getResult().toUpperCase());
        resultBadge.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; -fx-font-size: 11px; -fx-font-weight: bold; " +
            getResultBadgeStyle(log.getResult()));
        grid.add(resultBadge, 1, row++);

        // Description (multi-line)
        grid.add(createLabel("Description:", true), 0, row);
        TextArea descArea = new TextArea(log.getDescription());
        descArea.setWrapText(true);
        descArea.setEditable(false);
        descArea.setPrefRowCount(3);
        descArea.setMaxWidth(400);
        grid.add(descArea, 1, row++);

        // IP Address (if available)
        if (log.getIpAddress() != null && !log.getIpAddress().isEmpty()) {
            grid.add(createLabel("IP Address:", true), 0, row);
            grid.add(createLabel(log.getIpAddress(), false), 1, row++);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private Label createLabel(String text, boolean bold) {
        Label label = new Label(text);
        if (bold) {
            label.setStyle("-fx-font-weight: bold; -fx-text-fill: #4b5563;");
        } else {
            label.setStyle("-fx-text-fill: #6b7280;");
        }
        return label;
    }

    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Audit Logs");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        fileChooser.setInitialFileName("audit_logs_" + java.time.LocalDate.now());

        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (file != null) {
            try {
                if (file.getName().endsWith(".csv")) {
                    exportToCSV(file);
                } else {
                    exportToExcel(file);
                }

                // Log the export action (using READ action type)
                if (currentUser != null) {
                    auditLogDAO.logSuccess(currentUser.getUserId(), currentUser.getUsername(),
                        "READ", "Audit Logs", "Exported audit logs to " + file.getName());
                }

                showInfo("Export Successful", "Audit logs exported to:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                showError("Export Failed", "Failed to export audit logs: " + e.getMessage());
            }
        }
    }

    private void exportToCSV(File file) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write header
            writer.write("Log Code,Timestamp,User,Action Type,Module,Description,Result");
            writer.newLine();

            // Write data
            for (AuditLog log : logsData) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s",
                    escapeCsv(log.getLogCode()),
                    escapeCsv(log.getTimestamp().format(dateFormatter)),
                    escapeCsv(log.getUsername()),
                    escapeCsv(log.getActionType()),
                    escapeCsv(log.getModule()),
                    escapeCsv(log.getDescription()),
                    escapeCsv(log.getResult())
                ));
                writer.newLine();
            }
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void exportToExcel(File file) throws Exception {
        showError("Not Implemented", "Excel export requires Apache POI library.\nPlease use CSV export for now.");
    }

    @FXML
    private void handleGenerateReport() {
        showInfo("Generate Report", "PDF report generation functionality requires iText library.\nThis feature will be implemented in the next update.");
    }

    @FXML
    private void handleArchive() {
        // Create custom dialog with spinner for days
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Archive Old Logs");
        dialog.setHeaderText("Delete old successful audit logs");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        Spinner<Integer> daysSpinner = new Spinner<>(30, 365, 90, 1);
        daysSpinner.setEditable(true);

        grid.add(new Label("Delete logs older than:"), 0, 0);
        grid.add(daysSpinner, 1, 0);
        grid.add(new Label("days"), 2, 0);
        grid.add(new Label("(Only SUCCESS logs will be deleted)"), 0, 1, 3, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == ButtonType.OK) {
                return daysSpinner.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(days -> {
            // Confirmation dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Archive");
            confirm.setHeaderText("Archive logs older than " + days + " days?");
            confirm.setContentText("This will permanently delete old SUCCESS logs.\nFAILED and WARNING logs will be preserved.");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        int deletedCount = auditLogDAO.archiveOldLogs(days);

                        // Log the archive action (using DELETE action type)
                        if (currentUser != null) {
                            auditLogDAO.logSuccess(currentUser.getUserId(), currentUser.getUsername(),
                                "DELETE", "Audit Logs", "Archived " + deletedCount + " logs older than " + days + " days");
                        }

                        showInfo("Archive Complete", deletedCount + " old audit logs were deleted.");
                        handleRefresh();
                    } catch (Exception e) {
                        e.printStackTrace();
                        showError("Archive Failed", "Failed to archive logs: " + e.getMessage());
                    }
                }
            });
        });
    }

    @FXML
    private void handleRefresh() {
        currentOffset = 0;
        loadLogsFromDatabase();
        updateStats();
        showInfo("Refreshed", "Audit logs have been refreshed from the database.");
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
}

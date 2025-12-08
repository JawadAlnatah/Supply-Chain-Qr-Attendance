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

public class AdminAuditLogsController {

    @FXML private Label todayActivitiesLabel, dbChangesLabel, userActionsLabel, systemEventsLabel;
    @FXML private ComboBox<String> actionTypeFilter, moduleFilter, resultFilter;
    @FXML private TextField searchField;
    @FXML private Button exportButton, generateReportButton, archiveButton, refreshButton;
    @FXML private TableView<LogRecord> logsTable;
    @FXML private TableColumn<LogRecord, String> logIdColumn, timestampColumn, userColumn, actionTypeColumn, moduleColumn, descriptionColumn, ipAddressColumn, resultColumn;
    @FXML private TableColumn<LogRecord, Void> actionsColumn;

    private User currentUser;
    private ObservableList<LogRecord> logsData;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        System.out.println("AdminAuditLogsController initialized");
        logsData = FXCollections.observableArrayList();
        setupTable();
        setupFilters();
        loadDummyData();
        updateStats();
    }

    private void setupTable() {
        if (logsTable == null) return;

        logIdColumn.setCellValueFactory(new PropertyValueFactory<>("logId"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        userColumn.setCellValueFactory(new PropertyValueFactory<>("user"));
        actionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("actionType"));
        moduleColumn.setCellValueFactory(new PropertyValueFactory<>("module"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));

        resultColumn.setCellValueFactory(new PropertyValueFactory<>("result"));
        resultColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String result, boolean empty) {
                super.updateItem(result, empty);
                if (empty || result == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(result);
                    badge.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; -fx-font-size: 11px; -fx-font-weight: bold; " +
                        getResultBadgeStyle(result));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View");
            {
                viewBtn.setStyle("-fx-background-color: #22d3ee; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 12px; -fx-font-size: 11px; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        System.out.println("View: " + getTableView().getItems().get(getIndex()).getLogId());
                    }
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
            actionTypeFilter.getItems().addAll("All Actions", "CREATE", "UPDATE", "DELETE", "LOGIN", "BACKUP");
            actionTypeFilter.setValue("All Actions");
        }
        if (moduleFilter != null) {
            moduleFilter.getItems().addAll("All Modules", "Users", "Inventory", "Settings", "Authentication", "Database");
            moduleFilter.setValue("All Modules");
        }
        if (resultFilter != null) {
            resultFilter.getItems().addAll("All Results", "Success", "Failed", "Warning");
            resultFilter.setValue("All Results");
        }
    }

    private void loadDummyData() {
        logsData.clear();
        logsData.add(new LogRecord("LOG0001247", "2025-12-05 14:30:15", "admin", "UPDATE", "Users", "Modified role for user 'employee3' from EMPLOYEE to MANAGER", "192.168.1.10", "Success"));
        logsData.add(new LogRecord("LOG0001246", "2025-12-05 14:28:42", "manager1", "CREATE", "Inventory", "Added new inventory item 'Industrial Gloves (100 pairs)'", "192.168.1.45", "Success"));
        logsData.add(new LogRecord("LOG0001245", "2025-12-05 14:25:10", "employee1", "READ", "Reports", "Viewed attendance report for November 2025", "192.168.1.87", "Success"));
        logsData.add(new LogRecord("LOG0001244", "2025-12-05 14:20:05", "admin", "DELETE", "Settings", "Removed deprecated setting 'legacy_mode_enabled'", "192.168.1.10", "Success"));
        logsData.add(new LogRecord("LOG0001243", "2025-12-05 14:15:33", "system", "BACKUP", "Database", "Automated backup completed: backup_20251205_141530.sql", "localhost", "Success"));
        logsData.add(new LogRecord("LOG0001242", "2025-12-05 14:10:22", "supplier1", "LOGIN", "Authentication", "User logged in successfully", "172.16.0.12", "Success"));
        logsData.add(new LogRecord("LOG0001241", "2025-12-05 14:05:18", "manager2", "UPDATE", "Purchase Orders", "Updated PO-2025-089 status to 'Shipped'", "192.168.1.50", "Success"));
        logsData.add(new LogRecord("LOG0001240", "2025-12-05 14:00:45", "employee2", "CREATE", "Attendance", "Checked in via QR code", "192.168.1.88", "Success"));
        logsData.add(new LogRecord("LOG0001239", "2025-12-05 13:55:30", "admin", "UPDATE", "Security", "Changed password policy: min_length=12", "192.168.1.10", "Success"));
        logsData.add(new LogRecord("LOG0001238", "2025-12-05 13:50:12", "unknown", "LOGIN", "Authentication", "Failed login attempt for username 'hacker'", "185.220.101.45", "Failed"));
    }

    private void updateStats() {
        if (todayActivitiesLabel != null) todayActivitiesLabel.setText("1,247");

        long dbChanges = logsData.stream().filter(l -> "Database".equals(l.getModule()) || "Settings".equals(l.getModule())).count();
        if (dbChangesLabel != null) dbChangesLabel.setText(String.valueOf(dbChanges));

        long userActions = logsData.stream().filter(l -> !"system".equals(l.getUser())).count();
        if (userActionsLabel != null) userActionsLabel.setText(String.valueOf(userActions));

        long systemEvents = logsData.stream().filter(l -> "system".equals(l.getUser())).count();
        if (systemEventsLabel != null) systemEventsLabel.setText(String.valueOf(systemEvents));
    }

    private String getResultBadgeStyle(String result) {
        return switch (result) {
            case "Success" -> "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
            case "Failed" -> "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
            case "Warning" -> "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            default -> "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        };
    }

    @FXML private void handleExport() { showInfo("Export", "Audit log export functionality will be implemented."); }
    @FXML private void handleGenerateReport() { showInfo("Generate Report", "Report generation functionality will be implemented."); }
    @FXML private void handleArchive() { showInfo("Archive", "Old logs archiving functionality will be implemented."); }
    @FXML private void handleRefresh() { loadDummyData(); updateStats(); }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class LogRecord {
        private final StringProperty logId, timestamp, user, actionType, module, description, ipAddress, result;

        public LogRecord(String logId, String timestamp, String user, String actionType, String module, String description, String ipAddress, String result) {
            this.logId = new SimpleStringProperty(logId);
            this.timestamp = new SimpleStringProperty(timestamp);
            this.user = new SimpleStringProperty(user);
            this.actionType = new SimpleStringProperty(actionType);
            this.module = new SimpleStringProperty(module);
            this.description = new SimpleStringProperty(description);
            this.ipAddress = new SimpleStringProperty(ipAddress);
            this.result = new SimpleStringProperty(result);
        }

        public String getLogId() { return logId.get(); }
        public String getTimestamp() { return timestamp.get(); }
        public String getUser() { return user.get(); }
        public String getActionType() { return actionType.get(); }
        public String getModule() { return module.get(); }
        public String getDescription() { return description.get(); }
        public String getIpAddress() { return ipAddress.get(); }
        public String getResult() { return result.get(); }
    }
}

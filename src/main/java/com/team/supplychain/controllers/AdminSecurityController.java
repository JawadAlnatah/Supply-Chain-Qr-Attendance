package com.team.supplychain.controllers;

import com.team.supplychain.models.User;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminSecurityController {

    @FXML private Label failedLoginsLabel, activeSessionsLabel, blockedIpsLabel, securityScoreLabel;
    @FXML private ComboBox<String> eventTypeFilter, severityFilter, statusFilter;
    @FXML private TextField searchField;
    @FXML private Button exportButton, clearSessionsButton, refreshButton;
    @FXML private TableView<IncidentRecord> incidentsTable;
    @FXML private TableColumn<IncidentRecord, String> incidentIdColumn, timestampColumn, eventTypeColumn, usernameColumn, ipAddressColumn, locationColumn, severityColumn, incidentStatusColumn;
    @FXML private TableColumn<IncidentRecord, Void> actionsColumn;

    private User currentUser;
    private ObservableList<IncidentRecord> incidentsData;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        System.out.println("AdminSecurityController initialized");
        incidentsData = FXCollections.observableArrayList();
        setupTable();
        setupFilters();
        loadDummyData();
        updateStats();
    }

    private void setupTable() {
        if (incidentsTable == null) return;

        incidentIdColumn.setCellValueFactory(new PropertyValueFactory<>("incidentId"));
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        ipAddressColumn.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));

        eventTypeColumn.setCellValueFactory(new PropertyValueFactory<>("eventType"));
        eventTypeColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String eventType, boolean empty) {
                super.updateItem(eventType, empty);
                if (empty || eventType == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(eventType);
                    badge.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; -fx-font-size: 11px; -fx-font-weight: bold; " +
                        getEventTypeBadgeStyle(eventType));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        severityColumn.setCellValueFactory(new PropertyValueFactory<>("severity"));
        severityColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String severity, boolean empty) {
                super.updateItem(severity, empty);
                if (empty || severity == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(severity);
                    badge.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; -fx-font-size: 11px; -fx-font-weight: bold; " +
                        getSeverityBadgeStyle(severity));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        incidentStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        incidentStatusColumn.setCellFactory(column -> new TableCell<>() {
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

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewBtn = new Button("View Details");
            {
                viewBtn.setStyle("-fx-background-color: #22d3ee; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 12px; -fx-font-size: 11px; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        System.out.println("View: " + getTableView().getItems().get(getIndex()).getIncidentId());
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty) {
                    setGraphic(viewBtn);
                    setAlignment(Pos.CENTER);
                } else {
                    setGraphic(null);
                }
            }
        });

        incidentsTable.setItems(incidentsData);
    }

    private void setupFilters() {
        if (eventTypeFilter != null) {
            eventTypeFilter.getItems().addAll("All Events", "Failed Login", "Unauthorized Access", "Suspicious Activity", "Brute Force");
            eventTypeFilter.setValue("All Events");
        }
        if (severityFilter != null) {
            severityFilter.getItems().addAll("All Severities", "Critical", "High", "Medium", "Low");
            severityFilter.setValue("All Severities");
        }
        if (statusFilter != null) {
            statusFilter.getItems().addAll("All Statuses", "Open", "Investigating", "Resolved");
            statusFilter.setValue("All Statuses");
        }
    }

    private void loadDummyData() {
        incidentsData.clear();
        incidentsData.add(new IncidentRecord("SEC015", "2025-12-05 14:30", "Failed Login", "unknown", "192.168.1.105", "Unknown", "High", "Open"));
        incidentsData.add(new IncidentRecord("SEC014", "2025-12-05 13:45", "Failed Login", "employee1", "192.168.1.87", "Riyadh, SA", "Low", "Resolved"));
        incidentsData.add(new IncidentRecord("SEC013", "2025-12-05 12:20", "Unauthorized Access", "guest", "10.0.0.55", "Dammam, SA", "Critical", "Investigating"));
        incidentsData.add(new IncidentRecord("SEC012", "2025-12-05 11:15", "Suspicious Activity", "manager1", "192.168.1.45", "Riyadh, SA", "Medium", "Investigating"));
        incidentsData.add(new IncidentRecord("SEC011", "2025-12-05 10:00", "Failed Login", "unknown", "203.45.67.89", "Unknown", "High", "Open"));
        incidentsData.add(new IncidentRecord("SEC010", "2025-12-05 09:30", "Failed Login", "admin", "192.168.1.10", "Jeddah, SA", "Low", "Resolved"));
        incidentsData.add(new IncidentRecord("SEC009", "2025-12-05 08:15", "Brute Force", "unknown", "185.220.101.45", "Unknown", "Critical", "Open"));
        incidentsData.add(new IncidentRecord("SEC008", "2025-12-04 22:45", "Failed Login", "supplier1", "172.16.0.12", "Riyadh, SA", "Low", "Resolved"));
    }

    private void updateStats() {
        long failedLogins = incidentsData.stream().filter(i -> "Failed Login".equals(i.getEventType())).count();
        if (failedLoginsLabel != null) failedLoginsLabel.setText(String.valueOf(failedLogins));
        if (activeSessionsLabel != null) activeSessionsLabel.setText("24");
        if (blockedIpsLabel != null) blockedIpsLabel.setText("3");
        if (securityScoreLabel != null) securityScoreLabel.setText("87%");
    }

    private String getEventTypeBadgeStyle(String eventType) {
        return switch (eventType) {
            case "Failed Login" -> "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
            case "Unauthorized Access" -> "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            case "Suspicious Activity" -> "-fx-background-color: rgba(234,179,8,0.15); -fx-text-fill: #eab308;";
            case "Brute Force" -> "-fx-background-color: rgba(220,38,38,0.15); -fx-text-fill: #dc2626;";
            default -> "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        };
    }

    private String getSeverityBadgeStyle(String severity) {
        return switch (severity) {
            case "Critical" -> "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
            case "High" -> "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            case "Medium" -> "-fx-background-color: rgba(234,179,8,0.15); -fx-text-fill: #eab308;";
            case "Low" -> "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
            default -> "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        };
    }

    private String getStatusBadgeStyle(String status) {
        return switch (status) {
            case "Open" -> "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
            case "Investigating" -> "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            case "Resolved" -> "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
            default -> "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        };
    }

    @FXML private void handleExport() { showInfo("Export", "Security log export functionality will be implemented."); }
    @FXML private void handleClearSessions() { showInfo("Clear Sessions", "All active sessions will be cleared."); }
    @FXML private void handleRefresh() { loadDummyData(); updateStats(); }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class IncidentRecord {
        private final StringProperty incidentId, timestamp, eventType, username, ipAddress, location, severity, status;

        public IncidentRecord(String incidentId, String timestamp, String eventType, String username, String ipAddress, String location, String severity, String status) {
            this.incidentId = new SimpleStringProperty(incidentId);
            this.timestamp = new SimpleStringProperty(timestamp);
            this.eventType = new SimpleStringProperty(eventType);
            this.username = new SimpleStringProperty(username);
            this.ipAddress = new SimpleStringProperty(ipAddress);
            this.location = new SimpleStringProperty(location);
            this.severity = new SimpleStringProperty(severity);
            this.status = new SimpleStringProperty(status);
        }

        public String getIncidentId() { return incidentId.get(); }
        public String getTimestamp() { return timestamp.get(); }
        public String getEventType() { return eventType.get(); }
        public String getUsername() { return username.get(); }
        public String getIpAddress() { return ipAddress.get(); }
        public String getLocation() { return location.get(); }
        public String getSeverity() { return severity.get(); }
        public String getStatus() { return status.get(); }
    }
}

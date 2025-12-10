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

public class AdminReportsController {

    @FXML private Label reportsGeneratedLabel, scheduledLabel, avgTimeLabel, downloadsLabel;
    @FXML private ComboBox<String> reportTypeFilter, formatFilter, statusFilter;
    @FXML private TextField searchField;
    @FXML private Button generateButton, scheduleButton, dashboardButton, refreshButton;
    @FXML private TableView<ReportRecord> reportsTable;
    @FXML private TableColumn<ReportRecord, String> reportIdColumn, reportNameColumn, reportTypeColumn, generatedByColumn, generatedDateColumn, formatColumn, sizeColumn, reportStatusColumn;
    @FXML private TableColumn<ReportRecord, Void> actionsColumn;

    private User currentUser;
    private ObservableList<ReportRecord> reportsData;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        System.out.println("AdminReportsController initialized");
        reportsData = FXCollections.observableArrayList();
        setupTable();
        setupFilters();
        loadDummyData();
        updateStats();
    }

    private void setupTable() {
        if (reportsTable == null) return;

        reportIdColumn.setCellValueFactory(new PropertyValueFactory<>("reportId"));
        reportNameColumn.setCellValueFactory(new PropertyValueFactory<>("reportName"));
        reportTypeColumn.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        generatedByColumn.setCellValueFactory(new PropertyValueFactory<>("generatedBy"));
        generatedDateColumn.setCellValueFactory(new PropertyValueFactory<>("generatedDate"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        formatColumn.setCellValueFactory(new PropertyValueFactory<>("format"));
        formatColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String format, boolean empty) {
                super.updateItem(format, empty);
                if (empty || format == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(format);
                    badge.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; -fx-font-size: 11px; -fx-font-weight: bold; " +
                        getFormatBadgeStyle(format));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        reportStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        reportStatusColumn.setCellFactory(column -> new TableCell<>() {
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
            private final Button downloadBtn = new Button("Download");
            private final Button viewBtn = new Button("View");
            private final Button deleteBtn = new Button("Delete");
            private final HBox container = new HBox(6, downloadBtn, viewBtn, deleteBtn);
            {
                downloadBtn.setStyle("-fx-background-color: #22d3ee; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 12px; -fx-font-size: 11px; -fx-cursor: hand;");
                viewBtn.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 12px; -fx-font-size: 11px; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 12px; -fx-font-size: 11px; -fx-cursor: hand;");
                downloadBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        System.out.println("Download: " + getTableView().getItems().get(getIndex()).getReportName());
                    }
                });
                viewBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        System.out.println("View: " + getTableView().getItems().get(getIndex()).getReportName());
                    }
                });
                deleteBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        System.out.println("Delete: " + getTableView().getItems().get(getIndex()).getReportId());
                    }
                });
                container.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        reportsTable.setItems(reportsData);
    }

    private void setupFilters() {
        if (reportTypeFilter != null) {
            reportTypeFilter.getItems().addAll("All Types", "Inventory", "Users", "Financial", "Attendance", "Security", "Audit");
            reportTypeFilter.setValue("All Types");
        }
        if (formatFilter != null) {
            formatFilter.getItems().addAll("All Formats", "PDF", "Excel", "CSV");
            formatFilter.setValue("All Formats");
        }
        if (statusFilter != null) {
            statusFilter.getItems().addAll("All Statuses", "Ready", "Generating", "Failed");
            statusFilter.setValue("All Statuses");
        }
    }

    private void loadDummyData() {
        reportsData.clear();
        reportsData.add(new ReportRecord("RPT015", "Monthly Inventory Summary - December 2025", "Inventory", "admin", "2025-12-05 14:00", "PDF", "2.3 MB", "Ready"));
        reportsData.add(new ReportRecord("RPT014", "User Activity Report - Last 7 Days", "Users", "admin", "2025-12-05 13:30", "Excel", "1.8 MB", "Ready"));
        reportsData.add(new ReportRecord("RPT013", "Financial Summary Q4 2025", "Financial", "manager1", "2025-12-05 12:00", "PDF", "4.5 MB", "Ready"));
        reportsData.add(new ReportRecord("RPT012", "Employee Attendance November 2025", "Attendance", "manager2", "2025-12-04 16:45", "Excel", "890 KB", "Ready"));
        reportsData.add(new ReportRecord("RPT011", "Low Stock Items Alert", "Inventory", "system", "2025-12-04 15:30", "CSV", "145 KB", "Ready"));
        reportsData.add(new ReportRecord("RPT010", "Security Incidents Report - November 2025", "Security", "admin", "2025-12-04 14:00", "PDF", "1.2 MB", "Ready"));
        reportsData.add(new ReportRecord("RPT009", "Supplier Performance Analysis Q4", "Suppliers", "manager1", "2025-12-03 11:20", "Excel", "2.7 MB", "Ready"));
        reportsData.add(new ReportRecord("RPT008", "Purchase Orders Summary November 2025", "Purchase Orders", "manager2", "2025-12-03 10:00", "PDF", "3.1 MB", "Ready"));
        reportsData.add(new ReportRecord("RPT007", "System Audit Log - Last 30 Days", "Audit", "admin", "2025-12-02 09:30", "CSV", "5.8 MB", "Ready"));
        reportsData.add(new ReportRecord("RPT006", "Employee Performance Review Q4", "HR", "manager1", "2025-12-01 14:15", "PDF", "2.9 MB", "Ready"));
    }

    private void updateStats() {
        if (reportsGeneratedLabel != null) reportsGeneratedLabel.setText(String.valueOf(reportsData.size()));
        if (scheduledLabel != null) scheduledLabel.setText("8");
        if (avgTimeLabel != null) avgTimeLabel.setText("2.3s");
        if (downloadsLabel != null) downloadsLabel.setText("1,042");
    }

    private String getFormatBadgeStyle(String format) {
        switch (format) {
            case "PDF":
                return "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
            case "Excel":
                return "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
            case "CSV":
                return "-fx-background-color: rgba(59,130,246,0.15); -fx-text-fill: #3b82f6;";
            default:
                return "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        }
    }

    private String getStatusBadgeStyle(String status) {
        switch (status) {
            case "Ready":
                return "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
            case "Generating":
                return "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            case "Failed":
                return "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
            default:
                return "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        }
    }

    @FXML private void handleGenerate() { showInfo("Generate Report", "Report generation dialog will be implemented."); }
    @FXML private void handleSchedule() { showInfo("Schedule Report", "Report scheduling functionality will be implemented."); }
    @FXML private void handleDashboard() { showInfo("Dashboard", "Analytics dashboard will be implemented."); }
    @FXML private void handleRefresh() { loadDummyData(); updateStats(); }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class ReportRecord {
        private final StringProperty reportId, reportName, reportType, generatedBy, generatedDate, format, size, status;

        public ReportRecord(String reportId, String reportName, String reportType, String generatedBy, String generatedDate, String format, String size, String status) {
            this.reportId = new SimpleStringProperty(reportId);
            this.reportName = new SimpleStringProperty(reportName);
            this.reportType = new SimpleStringProperty(reportType);
            this.generatedBy = new SimpleStringProperty(generatedBy);
            this.generatedDate = new SimpleStringProperty(generatedDate);
            this.format = new SimpleStringProperty(format);
            this.size = new SimpleStringProperty(size);
            this.status = new SimpleStringProperty(status);
        }

        public String getReportId() { return reportId.get(); }
        public String getReportName() { return reportName.get(); }
        public String getReportType() { return reportType.get(); }
        public String getGeneratedBy() { return generatedBy.get(); }
        public String getGeneratedDate() { return generatedDate.get(); }
        public String getFormat() { return format.get(); }
        public String getSize() { return size.get(); }
        public String getStatus() { return status.get(); }
    }
}

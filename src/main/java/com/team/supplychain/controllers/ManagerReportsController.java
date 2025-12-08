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
import java.time.LocalDate;

public class ManagerReportsController {

    @FXML private ComboBox<String> reportTypeCombo, dateRangeCombo, exportFormatCombo, departmentCombo;
    @FXML private Button inventoryReportBtn, attendanceReportBtn, poReportBtn, generateBtn, previewBtn;
    @FXML private TableView<ReportRecord> recentReportsTable;
    @FXML private TableColumn<ReportRecord, String> reportNameColumn, reportTypeColumn, generatedDateColumn, generatedByColumn, sizeColumn;
    @FXML private TableColumn<ReportRecord, Void> actionsColumn;

    private User currentUser;
    private ObservableList<ReportRecord> reportsData;

    public void setCurrentUser(User user) { this.currentUser = user; }

    @FXML
    private void initialize() {
        System.out.println("ManagerReportsController initialized");
        reportsData = FXCollections.observableArrayList();
        setupComboBoxes();
        setupTable();
        loadDummyReports();
    }

    private void setupComboBoxes() {
        if (reportTypeCombo != null) {
            reportTypeCombo.getItems().addAll("Inventory Report", "Attendance Report", "Purchase Orders Report", "Employee Performance", "Financial Summary");
        }
        if (dateRangeCombo != null) {
            dateRangeCombo.getItems().addAll("Last 7 Days", "Last 30 Days", "Last 3 Months", "Last 6 Months", "Last Year", "Custom Range");
            dateRangeCombo.setValue("Last 30 Days");
        }
        if (exportFormatCombo != null) {
            exportFormatCombo.getItems().addAll("PDF", "Excel (XLSX)", "CSV", "JSON");
            exportFormatCombo.setValue("PDF");
        }
        if (departmentCombo != null) {
            departmentCombo.getItems().addAll("All Departments", "Production", "Quality Control", "Packaging", "Warehouse", "Procurement", "Administration");
            departmentCombo.setValue("All Departments");
        }
    }

    private void setupTable() {
        if (recentReportsTable == null) return;

        reportNameColumn.setCellValueFactory(new PropertyValueFactory<>("reportName"));
        reportTypeColumn.setCellValueFactory(new PropertyValueFactory<>("reportType"));
        generatedDateColumn.setCellValueFactory(new PropertyValueFactory<>("generatedDate"));
        generatedByColumn.setCellValueFactory(new PropertyValueFactory<>("generatedBy"));
        sizeColumn.setCellValueFactory(new PropertyValueFactory<>("size"));

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button downloadBtn = new Button("Download");
            private final Button viewBtn = new Button("View");
            private final HBox container = new HBox(6, viewBtn, downloadBtn);
            {
                viewBtn.setStyle("-fx-background-color: #a78bfa; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 10px; -fx-font-size: 11px; -fx-cursor: hand;");
                downloadBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 10px; -fx-font-size: 11px; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> System.out.println("View: " + getTableView().getItems().get(getIndex()).getReportName()));
                downloadBtn.setOnAction(e -> System.out.println("Download: " + getTableView().getItems().get(getIndex()).getReportName()));
                container.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        recentReportsTable.setItems(reportsData);
    }

    private void loadDummyReports() {
        reportsData.clear();
        reportsData.add(new ReportRecord("Inventory Summary - December 2024", "Inventory", "Dec 5, 2024", "Admin", "2.4 MB"));
        reportsData.add(new ReportRecord("Attendance Report - November 2024", "Attendance", "Dec 1, 2024", "Manager", "1.8 MB"));
        reportsData.add(new ReportRecord("Purchase Orders - Q4 2024", "PO Report", "Nov 28, 2024", "Manager", "3.1 MB"));
        reportsData.add(new ReportRecord("Employee Performance - Q4", "Performance", "Nov 15, 2024", "HR Manager", "1.2 MB"));
    }

    @FXML private void handleInventoryReport() { showInfo("Inventory Report", "Generating inventory report..."); }
    @FXML private void handleAttendanceReport() { showInfo("Attendance Report", "Generating attendance report..."); }
    @FXML private void handlePOReport() { showInfo("PO Report", "Generating purchase orders report..."); }
    @FXML private void handleGenerateCustom() { showInfo("Custom Report", "Generating custom report with selected parameters..."); }
    @FXML private void handlePreview() { showInfo("Preview", "Report preview will be shown here."); }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class ReportRecord {
        private final StringProperty reportName, reportType, generatedDate, generatedBy, size;

        public ReportRecord(String name, String type, String date, String by, String size) {
            this.reportName = new SimpleStringProperty(name);
            this.reportType = new SimpleStringProperty(type);
            this.generatedDate = new SimpleStringProperty(date);
            this.generatedBy = new SimpleStringProperty(by);
            this.size = new SimpleStringProperty(size);
        }

        public String getReportName() { return reportName.get(); }
        public String getReportType() { return reportType.get(); }
        public String getGeneratedDate() { return generatedDate.get(); }
        public String getGeneratedBy() { return generatedBy.get(); }
        public String getSize() { return size.get(); }
    }
}

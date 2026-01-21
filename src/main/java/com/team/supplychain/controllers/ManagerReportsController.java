package com.team.supplychain.controllers;

import com.team.supplychain.dao.AttendanceDAO;
import com.team.supplychain.dao.InventoryDAO;
import com.team.supplychain.dao.RequisitionDAO;
import com.team.supplychain.models.Attendance;
import com.team.supplychain.models.InventoryItem;
import com.team.supplychain.models.Requisition;
import com.team.supplychain.models.User;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

    @FXML
    private void handleInventoryReport() {
        generateInventoryReport();
    }

    @FXML
    private void handleAttendanceReport() {
        generateAttendanceReport();
    }

    @FXML
    private void handlePOReport() {
        generatePurchaseOrdersReport();
    }

    @FXML
    private void handleGenerateCustom() {
        String reportType = reportTypeCombo != null ? reportTypeCombo.getValue() : null;
        if (reportType == null) {
            showError("No Report Type", "Please select a report type.");
            return;
        }

        if (reportType.equals("Inventory Report")) {
            generateInventoryReport();
        } else if (reportType.equals("Attendance Report")) {
            generateAttendanceReport();
        } else if (reportType.equals("Purchase Orders Report")) {
            generatePurchaseOrdersReport();
        } else {
            showInfo("Coming Soon", "Report generation for '" + reportType + "' will be available soon.");
        }
    }

    @FXML
    private void handlePreview() {
        showInfo("Preview", "Report preview functionality will be available in a future update.");
    }

    // ==================== REPORT GENERATION METHODS ====================

    private void generateInventoryReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Inventory Report");
        fileChooser.setInitialFileName("inventory_report_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(inventoryReportBtn.getScene().getWindow());
        if (file != null) {
            try {
                InventoryDAO inventoryDAO = new InventoryDAO();
                List<InventoryItem> items = inventoryDAO.getAllInventoryItems();

                if (items.isEmpty()) {
                    showError("No Data", "No inventory items found.");
                    return;
                }

                try (PrintWriter writer = new PrintWriter(file)) {
                    // Write header
                    writer.println("Item ID,Item Name,Category,Quantity,Unit Price,Reorder Level,Location,Total Value,Status");

                    // Write data
                    for (InventoryItem item : items) {
                        double totalValue = item.getQuantity() * item.getUnitPrice().doubleValue();
                        String status = item.getQuantity() <= item.getReorderLevel() ? "Low Stock" :
                                       item.getQuantity() == 0 ? "Out of Stock" : "In Stock";

                        writer.printf("%d,%s,%s,%d,%.2f,%d,%s,%.2f,%s%n",
                            item.getItemId(),
                            escapeCSV(item.getItemName()),
                            escapeCSV(item.getCategory()),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            item.getReorderLevel(),
                            escapeCSV(item.getLocation()),
                            totalValue,
                            status
                        );
                    }
                }

                showInfo("Export Successful",
                    "Inventory report exported successfully!\n" +
                    items.size() + " items exported to:\n" + file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                showError("Export Failed", "Failed to generate inventory report: " + e.getMessage());
            }
        }
    }

    private void generateAttendanceReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance Report");
        fileChooser.setInitialFileName("attendance_report_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(attendanceReportBtn.getScene().getWindow());
        if (file != null) {
            try {
                AttendanceDAO attendanceDAO = new AttendanceDAO();
                List<Attendance> records = attendanceDAO.getAllAttendanceWithEmployeeDetails();

                if (records.isEmpty()) {
                    showError("No Data", "No attendance records found.");
                    return;
                }

                try (PrintWriter writer = new PrintWriter(file)) {
                    // Write header
                    writer.println("Employee ID,Employee Name,Department,Date,Check In,Check Out,Hours Worked,Status");

                    // Write data
                    for (Attendance record : records) {
                        writer.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                            record.getEmployeeId(),
                            escapeCSV(record.getEmployeeFullName()),
                            escapeCSV(record.getDepartment()),
                            record.getDate() != null ? record.getDate().toString() : "",
                            escapeCSV(record.getFormattedCheckInTime()),
                            escapeCSV(record.getFormattedCheckOutTime()),
                            escapeCSV(record.getFormattedHours()),
                            record.getStatus() != null ? record.getStatus().toString() : ""
                        );
                    }
                }

                showInfo("Export Successful",
                    "Attendance report exported successfully!\n" +
                    records.size() + " records exported to:\n" + file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                showError("Export Failed", "Failed to generate attendance report: " + e.getMessage());
            }
        }
    }

    private void generatePurchaseOrdersReport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Purchase Orders Report");
        fileChooser.setInitialFileName("purchase_orders_report_" + LocalDate.now() + ".csv");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(poReportBtn.getScene().getWindow());
        if (file != null) {
            try {
                RequisitionDAO requisitionDAO = new RequisitionDAO();

                // Load all requisitions
                List<Requisition> pending = requisitionDAO.getRequisitionsByStatus("Pending");
                List<Requisition> approved = requisitionDAO.getRequisitionsByStatus("Approved");
                List<Requisition> rejected = requisitionDAO.getRequisitionsByStatus("Rejected");

                List<Requisition> allRequisitions = new java.util.ArrayList<>();
                allRequisitions.addAll(pending);
                allRequisitions.addAll(approved);
                allRequisitions.addAll(rejected);

                if (allRequisitions.isEmpty()) {
                    showError("No Data", "No purchase orders found.");
                    return;
                }

                try (PrintWriter writer = new PrintWriter(file)) {
                    // Write header
                    writer.println("PO Number,Requested By,Department,Category,Priority,Date,Total Items,Total Amount,Status");

                    // Write data
                    for (Requisition req : allRequisitions) {
                        writer.printf("%s,%s,%s,%s,%s,%s,%d,%.2f,%s%n",
                            escapeCSV(req.getRequisitionCode()),
                            escapeCSV(req.getRequesterName()),
                            escapeCSV(req.getDepartment()),
                            escapeCSV(req.getCategory()),
                            escapeCSV(req.getPriority()),
                            req.getRequestDate() != null ? req.getRequestDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "",
                            req.getTotalItems(),
                            req.getTotalAmount(),
                            escapeCSV(req.getStatus())
                        );
                    }
                }

                showInfo("Export Successful",
                    "Purchase orders report exported successfully!\n" +
                    allRequisitions.size() + " orders exported to:\n" + file.getAbsolutePath());

            } catch (Exception e) {
                e.printStackTrace();
                showError("Export Failed", "Failed to generate purchase orders report: " + e.getMessage());
            }
        }
    }

    private String escapeCSV(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
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

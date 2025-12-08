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
import java.time.LocalTime;

public class ManagerAttendanceController {

    @FXML private Label totalEmployeesLabel;
    @FXML private Label presentLabel;
    @FXML private Label absentLabel;
    @FXML private Label lateLabel;
    @FXML private Label attendanceRateLabel;

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private TextField searchField;
    @FXML private Button todayButton;
    @FXML private Button exportButton;
    @FXML private Button refreshButton;

    @FXML private TableView<AttendanceRecord> attendanceTable;
    @FXML private TableColumn<AttendanceRecord, String> employeeIdColumn;
    @FXML private TableColumn<AttendanceRecord, String> employeeNameColumn;
    @FXML private TableColumn<AttendanceRecord, String> departmentColumn;
    @FXML private TableColumn<AttendanceRecord, String> checkInColumn;
    @FXML private TableColumn<AttendanceRecord, String> checkOutColumn;
    @FXML private TableColumn<AttendanceRecord, String> hoursWorkedColumn;
    @FXML private TableColumn<AttendanceRecord, String> statusColumn;
    @FXML private TableColumn<AttendanceRecord, Void> actionsColumn;

    private User currentUser;
    private ObservableList<AttendanceRecord> attendanceData;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        System.out.println("ManagerAttendanceController initialized");
        attendanceData = FXCollections.observableArrayList();
        setupTable();
        setupFilters();
        loadDummyData();
        updateStats();
        if (datePicker != null) datePicker.setValue(LocalDate.now());
    }

    private void setupTable() {
        if (attendanceTable == null) return;

        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        employeeNameColumn.setCellValueFactory(new PropertyValueFactory<>("employeeName"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        checkInColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCheckIn() != null ? cellData.getValue().getCheckIn().toString() : "-"));
        checkOutColumn.setCellValueFactory(cellData ->
            new SimpleStringProperty(cellData.getValue().getCheckOut() != null ? cellData.getValue().getCheckOut().toString() : "-"));
        hoursWorkedColumn.setCellValueFactory(new PropertyValueFactory<>("hoursWorked"));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    label.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; -fx-font-size: 11px; -fx-font-weight: bold; " + getStyle(status));
                    setGraphic(label);
                    setAlignment(Pos.CENTER);
                }
            }
            private String getStyle(String status) {
                return switch (status) {
                    case "Present" -> "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
                    case "Late" -> "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
                    case "Absent" -> "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
                    default -> "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
                };
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button("View");
            {
                viewButton.setStyle("-fx-background-color: #a78bfa; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 12px; -fx-font-size: 11px; -fx-cursor: hand;");
                viewButton.setOnAction(e -> System.out.println("View: " + getTableView().getItems().get(getIndex()).getEmployeeName()));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewButton);
                if (!empty) setAlignment(Pos.CENTER);
            }
        });

        attendanceTable.setItems(attendanceData);
    }

    private void setupFilters() {
        if (departmentFilter != null) {
            departmentFilter.getItems().addAll("All Departments", "Production", "Quality Control", "Packaging", "Warehouse", "Administration");
            departmentFilter.setValue("All Departments");
        }
    }

    private void loadDummyData() {
        attendanceData.clear();
        attendanceData.add(new AttendanceRecord("EMP001", "John Smith", "Production", LocalTime.of(8, 0), LocalTime.of(17, 0), "9.0", "Present"));
        attendanceData.add(new AttendanceRecord("EMP002", "Sarah Johnson", "Quality Control", LocalTime.of(8, 15), LocalTime.of(17, 15), "9.0", "Late"));
        attendanceData.add(new AttendanceRecord("EMP003", "Mike Wilson", "Packaging", null, null, "0.0", "Absent"));
        attendanceData.add(new AttendanceRecord("EMP004", "Emily Brown", "Warehouse", LocalTime.of(8, 0), LocalTime.of(17, 0), "9.0", "Present"));
        attendanceData.add(new AttendanceRecord("EMP005", "James Davis", "Production", LocalTime.of(8, 5), LocalTime.of(17, 5), "9.0", "Present"));
        attendanceData.add(new AttendanceRecord("EMP006", "Lisa Anderson", "Administration", LocalTime.of(8, 30), LocalTime.of(17, 30), "9.0", "Late"));
        attendanceData.add(new AttendanceRecord("EMP007", "Tom Martinez", "Production", LocalTime.of(8, 0), null, "0.0", "Present"));
        attendanceData.add(new AttendanceRecord("EMP008", "Anna Taylor", "Quality Control", LocalTime.of(8, 0), LocalTime.of(17, 0), "9.0", "Present"));
    }

    private void updateStats() {
        int total = attendanceData.size();
        long present = attendanceData.stream().filter(r -> r.getStatus().equals("Present") || r.getStatus().equals("Late")).count();
        long absent = attendanceData.stream().filter(r -> r.getStatus().equals("Absent")).count();
        long late = attendanceData.stream().filter(r -> r.getStatus().equals("Late")).count();
        double rate = total > 0 ? (present * 100.0 / total) : 0;

        if (totalEmployeesLabel != null) totalEmployeesLabel.setText(String.valueOf(total));
        if (presentLabel != null) presentLabel.setText(String.valueOf(present));
        if (absentLabel != null) absentLabel.setText(String.valueOf(absent));
        if (lateLabel != null) lateLabel.setText(String.valueOf(late));
        if (attendanceRateLabel != null) attendanceRateLabel.setText(String.format("%.0f%%", rate));
    }

    @FXML private void handleToday() { if (datePicker != null) datePicker.setValue(LocalDate.now()); }
    @FXML private void handleExport() { showInfo("Export", "Attendance report export functionality will be implemented."); }
    @FXML private void handleRefresh() { loadDummyData(); updateStats(); }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class AttendanceRecord {
        private final StringProperty employeeId, employeeName, department, hoursWorked, status;
        private final ObjectProperty<LocalTime> checkIn, checkOut;

        public AttendanceRecord(String id, String name, String dept, LocalTime in, LocalTime out, String hours, String status) {
            this.employeeId = new SimpleStringProperty(id);
            this.employeeName = new SimpleStringProperty(name);
            this.department = new SimpleStringProperty(dept);
            this.checkIn = new SimpleObjectProperty<>(in);
            this.checkOut = new SimpleObjectProperty<>(out);
            this.hoursWorked = new SimpleStringProperty(hours);
            this.status = new SimpleStringProperty(status);
        }

        public String getEmployeeId() { return employeeId.get(); }
        public String getEmployeeName() { return employeeName.get(); }
        public String getDepartment() { return department.get(); }
        public LocalTime getCheckIn() { return checkIn.get(); }
        public LocalTime getCheckOut() { return checkOut.get(); }
        public String getHoursWorked() { return hoursWorked.get(); }
        public String getStatus() { return status.get(); }
    }
}

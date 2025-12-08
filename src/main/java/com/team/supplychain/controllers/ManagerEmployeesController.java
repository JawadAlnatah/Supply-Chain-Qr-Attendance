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

public class ManagerEmployeesController {

    @FXML private Label totalEmployeesLabel, activeEmployeesLabel, onLeaveLabel, departmentsLabel;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private TextField searchField;
    @FXML private Button addEmployeeButton, exportButton, refreshButton;
    @FXML private TableView<Employee> employeeTable;
    @FXML private TableColumn<Employee, String> employeeIdColumn, nameColumn, emailColumn, departmentColumn, positionColumn, hireDateColumn, statusColumn;
    @FXML private TableColumn<Employee, Void> actionsColumn;

    private User currentUser;
    private ObservableList<Employee> employeeData;

    public void setCurrentUser(User user) { this.currentUser = user; }

    @FXML
    private void initialize() {
        System.out.println("ManagerEmployeesController initialized");
        employeeData = FXCollections.observableArrayList();
        setupTable();
        setupFilters();
        loadDummyData();
        updateStats();
    }

    private void setupTable() {
        if (employeeTable == null) return;

        employeeIdColumn.setCellValueFactory(new PropertyValueFactory<>("employeeId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        departmentColumn.setCellValueFactory(new PropertyValueFactory<>("department"));
        positionColumn.setCellValueFactory(new PropertyValueFactory<>("position"));
        hireDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getHireDate().toString()));

        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setGraphic(null);
                } else {
                    Label label = new Label(status);
                    label.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; -fx-font-size: 11px; -fx-font-weight: bold; " +
                        ("Active".equals(status) ? "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;" :
                         "On Leave".equals(status) ? "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;" :
                         "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;"));
                    setGraphic(label);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button viewBtn = new Button("View");
            private final HBox container = new HBox(6, viewBtn, editBtn);
            {
                viewBtn.setStyle("-fx-background-color: #a78bfa; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 10px; -fx-font-size: 11px; -fx-cursor: hand;");
                editBtn.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 10px; -fx-font-size: 11px; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> System.out.println("View: " + getTableView().getItems().get(getIndex()).getName()));
                editBtn.setOnAction(e -> System.out.println("Edit: " + getTableView().getItems().get(getIndex()).getName()));
                container.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });

        employeeTable.setItems(employeeData);
    }

    private void setupFilters() {
        if (departmentFilter != null) {
            departmentFilter.getItems().addAll("All Departments", "Production", "Quality Control", "Packaging", "Warehouse", "Procurement", "Administration");
            departmentFilter.setValue("All Departments");
        }
    }

    private void loadDummyData() {
        employeeData.clear();
        employeeData.add(new Employee("EMP001", "John Smith", "john.smith@company.com", "Production", "Production Manager", LocalDate.of(2020, 1, 15), "Active"));
        employeeData.add(new Employee("EMP002", "Sarah Johnson", "sarah.j@company.com", "Quality Control", "QC Specialist", LocalDate.of(2019, 6, 20), "Active"));
        employeeData.add(new Employee("EMP003", "Mike Wilson", "mike.w@company.com", "Packaging", "Packaging Supervisor", LocalDate.of(2021, 3, 10), "On Leave"));
        employeeData.add(new Employee("EMP004", "Emily Brown", "emily.b@company.com", "Warehouse", "Warehouse Lead", LocalDate.of(2018, 9, 5), "Active"));
        employeeData.add(new Employee("EMP005", "James Davis", "james.d@company.com", "Production", "Production Worker", LocalDate.of(2022, 1, 12), "Active"));
        employeeData.add(new Employee("EMP006", "Lisa Anderson", "lisa.a@company.com", "Administration", "HR Manager", LocalDate.of(2017, 4, 18), "Active"));
        employeeData.add(new Employee("EMP007", "Tom Martinez", "tom.m@company.com", "Procurement", "Buyer", LocalDate.of(2020, 11, 22), "Active"));
        employeeData.add(new Employee("EMP008", "Anna Taylor", "anna.t@company.com", "Quality Control", "QC Inspector", LocalDate.of(2021, 7, 8), "On Leave"));
    }

    private void updateStats() {
        int total = employeeData.size();
        long active = employeeData.stream().filter(e -> "Active".equals(e.getStatus())).count();
        long onLeave = employeeData.stream().filter(e -> "On Leave".equals(e.getStatus())).count();
        long depts = employeeData.stream().map(Employee::getDepartment).distinct().count();

        if (totalEmployeesLabel != null) totalEmployeesLabel.setText(String.valueOf(total));
        if (activeEmployeesLabel != null) activeEmployeesLabel.setText(String.valueOf(active));
        if (onLeaveLabel != null) onLeaveLabel.setText(String.valueOf(onLeave));
        if (departmentsLabel != null) departmentsLabel.setText(String.valueOf(depts));
    }

    @FXML private void handleAddEmployee() { showInfo("Add Employee", "Add new employee functionality will be implemented."); }
    @FXML private void handleExport() { showInfo("Export", "Employee list export functionality will be implemented."); }
    @FXML private void handleRefresh() { loadDummyData(); updateStats(); }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class Employee {
        private final StringProperty employeeId, name, email, department, position, status;
        private final ObjectProperty<LocalDate> hireDate;

        public Employee(String id, String name, String email, String dept, String pos, LocalDate hire, String status) {
            this.employeeId = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.email = new SimpleStringProperty(email);
            this.department = new SimpleStringProperty(dept);
            this.position = new SimpleStringProperty(pos);
            this.hireDate = new SimpleObjectProperty<>(hire);
            this.status = new SimpleStringProperty(status);
        }

        public String getEmployeeId() { return employeeId.get(); }
        public String getName() { return name.get(); }
        public String getEmail() { return email.get(); }
        public String getDepartment() { return department.get(); }
        public String getPosition() { return position.get(); }
        public LocalDate getHireDate() { return hireDate.get(); }
        public String getStatus() { return status.get(); }
    }
}

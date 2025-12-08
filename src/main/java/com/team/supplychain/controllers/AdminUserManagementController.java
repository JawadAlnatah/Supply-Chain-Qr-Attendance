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

public class AdminUserManagementController {

    @FXML private Label totalUsersLabel, activeUsersLabel, pendingApprovalsLabel, rolesLabel;
    @FXML private ComboBox<String> roleFilter, statusFilter;
    @FXML private TextField searchField;
    @FXML private Button addUserButton, exportButton, refreshButton;
    @FXML private TableView<UserRecord> usersTable;
    @FXML private TableColumn<UserRecord, String> userIdColumn, usernameColumn, fullNameColumn, emailColumn, roleColumn, statusColumn, lastLoginColumn;
    @FXML private TableColumn<UserRecord, Void> actionsColumn;

    private User currentUser;
    private ObservableList<UserRecord> userData;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        System.out.println("AdminUserManagementController initialized");
        userData = FXCollections.observableArrayList();
        setupTable();
        setupFilters();
        loadDummyData();
        updateStats();
    }

    private void setupTable() {
        if (usersTable == null) return;

        userIdColumn.setCellValueFactory(new PropertyValueFactory<>("userId"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        lastLoginColumn.setCellValueFactory(new PropertyValueFactory<>("lastLogin"));

        // Role badge cell
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(role);
                    badge.setStyle("-fx-background-radius: 12px; -fx-padding: 6px 14px; -fx-font-size: 11px; -fx-font-weight: bold; " +
                        getRoleBadgeStyle(role));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Status badge cell
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusColumn.setCellFactory(column -> new TableCell<>() {
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

        // Actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");
            private final Button resetBtn = new Button("Reset");
            private final HBox container = new HBox(6, editBtn, deleteBtn, resetBtn);
            {
                editBtn.setStyle("-fx-background-color: #22d3ee; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 12px; -fx-font-size: 11px; -fx-cursor: hand;");
                deleteBtn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 12px; -fx-font-size: 11px; -fx-cursor: hand;");
                resetBtn.setStyle("-fx-background-color: #fbbf24; -fx-text-fill: white; -fx-background-radius: 6px; -fx-padding: 5px 12px; -fx-font-size: 11px; -fx-cursor: hand;");
                editBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        System.out.println("Edit: " + getTableView().getItems().get(getIndex()).getUsername());
                    }
                });
                deleteBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        System.out.println("Delete: " + getTableView().getItems().get(getIndex()).getUsername());
                    }
                });
                resetBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        System.out.println("Reset Password: " + getTableView().getItems().get(getIndex()).getUsername());
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

        usersTable.setItems(userData);
    }

    private void setupFilters() {
        if (roleFilter != null) {
            roleFilter.getItems().addAll("All Roles", "ADMIN", "MANAGER", "EMPLOYEE", "SUPPLIER");
            roleFilter.setValue("All Roles");
        }
        if (statusFilter != null) {
            statusFilter.getItems().addAll("All Statuses", "Active", "Inactive", "Locked", "Pending");
            statusFilter.setValue("All Statuses");
        }
    }

    private void loadDummyData() {
        userData.clear();
        userData.add(new UserRecord("USR001", "admin", "Admin User", "admin@company.com", "ADMIN", "Active", "2025-12-05 14:30"));
        userData.add(new UserRecord("USR002", "manager1", "John Manager", "manager1@company.com", "MANAGER", "Active", "2025-12-05 13:15"));
        userData.add(new UserRecord("USR003", "employee1", "Sarah Employee", "employee1@company.com", "EMPLOYEE", "Active", "2025-12-05 12:00"));
        userData.add(new UserRecord("USR004", "supplier1", "ABC Supplies", "supplier1@company.com", "SUPPLIER", "Active", "2025-12-04 16:45"));
        userData.add(new UserRecord("USR005", "manager2", "Lisa Chen", "lisa.chen@company.com", "MANAGER", "Active", "2025-12-05 11:20"));
        userData.add(new UserRecord("USR006", "employee2", "Mike Wilson", "mike.w@company.com", "EMPLOYEE", "Inactive", "2025-11-28 09:30"));
        userData.add(new UserRecord("USR007", "admin2", "Jane Admin", "jadmin@company.com", "ADMIN", "Active", "2025-12-05 10:00"));
        userData.add(new UserRecord("USR008", "employee3", "Tom Brown", "tbrown@company.com", "EMPLOYEE", "Locked", "2025-12-03 08:15"));
        userData.add(new UserRecord("USR009", "supplier2", "XYZ Corp", "supplier2@company.com", "SUPPLIER", "Active", "2025-12-05 07:45"));
        userData.add(new UserRecord("USR010", "pending_user", "New User", "newuser@company.com", "EMPLOYEE", "Pending", "Never"));
    }

    private void updateStats() {
        int total = userData.size();
        long active = userData.stream().filter(u -> "Active".equals(u.getStatus())).count();
        long pending = userData.stream().filter(u -> "Pending".equals(u.getStatus())).count();
        long roles = userData.stream().map(UserRecord::getRole).distinct().count();

        if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(total));
        if (activeUsersLabel != null) activeUsersLabel.setText(String.valueOf(active));
        if (pendingApprovalsLabel != null) pendingApprovalsLabel.setText(String.valueOf(pending));
        if (rolesLabel != null) rolesLabel.setText(roles + " Roles");
    }

    private String getRoleBadgeStyle(String role) {
        return switch (role) {
            case "ADMIN" -> "-fx-background-color: rgba(34,211,238,0.15); -fx-text-fill: #22d3ee;";
            case "MANAGER" -> "-fx-background-color: rgba(168,139,250,0.15); -fx-text-fill: #a78bfa;";
            case "EMPLOYEE" -> "-fx-background-color: rgba(59,130,246,0.15); -fx-text-fill: #3b82f6;";
            case "SUPPLIER" -> "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            default -> "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        };
    }

    private String getStatusBadgeStyle(String status) {
        return switch (status) {
            case "Active" -> "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
            case "Inactive" -> "-fx-background-color: rgba(107,114,128,0.15); -fx-text-fill: #6b7280;";
            case "Locked" -> "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
            case "Pending" -> "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            default -> "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        };
    }

    @FXML private void handleAddUser() { showInfo("Add User", "Add new user functionality will be implemented."); }
    @FXML private void handleExport() { showInfo("Export", "User list export functionality will be implemented."); }
    @FXML private void handleRefresh() { loadDummyData(); updateStats(); }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class UserRecord {
        private final StringProperty userId, username, fullName, email, role, status, lastLogin;

        public UserRecord(String userId, String username, String fullName, String email, String role, String status, String lastLogin) {
            this.userId = new SimpleStringProperty(userId);
            this.username = new SimpleStringProperty(username);
            this.fullName = new SimpleStringProperty(fullName);
            this.email = new SimpleStringProperty(email);
            this.role = new SimpleStringProperty(role);
            this.status = new SimpleStringProperty(status);
            this.lastLogin = new SimpleStringProperty(lastLogin);
        }

        public String getUserId() { return userId.get(); }
        public String getUsername() { return username.get(); }
        public String getFullName() { return fullName.get(); }
        public String getEmail() { return email.get(); }
        public String getRole() { return role.get(); }
        public String getStatus() { return status.get(); }
        public String getLastLogin() { return lastLogin.get(); }
    }
}

package com.team.supplychain.controllers;

import com.team.supplychain.dao.AuditLogDAO;
import com.team.supplychain.dao.EmployeeDAO;
import com.team.supplychain.dao.UserDAO;
import com.team.supplychain.enums.UserRole;
import com.team.supplychain.models.Employee;
import com.team.supplychain.models.User;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

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
    private ObservableList<UserRecord> allUserData; // For filtering
    private final UserDAO userDAO = new UserDAO();
    private final EmployeeDAO employeeDAO = new EmployeeDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        System.out.println("AdminUserManagementController initialized");
        userData = FXCollections.observableArrayList();
        allUserData = FXCollections.observableArrayList();
        setupTable();
        setupFilters();
        loadUsersFromDatabase();
        updateStats();
        setupSearchAndFilters();
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
                        handleEdit(getTableView().getItems().get(getIndex()));
                    }
                });
                deleteBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        handleDelete(getTableView().getItems().get(getIndex()));
                    }
                });
                resetBtn.setOnAction(e -> {
                    if (getIndex() < getTableView().getItems().size()) {
                        handleResetPassword(getTableView().getItems().get(getIndex()));
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

    private void loadUsersFromDatabase() {
        try {
            List<User> users = userDAO.getAllUsers();
            allUserData.clear();
            userData.clear();

            for (User user : users) {
                String userId = String.format("USR%03d", user.getUserId());
                String fullName = user.getFirstName() + " " + user.getLastName();
                String status = user.isActive() ? "Active" : "Inactive";
                String lastLogin = user.getLastLogin() != null
                    ? user.getLastLogin().format(dateFormatter)
                    : "Never";

                UserRecord record = new UserRecord(
                    userId,
                    user.getUsername(),
                    fullName,
                    user.getEmail(),
                    user.getRole().name(),
                    status,
                    lastLogin
                );
                record.setUserId(user.getUserId()); // Store actual user_id
                allUserData.add(record);
            }

            userData.addAll(allUserData);
            System.out.println("Loaded " + users.size() + " users from database");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Database Error", "Failed to load users from database: " + e.getMessage());
        }
    }

    private void setupSearchAndFilters() {
        // Search field listener
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        }

        // Role filter listener
        if (roleFilter != null) {
            roleFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        }

        // Status filter listener
        if (statusFilter != null) {
            statusFilter.valueProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        }
    }

    private void applyFilters() {
        String searchText = searchField != null ? searchField.getText().toLowerCase() : "";
        String selectedRole = roleFilter != null ? roleFilter.getValue() : "All Roles";
        String selectedStatus = statusFilter != null ? statusFilter.getValue() : "All Statuses";

        List<UserRecord> filtered = allUserData.stream()
            .filter(user -> {
                // Search filter
                boolean matchesSearch = searchText.isEmpty() ||
                    user.getUsername().toLowerCase().contains(searchText) ||
                    user.getFullName().toLowerCase().contains(searchText) ||
                    user.getEmail().toLowerCase().contains(searchText);

                // Role filter
                boolean matchesRole = selectedRole.equals("All Roles") ||
                    user.getRole().equals(selectedRole);

                // Status filter
                boolean matchesStatus = selectedStatus.equals("All Statuses") ||
                    user.getStatus().equals(selectedStatus);

                return matchesSearch && matchesRole && matchesStatus;
            })
            .collect(Collectors.toList());

        userData.clear();
        userData.addAll(filtered);
        updateStats();
    }

    private void updateStats() {
        try {
            // Get real-time statistics from database
            int total = userDAO.getTotalUserCount();
            int active = userDAO.getActiveUserCount();
            int inactive = userDAO.getInactiveUserCount();
            int distinctRoles = userDAO.getDistinctRoleCount();

            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(total));
            if (activeUsersLabel != null) activeUsersLabel.setText(String.valueOf(active));
            if (pendingApprovalsLabel != null) pendingApprovalsLabel.setText(String.valueOf(inactive));
            if (rolesLabel != null) rolesLabel.setText(distinctRoles + " Roles");
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to filtered view statistics if database query fails
            int total = allUserData.size();
            long active = allUserData.stream().filter(u -> "Active".equals(u.getStatus())).count();
            long inactive = allUserData.stream().filter(u -> "Inactive".equals(u.getStatus())).count();
            long roles = allUserData.stream().map(UserRecord::getRole).distinct().count();

            if (totalUsersLabel != null) totalUsersLabel.setText(String.valueOf(total));
            if (activeUsersLabel != null) activeUsersLabel.setText(String.valueOf(active));
            if (pendingApprovalsLabel != null) pendingApprovalsLabel.setText(String.valueOf(inactive));
            if (rolesLabel != null) rolesLabel.setText(roles + " Roles");
        }
    }

    private String getRoleBadgeStyle(String role) {
        switch (role) {
            case "ADMIN":
                return "-fx-background-color: rgba(34,211,238,0.15); -fx-text-fill: #22d3ee;";
            case "MANAGER":
                return "-fx-background-color: rgba(168,139,250,0.15); -fx-text-fill: #a78bfa;";
            case "EMPLOYEE":
                return "-fx-background-color: rgba(59,130,246,0.15); -fx-text-fill: #3b82f6;";
            case "SUPPLIER":
                return "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            default:
                return "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        }
    }

    private String getStatusBadgeStyle(String status) {
        switch (status) {
            case "Active":
                return "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #10b981;";
            case "Inactive":
                return "-fx-background-color: rgba(107,114,128,0.15); -fx-text-fill: #6b7280;";
            case "Locked":
                return "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #ef4444;";
            case "Pending":
                return "-fx-background-color: rgba(251,191,36,0.15); -fx-text-fill: #fbbf24;";
            default:
                return "-fx-background-color: #e0e0e0; -fx-text-fill: #6b7280;";
        }
    }

    @FXML
    private void handleAddUser() {
        showAddUserDialog();
    }

    @FXML
    private void handleExport() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Export User List");
            fileChooser.setInitialFileName("users_export_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );

            Stage stage = (Stage) exportButton.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                exportToExcel(file);

                // Log the export action
                auditLogDAO.logSuccess(
                    currentUser != null ? currentUser.getUserId() : null,
                    currentUser != null ? currentUser.getUsername() : "System",
                    "EXPORT",
                    "Users",
                    String.format("Exported %d users to Excel: %s", userData.size(), file.getName())
                );

                showInfo("Success", "User list exported successfully to:\n" + file.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
            auditLogDAO.logFailure(
                currentUser != null ? currentUser.getUserId() : null,
                currentUser != null ? currentUser.getUsername() : "System",
                "EXPORT",
                "Users",
                "Failed to export users: " + e.getMessage()
            );
            showError("Export Error", "Failed to export user list: " + e.getMessage());
        }
    }

    private void exportToExcel(File file) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"User ID", "Username", "Full Name", "Email", "Role", "Status", "Last Login"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        for (UserRecord record : userData) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(record.getUserId());
            row.createCell(1).setCellValue(record.getUsername());
            row.createCell(2).setCellValue(record.getFullName());
            row.createCell(3).setCellValue(record.getEmail());
            row.createCell(4).setCellValue(record.getRole());
            row.createCell(5).setCellValue(record.getStatus());
            row.createCell(6).setCellValue(record.getLastLogin());
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }
        workbook.close();
    }

    @FXML
    private void handleRefresh() {
        loadUsersFromDatabase();
        updateStats();
    }

    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Create a new user account");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("MANAGER", "EMPLOYEE");
        roleCombo.setValue("EMPLOYEE");

        // Employee-specific fields (only shown if role is EMPLOYEE or MANAGER)
        TextField departmentField = new TextField();
        departmentField.setPromptText("Department");
        TextField positionField = new TextField();
        positionField.setPromptText("Position");
        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone");

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("First Name:"), 0, 2);
        grid.add(firstNameField, 1, 2);
        grid.add(new Label("Last Name:"), 0, 3);
        grid.add(lastNameField, 1, 3);
        grid.add(new Label("Email:"), 0, 4);
        grid.add(emailField, 1, 4);
        grid.add(new Label("Role:"), 0, 5);
        grid.add(roleCombo, 1, 5);
        grid.add(new Label("Department:"), 0, 6);
        grid.add(departmentField, 1, 6);
        grid.add(new Label("Position:"), 0, 7);
        grid.add(positionField, 1, 7);
        grid.add(new Label("Phone:"), 0, 8);
        grid.add(phoneField, 1, 8);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                if (usernameField.getText().trim().isEmpty() ||
                    passwordField.getText().trim().isEmpty() ||
                    firstNameField.getText().trim().isEmpty() ||
                    lastNameField.getText().trim().isEmpty() ||
                    emailField.getText().trim().isEmpty()) {
                    showError("Validation Error", "Please fill in all required fields.");
                    return null;
                }

                try {
                    // Create User
                    User user = new User();
                    user.setUsername(usernameField.getText().trim());
                    user.setFirstName(firstNameField.getText().trim());
                    user.setLastName(lastNameField.getText().trim());
                    user.setEmail(emailField.getText().trim());
                    user.setRole(UserRole.valueOf(roleCombo.getValue()));
                    user.setActive(true);

                    // Create user in database
                    boolean userCreated = userDAO.createUser(user, passwordField.getText().trim());

                    if (userCreated && (user.getRole() == UserRole.EMPLOYEE || user.getRole() == UserRole.MANAGER)) {
                        // Create Employee record
                        Employee employee = new Employee();
                        employee.setUserId(user.getUserId());
                        employee.setDepartment(departmentField.getText().trim());
                        employee.setPosition(positionField.getText().trim());
                        employee.setPhone(phoneField.getText().trim());
                        employee.setHireDate(LocalDate.now());
                        employee.setQrCode("QR-" + user.getUserId()); // Generate simple QR code

                        employeeDAO.createEmployee(employee);
                    }

                    if (userCreated) {
                        // Log the action to audit logs
                        String description = String.format("Created new %s user: %s (%s %s)",
                            user.getRole().name(), user.getUsername(),
                            user.getFirstName(), user.getLastName());
                        auditLogDAO.logSuccess(
                            currentUser != null ? currentUser.getUserId() : null,
                            currentUser != null ? currentUser.getUsername() : "System",
                            "CREATE",
                            "Users",
                            description
                        );

                        showInfo("Success", "User created successfully!");
                        loadUsersFromDatabase();
                        return user;
                    } else {
                        // Log failure
                        String description = String.format("Failed to create user: %s (username may exist)",
                            usernameField.getText().trim());
                        auditLogDAO.logFailure(
                            currentUser != null ? currentUser.getUserId() : null,
                            currentUser != null ? currentUser.getUsername() : "System",
                            "CREATE",
                            "Users",
                            description
                        );
                        showError("Error", "Failed to create user. Username may already exist.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", "Failed to create user: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleEdit(UserRecord record) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user: " + record.getUsername());

        ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField firstNameField = new TextField(record.getFullName().split(" ")[0]);
        TextField lastNameField = new TextField(record.getFullName().split(" ").length > 1 ?
            record.getFullName().split(" ")[1] : "");
        TextField emailField = new TextField(record.getEmail());
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "MANAGER", "EMPLOYEE", "SUPPLIER");
        roleCombo.setValue(record.getRole());
        CheckBox activeCheck = new CheckBox();
        activeCheck.setSelected(record.getStatus().equals("Active"));

        grid.add(new Label("First Name:"), 0, 0);
        grid.add(firstNameField, 1, 0);
        grid.add(new Label("Last Name:"), 0, 1);
        grid.add(lastNameField, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(emailField, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(roleCombo, 1, 3);
        grid.add(new Label("Active:"), 0, 4);
        grid.add(activeCheck, 1, 4);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    User user = userDAO.getUserById(record.getUserIdInt());
                    if (user != null) {
                        user.setFirstName(firstNameField.getText().trim());
                        user.setLastName(lastNameField.getText().trim());
                        user.setEmail(emailField.getText().trim());
                        user.setRole(UserRole.valueOf(roleCombo.getValue()));
                        user.setActive(activeCheck.isSelected());

                        if (userDAO.updateUser(user)) {
                            // Log the action to audit logs
                            String description = String.format("Updated user: %s - Changed to %s %s (%s, %s)",
                                user.getUsername(),
                                user.getFirstName(), user.getLastName(),
                                user.getRole().name(),
                                user.isActive() ? "Active" : "Inactive");
                            auditLogDAO.logSuccess(
                                currentUser != null ? currentUser.getUserId() : null,
                                currentUser != null ? currentUser.getUsername() : "System",
                                "UPDATE",
                                "Users",
                                description
                            );

                            showInfo("Success", "User updated successfully!");
                            loadUsersFromDatabase();
                        } else {
                            auditLogDAO.logFailure(
                                currentUser != null ? currentUser.getUserId() : null,
                                currentUser != null ? currentUser.getUsername() : "System",
                                "UPDATE",
                                "Users",
                                "Failed to update user: " + record.getUsername()
                            );
                            showError("Error", "Failed to update user.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("Error", "Failed to update user: " + e.getMessage());
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleDelete(UserRecord record) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete user: " + record.getUsername());
        alert.setContentText("Are you sure you want to delete this user? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (userDAO.deleteUser(record.getUserIdInt())) {
                    // Log the action to audit logs
                    String description = String.format("Deleted user: %s (%s)",
                        record.getUsername(), record.getFullName());
                    auditLogDAO.logSuccess(
                        currentUser != null ? currentUser.getUserId() : null,
                        currentUser != null ? currentUser.getUsername() : "System",
                        "DELETE",
                        "Users",
                        description
                    );

                    showInfo("Success", "User deleted successfully!");
                    loadUsersFromDatabase();
                } else {
                    auditLogDAO.logFailure(
                        currentUser != null ? currentUser.getUserId() : null,
                        currentUser != null ? currentUser.getUsername() : "System",
                        "DELETE",
                        "Users",
                        "Failed to delete user: " + record.getUsername()
                    );
                    showError("Error", "Failed to delete user.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                auditLogDAO.logFailure(
                    currentUser != null ? currentUser.getUserId() : null,
                    currentUser != null ? currentUser.getUsername() : "System",
                    "DELETE",
                    "Users",
                    "Exception deleting user " + record.getUsername() + ": " + e.getMessage()
                );
                showError("Error", "Failed to delete user: " + e.getMessage());
            }
        }
    }

    private void handleResetPassword(UserRecord record) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Reset password for: " + record.getUsername());
        dialog.setContentText("Enter new password:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            if (password.trim().length() < 6) {
                showError("Validation Error", "Password must be at least 6 characters long.");
                return;
            }

            try {
                if (userDAO.updatePassword(record.getUserIdInt(), password.trim())) {
                    // Log the action to audit logs
                    String description = String.format("Reset password for user: %s (%s)",
                        record.getUsername(), record.getFullName());
                    auditLogDAO.logSuccess(
                        currentUser != null ? currentUser.getUserId() : null,
                        currentUser != null ? currentUser.getUsername() : "System",
                        "UPDATE",
                        "Users",
                        description
                    );

                    showInfo("Success", "Password reset successfully!");
                } else {
                    auditLogDAO.logFailure(
                        currentUser != null ? currentUser.getUserId() : null,
                        currentUser != null ? currentUser.getUsername() : "System",
                        "UPDATE",
                        "Users",
                        "Failed to reset password for user: " + record.getUsername()
                    );
                    showError("Error", "Failed to reset password.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                auditLogDAO.logFailure(
                    currentUser != null ? currentUser.getUserId() : null,
                    currentUser != null ? currentUser.getUsername() : "System",
                    "UPDATE",
                    "Users",
                    "Exception resetting password for " + record.getUsername() + ": " + e.getMessage()
                );
                showError("Error", "Failed to reset password: " + e.getMessage());
            }
        });
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static class UserRecord {
        private final StringProperty userId, username, fullName, email, role, status, lastLogin;
        private int userIdInt; // Actual database user_id

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

        public void setUserId(int userIdInt) { this.userIdInt = userIdInt; }
        public int getUserIdInt() { return userIdInt; }
    }
}

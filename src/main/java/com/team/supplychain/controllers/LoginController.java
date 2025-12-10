package com.team.supplychain.controllers;

import com.team.supplychain.dao.AuditLogDAO;
import com.team.supplychain.dao.UserDAO;
import com.team.supplychain.models.User;
import com.team.supplychain.utils.AlertUtil;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();
    
    @FXML
    private void initialize() {
        // Set up event handlers
        usernameField.setOnAction(e -> handleLogin(null));
        passwordField.setOnAction(e -> handleLogin(null));
    }
    
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Disable button during login
        loginButton.setDisable(true);

        try {
            // Authenticate user against database
            User user = userDAO.authenticate(username, password);

            if (user != null) {
                // Login successful - Log to audit
                auditLogDAO.logSuccess(
                    user.getUserId(),
                    user.getUsername(),
                    "LOGIN",
                    "Authentication",
                    String.format("User %s logged in successfully", user.getUsername())
                );

                errorLabel.setVisible(false);
                openDashboard(user);
            } else {
                // Failed login - Log to audit
                auditLogDAO.logFailure(
                    null,
                    username,
                    "LOGIN",
                    "Authentication",
                    String.format("Failed login attempt for username: %s", username)
                );
                showError("Invalid username or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
            auditLogDAO.logFailure(
                null,
                username,
                "LOGIN",
                "Authentication",
                String.format("Login error for username %s: %s", username, e.getMessage())
            );
            showError("An error occurred. Please try again.");
        } finally {
            loginButton.setDisable(false);
        }
    }

    @FXML
    private void handleAdminCredentialsClick(ActionEvent event) {
        usernameField.setText("admin");
        passwordField.setText("password123");
    }
    @FXML
    private void handleManagerCredentialsClick(ActionEvent event) {
        usernameField.setText("manager");
        passwordField.setText("password123");
    }
    @FXML
    private void handleEmployeeCredentialsClick(ActionEvent event) {
        usernameField.setText("employee");
        passwordField.setText("password123");
    }

    
    private void openDashboard(User user) {
        // Declare variables outside try block for error handling
        String fxmlFile = "";
        String dashboardTitle = "";

        try {
            // Determine which dashboard to load based on user role
            switch (user.getRole()) {
                case ADMIN:
                    fxmlFile = "/fxml/AdminDashboard.fxml";
                    dashboardTitle = "Admin Dashboard - Fresh Dairy Co.";
                    break;
                case MANAGER:
                    fxmlFile = "/fxml/ManagerDashboard.fxml";
                    dashboardTitle = "Operations Hub - Fresh Dairy Co.";
                    break;
                case EMPLOYEE:
                    fxmlFile = "/fxml/EmployeeDashboard.fxml";
                    dashboardTitle = "My Portal - Fresh Dairy Co.";
                    break;
                case SUPPLIER:
                    // Suppliers don't have dashboard access (data-only role)
                    showError("Supplier accounts do not have dashboard access");
                    return;
                default:
                    // Fallback to generic dashboard
                    fxmlFile = "/fxml/Dashboard.fxml";
                    dashboardTitle = "Dashboard - " + user.getUsername();
                    break;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            // Pass user to the appropriate dashboard controller
            Object controller = loader.getController();
            if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).setCurrentUser(user);
            } else if (controller instanceof ManagerDashboardController) {
                ((ManagerDashboardController) controller).setCurrentUser(user);
            } else if (controller instanceof EmployeeDashboardController) {
                ((EmployeeDashboardController) controller).setCurrentUser(user);
            } else if (controller instanceof DashboardController) {
                ((DashboardController) controller).setCurrentUser(user);
            }

            // Get current stage and set new scene
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(dashboardTitle);

            // Make window resizable and maximized
            stage.setResizable(true);
            stage.setMaximized(true);
            stage.setFullScreen(false); // Use maximized instead of true fullscreen for better UX

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("DETAILED ERROR: " + e.getMessage());
            System.err.println("FXML FILE: " + fxmlFile);
            if (e.getCause() != null) {
                System.err.println("CAUSE: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            AlertUtil.showError("Error", "Could not load dashboard: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("UNEXPECTED ERROR: " + e.getMessage());
            AlertUtil.showError("Error", "Unexpected error: " + e.getMessage());
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
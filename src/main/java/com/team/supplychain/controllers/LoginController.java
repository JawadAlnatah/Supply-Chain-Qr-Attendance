package com.team.supplychain.controllers;

import com.team.supplychain.dao.AuditLogDAO;
import com.team.supplychain.dao.UserDAO;
import com.team.supplychain.models.User;
import com.team.supplychain.utils.AlertUtil;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the login screen.
 * Handles authentication and routes users to appropriate dashboards based on their role.
 *
 * PERFORMANCE OPTIMIZATION:
 * Authentication runs asynchronously on a background thread to prevent UI freezing.
 * Database queries (especially with BCrypt password verification) can take 100-500ms,
 * which would make the UI feel unresponsive if done on the JavaFX Application Thread.
 *
 * FLOW:
 * 1. User enters credentials and clicks login
 * 2. Validation happens instantly on UI thread
 * 3. Authentication query runs on background thread (doesn't block UI)
 * 4. On success: Audit log is recorded (also async) and dashboard loads
 * 5. On failure: Error message shown, login button re-enabled
 *
 * FXML FILE: Login.fxml
 */
public class LoginController {

    // UI Components (injected from FXML)
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    // Data Access Objects
    private final UserDAO userDAO = new UserDAO();
    private final AuditLogDAO auditLogDAO = new AuditLogDAO();

    /**
     * Initialize the controller after FXML is loaded.
     * Sets up Enter key to trigger login from both username and password fields.
     */
    @FXML
    private void initialize() {
        // Pressing Enter in username field moves focus to password or triggers login
        usernameField.setOnAction(e -> handleLogin(null));

        // Pressing Enter in password field triggers login immediately
        passwordField.setOnAction(e -> handleLogin(null));
    }
    
    /**
     * Handle login button click or Enter key press.
     *
     * This method uses JavaFX Task pattern for async authentication.
     * WHY? Because database queries (especially BCrypt password checking) are SLOW.
     * Running them on the UI thread would freeze the entire interface for 100-500ms.
     *
     * The Task pattern:
     * 1. Task.call() runs on background thread (doesn't block UI)
     * 2. Task.setOnSucceeded() runs on UI thread when done (safe to update UI)
     * 3. Task.setOnFailed() runs on UI thread if an exception occurs
     *
     * @param event The action event (can be null when triggered from Enter key)
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        // Quick validation on UI thread (instant feedback)
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        // Disable login button to prevent double-clicks
        // Button stays disabled until we get a response (success or failure)
        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        // Create async task for authentication
        // This Task object will run on a separate thread
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                // RUNS ON BACKGROUND THREAD - safe to do slow operations here
                // This is where we hit the database and check the password
                // BCrypt password verification takes ~100ms (intentionally slow for security)
                return userDAO.authenticate(username, password);
            }
        };

        // This callback runs AFTER authentication completes (on UI thread)
        // It's safe to update UI components here
        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();  // Get the result from background thread

            if (user != null) {
                // Authentication successful! User exists and password matches

                // Log successful login to audit trail
                // We do this in ANOTHER background thread so dashboard loads immediately
                // Audit logging is "fire and forget" - we don't wait for it
                new Thread(() -> {
                    auditLogDAO.logSuccess(
                        user.getUserId(),
                        user.getUsername(),
                        "LOGIN",
                        "Authentication",
                        String.format("User %s logged in successfully", user.getUsername())
                    );
                }).start();

                // Load the appropriate dashboard (AdminDashboard, EmployeeDashboard, etc.)
                openDashboard(user);

            } else {
                // Authentication failed - username not found or wrong password
                // For security, we don't tell the user WHICH one is wrong

                // Log failed login attempt (helps detect brute force attacks)
                new Thread(() -> {
                    auditLogDAO.logFailure(
                        null,  // No user ID since login failed
                        username,
                        "LOGIN",
                        "Authentication",
                        String.format("Failed login attempt for username: %s", username)
                    );
                }).start();

                // Show generic error message (don't reveal if username exists)
                showError("Invalid username or password");

                // Re-enable login button so user can try again
                loginButton.setDisable(false);
            }
        });

        // This callback runs if an exception occurs during authentication
        // For example: database connection failure, SQL error, etc.
        loginTask.setOnFailed(e -> {
            Throwable exception = loginTask.getException();
            exception.printStackTrace();

            // Log the technical error for debugging
            new Thread(() -> {
                auditLogDAO.logFailure(
                    null,
                    username,
                    "LOGIN",
                    "Authentication",
                    String.format("Login error for username %s: %s", username, exception.getMessage())
                );
            }).start();

            // Show user-friendly error message (don't expose technical details)
            showError("An error occurred. Please try again.");

            // Re-enable login button so user can retry
            loginButton.setDisable(false);
        });

        // Start the background task
        // This creates a new thread and runs loginTask.call() on it
        new Thread(loginTask).start();
    }

    // ========== TEST CREDENTIAL BUTTONS ==========
    // These buttons auto-fill login credentials for testing
    // Should be removed or hidden in production builds

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

    /**
     * Open the appropriate dashboard based on user's role.
     *
     * ROLE-BASED ROUTING:
     * Different user roles see completely different dashboards:
     * - ADMIN → AdminDashboard.fxml (full system control)
     * - MANAGER → ManagerDashboard.fxml (team management, reports)
     * - EMPLOYEE → EmployeeDashboard.fxml (personal attendance, requisitions)
     * - SUPPLIER → No dashboard access (data-only role for purchase orders)
     *
     * Each dashboard has its own controller class with role-specific features.
     *
     * @param user The authenticated user object with role information
     */
    private void openDashboard(User user) {
        // Declare variables outside try block so error handling can see them
        String fxmlFile = "";
        String dashboardTitle = "";

        try {
            // Route to dashboard based on user role (enum)
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
                    // Suppliers are external entities (vendors, manufacturers)
                    // They don't get UI access - only API access for order management
                    showError("Supplier accounts do not have dashboard access");
                    return;
                default:
                    // Safety fallback if someone adds a new role and forgets to update this switch
                    fxmlFile = "/fxml/Dashboard.fxml";
                    dashboardTitle = "Dashboard - " + user.getUsername();
                    break;
            }

            // Load the FXML file and create the dashboard UI
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();  // This parses FXML and creates JavaFX nodes

            // Pass the user object to the dashboard controller
            // Each dashboard controller needs to know WHO is logged in
            // We use instanceof to check the controller type and call the right method
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

            // Switch from login screen to dashboard screen
            // In JavaFX: Window = Stage, Screen = Scene
            Stage stage = (Stage) loginButton.getScene().getWindow();  // Get the current window
            Scene scene = new Scene(root);  // Create new scene with dashboard content
            stage.setScene(scene);  // Replace login scene with dashboard scene
            stage.setTitle(dashboardTitle);  // Update window title bar

            // Dashboard needs more screen real estate than login
            stage.setResizable(true);  // Allow window resizing (login was fixed size)
            stage.setMaximized(true);  // Start maximized (but not fullscreen - user can still see taskbar)
            stage.setFullScreen(false);  // Don't use true fullscreen (F11 style) - annoying for desktop apps

            stage.show();  // Display the dashboard

        } catch (IOException e) {
            // FXML loading error - usually means:
            // 1. FXML file is missing from resources folder
            // 2. FXML file has syntax errors (malformed XML)
            // 3. Controller class specified in FXML doesn't exist
            // 4. FXML references components that don't exist in controller
            e.printStackTrace();
            System.err.println("DETAILED ERROR: " + e.getMessage());
            System.err.println("FXML FILE: " + fxmlFile);  // Show which file failed to load
            if (e.getCause() != null) {
                System.err.println("CAUSE: " + e.getCause().getMessage());
                e.getCause().printStackTrace();
            }
            AlertUtil.showError("Error", "Could not load dashboard: " + e.getMessage());

        } catch (Exception e) {
            // Catch-all for any other unexpected errors
            // This prevents the app from crashing and shows a user-friendly message
            e.printStackTrace();
            System.err.println("UNEXPECTED ERROR: " + e.getMessage());
            AlertUtil.showError("Error", "Unexpected error: " + e.getMessage());
        }
    }

    /**
     * Show an error message below the login form.
     * Used for validation errors and authentication failures.
     *
     * @param message The error message to display
     */
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
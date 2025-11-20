package com.team.supplychain.controllers;

import com.team.supplychain.dao.UserDAO;
import com.team.supplychain.models.User;
import com.team.supplychain.utils.AlertUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.io.IOException;

public class LoginScreen {
    
    private Stage stage;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton;
    private Label errorLabel;
    private UserDAO userDAO;
    
    public LoginScreen(Stage stage) {
        this.stage = stage;
        this.userDAO = new UserDAO();
    }
    
    public Scene createScene() {
        // Main container
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Title
        Label titleLabel = new Label("Supply Chain Management System");
        titleLabel.setFont(new Font("System Bold", 24));
        titleLabel.setStyle("-fx-text-fill: #2c3e50;");
        
        // Login box
        VBox loginBox = new VBox(15);
        loginBox.setAlignment(Pos.CENTER);
        loginBox.setPadding(new Insets(30, 40, 30, 40));
        loginBox.setMaxWidth(400);
        loginBox.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                         "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 5);");
        
        // Subtitle
        Label subtitleLabel = new Label("Login");
        subtitleLabel.setFont(new Font("System", 20));
        
        // Username field
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        usernameField.setPrefWidth(300);
        usernameField.setStyle("-fx-padding: 10; -fx-font-size: 14;");
        
        // Password field
        passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.setPrefWidth(300);
        passwordField.setStyle("-fx-padding: 10; -fx-font-size: 14;");
        
        // Login button
        loginButton = new Button("Login");
        loginButton.setPrefWidth(300);
        loginButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                            "-fx-font-size: 14; -fx-padding: 12; -fx-cursor: hand;");
        loginButton.setOnAction(e -> handleLogin());
        
        // Error label
        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");
        errorLabel.setVisible(false);
        errorLabel.setWrapText(true);
        errorLabel.setMaxWidth(280);
        
        // Add all to login box
        loginBox.getChildren().addAll(subtitleLabel, usernameField, passwordField, 
                                      loginButton, errorLabel);
        
        // Add to root
        root.getChildren().addAll(titleLabel, loginBox);
        
        // Create scene
        Scene scene = new Scene(root, 800, 600);
        stage.setWidth(1100);   
        stage.setHeight(700);   
        
        // Enter key support
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> handleLogin());
        
        return scene;
    }
    
    private void handleLogin() {
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
            // ✅ FIXED: Changed from login() to authenticate()
            User user = userDAO.authenticate(username, password);
            
            if (user != null) {
                // Login successful
                errorLabel.setVisible(false);
                openDashboard(user);
            } else {
                showError("Invalid username or password");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("An error occurred. Please try again.");
        } finally {
            loginButton.setDisable(false);
        }
    }
    
    private void openDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass()
                .getResource("/fxml/Dashboard.fxml"));
            Parent root = loader.load();
            
            // Pass user to dashboard controller
            DashboardController controller = loader.getController();
            controller.setCurrentUser(user);
            
            // Set new scene
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Dashboard - " + user.getUsername());
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            AlertUtil.showError("Error", "Could not load dashboard");
        }
    }
    
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }
}
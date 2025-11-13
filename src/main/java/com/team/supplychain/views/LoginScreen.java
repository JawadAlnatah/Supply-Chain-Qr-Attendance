package com.team.supplychain.views;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.team.supplychain.dao.UserDAO;
import com.team.supplychain.models.User;

/**
 * Login Screen for Supply Chain Management System
 */
public class LoginScreen extends Application {
    
    private UserDAO userDAO = new UserDAO();
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Supply Chain Management System - Login");
        
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        
        // Title
        Text scenetitle = new Text("Welcome");
        scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
        grid.add(scenetitle, 0, 0, 2, 1);
        
        // Username Label and Field
        Label userName = new Label("Username:");
        grid.add(userName, 0, 1);
        
        TextField userTextField = new TextField();
        userTextField.setPromptText("Enter username");
        grid.add(userTextField, 1, 1);
        
        // Password Label and Field
        Label pw = new Label("Password:");
        grid.add(pw, 0, 2);
        
        PasswordField pwBox = new PasswordField();
        pwBox.setPromptText("Enter password");
        grid.add(pwBox, 1, 2);
        
        // Login Button
        Button btn = new Button("Sign in");
        HBox hbBtn = new HBox(10);
        hbBtn.setAlignment(Pos.BOTTOM_RIGHT);
        hbBtn.getChildren().add(btn);
        grid.add(hbBtn, 1, 4);
        
        // Action text for feedback
        final Text actiontarget = new Text();
        grid.add(actiontarget, 0, 6, 2, 1);
        
        // Button Action
        btn.setOnAction(e -> {
            String username = userTextField.getText();
            String password = pwBox.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("Please enter username and password");
                return;
            }
            
            // Authenticate user
            User user = userDAO.login(username, password);
            if (user != null) {
                actiontarget.setFill(Color.GREEN);
                actiontarget.setText("Login successful! Welcome " + user.getUsername());
                // TODO: Navigate to main dashboard
                openMainDashboard(primaryStage, user);
            } else {
                actiontarget.setFill(Color.FIREBRICK);
                actiontarget.setText("Invalid username or password");
            }
        });
        
        // Scene setup
        Scene scene = new Scene(grid, 400, 275);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    private void openMainDashboard(Stage stage, User user) {
        // TODO: Implement main dashboard
        System.out.println("Opening dashboard for user: " + user.getUsername());
        // For now, just print
    }
}
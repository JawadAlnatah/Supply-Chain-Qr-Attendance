package com.team.supplychain;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            System.out.println("Loading FXML...");
            
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            
            System.out.println("✓ FXML loaded successfully");
            
            // Create scene
            Scene scene = new Scene(root);
            
            // Set up stage
            primaryStage.setTitle("Supply Chain Management System - Login");
            primaryStage.setScene(scene);
            primaryStage.setResizable(true);
            primaryStage.show();
            
            System.out.println("✓ Application started successfully!");
            
        } catch (Exception e) {
            System.err.println("✗ Error loading application:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Starting Supply Chain Management System...");
        System.out.println("Java version: " + System.getProperty("java.version"));
        launch(args);
    }
}
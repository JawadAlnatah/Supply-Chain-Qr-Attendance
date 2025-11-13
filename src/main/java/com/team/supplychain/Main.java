package com.team.supplychain;

import javafx.application.Application;
import com.team.supplychain.views.LoginScreen;

/**
 * Main entry point for the Supply Chain Management System
 * @author Team
 */
public class Main {
    public static void main(String[] args) {
        // Launch the JavaFX application
        Application.launch(LoginScreen.class, args);
    }
}
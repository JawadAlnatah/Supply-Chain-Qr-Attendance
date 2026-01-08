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

public class AdminSystemSettingsController {

    @FXML private Label uptimeLabel, dbSizeLabel, lastBackupLabel, configFilesLabel;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextField searchField;
    @FXML private Button refreshButton;
    @FXML private TableView<SettingRecord> settingsTable;
    @FXML private TableColumn<SettingRecord, String> categoryColumn, settingNameColumn, currentValueColumn, defaultValueColumn, modifiedByColumn, modifiedDateColumn;

    private User currentUser;
    private ObservableList<SettingRecord> settingsData;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    @FXML
    private void initialize() {
        System.out.println("AdminSystemSettingsController initialized");
        settingsData = FXCollections.observableArrayList();
        setupTable();
        setupFilters();
        loadDummyData();
        updateStats();
    }

    private void setupTable() {
        if (settingsTable == null) return;

        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        settingNameColumn.setCellValueFactory(new PropertyValueFactory<>("settingName"));
        currentValueColumn.setCellValueFactory(new PropertyValueFactory<>("currentValue"));
        defaultValueColumn.setCellValueFactory(new PropertyValueFactory<>("defaultValue"));
        modifiedByColumn.setCellValueFactory(new PropertyValueFactory<>("modifiedBy"));
        modifiedDateColumn.setCellValueFactory(new PropertyValueFactory<>("modifiedDate"));

        settingsTable.setItems(settingsData);
    }

    private void setupFilters() {
        if (categoryFilter != null) {
            categoryFilter.getItems().addAll("All Categories", "Database", "Email", "Security", "Application");
            categoryFilter.setValue("All Categories");
        }
    }

    private void loadDummyData() {
        settingsData.clear();
        settingsData.add(new SettingRecord("Database", "Max Connections", "100", "50", "admin", "2025-12-01 10:30"));
        settingsData.add(new SettingRecord("Database", "Connection Timeout", "30s", "10s", "admin", "2025-11-15 14:20"));
        settingsData.add(new SettingRecord("Database", "Auto Backup Interval", "24h", "168h", "admin", "2025-11-20 09:00"));
        settingsData.add(new SettingRecord("Email", "SMTP Server", "smtp.gmail.com", "localhost", "admin", "2025-10-15 16:45"));
        settingsData.add(new SettingRecord("Email", "SMTP Port", "587", "25", "admin", "2025-10-15 16:45"));
        settingsData.add(new SettingRecord("Email", "Use TLS", "Enabled", "Disabled", "admin", "2025-10-15 16:45"));
        settingsData.add(new SettingRecord("Security", "Session Timeout", "30min", "15min", "system", "2025-12-03 11:00"));
        settingsData.add(new SettingRecord("Security", "Max Login Attempts", "5", "3", "admin", "2025-11-28 13:15"));
        settingsData.add(new SettingRecord("Security", "Password Expiry Days", "90", "60", "admin", "2025-11-28 13:15"));
        settingsData.add(new SettingRecord("Application", "Default Language", "English", "English", "system", "2025-01-01 00:00"));
        settingsData.add(new SettingRecord("Application", "Date Format", "YYYY-MM-DD", "MM/DD/YYYY", "admin", "2025-09-20 10:30"));
        settingsData.add(new SettingRecord("Application", "Timezone", "Asia/Riyadh", "UTC", "admin", "2025-09-20 10:30"));
    }

    private void updateStats() {
        if (uptimeLabel != null) uptimeLabel.setText("15d 8h");
        if (dbSizeLabel != null) dbSizeLabel.setText("2.3 GB");
        if (lastBackupLabel != null) lastBackupLabel.setText("2h ago");
        if (configFilesLabel != null) configFilesLabel.setText(String.valueOf(settingsData.size()));
    }

    @FXML private void handleRefresh() { loadDummyData(); updateStats(); }

    public static class SettingRecord {
        private final StringProperty category, settingName, currentValue, defaultValue, modifiedBy, modifiedDate;

        public SettingRecord(String category, String settingName, String currentValue, String defaultValue, String modifiedBy, String modifiedDate) {
            this.category = new SimpleStringProperty(category);
            this.settingName = new SimpleStringProperty(settingName);
            this.currentValue = new SimpleStringProperty(currentValue);
            this.defaultValue = new SimpleStringProperty(defaultValue);
            this.modifiedBy = new SimpleStringProperty(modifiedBy);
            this.modifiedDate = new SimpleStringProperty(modifiedDate);
        }

        public String getCategory() { return category.get(); }
        public String getSettingName() { return settingName.get(); }
        public String getCurrentValue() { return currentValue.get(); }
        public String getDefaultValue() { return defaultValue.get(); }
        public String getModifiedBy() { return modifiedBy.get(); }
        public String getModifiedDate() { return modifiedDate.get(); }
    }
}

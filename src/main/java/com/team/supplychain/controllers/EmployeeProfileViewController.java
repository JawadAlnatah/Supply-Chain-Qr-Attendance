package com.team.supplychain.controllers;

import com.team.supplychain.dao.EmployeeDAO;
import com.team.supplychain.models.Employee;
import com.team.supplychain.models.User;
import com.team.supplychain.services.QRCodeService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;

/**
 * Controller for Employee Profile View
 * Displays employee personal information and QR code for attendance tracking
 */
public class EmployeeProfileViewController {

    // ==================== PROFILE HEADER ====================
    @FXML private Label profileNameLabel;
    @FXML private Label profileRoleLabel;
    @FXML private Label profileDepartmentLabel;

    // ==================== PERSONAL INFO SECTION ====================
    @FXML private Label employeeIdValue;
    @FXML private Label emailValue;
    @FXML private Label phoneValue;
    @FXML private Label positionValue;
    @FXML private Label departmentValue;
    @FXML private Label hireDateValue;
    @FXML private Label usernameValue;

    // ==================== QR CODE SECTION ====================
    @FXML private ImageView qrCodeImageView;
    @FXML private Label qrCodeStatusLabel;
    @FXML private Label qrCodeTextLabel;
    @FXML private VBox qrCodeContainer;

    private User currentUser;
    private Employee currentEmployee;
    private EmployeeDAO employeeDAO;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, yyyy");

    /**
     * Set the current logged-in user and load profile data
     * @param user The current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadProfileData();
    }

    /**
     * Initialize the controller
     */
    @FXML
    private void initialize() {
        System.out.println("EmployeeProfileViewController initialized");
        employeeDAO = new EmployeeDAO();

        // Set default QR code placeholder
        if (qrCodeStatusLabel != null) {
            qrCodeStatusLabel.setText("Loading QR code...");
        }
    }

    /**
     * Load employee profile data from database
     */
    private void loadProfileData() {
        if (currentUser == null) {
            showError("Error", "No user session found. Please log in again.");
            return;
        }

        try {
            // Fetch employee record for current user
            currentEmployee = employeeDAO.getEmployeeByUserId(currentUser.getUserId());

            if (currentEmployee == null) {
                showError("Profile Not Found",
                    "No employee profile found for your account. Please contact HR.");
                return;
            }

            // Populate UI with employee data
            updateProfileHeader();
            updatePersonalInfo();
            updateQRCode();

            System.out.println("Profile data loaded successfully for: " + currentEmployee.getFullName());

        } catch (Exception e) {
            e.printStackTrace();
            showError("Loading Error", "Failed to load profile data: " + e.getMessage());
        }
    }

    /**
     * Update profile header section (avatar, name, role, department)
     */
    private void updateProfileHeader() {
        if (profileNameLabel != null) {
            profileNameLabel.setText(currentEmployee.getFullName());
        }

        if (profileRoleLabel != null) {
            profileRoleLabel.setText(currentUser.getRole().toString());
        }

        if (profileDepartmentLabel != null) {
            profileDepartmentLabel.setText(currentEmployee.getDepartment());
        }
    }

    /**
     * Update personal information section with employee details
     */
    private void updatePersonalInfo() {
        if (employeeIdValue != null) {
            employeeIdValue.setText(String.format("EMP-%05d", currentEmployee.getEmployeeId()));
        }

        if (emailValue != null) {
            emailValue.setText(currentEmployee.getEmail());
        }

        if (phoneValue != null) {
            phoneValue.setText(currentEmployee.getPhone() != null ?
                currentEmployee.getPhone() : "Not provided");
        }

        if (positionValue != null) {
            positionValue.setText(currentEmployee.getPosition());
        }

        if (departmentValue != null) {
            departmentValue.setText(currentEmployee.getDepartment());
        }

        if (hireDateValue != null && currentEmployee.getHireDate() != null) {
            hireDateValue.setText(currentEmployee.getHireDate().format(DATE_FORMATTER));
        }

        if (usernameValue != null) {
            usernameValue.setText(currentUser.getUsername());
        }
    }

    /**
     * Generate and display QR code for attendance tracking
     * Handles three scenarios:
     * 1. QR code exists → Generate and display
     * 2. QR code is null → Show placeholder message
     * 3. QR generation fails → Show error message
     */
    private void updateQRCode() {
        String qrCodeData = currentEmployee.getQrCode();

        if (qrCodeData == null || qrCodeData.isEmpty()) {
            // No QR code in database - show placeholder
            if (qrCodeStatusLabel != null) {
                qrCodeStatusLabel.setText("QR Code not generated yet. Contact HR for QR code assignment.");
                qrCodeStatusLabel.setStyle("-fx-text-fill: #fb8c00; -fx-font-size: 12px;");
            }
            if (qrCodeImageView != null) {
                qrCodeImageView.setVisible(false);
            }
            System.out.println("No QR code found for employee: " + currentEmployee.getFullName());
            return;
        }

        // Generate QR code image using ZXing
        Image qrImage = QRCodeService.generateQRCodeImage(qrCodeData, 300, 300);

        if (qrImage == null) {
            // Generation failed
            if (qrCodeStatusLabel != null) {
                qrCodeStatusLabel.setText("Failed to generate QR code visualization.");
                qrCodeStatusLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 12px;");
            }
            if (qrCodeImageView != null) {
                qrCodeImageView.setVisible(false);
            }
            System.err.println("QR code generation failed for data: " + qrCodeData);
            return;
        }

        // Successfully generated - display QR code
        if (qrCodeImageView != null) {
            qrCodeImageView.setImage(qrImage);
            qrCodeImageView.setVisible(true);
            qrCodeImageView.setFitWidth(300);
            qrCodeImageView.setFitHeight(300);
            qrCodeImageView.setPreserveRatio(true);
        }

        if (qrCodeStatusLabel != null) {
            qrCodeStatusLabel.setText("Scan this code for attendance check-in");
            qrCodeStatusLabel.setStyle("-fx-text-fill: #43a047; -fx-font-size: 13px; -fx-font-weight: bold;");
        }

        // Display QR code text for manual entry
        if (qrCodeTextLabel != null) {
            qrCodeTextLabel.setText(qrCodeData);
        }

        System.out.println("QR code generated successfully for: " + currentEmployee.getFullName());
    }

    /**
     * Show error alert dialog
     * @param title Dialog title
     * @param message Error message
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

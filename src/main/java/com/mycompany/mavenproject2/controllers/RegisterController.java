package com.mycompany.mavenproject2.controllers;

import com.mycompany.mavenproject2.models.User;
import com.mycompany.mavenproject2.services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;

public class RegisterController {
    
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField fullNameField;
    @FXML private TextField phoneField;
    @FXML private TextArea addressField;
    @FXML private Label errorLabel;
    
    private UserService userService = new UserService();
    
    @FXML
    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        
        // Validate fields
        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            errorLabel.setText("Please fill in all required fields");
            return;
        }
        
        // Create new user
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setRole("CUSTOMER");
        user.setPhone(phone);
        user.setAddress(address);
        
        try {
            if (userService.register(user)) {
                showAlert("Registration Successful", "Your account has been created successfully!", Alert.AlertType.INFORMATION);
                handleBackToLogin();
            } else {
                errorLabel.setText("Registration failed. Username may already exist.");
            }
        } catch (Exception e) {
            String errorMessage = e.getMessage() != null ? e.getMessage() : "An unexpected error occurred";
            
            // Check for common registration errors
            if (errorMessage.toLowerCase().contains("duplicate") || 
                errorMessage.toLowerCase().contains("unique") ||
                errorMessage.toLowerCase().contains("constraint")) {
                errorLabel.setText("Username already exists. Please choose another.");
            } else {
                errorLabel.setText("Error during registration: " + errorMessage);
            }
            
            // Log the full error for debugging
            System.err.println("Registration error: " + errorMessage);
            e.printStackTrace();
        }
    }
    
    
    @FXML
    private void handleBackToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Restaurant Login");
        } catch (IOException e) {
            errorLabel.setText("Error returning to login page");
            System.err.println("Login page navigation error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
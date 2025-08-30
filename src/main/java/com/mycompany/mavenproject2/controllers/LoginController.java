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
import java.sql.SQLException;

public class LoginController {
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    private UserService userService = new UserService();
    
    @FXML
    private void initialize() {
        try {
            userService.printAllUsers();
        } catch (Exception e) {
            System.out.println("Error printing users: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        
        System.out.println("Login attempt - Username: " + username);
        
        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter both username and password");
            return;
        }
        
        try {
            User user = userService.login(username, password);
            
            if (user != null) {
                System.out.println("Login successful - Role: " + user.getRole());
                
                if (user.isAdmin()) {
                    System.out.println("Redirecting to admin dashboard");
                    showAdminDashboard(user);
                } else {
                    System.out.println("Redirecting to customer dashboard");
                    showCustomerDashboard(user);
                }
            } else {
                errorLabel.setText("Invalid username or password");
            }
        } catch (Exception e) {
            System.out.println("Login error: " + e.getMessage());
            errorLabel.setText("Login error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @FXML
    private void handleRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/register.fxml"));
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Register New Account");
        } catch (IOException e) {
            System.out.println("Error loading registration page: " + e.getMessage());
            errorLabel.setText("Error loading registration page");
            e.printStackTrace();
        }
    }
    
    private void showAdminDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin_dashboard.fxml"));
            Parent root = loader.load();
            
            AdminDashboardController controller = loader.getController();
            controller.initData(user);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Admin Dashboard");
            stage.setMaximized(true);
        } catch (IOException e) {
            System.out.println("Error loading admin dashboard: " + e.getMessage());
            errorLabel.setText("Error loading admin dashboard");
            e.printStackTrace();
        }
    }
    
    private void showCustomerDashboard(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/customer_dashboard.fxml"));
            Parent root = loader.load();
            
            CustomerDashboardController controller = loader.getController();
            controller.initData(user);
            
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Restaurant Menu");
            stage.setMaximized(true);
        } catch (IOException e) {
            System.out.println("Error loading customer dashboard: " + e.getMessage());
            errorLabel.setText("Error loading customer dashboard");
            e.printStackTrace();
        }
    }
}
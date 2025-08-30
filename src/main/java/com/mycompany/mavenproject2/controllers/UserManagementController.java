package com.mycompany.mavenproject2.controllers;

import com.mycompany.mavenproject2.models.*;
import com.mycompany.mavenproject2.services.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import java.net.URL;
import java.util.*;

public class UserManagementController implements Initializable {
    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, Integer> idColumn;
    @FXML private TableColumn<User, String> usernameColumn;
    @FXML private TableColumn<User, String> fullNameColumn;
    @FXML private TableColumn<User, String> roleColumn;
    
    private UserService userService = new UserService();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadUsers();
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        fullNameColumn.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
    }
    
    private void loadUsers() {
        try {
            List<User> users = userService.getAllUsers();
            usersTable.setItems(FXCollections.observableArrayList(users));
        } catch (Exception e) {
            showAlert("Error", "Could not load users: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void showAddUserDialog() {
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Add New User");
        dialog.setHeaderText("Enter user details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField username = new TextField();
        TextField fullName = new TextField();
        PasswordField password = new PasswordField();
        ComboBox<String> role = new ComboBox<>();
        
        username.setPromptText("Username");
        fullName.setPromptText("Full Name");
        password.setPromptText("Password");
        role.getItems().addAll("ADMIN", "CUSTOMER");
        role.setValue("CUSTOMER");
        
        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullName, 1, 1);
        grid.add(new Label("Password:"), 0, 2);
        grid.add(password, 1, 2);
        grid.add(new Label("Role:"), 0, 3);
        grid.add(role, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    User newUser = new User();
                    newUser.setUsername(username.getText());
                    newUser.setFullName(fullName.getText());
                    newUser.setPassword(password.getText());
                    newUser.setRole(role.getValue());
                    
                    userService.createUser(newUser);
                    loadUsers();
                    showAlert("Success", "User created successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Could not create user: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    @FXML
    private void editUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Error", "Please select a user to edit", Alert.AlertType.WARNING);
            return;
        }
        
        Dialog<User> dialog = new Dialog<>();
        dialog.setTitle("Edit User");
        dialog.setHeaderText("Edit user details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField username = new TextField(selectedUser.getUsername());
        TextField fullName = new TextField(selectedUser.getFullName());
        ComboBox<String> role = new ComboBox<>();
        
        role.getItems().addAll("ADMIN", "CUSTOMER");
        role.setValue(selectedUser.getRole());
        
        grid.add(new Label("Username:"), 0, 0);
        grid.add(username, 1, 0);
        grid.add(new Label("Full Name:"), 0, 1);
        grid.add(fullName, 1, 1);
        grid.add(new Label("Role:"), 0, 2);
        grid.add(role, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    selectedUser.setUsername(username.getText());
                    selectedUser.setFullName(fullName.getText());
                    selectedUser.setRole(role.getValue());
                    
                    userService.updateUser(selectedUser);
                    loadUsers();
                    showAlert("Success", "User updated successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Could not update user: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    @FXML
    private void resetPassword() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Error", "Please select a user", Alert.AlertType.WARNING);
            return;
        }
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Reset Password");
        dialog.setHeaderText("Enter new password for " + selectedUser.getUsername());
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        PasswordField password = new PasswordField();
        PasswordField confirmPassword = new PasswordField();
        
        grid.add(new Label("New Password:"), 0, 0);
        grid.add(password, 1, 0);
        grid.add(new Label("Confirm Password:"), 0, 1);
        grid.add(confirmPassword, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (!password.getText().equals(confirmPassword.getText())) {
                    showAlert("Error", "Passwords do not match", Alert.AlertType.ERROR);
                    return null;
                }
                
                try {
                    userService.resetPassword(selectedUser.getId(), password.getText());
                    showAlert("Success", "Password reset successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Could not reset password: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    @FXML
    private void deleteUser() {
        User selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showAlert("Error", "Please select a user", Alert.AlertType.WARNING);
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete User");
        alert.setHeaderText("Delete User: " + selectedUser.getUsername());
        alert.setContentText("Are you sure you want to delete this user? This action cannot be undone.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                userService.deleteUser(selectedUser.getId());
                loadUsers();
                showAlert("Success", "User deleted successfully", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Could not delete user: " + e.getMessage(), Alert.AlertType.ERROR);
            }
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
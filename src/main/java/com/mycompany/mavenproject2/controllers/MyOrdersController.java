package com.mycompany.mavenproject2.controllers;

import com.mycompany.mavenproject2.models.*;
import com.mycompany.mavenproject2.services.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import java.net.URL;
import java.util.*;
import java.sql.SQLException;

public class MyOrdersController implements Initializable {
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, Double> totalColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, Date> dateColumn;
    
    private User currentUser;
    private OrderService orderService = new OrderService();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
    }
    
    public void initData(User user) {
        this.currentUser = user;
        System.out.println("Loading orders for user: " + user.getUsername());
        loadOrders();
    }
    
    private void setupTable() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }
    
    private void loadOrders() {
        try {
            List<Order> userOrders = orderService.getOrdersByUser(currentUser.getId());
            System.out.println("Found " + userOrders.size() + " orders");
            ordersTable.setItems(FXCollections.observableArrayList(userOrders));
        } catch (SQLException e) {
            System.out.println("Error loading orders: " + e.getMessage());
            showAlert("Error", "Could not load orders: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleCancelOrder() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert("Error", "Please select an order to cancel", Alert.AlertType.WARNING);
            return;
        }
        
        if (!selectedOrder.getStatus().equals("PENDING")) {
            showAlert("Error", "Only pending orders can be cancelled", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            orderService.updateOrderStatus(selectedOrder.getId(), "CANCELLED");
            loadOrders(); // Refresh the table
            showAlert("Success", "Order cancelled successfully", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Could not cancel order: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void handleClose() {
        Stage stage = (Stage) ordersTable.getScene().getWindow();
        stage.close();
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
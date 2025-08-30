package com.mycompany.mavenproject2.controllers;

import com.mycompany.mavenproject2.models.*;
import com.mycompany.mavenproject2.services.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.io.IOException;  // Add this import

public class AdminDashboardController implements Initializable {
    @FXML private TableView<Order> ordersTable;
    @FXML private TableColumn<Order, Integer> orderIdColumn;
    @FXML private TableColumn<Order, String> customerColumn;
    @FXML private TableColumn<Order, Double> totalColumn;
    @FXML private TableColumn<Order, String> statusColumn;
    @FXML private TableColumn<Order, Date> dateColumn;
    @FXML private ComboBox<String> orderStatusCombo;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    @FXML private ListView<String> orderItemsList;
    @FXML private Label customerDetailsLabel;
    @FXML private Label orderDateLabel;
    @FXML private Label orderTotalLabel;
    @FXML private TextArea orderNotesArea;
    @FXML private VBox orderDetailsBox;
    @FXML private Label totalOrdersLabel;
    @FXML private Label pendingOrdersLabel;
    @FXML private Label preparingOrdersLabel;
    @FXML private Label readyOrdersLabel;
    @FXML private Label deliveredOrdersLabel;
    @FXML private Label cancelledOrdersLabel;
    @FXML private Label totalRevenueLabel;
    
    private User currentUser;
    private OrderService orderService = new OrderService();
    private UserService userService = new UserService();
    private ProductService productService = new ProductService();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupDatePickers();
        setupOrderStatusFilter();
        setupTableSelection();
        refreshOrders();
        updateDashboardStats();
    }
    
    private void setupTable() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        customerColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
    }
    
    private void setupDatePickers() {
        startDate.setValue(LocalDate.now().minusWeeks(1));
        endDate.setValue(LocalDate.now());
    }
    
    private void setupOrderStatusFilter() {
        orderStatusCombo.setItems(FXCollections.observableArrayList(
            "ALL", "PENDING", "PREPARING", "READY", "DELIVERED", "CANCELLED"));
        orderStatusCombo.setValue("ALL");
    }
    
    private void setupTableSelection() {
        ordersTable.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    showOrderDetails(newSelection);
                }
            });
    }
    
    @FXML
    private void refreshOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            ordersTable.setItems(FXCollections.observableArrayList(orders));
            updateDashboardStats();
        } catch (SQLException e) {
            showAlert("Error", "Could not load orders: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void viewOrderDetails() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            showOrderDetails(selectedOrder);
        }
    }
    
    @FXML
    private void printReceipt() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder != null) {
            // Implement receipt printing logic
            showAlert("Info", "Receipt printing feature coming soon!", Alert.AlertType.INFORMATION);
        }
    }
    
    @FXML
    private void handleCancelOrder() {
        changeOrderStatus("CANCELLED");
    }
    
    @FXML
    private void applyFilters() {
        try {
            String status = orderStatusCombo.getValue();
            List<Order> filteredOrders;
            
            LocalDateTime startDateTime = startDate.getValue().atStartOfDay();
            LocalDateTime endDateTime = endDate.getValue().plusDays(1).atStartOfDay();
            
            Timestamp startTs = Timestamp.valueOf(startDateTime);
            Timestamp endTs = Timestamp.valueOf(endDateTime);
            
            if ("ALL".equals(status)) {
                filteredOrders = orderService.getOrdersByDateRange(startTs, endTs);
            } else {
                filteredOrders = orderService.getOrdersByStatusAndDateRange(status, startTs, endTs);
            }
            
            ordersTable.setItems(FXCollections.observableArrayList(filteredOrders));
            updateDashboardStats();
        } catch (SQLException e) {
            showAlert("Error", "Could not apply filters: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void resetFilters() {
        startDate.setValue(LocalDate.now().minusWeeks(1));
        endDate.setValue(LocalDate.now());
        orderStatusCombo.setValue("ALL");
        refreshOrders();
    }
    
    private void showOrderDetails(Order order) {
        customerDetailsLabel.setText("Customer: " + order.getCustomerName());
        orderDateLabel.setText("Order Date: " + order.getCreatedAt().toString());
        orderTotalLabel.setText(String.format("Total: $%.2f", order.getTotalAmount()));
        orderNotesArea.setText(order.getNotes());
        
        List<String> itemDetails = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            String detail = String.format("%s x%d - $%.2f", 
                item.getProduct().getName(),
                item.getQuantity(),
                item.getProduct().getPrice() * item.getQuantity());
            itemDetails.add(detail);
        }
        orderItemsList.setItems(FXCollections.observableArrayList(itemDetails));
    }
    
    @FXML
    private void handlePreparingStatus() {
        changeOrderStatus("PREPARING");
    }
    
    @FXML
    private void handleReadyStatus() {
        changeOrderStatus("READY");
    }
    
    @FXML
    private void handleDeliveredStatus() {
        changeOrderStatus("DELIVERED");
    }
    
    private void setupAutoRefresh() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(30), e -> refreshOrders()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void changeOrderStatus(String newStatus) {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert("Error", "Please select an order", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            orderService.updateOrderStatus(selectedOrder.getId(), newStatus);
            refreshOrders();  // Immediate refresh
            updateDashboardStats();
            
            // Refresh other tables after a short delay
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> {
                refreshOrders();
                updateDashboardStats();
            }));
            timeline.play();
            
            showAlert("Success", "Order status updated successfully", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Could not update order status: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void saveOrderNotes() {
        Order selectedOrder = ordersTable.getSelectionModel().getSelectedItem();
        if (selectedOrder == null) {
            showAlert("Error", "Please select an order", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            String notes = orderNotesArea.getText();
            orderService.updateOrderNotes(selectedOrder.getId(), notes);
            showAlert("Success", "Notes saved successfully", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            showAlert("Error", "Could not save notes: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
private void showUserManagement() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UserManagement.fxml"));
        Parent root = loader.load();
        
        Stage stage = new Stage();
        stage.setTitle("User Management");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        showAlert("Error", "Could not open user management: " + e.getMessage(), Alert.AlertType.ERROR);
        e.printStackTrace();
    }
}

@FXML
private void showProductManagement() {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProductManagement.fxml"));
        Parent root = loader.load();
        
        Stage stage = new Stage();
        stage.setTitle("Product Management");
        stage.setScene(new Scene(root));
        stage.show();
    } catch (IOException e) {
        showAlert("Error", "Could not open product management: " + e.getMessage(), Alert.AlertType.ERROR);
        e.printStackTrace();
    }
}
    
    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ordersTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (Exception e) {
            showAlert("Error", "Could not logout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void updateDashboardStats() {
        try {
            Map<String, Integer> orderStats = orderService.getOrderStatistics();
            Map<String, Double> revenueStats = orderService.getRevenueStatistics();
            
            totalOrdersLabel.setText("Total Orders: " + orderStats.get("total"));
            pendingOrdersLabel.setText("Pending: " + orderStats.getOrDefault("pending", 0));
            preparingOrdersLabel.setText("Preparing: " + orderStats.getOrDefault("preparing", 0));
            readyOrdersLabel.setText("Ready: " + orderStats.getOrDefault("ready", 0));
            deliveredOrdersLabel.setText("Delivered: " + orderStats.getOrDefault("delivered", 0));
            cancelledOrdersLabel.setText("Cancelled: " + orderStats.getOrDefault("cancelled", 0));
            
            double totalRevenue = revenueStats.getOrDefault("totalRevenue", 0.0);
            totalRevenueLabel.setText(String.format("Total Revenue: $%.2f", totalRevenue));
            
        } catch (SQLException e) {
            showAlert("Error", "Could not update dashboard statistics: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    public void initData(User user) {
        this.currentUser = user;
    }
    
    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
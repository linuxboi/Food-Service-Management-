package com.mycompany.mavenproject2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import com.mycompany.mavenproject2.models.Order;
import com.mycompany.mavenproject2.models.Product;
import com.mycompany.mavenproject2.models.OrderItem;
import com.mycompany.mavenproject2.services.OrderService;
import com.mycompany.mavenproject2.services.ProductService;

import java.net.URL;
import java.util.ResourceBundle;
import java.sql.SQLException;

public class MainController implements Initializable {
    @FXML private TableView<Order> orderTable;
    @FXML private TableView<Product> productTable;
    @FXML private TextField customerNameField;
    @FXML private ComboBox<Product> productComboBox;
    @FXML private Spinner<Integer> quantitySpinner;
    
    private OrderService orderService;
    private ProductService productService;
    private ObservableList<Order> orders;
    private ObservableList<Product> products;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        orderService = new OrderService();
        productService = new ProductService();
        
        initializeTables();
        loadData();
        
        // Initialize the spinner
        SpinnerValueFactory<Integer> valueFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        quantitySpinner.setValueFactory(valueFactory);
    }

    private void initializeTables() {
        // Initialize order table
        TableColumn<Order, Integer> orderIdCol = new TableColumn<>("N° Commande");
        orderIdCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        
        TableColumn<Order, String> customerCol = new TableColumn<>("Client");
        customerCol.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        orderTable.getColumns().addAll(orderIdCol, customerCol, statusCol);

        // Initialize product table
        TableColumn<Product, String> nameCol = new TableColumn<>("Nom");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        TableColumn<Product, Double> priceCol = new TableColumn<>("Prix (€)");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        
        TableColumn<Product, Integer> stockCol = new TableColumn<>("Stock");
        stockCol.setCellValueFactory(new PropertyValueFactory<>("stock"));
        
        productTable.getColumns().addAll(nameCol, priceCol, stockCol);
    }

    private void loadData() {
        try {
            products = FXCollections.observableArrayList(productService.getAllProducts());
            orders = FXCollections.observableArrayList(orderService.getAllOrders());
            
            productTable.setItems(products);
            orderTable.setItems(orders);
            productComboBox.setItems(products);
        } catch (SQLException e) {
            showAlert("Error", "Failed to load data: " + e.getMessage());
        }
    }

    @FXML
    private void handleNewOrder() {
        String customerName = customerNameField.getText().trim();
        if (customerName.isEmpty()) {
            showAlert("Erreur", "Le nom du client est requis");
            return;
        }

        try {
            Order newOrder = new Order(0, customerName); // 0 is temporary userId
            orderService.createOrder(newOrder);
            orders.add(newOrder);
            clearFields();
        } catch (SQLException e) {
            showAlert("Error", "Failed to create order: " + e.getMessage());
        }
    }

    @FXML
    private void handleAddProduct() {
        Order selectedOrder = orderTable.getSelectionModel().getSelectedItem();
        Product selectedProduct = productComboBox.getValue();
        int quantity = quantitySpinner.getValue();

        if (selectedOrder == null || selectedProduct == null) {
            showAlert("Erreur", "Veuillez sélectionner une commande et un produit");
            return;
        }

        if (quantity <= 0 || quantity > selectedProduct.getStock()) {
            showAlert("Erreur", "Quantité invalide");
            return;
        }

        try {
            // Create an OrderItem
            OrderItem orderItem = new OrderItem(selectedProduct, quantity);
            selectedOrder.getItems().add(orderItem);
            
            // Update the order
            orderService.createOrder(selectedOrder);
            refreshTables();
        } catch (SQLException e) {
            showAlert("Error", "Failed to add product: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearFields() {
        customerNameField.clear();
        productComboBox.setValue(null);
        quantitySpinner.getValueFactory().setValue(1);
    }

    private void refreshTables() {
        loadData();
    }
}
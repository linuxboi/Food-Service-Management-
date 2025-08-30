package com.mycompany.mavenproject2.controllers;

import com.mycompany.mavenproject2.models.*;
import com.mycompany.mavenproject2.services.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.geometry.Pos;
import javafx.scene.text.TextAlignment;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.net.URL;
import java.util.*;
import java.sql.SQLException;
import java.io.IOException;

public class CustomerDashboardController implements Initializable {
    @FXML private Label welcomeLabel;
    @FXML private FlowPane categoryPane;
    @FXML private ScrollPane menuScrollPane;
    @FXML private GridPane menuGrid;
    @FXML private ListView<CartItem> cartList;
    @FXML private Label totalLabel;
    @FXML private Button placeOrderButton;
    
    private User currentUser;
    private MenuService menuService = new MenuService();
    private OrderService orderService = new OrderService();
    private Map<Product, Integer> cart = new HashMap<>();
    private double total = 0.0;
    private Category selectedCategory;

    private static class CartItem {
        private final Product product;
        private final int quantity;
        private final double totalPrice;

        public CartItem(Product product, int quantity) {
            this.product = product;
            this.quantity = quantity;
            this.totalPrice = product.getPrice() * quantity;
        }

        @Override
        public String toString() {
            return String.format("%s    x%d\n$%.2f each    Total: $%.2f", 
                product.getName(), 
                quantity,
                product.getPrice(),
                totalPrice);
        }

        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public double getTotalPrice() { return totalPrice; }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupUI();
        loadCategories();
        setupCartListeners();
    }
    
    

    private void setupUI() {
        menuScrollPane.setFitToWidth(true);
        menuGrid.setHgap(15);
        menuGrid.setVgap(15);
        
        cartList.setCellFactory(param -> new ListCell<CartItem>() {
            private final HBox content = new HBox(10);
            private final VBox itemInfo = new VBox(5);
            private final HBox quantityBox = new HBox(5);
            private final Label nameLabel = new Label();
            private final Label priceLabel = new Label();
            private final Label totalLabel = new Label();
            private final Button decreaseBtn = new Button("-");
            private final Button increaseBtn = new Button("+");
            private final Label quantityLabel = new Label();

            {
                content.setAlignment(Pos.CENTER_LEFT);
                itemInfo.getChildren().addAll(nameLabel, priceLabel, totalLabel);
                quantityBox.getChildren().addAll(decreaseBtn, quantityLabel, increaseBtn);
                content.getChildren().addAll(itemInfo, new Region(), quantityBox);
                HBox.setHgrow(itemInfo, Priority.ALWAYS);

                decreaseBtn.setOnAction(e -> adjustSelectedItemQuantity(getItem().getProduct(), -1));
                increaseBtn.setOnAction(e -> adjustSelectedItemQuantity(getItem().getProduct(), 1));

                content.setStyle("-fx-padding: 5;");
                decreaseBtn.setStyle("-fx-min-width: 30;");
                increaseBtn.setStyle("-fx-min-width: 30;");
            }

            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    nameLabel.setText(item.getProduct().getName());
                    priceLabel.setText(String.format("$%.2f each", item.getProduct().getPrice()));
                    totalLabel.setText(String.format("Total: $%.2f", item.getTotalPrice()));
                    quantityLabel.setText(String.valueOf(item.getQuantity()));
                    decreaseBtn.setDisable(item.getQuantity() <= 1);
                    increaseBtn.setDisable(item.getQuantity() >= item.getProduct().getStock());
                    setGraphic(content);
                }
            }
        });

        placeOrderButton.setDisable(true);
    }

    private void setupCartListeners() {
        cartList.getItems().addListener((javafx.collections.ListChangeListener.Change<? extends CartItem> c) -> {
            placeOrderButton.setDisable(cartList.getItems().isEmpty());
        });
    }

    private void loadCategories() {
        try {
            List<Category> categories = menuService.getAllCategories();
            categoryPane.getChildren().clear();
            
            for (Category category : categories) {
                Button catButton = createCategoryButton(category);
                categoryPane.getChildren().add(catButton);
                
                if (selectedCategory == null) {
                    selectedCategory = category;
                    catButton.getStyleClass().add("selected-category");
                    loadMenuItems(category.getId());
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Could not load categories: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private Button createCategoryButton(Category category) {
        Button button = new Button(category.getName());
        button.getStyleClass().add("category-button");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setWrapText(true);
        button.setTextAlignment(TextAlignment.CENTER);
        
        button.setOnAction(e -> {
            categoryPane.getChildren().forEach(node -> 
                node.getStyleClass().remove("selected-category"));
            button.getStyleClass().add("selected-category");
            selectedCategory = category;
            loadMenuItems(category.getId());
        });
        
        return button;
    }

    private void loadMenuItems(int categoryId) {
        try {
            menuGrid.getChildren().clear();
            List<Product> products = menuService.getProductsByCategory(categoryId);
            int row = 0, col = 0;
            int maxCols = 2;
            
            for (Product product : products) {
                VBox productBox = createProductBox(product);
                menuGrid.add(productBox, col, row);
                
                col++;
                if (col >= maxCols) {
                    col = 0;
                    row++;
                }
            }
        } catch (SQLException e) {
            showAlert("Error", "Could not load menu items: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private VBox createProductBox(Product product) {
        VBox box = new VBox(10);
        box.getStyleClass().add("product-box");
        
        Label nameLabel = new Label(product.getName());
        nameLabel.getStyleClass().add("product-name");
        
        Label descLabel = new Label(product.getDescription());
        descLabel.getStyleClass().add("product-description");
        descLabel.setWrapText(true);
        
        Label priceLabel = new Label(String.format("$%.2f", product.getPrice()));
        priceLabel.getStyleClass().add("product-price");
        
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER);
        
        Label stockLabel = new Label(String.format("Stock: %d", product.getStock()));
        stockLabel.getStyleClass().add(product.getStock() > 0 ? "stock-available" : "stock-unavailable");
        
        Button addButton = new Button("Add to Cart");
        addButton.getStyleClass().add("add-to-cart-button");
        addButton.setDisable(product.getStock() <= 0);
        addButton.setOnAction(e -> addToCart(product));
        
        controlBox.getChildren().addAll(stockLabel, addButton);
        box.getChildren().addAll(nameLabel, descLabel, priceLabel, controlBox);
        
        return box;
    }

    private void addToCart(Product product) {
        int currentQuantity = cart.getOrDefault(product, 0);
        if (currentQuantity + 1 > product.getStock()) {
            showAlert("Stock Limit", 
                "Cannot add more. Only " + product.getStock() + " available.", 
                Alert.AlertType.WARNING);
            return;
        }
        
        cart.merge(product, 1, Integer::sum);
        updateCartDisplay();
    }

    private void adjustSelectedItemQuantity(Product product, int change) {
        int currentQuantity = cart.getOrDefault(product, 0);
        int newQuantity = currentQuantity + change;
        
        if (newQuantity > 0 && newQuantity <= product.getStock()) {
            cart.put(product, newQuantity);
        } else if (newQuantity <= 0) {
            cart.remove(product);
        }
        
        updateCartDisplay();
    }

    private void updateCartDisplay() {
        ObservableList<CartItem> items = FXCollections.observableArrayList();
        total = 0.0;
        
        for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
            CartItem item = new CartItem(entry.getKey(), entry.getValue());
            items.add(item);
            total += item.getTotalPrice();
        }
        
        cartList.setItems(items);
        totalLabel.setText(String.format("$%.2f", total));
        placeOrderButton.setDisable(cart.isEmpty());
    }

    @FXML
    private void placeOrder() {
        if (cart.isEmpty()) {
            showAlert("Error", "Your cart is empty!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Order");
        confirmAlert.setHeaderText("Place Order");
        confirmAlert.setContentText("Are you sure you want to place this order?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                    Product product = entry.getKey();
                    int requestedQuantity = entry.getValue();
                    
                    if (requestedQuantity > product.getStock()) {
                        showAlert("Stock Error", 
                            product.getName() + " has insufficient stock. " +
                            "Available: " + product.getStock(), 
                            Alert.AlertType.ERROR);
                        return;
                    }
                }

                Order order = new Order(currentUser.getId(), currentUser.getFullName());
                order.setTotalAmount(total);
                
                for (Map.Entry<Product, Integer> entry : cart.entrySet()) {
                    OrderItem item = new OrderItem(entry.getKey(), entry.getValue());
                    order.getItems().add(item);
                }
                
                orderService.createOrder(order);
                
                cart.clear();
                updateCartDisplay();
                
                showAlert("Success", "Order placed successfully!", Alert.AlertType.INFORMATION);
                
            } catch (SQLException e) {
                showAlert("Error", "Could not place order: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void showMyOrders() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/my_orders.fxml"));
            Parent root = loader.load();
            
            MyOrdersController controller = loader.getController();
            controller.initData(currentUser);
            
            Stage stage = new Stage();
            stage.setTitle("My Orders");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert("Error", "Could not load orders view: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Login");
        } catch (IOException e) {
            showAlert("Error", "Could not logout: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    public void initData(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getFullName() + "!");
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
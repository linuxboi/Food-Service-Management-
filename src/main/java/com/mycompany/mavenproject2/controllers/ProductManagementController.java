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
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import java.net.URL;
import java.util.*;
import java.sql.SQLException;

public class ProductManagementController implements Initializable {
    @FXML private TableView<Product> productsTable;
    @FXML private TableColumn<Product, Integer> idColumn;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, String> categoryColumn;
    @FXML private TableColumn<Product, String> descriptionColumn;
    @FXML private TableColumn<Product, Double> priceColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String> statusColumn;
    @FXML private ComboBox<Category> categoryFilter;
    @FXML private Label totalProductsLabel;
    @FXML private Label lowStockLabel;
    @FXML private Label outOfStockLabel;
    @FXML private Label totalValueLabel;
    
    private ProductService productService = new ProductService();
    private MenuService menuService = new MenuService();
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupCategoryFilter();
        loadProducts();
        updateDashboardStats();
    }
    
    private void setupTable() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categoryId"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("stock"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
    }
    
    private void setupCategoryFilter() {
        try {
            List<Category> categories = menuService.getAllCategories();
            categoryFilter.setItems(FXCollections.observableArrayList(categories));
            categoryFilter.setOnAction(e -> filterProducts());
        } catch (Exception e) {
            showAlert("Error", "Could not load categories: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void loadProducts() {
        try {
            List<Product> products = productService.getAllProducts();
            productsTable.setItems(FXCollections.observableArrayList(products));
            updateDashboardStats();
        } catch (Exception e) {
            showAlert("Error", "Could not load products: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    private void filterProducts() {
        Category selectedCategory = categoryFilter.getValue();
        if (selectedCategory == null) {
            loadProducts();
            return;
        }
        
        try {
            List<Product> products = productService.getProductsByCategory(selectedCategory.getId());
            productsTable.setItems(FXCollections.observableArrayList(products));
            updateDashboardStats();
        } catch (Exception e) {
            showAlert("Error", "Could not filter products: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void showAddProductDialog() {
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Add New Product");
        dialog.setHeaderText("Enter product details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField name = new TextField();
        ComboBox<Category> category = new ComboBox<>();
        TextArea description = new TextArea();
        TextField price = new TextField();
        TextField stock = new TextField();
        
        try {
            category.setItems(FXCollections.observableArrayList(menuService.getAllCategories()));
        } catch (Exception e) {
            showAlert("Error", "Could not load categories: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        
        description.setPrefRowCount(3);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(category, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(description, 1, 2);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(price, 1, 3);
        grid.add(new Label("Stock:"), 0, 4);
        grid.add(stock, 1, 4);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Product newProduct = new Product();
                    newProduct.setName(name.getText());
                    newProduct.setCategoryId(category.getValue().getId());
                    newProduct.setDescription(description.getText());
                    newProduct.setPrice(Double.parseDouble(price.getText()));
                    newProduct.setStock(Integer.parseInt(stock.getText()));
                    newProduct.setAvailable(true);
                    
                    productService.createProduct(newProduct);
                    loadProducts();
                    showAlert("Success", "Product created successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Could not create product: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    @FXML
    private void editProduct() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Error", "Please select a product to edit", Alert.AlertType.WARNING);
            return;
        }
        
        Dialog<Product> dialog = new Dialog<>();
        dialog.setTitle("Edit Product");
        dialog.setHeaderText("Edit product details");
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField name = new TextField(selectedProduct.getName());
        ComboBox<Category> category = new ComboBox<>();
        TextArea description = new TextArea(selectedProduct.getDescription());
        TextField price = new TextField(String.valueOf(selectedProduct.getPrice()));
        
        try {
            category.setItems(FXCollections.observableArrayList(menuService.getAllCategories()));
            category.getSelectionModel().select(
                category.getItems().stream()
                    .filter(c -> c.getId() == selectedProduct.getCategoryId())
                    .findFirst()
                    .orElse(null)
            );
        } catch (Exception e) {
            showAlert("Error", "Could not load categories: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        
        description.setPrefRowCount(3);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(category, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(description, 1, 2);
        grid.add(new Label("Price:"), 0, 3);
        grid.add(price, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    selectedProduct.setName(name.getText());
                    selectedProduct.setCategoryId(category.getValue().getId());
                    selectedProduct.setDescription(description.getText());
                    selectedProduct.setPrice(Double.parseDouble(price.getText()));
                    
                    productService.updateProduct(selectedProduct);
                    loadProducts();
                    showAlert("Success", "Product updated successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Could not update product: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    @FXML
    private void adjustStock() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Error", "Please select a product", Alert.AlertType.WARNING);
            return;
        }
        
        Dialog<Integer> dialog = new Dialog<>();
        dialog.setTitle("Adjust Stock");
        dialog.setHeaderText("Adjust stock for " + selectedProduct.getName());
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        Label currentStock = new Label("Current Stock: " + selectedProduct.getStock());
        TextField adjustment = new TextField();
        adjustment.setPromptText("Enter adjustment (+/-)");
        
        grid.add(currentStock, 0, 0);
        grid.add(new Label("Adjustment:"), 0, 1);
        grid.add(adjustment, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int adjustmentValue = Integer.parseInt(adjustment.getText());
                    int newStock = selectedProduct.getStock() + adjustmentValue;
                    
                    if (newStock < 0) {
                        showAlert("Error", "Stock cannot be negative", Alert.AlertType.ERROR);
                        return null;
                    }
                    
                    selectedProduct.setStock(newStock);
                    productService.updateProduct(selectedProduct);
                    loadProducts();
                    showAlert("Success", "Stock adjusted successfully", Alert.AlertType.INFORMATION);
                } catch (Exception e) {
                    showAlert("Error", "Could not adjust stock: " + e.getMessage(), Alert.AlertType.ERROR);
                }
            }
            return null;
        });
        
        dialog.showAndWait();
    }
    
    @FXML
    private void toggleProductStatus() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Error", "Please select a product", Alert.AlertType.WARNING);
            return;
        }
        
        try {
            selectedProduct.setAvailable(!selectedProduct.isAvailable());
            productService.updateProduct(selectedProduct);
            loadProducts();
            showAlert("Success", "Product status updated successfully", Alert.AlertType.INFORMATION);
        } catch (Exception e) {
            showAlert("Error", "Could not update product status: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    
    @FXML
    private void deleteProduct() {
        Product selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct == null) {
            showAlert("Error", "Please select a product", Alert.AlertType.WARNING);
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Product");
        alert.setHeaderText("Delete Product: " + selectedProduct.getName());
        alert.setContentText("Are you sure you want to delete this product? This action cannot be undone.");
        
        if (alert.showAndWait().get() == ButtonType.OK) {
            try {
                productService.deleteProduct(selectedProduct.getId());
                loadProducts();
                showAlert("Success", "Product deleted successfully", Alert.AlertType.INFORMATION);
            } catch (Exception e) {
                showAlert("Error", "Could not delete product: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }
    
    @FXML
    private void showCategoryManagement() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Category Management");
        dialog.setHeaderText("Manage Product Categories");
        
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButton);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        
        ListView<Category> categoryList = new ListView<>();
        Button addButton = new Button("Add Category");
        Button editButton = new Button("Edit Category");
        Button deleteButton = new Button("Delete Category");
        
        HBox buttonBox = new HBox(10, addButton, editButton, deleteButton);
        content.getChildren().addAll(categoryList, buttonBox);
        
        try {
            List<Category> categories = menuService.getAllCategories();
            categoryList.setItems(FXCollections.observableArrayList(categories));
        } catch (Exception e) {
            showAlert("Error", "Could not load categories: " + e.getMessage(), Alert.AlertType.ERROR);
        }
        
        addButton.setOnAction(e -> {
            TextInputDialog addDialog = new TextInputDialog();
            addDialog.setTitle("Add Category");
            addDialog.setHeaderText("Enter new category name");
            addDialog.setContentText("Name:");
            
            addDialog.showAndWait().ifPresent(name -> {
                try {
                    Category newCategory = new Category();
                    newCategory.setName(name);
                    menuService.addCategory(newCategory);
                    List<Category> updatedCategories = menuService.getAllCategories();
                    categoryList.setItems(FXCollections.observableArrayList(updatedCategories));
                    setupCategoryFilter(); // Refresh category filter
                } catch (Exception ex) {
                    showAlert("Error", "Could not add category: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            });
        });
        
        editButton.setOnAction(e -> {
            Category selectedCategory = categoryList.getSelectionModel().getSelectedItem();
            if(selectedCategory == null) {
                showAlert("Error", "Please select a category", Alert.AlertType.WARNING);
                return;
            }
            
            TextInputDialog editDialog = new TextInputDialog(selectedCategory.getName());
            editDialog.setTitle("Edit Category");
            editDialog.setHeaderText("Edit category name");
            editDialog.setContentText("Name:");
            
            editDialog.showAndWait().ifPresent(name -> {
                try {
                    selectedCategory.setName(name);
                    menuService.updateCategory(selectedCategory);
                    List<Category> updatedCategories = menuService.getAllCategories();
                    categoryList.setItems(FXCollections.observableArrayList(updatedCategories));
                    setupCategoryFilter(); // Refresh category filter
                } catch (Exception ex) {
                    showAlert("Error", "Could not update category: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            });
        });
        
        deleteButton.setOnAction(e -> {
            Category selectedCategory = categoryList.getSelectionModel().getSelectedItem();
            if (selectedCategory == null) {
                showAlert("Error", "Please select a category", Alert.AlertType.WARNING);
                return;
            }
            
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Delete Category");
            confirmAlert.setHeaderText("Delete Category: " + selectedCategory.getName());
            confirmAlert.setContentText("Are you sure? This will also delete all products in this category.");
            
            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                try {
                    menuService.deleteCategory(selectedCategory.getId());
                    List<Category> updatedCategories = menuService.getAllCategories();
                    categoryList.setItems(FXCollections.observableArrayList(updatedCategories));
                    setupCategoryFilter(); // Refresh category filter
                    loadProducts(); // Refresh products
                } catch (Exception ex) {
                    showAlert("Error", "Could not delete category: " + ex.getMessage(), Alert.AlertType.ERROR);
                }
            }
        });
        
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }
    
    private void updateDashboardStats() {
        try {
            List<Product> products = productsTable.getItems();
            int totalProducts = products.size();
            int lowStock = (int) products.stream().filter(p -> p.getStock() < 10 && p.getStock() > 0).count();
            int outOfStock = (int) products.stream().filter(p -> p.getStock() == 0).count();
            double totalValue = products.stream()
                .mapToDouble(p -> p.getPrice() * p.getStock())
                .sum();
            
            totalProductsLabel.setText("Total Products: " + totalProducts);
            lowStockLabel.setText("Low Stock Items: " + lowStock);
            outOfStockLabel.setText("Out of Stock: " + outOfStock);
            totalValueLabel.setText(String.format("Total Inventory Value: $%.2f", totalValue));
        } catch (Exception e) {
            showAlert("Error", "Could not update dashboard stats: " + e.getMessage(), Alert.AlertType.ERROR);
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
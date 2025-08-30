package com.mycompany.mavenproject2.models;

public class Product extends BaseEntity {
    private int id;
    private int categoryId;
    private String name;
    private String description;
    private double price;
    private int stock;
    private boolean available;
    private String imageUrl;
    
    public Product() {
        this.available = true;
    }
    
    public Product(int id, String name, String description, double price, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.available = true;
    }
    
    public Product(int id, int categoryId, String name, String description, double price, int stock, boolean available, String imageUrl) {
        this.id = id;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.available = available;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public int getStock() { return stock; }
    public void setStock(int stock) { 
        this.stock = stock;
        this.available = stock > 0;
    }
    
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    // Helper methods
    public boolean hasStock() {
        return stock > 0 && available;
    }
    
    public void decrementStock(int quantity) {
        if (quantity <= this.stock) {
            this.stock -= quantity;
            this.available = this.stock > 0;
        }
    }
    
    public String getFormattedPrice() {
        return String.format("$%.2f", price);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return id == product.id;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}
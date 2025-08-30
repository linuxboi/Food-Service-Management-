package com.mycompany.mavenproject2.models;

public class OrderItem {
    private Product product;
    private int quantity;
    private double itemTotal;

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        calculateItemTotal();
    }

    // Getters and setters
    public Product getProduct() { return product; }
    public void setProduct(Product product) { 
        this.product = product;
        calculateItemTotal();
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        calculateItemTotal();
    }

    public double getItemTotal() { return itemTotal; }

    private void calculateItemTotal() {
        if (product != null) {
            this.itemTotal = product.getPrice() * quantity;
        } else {
            this.itemTotal = 0.0;
        }
    }
}
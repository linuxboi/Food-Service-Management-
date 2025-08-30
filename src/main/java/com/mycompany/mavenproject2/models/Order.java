package com.mycompany.mavenproject2.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Order extends BaseEntity {
    private int id;
    private int userId;
    private String customerName;
    private double totalAmount;
    private String status;
    private Date createdAt;
    private List<OrderItem> items;

    public Order(int userId, String customerName) {
        this.userId = userId;
        this.customerName = customerName;
        this.status = "PENDING";
        this.createdAt = new Date();
        this.items = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    
    // Add this to your Order class
        private String notes;

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
}
package com.mycompany.mavenproject2.services;

import com.mycompany.mavenproject2.models.*;
import com.mycompany.mavenproject2.utils.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderService implements BaseService<Order> {
    public void createOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (user_id, customer_name, total_amount, status, notes) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, order.getUserId());
            stmt.setString(2, order.getCustomerName());
            stmt.setDouble(3, order.getTotalAmount());
            stmt.setString(4, "PENDING");
            stmt.setString(5, order.getNotes());
            
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                order.setId(rs.getInt(1));
                saveOrderItems(conn, order);
            }
        }
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name as customer_name FROM orders o " +
                    "JOIN users u ON o.user_id = u.id ORDER BY o.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        }
        return orders;
    }

    public List<Order> getOrdersByUser(int userId) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name as customer_name FROM orders o " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE o.user_id = ? ORDER BY o.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        }
        return orders;
    }
    
    public List<Order> getOrdersByStatus(String status) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name as customer_name FROM orders o " +
                    "JOIN users u ON o.user_id = u.id WHERE o.status = ? ORDER BY o.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        }
        return orders;
    }
    
    public List<Order> getOrdersByDateRange(Timestamp startDate, Timestamp endDate) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name as customer_name FROM orders o " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE o.created_at BETWEEN ? AND ? ORDER BY o.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, startDate);
            stmt.setTimestamp(2, endDate);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        }
        return orders;
    }
    
    public List<Order> getOrdersByStatusAndDateRange(String status, Timestamp startDate, Timestamp endDate) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT o.*, u.full_name as customer_name FROM orders o " +
                    "JOIN users u ON o.user_id = u.id " +
                    "WHERE o.status = ? AND o.created_at BETWEEN ? AND ? ORDER BY o.created_at DESC";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setTimestamp(2, startDate);
            stmt.setTimestamp(3, endDate);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Order order = extractOrderFromResultSet(rs);
                loadOrderItems(order);
                orders.add(order);
            }
        }
        return orders;
    }
    
    

    public void updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
    }
    
    public Map<String, Integer> getOrderStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        String sql = "SELECT status, COUNT(*) as count FROM orders GROUP BY status";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            stats.put("total", 0);
            stats.put("pending", 0);
            stats.put("preparing", 0);
            stats.put("ready", 0);
            stats.put("delivered", 0);
            stats.put("cancelled", 0);
            
            while (rs.next()) {
                String status = rs.getString("status").toLowerCase();
                int count = rs.getInt("count");
                stats.put(status, count);
                stats.put("total", stats.get("total") + count);
            }
        }
        return stats;
    }
    
    public Map<String, Double> getRevenueStatistics() throws SQLException {
        Map<String, Double> stats = new HashMap<>();
        String sql = "SELECT SUM(total_amount) as total_revenue, " +
                    "AVG(total_amount) as avg_order_value " +
                    "FROM orders WHERE status != 'CANCELLED'";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                stats.put("totalRevenue", rs.getDouble("total_revenue"));
                stats.put("avgOrderValue", rs.getDouble("avg_order_value"));
            }
        }
        return stats;
    }
    
    private void saveOrderItems(Connection conn, Order order) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, item_total) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (OrderItem item : order.getItems()) {
                stmt.setInt(1, order.getId());
                stmt.setInt(2, item.getProduct().getId());
                stmt.setInt(3, item.getQuantity());
                stmt.setDouble(4, item.getProduct().getPrice() * item.getQuantity());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void loadOrderItems(Order order) throws SQLException {
        String sql = "SELECT oi.*, p.* FROM order_items oi " +
                    "JOIN products p ON oi.product_id = p.id " +
                    "WHERE oi.order_id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, order.getId());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("product_id"));
                product.setName(rs.getString("name"));
                product.setPrice(rs.getDouble("price"));
                
                OrderItem item = new OrderItem(product, rs.getInt("quantity"));
                order.getItems().add(item);
            }
        }
    }

    private Order extractOrderFromResultSet(ResultSet rs) throws SQLException {
        Order order = new Order(
            rs.getInt("user_id"),
            rs.getString("customer_name")
        );
        order.setId(rs.getInt("id"));
        order.setTotalAmount(rs.getDouble("total_amount"));
        order.setStatus(rs.getString("status"));
        order.setCreatedAt(rs.getTimestamp("created_at"));
        
        try {
            order.setNotes(rs.getString("notes"));
        } catch (SQLException e) {
            order.setNotes("");
        }
        
        return order;
    }
    
    public void updateOrderNotes(int orderId, String notes) throws SQLException {
        String sql = "UPDATE orders SET notes = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, notes);
            stmt.setInt(2, orderId);
            stmt.executeUpdate();
        }
    }
}
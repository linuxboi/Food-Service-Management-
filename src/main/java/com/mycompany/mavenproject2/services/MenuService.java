package com.mycompany.mavenproject2.services;

import com.mycompany.mavenproject2.models.*;
import com.mycompany.mavenproject2.utils.DatabaseUtil;
import java.sql.*;
import java.util.*;

public class MenuService {
    
    public List<Category> getAllCategories() throws SQLException {
        List<Category> categories = new ArrayList<>();
        Set<String> categoryNames = new HashSet<>();  // To track unique categories
        String sql = "SELECT * FROM categories ORDER BY name";
        
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String categoryName = rs.getString("name");
                if (!categoryNames.contains(categoryName)) {  // Only add if not already present
                    Category category = new Category();
                    category.setId(rs.getInt("id"));
                    category.setName(categoryName);
                    categories.add(category);
                    categoryNames.add(categoryName);
                }
            }
        }
        return categories;
    }
    
    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ? AND available = true ORDER BY name";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, categoryId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                products.add(extractProductFromResultSet(rs));
            }
        }
        return products;
    }
    
    // Method to add a new category
    public Category addCategory(Category category) throws SQLException {
        String sql = "INSERT INTO categories (name) VALUES (?)";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, category.getName());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating category failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    category.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating category failed, no ID obtained.");
                }
            }
            
            return category;
        }
    }
    
    // Method to update an existing category
    public void updateCategory(Category category) throws SQLException {
        String sql = "UPDATE categories SET name = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.getName());
            stmt.setInt(2, category.getId());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Updating category failed, no rows affected.");
            }
        }
    }
    
    // Method to delete a category and its associated products
    public void deleteCategory(int categoryId) throws SQLException {
        // First, delete all products in this category
        String deleteProductsSql = "DELETE FROM products WHERE category_id = ?";
        
        // Then, delete the category
        String deleteCategorySql = "DELETE FROM categories WHERE id = ?";
        
        Connection conn = null;
        try {
            conn = DatabaseUtil.getConnection();
            // Disable auto-commit to ensure both operations succeed or fail together
            conn.setAutoCommit(false);
            
            // Delete products first
            try (PreparedStatement stmt = conn.prepareStatement(deleteProductsSql)) {
                stmt.setInt(1, categoryId);
                stmt.executeUpdate();
            }
            
            // Delete category
            try (PreparedStatement stmt = conn.prepareStatement(deleteCategorySql)) {
                stmt.setInt(1, categoryId);
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Deleting category failed, no rows affected.");
                }
            }
            
            // Commit the transaction
            conn.commit();
        } catch (SQLException e) {
            // Rollback in case of error
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Error rolling back transaction", ex);
                }
            }
            throw e;
        } finally {
            // Restore auto-commit to default
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    // Log or handle connection close error
                }
            }
        }
    }
    
    public boolean updateProductStock(Product product) throws SQLException {
        String sql = "UPDATE products SET stock = ?, available = ? WHERE id = ?";
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, product.getStock());
            stmt.setBoolean(2, product.getStock() > 0);  // Update availability based on stock
            stmt.setInt(3, product.getId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setName(rs.getString("name"));
        
        try {
            product.setDescription(rs.getString("description"));
        } catch (SQLException e) {
            product.setDescription("");
        }
        
        product.setPrice(rs.getDouble("price"));
        product.setStock(rs.getInt("stock"));
        product.setAvailable(rs.getBoolean("available"));
        return product;
    }
}
package com.mycompany.mavenproject2.services;

import com.mycompany.mavenproject2.models.Product;
import com.mycompany.mavenproject2.utils.DatabaseUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductService {

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products";

        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Product product = extractProductFromResultSet(rs);
                products.add(product);
            }
        }
        return products;
    }

    public List<Product> getProductsByCategory(int categoryId) throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products WHERE category_id = ? AND available = true";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, categoryId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Product product = extractProductFromResultSet(rs);
                    products.add(product);
                }
            }
        }
        return products;
    }

    public void createProduct(Product product) throws SQLException {
        String sql = "INSERT INTO products (name, category_id, description, price, stock, available) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, product.getName());
            stmt.setInt(2, product.getCategoryId());
            stmt.setString(3, product.getDescription());
            stmt.setDouble(4, product.getPrice());
            stmt.setInt(5, product.getStock());
            stmt.setBoolean(6, product.isAvailable());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating product failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    product.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating product failed, no ID obtained.");
                }
            }
        }
    }

    public void updateProduct(Product updatedProduct) throws SQLException {
        String sql = "UPDATE products SET name = ?, category_id = ?, description = ?, price = ?, stock = ?, available = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, updatedProduct.getName());
            stmt.setInt(2, updatedProduct.getCategoryId());
            stmt.setString(3, updatedProduct.getDescription());
            stmt.setDouble(4, updatedProduct.getPrice());
            stmt.setInt(5, updatedProduct.getStock());
            stmt.setBoolean(6, updatedProduct.isAvailable());
            stmt.setInt(7, updatedProduct.getId());

            stmt.executeUpdate();
        }
    }

    public void deleteProduct(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            stmt.executeUpdate();
        }
    }

    public boolean updateProductStock(int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock = ?, available = ? WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            int newStock = getProductById(productId).getStock() - quantity;
            stmt.setInt(1, newStock);
            stmt.setBoolean(2, newStock > 0);
            stmt.setInt(3, productId);

            return stmt.executeUpdate() > 0;
        }
    }

    public Product getProductById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";

        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractProductFromResultSet(rs);
                }
            }
        }
        return null;
    }

    private Product extractProductFromResultSet(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setCategoryId(rs.getInt("category_id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getDouble("price"));
        product.setStock(rs.getInt("stock"));
        product.setAvailable(rs.getBoolean("available"));
        return product;
    }
}
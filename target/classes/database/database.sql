-- Create the database
CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('CUSTOMER', 'ADMIN') NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    description TEXT
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category_id INT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    image_url VARCHAR(255),
    available BOOLEAN DEFAULT true,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Create orders table
CREATE TABLE IF NOT EXISTS orders (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT,
    status ENUM('PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'DELIVERED', 'CANCELLED') NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    delivery_address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Create order items table
CREATE TABLE IF NOT EXISTS order_items (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT,
    product_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- Insert sample data
-- Insert admin user (password: admin123)
INSERT INTO users (username, password, full_name, role) VALUES
('admin', 'admin123', 'System Administrator', 'ADMIN');

-- Insert categories
INSERT INTO categories (name, description) VALUES
('Burgers', 'Delicious handcrafted burgers'),
('Pizzas', 'Traditional Italian pizzas'),
('Drinks', 'Refreshing beverages'),
('Desserts', 'Sweet treats');

-- Insert products
INSERT INTO products (category_id, name, description, price) VALUES
(1, 'Classic Burger', 'Beef patty with lettuce, tomato, and cheese', 9.99),
(1, 'Chicken Burger', 'Grilled chicken with special sauce', 8.99),
(2, 'Margherita Pizza', 'Classic tomato and mozzarella', 12.99),
(2, 'Pepperoni Pizza', 'Spicy pepperoni with cheese', 14.99),
(3, 'Cola', 'Classic cola drink', 2.99),
(3, 'Water', 'Mineral water', 1.99),
(4, 'Chocolate Cake', 'Rich chocolate layer cake', 5.99);
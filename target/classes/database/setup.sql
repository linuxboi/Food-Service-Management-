-- Create database if it doesn't exist
CREATE DATABASE IF NOT EXISTS restaurant_db;
USE restaurant_db;

-- Drop existing tables if they exist
DROP TABLE IF EXISTS users;

-- Create users table
CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(50) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role ENUM('ADMIN', 'CUSTOMER') NOT NULL,
    phone VARCHAR(20),
    address TEXT
);

-- Insert admin account
INSERT INTO users (username, password, full_name, role) 
VALUES ('admin', 'admin', 'Administrator', 'ADMIN');
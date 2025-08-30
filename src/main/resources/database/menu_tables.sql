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
    available BOOLEAN DEFAULT true,
    FOREIGN KEY (category_id) REFERENCES categories(id)
);

-- Insert sample categories
INSERT INTO categories (name, description) VALUES
('Burgers', 'Delicious handcrafted burgers'),
('Pizzas', 'Traditional Italian pizzas'),
('Drinks', 'Refreshing beverages'),
('Desserts', 'Sweet treats');

-- Insert sample products
INSERT INTO products (category_id, name, description, price, available) VALUES
(1, 'Classic Burger', 'Beef patty with lettuce, tomato, and cheese', 9.99, true),
(1, 'Chicken Burger', 'Grilled chicken with special sauce', 8.99, true),
(1, 'Veggie Burger', 'Plant-based patty with fresh vegetables', 7.99, true),
(2, 'Margherita Pizza', 'Fresh tomatoes, mozzarella, and basil', 12.99, true),
(2, 'Pepperoni Pizza', 'Classic pepperoni with cheese', 14.99, true),
(2, 'Vegetarian Pizza', 'Assorted vegetables and cheese', 13.99, true),
(3, 'Cola', 'Classic cola drink', 2.99, true),
(3, 'Lemonade', 'Fresh squeezed lemonade', 3.99, true),
(3, 'Iced Tea', 'Homemade iced tea', 2.99, true),
(4, 'Chocolate Cake', 'Rich chocolate layer cake', 5.99, true),
(4, 'Ice Cream', 'Vanilla ice cream with toppings', 4.99, true),
(4, 'Apple Pie', 'Homemade apple pie', 5.99, true);
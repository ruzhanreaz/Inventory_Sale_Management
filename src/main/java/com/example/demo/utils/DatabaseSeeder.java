package com.example.demo.utils;

import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSeeder {

    public static void main(String[] args) {
        System.out.println("Attempting to connect to DB at: " + new java.io.File("pos.db").getAbsolutePath());
        try (
                Connection connection = DBConnector.getConnection();
                Statement statement = connection.createStatement()
        ) {

            // Drop existing tables
            statement.executeUpdate("DROP TABLE IF EXISTS order_items;");
            statement.executeUpdate("DROP TABLE IF EXISTS orders;");
            statement.executeUpdate("DROP TABLE IF EXISTS products;");
            statement.executeUpdate("DROP TABLE IF EXISTS users;");
            statement.executeUpdate("DROP TABLE IF EXISTS customers;");

            // Create tables
            statement.executeUpdate("""
                    CREATE TABLE users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(100) NOT NULL,
                        type VARCHAR(100) NOT NULL,
                        password VARCHAR(255) NOT NULL
                    );
                    """);

            statement.executeUpdate("""
                    CREATE TABLE customers (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name VARCHAR(100) NOT NULL,
                        email VARCHAR(100) NOT NULL,
                        phone VARCHAR(20) NOT NULL UNIQUE
                    );
                    """);

            statement.executeUpdate("""
                    CREATE TABLE products (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        category VARCHAR(100) NOT NULL,
                        name VARCHAR(100) NOT NULL,
                        buying_price REAL NOT NULL,
                        selling_price REAL NOT NULL,
                        quantity INT DEFAULT 0,
                        adding_date VARCHAR(50) NOT NULL,
                        expire_date VARCHAR(50)
                    );
                    """);

            statement.executeUpdate("""
                    CREATE TABLE orders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        customer_id INT NOT NULL,
                        user_id INT NOT NULL,
                        date DATETIME DEFAULT CURRENT_TIMESTAMP,
                        status VARCHAR(50) NOT NULL,
                        FOREIGN KEY (customer_id) REFERENCES customers (id),
                        FOREIGN KEY (user_id) REFERENCES users (id)
                    );
                    """);

            statement.executeUpdate("""
                    CREATE TABLE order_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        order_id INT NOT NULL,
                        product_id INT NOT NULL,
                        quantity INT NOT NULL,
                        price REAL NOT NULL,
                        FOREIGN KEY (order_id) REFERENCES orders (id),
                        FOREIGN KEY (product_id) REFERENCES products (id)
                    );
                    """);

            // Insert users with hashed password (let SQLite handle the id)
            String samplePass = BCrypt.hashpw("qwer1234", BCrypt.gensalt());
            statement.executeUpdate(String.format("""
                    INSERT INTO users (name, email, type, password) VALUES
                    ('Admin One', 'admin1@example.com', 'admin', '%s'),
                    ('Employee One', 'emp1@example.com', 'employee', '%s');
                    """, samplePass, samplePass));

            // Insert customers
            statement.executeUpdate("""
                    INSERT INTO customers (name, email, phone) VALUES
                    ('John Doe', 'john@gmail.com', '01712345678'),
                    ('Jane Smith', 'jane@gmail.com', '01887654321'),
                    ('Alice Johnson', 'alice@gmail.com', '01911223344'),
                    ('Bob Wilson', 'bob@gmail.com', '01555667788');
                    """);

            // Insert products
            statement.executeUpdate("""
                    INSERT INTO products (category, name, buying_price, selling_price, quantity, adding_date, expire_date) VALUES
                    ('Electronics', 'Product A', 10.00, 19.99, 2, '2023-09-01', '2024-09-01'),
                    ('Books', 'Product B', 5.00, 9.99, 4, '2023-09-02', NULL),
                    ('Clothing', 'Product C', 15.00, 29.99, 3, '2023-09-03', NULL),
                    ('Home', 'Product D', 8.00, 15.49, 5, '2023-09-04', NULL),
                    ('Toys', 'Product E', 25.00, 42.00, 1, '2023-09-05', '2025-09-05');
                    """);

            // Insert orders
            statement.executeUpdate("""
                    INSERT INTO orders (customer_id, user_id, date, status) VALUES
                    (1, 1, '2023-10-01 10:00:00', 'shipped'),
                    (2, 2, '2023-10-02 11:00:00', 'pending'),
                    (1, 1, '2023-10-03 15:30:00', 'delivered');
                    """);

            // Insert order_items
            statement.executeUpdate("""
                    INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
                    (1, 1, 2, 19.99),
                    (1, 2, 1, 9.99),
                    (2, 3, 3, 29.99),
                    (3, 4, 2, 15.49),
                    (3, 5, 1, 42.00);
                    """);

            // Insert more users
            String samplePass2 = BCrypt.hashpw("password123", BCrypt.gensalt());
            statement.executeUpdate(String.format("""
                    INSERT INTO users (name, email, type, password) VALUES
                    ('Admin Two', 'admin2@example.com', 'admin', '%s'),
                    ('Employee Two', 'emp2@example.com', 'employee', '%s'),
                    ('Employee Three', 'emp3@example.com', 'employee', '%s');
                    """, samplePass2, samplePass2, samplePass2));

            // Insert more customers
            statement.executeUpdate("""
                    INSERT INTO customers (name, email, phone) VALUES
                    ('Charlie Brown', 'charlie@gmail.com', '01799998888'),
                    ('Diana Prince', 'diana@gmail.com', '01888887777'),
                    ('Eve Adams', 'eve@gmail.com', '01922334455'),
                    ('Frank Castle', 'frank@gmail.com', '01611112222');
                    """);

            // Insert more products
            statement.executeUpdate("""
                    INSERT INTO products (category, name, buying_price, selling_price, quantity, adding_date, expire_date) VALUES
                    ('Electronics', 'Product F', 12.00, 22.99, 6, '2025-09-15', '2026-09-15'),
                    ('Books', 'Product G', 6.00, 12.99, 8, '2025-09-16', NULL),
                    ('Clothing', 'Product H', 18.00, 34.99, 10, '2025-09-17', NULL),
                    ('Home', 'Product I', 9.00, 17.49, 7, '2025-09-18', NULL),
                    ('Toys', 'Product J', 30.00, 50.00, 4, '2025-09-19', '2026-09-19'),
                    ('Electronics', 'Product K', 20.00, 35.00, 3, '2025-09-20', '2026-09-20'),
                    ('Books', 'Product L', 7.00, 13.99, 5, '2025-09-21', NULL);
                    """);

            // Insert more orders (including some in the current week)
            statement.executeUpdate("""
                    INSERT INTO orders (customer_id, user_id, date, status) VALUES
                    (3, 3, '2025-09-15 09:00:00', 'pending'),
                    (4, 4, '2025-09-16 14:00:00', 'shipped'),
                    (5, 2, '2025-09-18 16:30:00', 'delivered'),
                    (6, 5, '2025-09-19 12:45:00', 'pending'),
                    (7, 1, '2025-09-20 10:15:00', 'shipped'),
                    (8, 2, '2025-09-21 11:30:00', 'delivered');
                    """);

            // Insert more order_items
            statement.executeUpdate("""
                    INSERT INTO order_items (order_id, product_id, quantity, price) VALUES
                    (4, 6, 2, 22.99),
                    (4, 7, 1, 12.99),
                    (5, 8, 3, 34.99),
                    (6, 9, 2, 17.49),
                    (7, 10, 1, 50.00),
                    (8, 11, 2, 35.00),
                    (8, 12, 1, 13.99);
                    """);

            // Insert even more products for a richer experience
            statement.executeUpdate("""
                    INSERT INTO products (category, name, buying_price, selling_price, quantity, adding_date, expire_date) VALUES
                    ('Electronics', 'Smartphone X', 200.00, 350.00, 15, '2025-09-10', '2027-09-10'),
                    ('Electronics', 'Laptop Pro', 800.00, 1200.00, 8, '2025-09-11', '2028-09-11'),
                    ('Electronics', 'Bluetooth Headphones', 40.00, 75.00, 25, '2025-09-12', '2027-09-12'),
                    ('Books', 'Java Programming', 12.00, 25.00, 20, '2025-09-13', NULL),
                    ('Books', 'Data Structures', 10.00, 22.00, 18, '2025-09-14', NULL),
                    ('Clothing', 'Men T-Shirt', 7.00, 15.00, 30, '2025-09-15', NULL),
                    ('Clothing', 'Women Dress', 20.00, 45.00, 12, '2025-09-16', NULL),
                    ('Home', 'Blender', 18.00, 35.00, 10, '2025-09-17', '2027-09-17'),
                    ('Home', 'Microwave Oven', 60.00, 110.00, 6, '2025-09-18', '2028-09-18'),
                    ('Toys', 'Remote Car', 15.00, 29.99, 22, '2025-09-19', '2027-09-19'),
                    ('Toys', 'Puzzle Set', 5.00, 12.00, 40, '2025-09-20', NULL),
                    ('Electronics', 'Tablet Z', 120.00, 210.00, 10, '2025-09-21', '2027-09-21'),
                    ('Books', 'Machine Learning', 18.00, 35.00, 9, '2025-09-21', NULL),
                    ('Clothing', 'Kids Shorts', 6.00, 13.00, 25, '2025-09-21', NULL),
                    ('Home', 'Coffee Maker', 22.00, 40.00, 7, '2025-09-21', '2027-09-21');
                    """);

            System.out.println("Database setup completed successfully.");

        } catch (SQLException e) {
            System.err.println("SQLException: " + e.getMessage());
            e.printStackTrace(System.err);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace(System.err);
        }
    }
}
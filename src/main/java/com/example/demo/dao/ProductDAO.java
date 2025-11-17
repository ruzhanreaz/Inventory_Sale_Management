package com.example.demo.dao;

import com.example.demo.models.Product;
import com.example.demo.utils.DBConnector;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {
    public List<Product> getAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT id, category, name, buying_price, selling_price, quantity, adding_date, expire_date FROM products";
        try (Connection conn = DBConnector.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                LocalDate addingDate = null;
                LocalDate expireDate = null;
                String addingDateStr = rs.getString("adding_date");
                String expireDateStr = rs.getString("expire_date");
                try {
                    if (addingDateStr != null && !addingDateStr.isEmpty()) {
                        addingDate = LocalDate.parse(addingDateStr);
                    }
                } catch (Exception ex) {
                    System.out.println("[DEBUG] Failed to parse adding_date: " + addingDateStr + " for product id " + rs.getInt("id"));
                    ex.printStackTrace();
                }
                try {
                    if (expireDateStr != null && !expireDateStr.isEmpty()) {
                        expireDate = LocalDate.parse(expireDateStr);
                    }
                } catch (Exception ex) {
                    System.out.println("[DEBUG] Failed to parse expire_date: " + expireDateStr + " for product id " + rs.getInt("id"));
                    ex.printStackTrace();
                }
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getDouble("buying_price"),
                        rs.getDouble("selling_price"),
                        rs.getInt("quantity"),
                        addingDate,
                        expireDate
                );
                System.out.println("[DEBUG] Product row: id=" + product.getId() + ", name=" + product.getName() + ", category=" + product.getCategory());
                products.add(product);
            }
            System.out.println("[DEBUG] Products fetched: " + products.size());
            if (!products.isEmpty()) {
                System.out.println("[DEBUG] First product: " + products.get(0).getName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public void updateProductQuantity(int productId, int newQuantity) {
        String sql = "UPDATE products SET quantity = ? WHERE id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, newQuantity);
            pstmt.setInt(2, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (category, name, buying_price, selling_price, quantity, adding_date, expire_date) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getCategory());
            pstmt.setString(2, product.getName());
            pstmt.setDouble(3, product.getPurchasePrice());
            pstmt.setDouble(4, product.getSalePrice());
            pstmt.setInt(5, product.getQuantity());
            pstmt.setString(6, product.getDateAdded() != null ? product.getDateAdded().toString() : null);
            pstmt.setString(7, product.getExpiryDate() != null ? product.getExpiryDate().toString() : null);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, productId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateProduct(Product product) {
        String sql = "UPDATE products SET category = ?, name = ?, buying_price = ?, selling_price = ?, quantity = ?, adding_date = ?, expire_date = ? WHERE id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getCategory());
            pstmt.setString(2, product.getName());
            pstmt.setDouble(3, product.getPurchasePrice());
            pstmt.setDouble(4, product.getSalePrice());
            pstmt.setInt(5, product.getQuantity());
            pstmt.setString(6, product.getDateAdded() != null ? product.getDateAdded().toString() : null);
            pstmt.setString(7, product.getExpiryDate() != null ? product.getExpiryDate().toString() : null);
            pstmt.setInt(8, product.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Product findProductById(int id) {
        String sql = "SELECT id, category, name, buying_price, selling_price, quantity, adding_date, expire_date FROM products WHERE id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate addingDate = null;
                    LocalDate expireDate = null;
                    String addingDateStr = rs.getString("adding_date");
                    String expireDateStr = rs.getString("expire_date");
                    if (addingDateStr != null && !addingDateStr.isEmpty()) {
                        addingDate = LocalDate.parse(addingDateStr);
                    }
                    if (expireDateStr != null && !expireDateStr.isEmpty()) {
                        expireDate = LocalDate.parse(expireDateStr);
                    }
                    return new Product(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getDouble("buying_price"),
                        rs.getDouble("selling_price"),
                        rs.getInt("quantity"),
                        addingDate,
                        expireDate
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Product findProductByName(String name) {
        String sql = "SELECT id, category, name, buying_price, selling_price, quantity, adding_date, expire_date FROM products WHERE LOWER(name) = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name.toLowerCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    LocalDate addingDate = null;
                    LocalDate expireDate = null;
                    String addingDateStr = rs.getString("adding_date");
                    String expireDateStr = rs.getString("expire_date");
                    if (addingDateStr != null && !addingDateStr.isEmpty()) {
                        addingDate = LocalDate.parse(addingDateStr);
                    }
                    if (expireDateStr != null && !expireDateStr.isEmpty()) {
                        expireDate = LocalDate.parse(expireDateStr);
                    }
                    return new Product(
                        rs.getInt("id"),
                        rs.getString("category"),
                        rs.getString("name"),
                        rs.getDouble("buying_price"),
                        rs.getDouble("selling_price"),
                        rs.getInt("quantity"),
                        addingDate,
                        expireDate
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

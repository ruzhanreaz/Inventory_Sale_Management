package com.example.demo.dao;

import com.example.demo.models.SaleReportRow;
import com.example.demo.utils.DBConnector;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderDAO {

    public int createOrder(int customerId, int userId, Map<Integer, Integer> items) throws SQLException {
        System.out.println("[DEBUG] OrderDAO.createOrder called with customerId=" + customerId + ", userId=" + userId + ", items=" + items);
        if (items == null || items.isEmpty()) return -1;
        String insertOrderSql = "INSERT INTO orders (customer_id, user_id, status) VALUES (?, ?, ?)";
        String selectProdSql = "SELECT selling_price, quantity FROM products WHERE id = ?";
        String insertItemSql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        String updateStockSql = "UPDATE products SET quantity = quantity - ? WHERE id = ?";
        try (Connection conn = DBConnector.getConnection()) {
            boolean oldAuto = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try (
                PreparedStatement insertOrder = conn.prepareStatement(insertOrderSql, Statement.RETURN_GENERATED_KEYS);
                PreparedStatement selectProd = conn.prepareStatement(selectProdSql);
                PreparedStatement insertItem = conn.prepareStatement(insertItemSql);
                PreparedStatement updateStock = conn.prepareStatement(updateStockSql)
            ) {
                // Create order
                insertOrder.setInt(1, customerId);
                insertOrder.setInt(2, userId);
                insertOrder.setString(3, "pending");
                int affected = insertOrder.executeUpdate();
                System.out.println("[DEBUG] Inserted order, affected rows: " + affected);
                if (affected == 0) throw new SQLException("Creating order failed, no rows affected.");
                int orderId;
                try (ResultSet keys = insertOrder.getGeneratedKeys()) {
                    if (keys.next()) orderId = keys.getInt(1);
                    else throw new SQLException("Creating order failed, no ID obtained.");
                }
                System.out.println("[DEBUG] New orderId: " + orderId);
                // Validate stock and insert items
                for (Map.Entry<Integer, Integer> e : items.entrySet()) {
                    int productId = e.getKey();
                    int qty = e.getValue();
                    if (qty <= 0) continue;
                    selectProd.setInt(1, productId);
                    try (ResultSet rs = selectProd.executeQuery()) {
                        if (!rs.next()) throw new SQLException("Product not found: id=" + productId);
                        double price = rs.getDouble("selling_price");
                        int stock = rs.getInt("quantity");
                        if (stock < qty) {
                            throw new IllegalStateException("Insufficient stock for product " + productId + ". Available: " + stock + ", requested: " + qty);
                        }
                        insertItem.setInt(1, orderId);
                        insertItem.setInt(2, productId);
                        insertItem.setInt(3, qty);
                        insertItem.setDouble(4, price);
                        insertItem.addBatch();
                        updateStock.setInt(1, qty);
                        updateStock.setInt(2, productId);
                        updateStock.addBatch();
                        System.out.println("[DEBUG] Prepared order_item and stock update for productId=" + productId + ", qty=" + qty);
                    }
                }
                insertItem.executeBatch();
                updateStock.executeBatch();
                conn.commit();
                conn.setAutoCommit(oldAuto);
                System.out.println("[DEBUG] Order and items committed to DB.");
                return orderId;
            } catch (Exception e) {
                conn.rollback();
                System.err.println("[DEBUG] Exception in createOrder, transaction rolled back.");
                e.printStackTrace();
                throw e;
            }
        }
    }

    public List<SaleReportRow> getSalesReportRows() {
        List<SaleReportRow> reportRows = new ArrayList<>();
        String sql = "SELECT p.name AS product, p.category AS category, oi.quantity, oi.price AS selling_price, p.buying_price, o.date AS date " +
                "FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id " +
                "JOIN orders o ON oi.order_id = o.id ";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String product = rs.getString("product");
                String category = "";
                try { category = rs.getString("category"); } catch (Exception ignored) {}
                int quantity = rs.getInt("quantity");
                double sellingPrice = rs.getDouble("selling_price");
                double costPrice = rs.getDouble("buying_price");
                double profit = (sellingPrice - costPrice) * quantity;
                LocalDate date = null;
                try {
                    date = rs.getDate("date").toLocalDate();
                } catch (Exception e) { /* fallback to null */ }
                reportRows.add(new SaleReportRow(product, category, quantity, profit, date));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reportRows;
    }

    public boolean deleteSale(int saleId) {
        String deleteOrderItemsSql = "DELETE FROM order_items WHERE order_id = ?";
        String deleteOrderSql = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = DBConnector.getConnection();
             PreparedStatement deleteOrderItems = conn.prepareStatement(deleteOrderItemsSql);
             PreparedStatement deleteOrder = conn.prepareStatement(deleteOrderSql)) {
            conn.setAutoCommit(false);
            deleteOrderItems.setInt(1, saleId);
            deleteOrderItems.executeUpdate();
            deleteOrder.setInt(1, saleId);
            int affected = deleteOrder.executeUpdate();
            conn.commit();
            return affected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

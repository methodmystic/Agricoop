package com.agricoop.dao;

import com.agricoop.config.DatabaseConfig;
import com.agricoop.model.Order;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure JDBC Data Access Object for Orders.
 */
public class OrderDAO {

    public boolean placeOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (user_email, product_name, category, price, farmer_name, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, order.getUserEmail());
            stmt.setString(2, order.getProductName());
            stmt.setString(3, order.getCategory());
            stmt.setDouble(4, order.getPrice());
            stmt.setString(5, order.getFarmerName());
            stmt.setString(6, order.getStatus() != null ? order.getStatus() : "Processing");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        order.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public List<Order> getOrdersByUserEmail(String email) throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE user_email = ? ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Order o = new Order(
                        rs.getInt("id"),
                        rs.getString("user_email"),
                        rs.getString("product_name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("farmer_name"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    );
                    orders.add(o);
                }
            }
        }
        return orders;
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Order o = new Order(
                    rs.getInt("id"),
                    rs.getString("user_email"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getString("farmer_name"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                );
                orders.add(o);
            }
        }
        return orders;
    }

    public int countOrders() throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}

package com.agricoop.dao;

import com.agricoop.config.DatabaseConfig;
import com.agricoop.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure JDBC Data Access Object for Products in the AgriCoop marketplace.
 */
public class ProductDAO {

    public List<Product> getAllProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM products ORDER BY name ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("category"),
                    rs.getDouble("price"),
                    rs.getString("unit"),
                    rs.getInt("stock"),
                    rs.getTimestamp("created_at")
                );
                products.add(p);
            }
        }
        return products;
    }

    public Product getProductById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getDouble("price"),
                        rs.getString("unit"),
                        rs.getInt("stock"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }

    public boolean updateProduct(int id, double price, int stock) throws SQLException {
        String sql = "UPDATE products SET price = ?, stock = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, price);
            stmt.setInt(2, stock);
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        }
    }

    public int countProducts() throws SQLException {
        String sql = "SELECT COUNT(*) FROM products";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}

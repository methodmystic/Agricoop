package com.agricoop.dao;

import com.agricoop.config.DatabaseConfig;
import com.agricoop.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Pure JDBC Data Access Object for Users and Authentication.
 */
public class UserDAO {

    /**
     * Registers a new user with BCrypt hashed password using JDBC PreparedStatement.
     */
    public boolean registerUser(User user) throws SQLException {
        if (emailExists(user.getEmail())) {
            return false;
        }

        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(10));
        String sql = "INSERT INTO users (full_name, email, password, role) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, user.getRole() != null ? user.getRole() : "farmer");

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Authenticates a user by email and password using JDBC.
     */
    public User authenticate(String email, String rawPassword) throws SQLException {
        String sql = "SELECT id, full_name, email, password, role, created_at FROM users WHERE email = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (storedHash != null && BCrypt.checkpw(rawPassword, storedHash)) {
                        return new User(
                            rs.getInt("id"),
                            rs.getString("full_name"),
                            rs.getString("email"),
                            null, // Do not expose hash
                            rs.getString("role"),
                            rs.getTimestamp("created_at")
                        );
                    }
                }
            }
        }
        return null;
    }

    /**
     * Checks if an email is already registered.
     */
    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Finds a user by ID using JDBC.
     */
    public User findById(int id) throws SQLException {
        String sql = "SELECT id, full_name, email, role, created_at FROM users WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getInt("id"),
                        rs.getString("full_name"),
                        rs.getString("email"),
                        null,
                        rs.getString("role"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }

    /**
     * Returns total user count using JDBC.
     */
    public int countUsers() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}

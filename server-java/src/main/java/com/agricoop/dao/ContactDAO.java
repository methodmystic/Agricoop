package com.agricoop.dao;

import com.agricoop.config.DatabaseConfig;
import com.agricoop.model.ContactMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure JDBC Data Access Object for Contact Messages.
 */
public class ContactDAO {

    public boolean saveMessage(ContactMessage msg) throws SQLException {
        String sql = "INSERT INTO contact_messages (name, email, subject, message) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, msg.getName());
            stmt.setString(2, msg.getEmail());
            stmt.setString(3, msg.getSubject());
            stmt.setString(4, msg.getMessage());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        msg.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    public List<ContactMessage> getAllMessages() throws SQLException {
        List<ContactMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM contact_messages ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ContactMessage m = new ContactMessage(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("subject"),
                    rs.getString("message"),
                    rs.getTimestamp("created_at")
                );
                list.add(m);
            }
        }
        return list;
    }

    public int countMessages() throws SQLException {
        String sql = "SELECT COUNT(*) FROM contact_messages";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }
}

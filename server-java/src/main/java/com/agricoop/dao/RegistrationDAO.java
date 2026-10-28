package com.agricoop.dao;

import com.agricoop.config.DatabaseConfig;
import com.agricoop.model.Registration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pure JDBC Data Access Object for Farm Registrations (replacing Firebase Firestore).
 */
public class RegistrationDAO {

    /**
     * Inserts a farm registration record into MySQL using JDBC.
     */
    public boolean saveRegistration(Registration reg) throws SQLException {
        String sql = "INSERT INTO registrations (first_name, last_name, farm_location, crop_type, email, phone, farm_size, user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, reg.getFirstName());
            stmt.setString(2, reg.getLastName());
            stmt.setString(3, reg.getFarmLocation());
            stmt.setString(4, reg.getCropType());
            stmt.setString(5, reg.getEmail());
            stmt.setString(6, reg.getPhone());

            if (reg.getFarmSize() != null) {
                stmt.setDouble(7, reg.getFarmSize());
            } else {
                stmt.setNull(7, Types.DECIMAL);
            }

            if (reg.getUserId() != null) {
                stmt.setInt(8, reg.getUserId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        reg.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves all registrations ordered by created_at descending.
     */
    public List<Registration> getAllRegistrations() throws SQLException {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registrations ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Registration r = new Registration(
                    rs.getInt("id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("farm_location"),
                    rs.getString("crop_type"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getObject("farm_size") != null ? rs.getDouble("farm_size") : null,
                    rs.getObject("user_id") != null ? rs.getInt("user_id") : null,
                    rs.getTimestamp("created_at")
                );
                list.add(r);
            }
        }
        return list;
    }

    /**
     * Counts total registered farms.
     */
    public int countRegistrations() throws SQLException {
        String sql = "SELECT COUNT(*) FROM registrations";
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Aggregates crop breakdown stats using SQL GROUP BY via JDBC.
     */
    public Map<String, Integer> getCropBreakdown() throws SQLException {
        Map<String, Integer> map = new LinkedHashMap<>();
        String sql = "SELECT crop_type, COUNT(*) as count FROM registrations GROUP BY crop_type ORDER BY count DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                map.put(rs.getString("crop_type"), rs.getInt("count"));
            }
        }
        return map;
    }
}

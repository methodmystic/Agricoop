package com.agricoop.dao;

import com.agricoop.config.DatabaseConfig;
import com.agricoop.model.Event;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Pure JDBC Data Access Object for AgriCoop Community Events.
 */
public class EventDAO {

    public List<Event> getAllEvents() throws SQLException {
        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM events ORDER BY date ASC";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Event e = new Event(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("date"),
                    rs.getString("location"),
                    rs.getString("type"),
                    rs.getString("description"),
                    rs.getTimestamp("created_at")
                );
                events.add(e);
            }
        }
        return events;
    }

    public boolean addEvent(Event event) throws SQLException {
        String sql = "INSERT INTO events (title, date, location, type, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, event.getTitle());
            stmt.setString(2, event.getDate());
            stmt.setString(3, event.getLocation());
            stmt.setString(4, event.getType());
            stmt.setString(5, event.getDescription());

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        event.setId(keys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }
}

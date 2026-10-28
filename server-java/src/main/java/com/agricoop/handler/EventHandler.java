package com.agricoop.handler;

import com.agricoop.dao.EventDAO;
import com.agricoop.model.Event;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Handler for Community Events using JDBC and MySQL.
 */
public class EventHandler implements HttpHandler {

    private final EventDAO eventDAO = new EventDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleGetEvents(exchange);
            } else if ("POST".equalsIgnoreCase(method)) {
                handleCreateEvent(exchange);
            } else {
                HttpUtils.sendErrorMessage(exchange, 405, "Method Not Allowed");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            HttpUtils.sendErrorMessage(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.sendErrorMessage(exchange, 400, "Request error: " + e.getMessage());
        }
    }

    private void handleGetEvents(HttpExchange exchange) throws IOException, SQLException {
        List<Event> events = eventDAO.getAllEvents();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", events);
        HttpUtils.sendJsonResponse(exchange, 200, resp);
    }

    private void handleCreateEvent(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();

        String title = json.has("title") ? json.get("title").getAsString().trim() : null;
        String date = json.has("date") ? json.get("date").getAsString().trim() : null;
        String location = json.has("location") ? json.get("location").getAsString().trim() : null;
        String type = json.has("type") ? json.get("type").getAsString().trim() : "Workshop";
        String description = json.has("description") ? json.get("description").getAsString().trim() : "";

        if (title == null || date == null || location == null) {
            HttpUtils.sendErrorMessage(exchange, 400, "Title, date, and location are required.");
            return;
        }

        Event event = new Event();
        event.setTitle(title);
        event.setDate(date);
        event.setLocation(location);
        event.setType(type);
        event.setDescription(description);

        boolean saved = eventDAO.addEvent(event);
        if (saved) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Event created successfully via JDBC!");
            resp.put("id", event.getId());
            HttpUtils.sendJsonResponse(exchange, 201, resp);
        } else {
            HttpUtils.sendErrorMessage(exchange, 500, "Failed to create event in MySQL.");
        }
    }
}

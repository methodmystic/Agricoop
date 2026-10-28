package com.agricoop.handler;

import com.agricoop.dao.ContactDAO;
import com.agricoop.model.ContactMessage;
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
 * HTTP Handler for Contact Messages using JDBC and MySQL.
 */
public class ContactHandler implements HttpHandler {

    private final ContactDAO contactDAO = new ContactDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        String method = exchange.getRequestMethod();

        try {
            if ("POST".equalsIgnoreCase(method)) {
                handleSendMessage(exchange);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleGetMessages(exchange);
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

    private void handleSendMessage(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();

        String name = json.has("name") ? json.get("name").getAsString().trim() : null;
        String email = json.has("email") ? json.get("email").getAsString().trim() : null;
        String subject = json.has("subject") && !json.get("subject").isJsonNull() ? json.get("subject").getAsString().trim() : "General Inquiry";
        String message = json.has("message") ? json.get("message").getAsString().trim() : null;

        if (name == null || name.isEmpty() || email == null || email.isEmpty() || message == null || message.isEmpty()) {
            HttpUtils.sendErrorMessage(exchange, 400, "Name, email, and message are required.");
            return;
        }

        ContactMessage msg = new ContactMessage();
        msg.setName(name);
        msg.setEmail(email);
        msg.setSubject(subject);
        msg.setMessage(message);

        boolean saved = contactDAO.saveMessage(msg);
        if (saved) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Message recorded successfully via JDBC!");
            resp.put("id", msg.getId());
            HttpUtils.sendJsonResponse(exchange, 201, resp);
        } else {
            HttpUtils.sendErrorMessage(exchange, 500, "Failed to record message in MySQL.");
        }
    }

    private void handleGetMessages(HttpExchange exchange) throws IOException, SQLException {
        List<ContactMessage> list = contactDAO.getAllMessages();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", list);
        HttpUtils.sendJsonResponse(exchange, 200, resp);
    }
}

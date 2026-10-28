package com.agricoop.handler;

import com.agricoop.dao.OrderDAO;
import com.agricoop.model.Order;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Handler for Marketplace Orders using JDBC and MySQL.
 */
public class OrderHandler implements HttpHandler {

    private final OrderDAO orderDAO = new OrderDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleGetOrders(exchange, path);
            } else if ("POST".equalsIgnoreCase(method)) {
                handleCreateOrder(exchange);
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

    private void handleGetOrders(HttpExchange exchange, String path) throws IOException, SQLException {
        String[] parts = path.split("/");
        // e.g. /api/orders/user@example.com
        if (parts.length > 3 && !parts[3].isEmpty()) {
            String email = URLDecoder.decode(parts[3], StandardCharsets.UTF_8);
            List<Order> userOrders = orderDAO.getOrdersByUserEmail(email);
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", userOrders);
            HttpUtils.sendJsonResponse(exchange, 200, resp);
            return;
        }

        // Get all orders
        List<Order> allOrders = orderDAO.getAllOrders();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", allOrders);
        HttpUtils.sendJsonResponse(exchange, 200, resp);
    }

    private void handleCreateOrder(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();

        String userEmail = json.has("user_email") ? json.get("user_email").getAsString() : null;
        String productName = json.has("product_name") ? json.get("product_name").getAsString() : null;
        String category = json.has("category") ? json.get("category").getAsString() : "General";
        Double price = json.has("price") ? json.get("price").getAsDouble() : null;
        String farmerName = json.has("farmer_name") && !json.get("farmer_name").isJsonNull()
                ? json.get("farmer_name").getAsString() : "AgriCoop Verified Farm";

        if (userEmail == null || productName == null || price == null) {
            HttpUtils.sendErrorMessage(exchange, 400, "Incomplete order data (user_email, product_name, price required).");
            return;
        }

        Order order = new Order();
        order.setUserEmail(userEmail);
        order.setProductName(productName);
        order.setCategory(category);
        order.setPrice(price);
        order.setFarmerName(farmerName);
        order.setStatus("Processing");

        boolean placed = orderDAO.placeOrder(order);
        if (placed) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Order placed successfully via JDBC!");
            resp.put("order_id", order.getId());
            HttpUtils.sendJsonResponse(exchange, 201, resp);
        } else {
            HttpUtils.sendErrorMessage(exchange, 500, "Failed to record order in MySQL.");
        }
    }
}

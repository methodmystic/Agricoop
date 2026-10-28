package com.agricoop.handler;

import com.agricoop.dao.ProductDAO;
import com.agricoop.model.Product;
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
 * HTTP Handler for Marketplace Products using JDBC and MySQL.
 */
public class ProductHandler implements HttpHandler {

    private final ProductDAO productDAO = new ProductDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("GET".equalsIgnoreCase(method)) {
                handleGetProducts(exchange, path);
            } else if ("PUT".equalsIgnoreCase(method)) {
                handleUpdateProduct(exchange, path);
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

    private void handleGetProducts(HttpExchange exchange, String path) throws IOException, SQLException {
        // Check if path contains an ID, e.g. /api/products/3
        String[] parts = path.split("/");
        if (parts.length > 3) {
            try {
                int id = Integer.parseInt(parts[3]);
                Product p = productDAO.getProductById(id);
                if (p != null) {
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("success", true);
                    resp.put("data", p);
                    HttpUtils.sendJsonResponse(exchange, 200, resp);
                } else {
                    HttpUtils.sendErrorMessage(exchange, 404, "Product not found");
                }
                return;
            } catch (NumberFormatException ignored) {}
        }

        List<Product> products = productDAO.getAllProducts();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", products);
        HttpUtils.sendJsonResponse(exchange, 200, resp);
    }

    private void handleUpdateProduct(HttpExchange exchange, String path) throws IOException, SQLException {
        String[] parts = path.split("/");
        if (parts.length <= 3) {
            HttpUtils.sendErrorMessage(exchange, 400, "Product ID required in URL path");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(parts[3]);
        } catch (NumberFormatException e) {
            HttpUtils.sendErrorMessage(exchange, 400, "Invalid product ID");
            return;
        }

        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();

        if (!json.has("price")) {
            HttpUtils.sendErrorMessage(exchange, 400, "Price is required");
            return;
        }

        double price = json.get("price").getAsDouble();
        int stock = json.has("stock") ? json.get("stock").getAsInt() : 100;

        boolean updated = productDAO.updateProduct(id, price, stock);
        if (updated) {
            HttpUtils.sendSuccessMessage(exchange, 200, "Product updated successfully");
        } else {
            HttpUtils.sendErrorMessage(exchange, 404, "Product not found");
        }
    }
}

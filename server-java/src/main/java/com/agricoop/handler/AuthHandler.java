package com.agricoop.handler;

import com.agricoop.dao.UserDAO;
import com.agricoop.model.User;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Handler for Authentication using JDBC and MySQL (replacing Firebase Auth).
 */
public class AuthHandler implements HttpHandler {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        String path = exchange.getRequestURI().getPath();
        String method = exchange.getRequestMethod();

        try {
            if ("POST".equalsIgnoreCase(method) && path.endsWith("/signup")) {
                handleSignup(exchange);
            } else if ("POST".equalsIgnoreCase(method) && path.endsWith("/login")) {
                handleLogin(exchange);
            } else if ("GET".equalsIgnoreCase(method) && path.contains("/me")) {
                handleMe(exchange);
            } else {
                HttpUtils.sendErrorMessage(exchange, 404, "Endpoint not found: " + path);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            HttpUtils.sendErrorMessage(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.sendErrorMessage(exchange, 400, "Invalid request: " + e.getMessage());
        }
    }

    private void handleSignup(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();

        String fullName = json.has("full_name") ? json.get("full_name").getAsString().trim() : null;
        String email = json.has("email") ? json.get("email").getAsString().trim().toLowerCase() : null;
        String password = json.has("password") ? json.get("password").getAsString() : null;

        if (fullName == null || fullName.isEmpty() || email == null || email.isEmpty() || password == null || password.isEmpty()) {
            HttpUtils.sendErrorMessage(exchange, 400, "All fields (full_name, email, password) are required.");
            return;
        }

        if (password.length() < 6) {
            HttpUtils.sendErrorMessage(exchange, 400, "Password must be at least 6 characters.");
            return;
        }

        if (userDAO.emailExists(email)) {
            HttpUtils.sendErrorMessage(exchange, 409, "Email is already registered.");
            return;
        }

        User newUser = new User();
        newUser.setFullName(fullName);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole("farmer");

        boolean success = userDAO.registerUser(newUser);
        if (success) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Account created successfully via JDBC!");
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", newUser.getId());
            userData.put("full_name", newUser.getFullName());
            userData.put("email", newUser.getEmail());
            userData.put("role", newUser.getRole());
            resp.put("user", userData);

            HttpUtils.sendJsonResponse(exchange, 201, resp);
        } else {
            HttpUtils.sendErrorMessage(exchange, 500, "Failed to create user record.");
        }
    }

    private void handleLogin(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();

        String email = json.has("email") ? json.get("email").getAsString().trim().toLowerCase() : null;
        String password = json.has("password") ? json.get("password").getAsString() : null;

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            HttpUtils.sendErrorMessage(exchange, 400, "Email and password are required.");
            return;
        }

        User user = userDAO.authenticate(email, password);
        if (user == null) {
            HttpUtils.sendErrorMessage(exchange, 401, "Invalid email or password.");
            return;
        }

        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Login successful!");
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("full_name", user.getFullName());
        userData.put("email", user.getEmail());
        userData.put("role", user.getRole());
        resp.put("user", userData);

        HttpUtils.sendJsonResponse(exchange, 200, resp);
    }

    private void handleMe(HttpExchange exchange) throws IOException, SQLException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.contains("id=")) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("id=")) {
                    try {
                        int id = Integer.parseInt(param.substring(3));
                        User user = userDAO.findById(id);
                        if (user != null) {
                            Map<String, Object> resp = new HashMap<>();
                            resp.put("success", true);
                            resp.put("user", user);
                            HttpUtils.sendJsonResponse(exchange, 200, resp);
                            return;
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        HttpUtils.sendErrorMessage(exchange, 400, "User ID required (e.g. /api/auth/me?id=1)");
    }
}

package com.agricoop.handler;

import com.agricoop.dao.RegistrationDAO;
import com.agricoop.model.Registration;
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
 * HTTP Handler for Farm Registrations using JDBC and MySQL.
 */
public class RegistrationHandler implements HttpHandler {

    private final RegistrationDAO registrationDAO = new RegistrationDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        String method = exchange.getRequestMethod();

        try {
            if ("POST".equalsIgnoreCase(method)) {
                handleRegisterFarm(exchange);
            } else if ("GET".equalsIgnoreCase(method)) {
                handleGetRegistrations(exchange);
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

    private void handleRegisterFarm(HttpExchange exchange) throws IOException, SQLException {
        String body = HttpUtils.readRequestBody(exchange);
        JsonObject json = JsonParser.parseString(body).getAsJsonObject();

        String firstName = json.has("first_name") ? json.get("first_name").getAsString().trim() : null;
        String lastName = json.has("last_name") ? json.get("last_name").getAsString().trim() : null;
        String farmLocation = json.has("farm_location") ? json.get("farm_location").getAsString().trim() : null;
        String cropType = json.has("crop_type") ? json.get("crop_type").getAsString().trim() : null;
        String email = json.has("email") && !json.get("email").isJsonNull() ? json.get("email").getAsString().trim() : null;
        String phone = json.has("phone") && !json.get("phone").isJsonNull() ? json.get("phone").getAsString().trim() : null;
        Double farmSize = json.has("farm_size") && !json.get("farm_size").isJsonNull() && !json.get("farm_size").getAsString().isEmpty()
                ? json.get("farm_size").getAsDouble() : null;

        if (firstName == null || firstName.isEmpty() ||
            lastName == null || lastName.isEmpty() ||
            farmLocation == null || farmLocation.isEmpty() ||
            cropType == null || cropType.isEmpty()) {
            HttpUtils.sendErrorMessage(exchange, 400, "First name, last name, location, and crop type are required.");
            return;
        }

        Registration reg = new Registration();
        reg.setFirstName(firstName);
        reg.setLastName(lastName);
        reg.setFarmLocation(farmLocation);
        reg.setCropType(cropType);
        reg.setEmail(email);
        reg.setPhone(phone);
        reg.setFarmSize(farmSize);

        boolean saved = registrationDAO.saveRegistration(reg);
        if (saved) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Farm registered successfully in MySQL via JDBC!");
            resp.put("id", reg.getId());
            HttpUtils.sendJsonResponse(exchange, 201, resp);
        } else {
            HttpUtils.sendErrorMessage(exchange, 500, "Failed to register farm.");
        }
    }

    private void handleGetRegistrations(HttpExchange exchange) throws IOException, SQLException {
        List<Registration> list = registrationDAO.getAllRegistrations();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("data", list);
        HttpUtils.sendJsonResponse(exchange, 200, resp);
    }
}

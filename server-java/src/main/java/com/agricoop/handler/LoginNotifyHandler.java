package com.agricoop.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * HTTP Handler for Login Notification alerts.
 */
public class LoginNotifyHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendErrorMessage(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            String body = HttpUtils.readRequestBody(exchange);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();
            String email = json.has("email") ? json.get("email").getAsString() : "unknown";

            System.out.println("AgriCoop Login Notification received for: " + email);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("message", "Login notification processed for " + email);
            HttpUtils.sendJsonResponse(exchange, 200, resp);

        } catch (Exception e) {
            HttpUtils.sendErrorMessage(exchange, 400, "Invalid notification request");
        }
    }
}

package com.agricoop.handler;

import com.agricoop.dao.ContactDAO;
import com.agricoop.dao.ProductDAO;
import com.agricoop.dao.RegistrationDAO;
import com.agricoop.dao.UserDAO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP Handler for System Analytics and Dashboard Stats using JDBC.
 */
public class StatsHandler implements HttpHandler {

    private final RegistrationDAO registrationDAO = new RegistrationDAO();
    private final ContactDAO contactDAO = new ContactDAO();
    private final ProductDAO productDAO = new ProductDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (HttpUtils.handlePreflight(exchange)) return;

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            HttpUtils.sendErrorMessage(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            int totalFarms = registrationDAO.countRegistrations();
            int totalMessages = contactDAO.countMessages();
            int totalProducts = productDAO.countProducts();
            int totalUsers = userDAO.countUsers();
            Map<String, Integer> rawBreakdown = registrationDAO.getCropBreakdown();

            List<Map<String, Object>> cropBreakdownList = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : rawBreakdown.entrySet()) {
                Map<String, Object> item = new HashMap<>();
                item.put("crop_type", entry.getKey());
                item.put("count", entry.getValue());
                cropBreakdownList.add(item);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("total_farms", totalFarms);
            data.put("total_messages", totalMessages);
            data.put("total_products", totalProducts);
            data.put("total_users", totalUsers);
            data.put("crop_breakdown", cropBreakdownList);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("data", data);

            HttpUtils.sendJsonResponse(exchange, 200, resp);

        } catch (SQLException e) {
            e.printStackTrace();
            HttpUtils.sendErrorMessage(exchange, 500, "Database error: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.sendErrorMessage(exchange, 400, "Request error: " + e.getMessage());
        }
    }
}

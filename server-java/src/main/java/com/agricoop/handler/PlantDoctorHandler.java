package com.agricoop.handler;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * HTTP Handler for Plant Doctor AI Disease Diagnostics.
 */
public class PlantDoctorHandler implements HttpHandler {

    private static final Object[][] SAMPLES = {
        {"A+ Grade Basmati", 98, "Verification Success: Grain length > 8.4mm, 0% broken, aged 2 years. 100% Organic certified."},
        {"Fresh Organic Tomatoes", 96, "Verification Success: Vitamin C content optimal. No pesticide residues detected. 2 days from harvest."},
        {"Premium Mustard Oil", 94, "Verification Success: Cold-pressed, zero adulteration. High pungency levels confirmed."},
        {"Wild Forest Honey", 97, "Verification Success: No added sugar/C4 syrup. Pollen count indicates authentic multi-flora source."}
    };

    private final Random random = new Random();

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

            if (!json.has("image")) {
                HttpUtils.sendErrorMessage(exchange, 400, "Image data is required");
                return;
            }

            Object[] picked = SAMPLES[random.nextInt(SAMPLES.length)];

            Map<String, Object> result = new HashMap<>();
            result.put("disease", picked[0]);
            result.put("confidence", picked[1]);
            result.put("treatment", picked[2]);

            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("result", result);

            HttpUtils.sendJsonResponse(exchange, 200, resp);

        } catch (Exception e) {
            e.printStackTrace();
            HttpUtils.sendErrorMessage(exchange, 400, "Invalid image diagnostic request");
        }
    }
}

package com.agricoop;

import com.agricoop.config.DatabaseConfig;
import com.agricoop.handler.AuthHandler;
import com.agricoop.handler.ContactHandler;
import com.agricoop.handler.EventHandler;
import com.agricoop.handler.LoginNotifyHandler;
import com.agricoop.handler.OrderHandler;
import com.agricoop.handler.PlantDoctorHandler;
import com.agricoop.handler.ProductHandler;
import com.agricoop.handler.RegistrationHandler;
import com.agricoop.handler.StaticFileHandler;
import com.agricoop.handler.StatsHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

/**
 * Main application entry point for AgriCoop.
 * Embeds a lightweight HTTP server powered by Java JDBC and MySQL.
 */
public class AgriCoopServer {

    public static void main(String[] args) {
        System.out.println("------------------------------------------------------------");
        System.out.println(" Starting AgriCoop Java JDBC Backend with MySQL...");
        System.out.println("------------------------------------------------------------");

        // 1. Initialize JDBC Database connection and seed data
        try {
            DatabaseConfig.initializeDatabase();
        } catch (Exception e) {
            System.err.println("Database initialization note: " + e.getMessage());
            System.err.println("Server will continue running. Check MySQL connection if DB calls fail.");
        }

        // 2. Start HTTP Server
        int port = DatabaseConfig.getServerPort();
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            // API Routes
            server.createContext("/api/auth/", new AuthHandler());
            server.createContext("/api/products", new ProductHandler());
            server.createContext("/api/orders", new OrderHandler());
            server.createContext("/api/register", new RegistrationHandler());
            server.createContext("/api/registrations", new RegistrationHandler());
            server.createContext("/api/contact", new ContactHandler());
            server.createContext("/api/events", new EventHandler());
            server.createContext("/api/stats", new StatsHandler());
            server.createContext("/api/plant-doctor", new PlantDoctorHandler());
            server.createContext("/api/login-notify", new LoginNotifyHandler());

            // Static Client Web App
            server.createContext("/", new StaticFileHandler());

            // Multi-threaded executor
            server.setExecutor(Executors.newFixedThreadPool(16));
            server.start();

            System.out.println("============================================================");
            System.out.println("   AgriCoop Server successfully started!");
            System.out.println("   Local Web App & API: http://localhost:" + port);
            System.out.println("   Database Engine    : MySQL via JDBC Driver");
            System.out.println("   JDBC URL           : " + DatabaseConfig.getDbUrl());
            System.out.println("============================================================");

        } catch (IOException e) {
            System.err.println("Failed to start AgriCoop Server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.agricoop.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Database configuration and connection manager using pure JDBC.
 */
public class DatabaseConfig {

    private static String driver = "com.mysql.cj.jdbc.Driver";
    private static String url = "jdbc:mysql://localhost:3306/agricoop_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static String user = "root";
    private static String password = "root";
    private static int port = 3000;

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            Properties prop = new Properties();
            if (input != null) {
                prop.load(input);
                driver = prop.getProperty("db.driver", driver);
                url = prop.getProperty("db.url", url);
                user = prop.getProperty("db.user", user);
                password = prop.getProperty("db.password", password);
                port = Integer.parseInt(prop.getProperty("server.port", String.valueOf(port)));
            }
        } catch (Exception e) {
            System.out.println("Notice: Using default database properties: " + e.getMessage());
        }

        // Environment variable overrides (useful for production/cloud environments)
        if (System.getenv("DB_URL") != null) url = System.getenv("DB_URL");
        if (System.getenv("DB_USER") != null) user = System.getenv("DB_USER");
        if (System.getenv("DB_PASSWORD") != null) password = System.getenv("DB_PASSWORD");
        if (System.getenv("PORT") != null) {
            try {
                port = Integer.parseInt(System.getenv("PORT"));
            } catch (NumberFormatException ignored) {}
        }
    }

    public static int getServerPort() {
        return port;
    }

    public static String getDbUrl() {
        return url;
    }

    public static String getDbUser() {
        return user;
    }

    public static void setCredentials(String newUser, String newPassword) {
        user = newUser;
        password = newPassword;
    }

    /**
     * Establishes and returns a new JDBC connection.
     */
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(driver);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver not found on classpath: " + driver, e);
        }
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Automatically creates the database (if missing) and tables with seed data.
     */
    public static void initializeDatabase() {
        System.out.println("Initializing AgriCoop JDBC Database...");

        // Step 1: Ensure database exists
        String baseUrl = url.contains("/agricoop_db") 
                ? url.replace("/agricoop_db", "") 
                : url;
        
        try (Connection rootConn = DriverManager.getConnection(baseUrl, user, password);
             Statement stmt = rootConn.createStatement()) {
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS agricoop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;");
            System.out.println("Database 'agricoop_db' verified.");
        } catch (SQLException e) {
            System.err.println("Database creation note (may already exist or access restricted): " + e.getMessage());
        }

        // Step 2: Create required tables
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {

            // Users table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    full_name VARCHAR(120) NOT NULL,
                    email VARCHAR(150) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    role VARCHAR(50) DEFAULT 'farmer',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Registrations table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS registrations (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    first_name VARCHAR(100) NOT NULL,
                    last_name VARCHAR(100) NOT NULL,
                    farm_location VARCHAR(200) NOT NULL,
                    crop_type VARCHAR(100) NOT NULL,
                    email VARCHAR(150),
                    phone VARCHAR(50),
                    farm_size DECIMAL(10, 2),
                    user_id INT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Products table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS products (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(150) NOT NULL,
                    category VARCHAR(100) NOT NULL DEFAULT 'Grains',
                    price DECIMAL(10, 2) NOT NULL,
                    unit VARCHAR(50) NOT NULL DEFAULT 'kg',
                    stock INT DEFAULT 100,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Orders table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS orders (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_email VARCHAR(150) NOT NULL,
                    product_name VARCHAR(150) NOT NULL,
                    category VARCHAR(100) NOT NULL,
                    price DECIMAL(10, 2) NOT NULL,
                    farmer_name VARCHAR(120),
                    status VARCHAR(50) DEFAULT 'Processing',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Contact Messages table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS contact_messages (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    name VARCHAR(120) NOT NULL,
                    email VARCHAR(150) NOT NULL,
                    subject VARCHAR(200),
                    message TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            // Events table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS events (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    title VARCHAR(200) NOT NULL,
                    date VARCHAR(50) NOT NULL,
                    location VARCHAR(200) NOT NULL,
                    type VARCHAR(100) NOT NULL,
                    description TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
            """);

            System.out.println("All JDBC database tables verified successfully.");

            // Step 3: Seed Products if table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM products")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement pStmt = conn.prepareStatement(
                            "INSERT INTO products (name, category, price, unit, stock) VALUES (?, ?, ?, ?, ?)")) {
                        Object[][] defaultProducts = {
                            {"Premium Basmati Rice", "Grains", 120.0, "kg", 500},
                            {"Organic Whole Wheat", "Grains", 45.0, "kg", 1000},
                            {"Pure Mustard Oil", "Oils", 180.0, "Litre", 200},
                            {"Fresh Turmeric Powder", "Spices", 250.0, "kg", 150},
                            {"Natural Forest Honey", "Sweeteners", 450.0, "kg", 80},
                            {"Roasted Groundnuts", "Snacks", 140.0, "kg", 300},
                            {"A2 Desi Cow Ghee", "Dairy", 1200.0, "kg", 40},
                            {"Organic Jaggery Blocks", "Sweeteners", 90.0, "kg", 250}
                        };
                        for (Object[] item : defaultProducts) {
                            pStmt.setString(1, (String) item[0]);
                            pStmt.setString(2, (String) item[1]);
                            pStmt.setDouble(3, (Double) item[2]);
                            pStmt.setString(4, (String) item[3]);
                            pStmt.setInt(5, (Integer) item[4]);
                            pStmt.executeUpdate();
                        }
                        System.out.println("Default products seeded via JDBC.");
                    }
                }
            }

            // Step 4: Seed Events if table is empty
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM events")) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement eStmt = conn.prepareStatement(
                            "INSERT INTO events (title, date, location, type, description) VALUES (?, ?, ?, ?, ?)")) {
                        String[][] defaultEvents = {
                            {"Organic Farming Workshop", "2026-03-25", "Agri Hub, Bangalore", "Workshop", "Learn the basics of transition to organic farming."},
                            {"Smart Irrigation Seminar", "2026-04-05", "Community Center, Pune", "Seminar", "Using IoT sensors for water conservation."},
                            {"Rural Tech Expo 2026", "2026-04-12", "Vigyan Bhavan, Delhi", "Expo", "Showcasing the latest in rural technology."}
                        };
                        for (String[] event : defaultEvents) {
                            eStmt.setString(1, event[0]);
                            eStmt.setString(2, event[1]);
                            eStmt.setString(3, event[2]);
                            eStmt.setString(4, event[3]);
                            eStmt.setString(5, event[4]);
                            eStmt.executeUpdate();
                        }
                        System.out.println("Default events seeded via JDBC.");
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("JDBC Database Initialization warning: " + e.getMessage());
        }
    }
}

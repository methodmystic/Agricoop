-- ========================================================
-- AgriCoop Database Schema (MySQL)
-- Used with Java JDBC (Java Database Connectivity)
-- ========================================================

CREATE DATABASE IF NOT EXISTS agricoop_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agricoop_db;

-- 1. Users Table (Authentication)
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) DEFAULT 'farmer',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Farm Registrations Table
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

-- 3. Products Table (Marketplace)
CREATE TABLE IF NOT EXISTS products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    category VARCHAR(100) NOT NULL DEFAULT 'Grains',
    price DECIMAL(10, 2) NOT NULL,
    unit VARCHAR(50) NOT NULL DEFAULT 'kg',
    stock INT DEFAULT 100,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Orders Table
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

-- 5. Contact Messages
CREATE TABLE IF NOT EXISTS contact_messages (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    email VARCHAR(150) NOT NULL,
    subject VARCHAR(200),
    message TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Events Table
CREATE TABLE IF NOT EXISTS events (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    date VARCHAR(50) NOT NULL,
    location VARCHAR(200) NOT NULL,
    type VARCHAR(100) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ========================================================
-- Seed Initial Products & Events (if empty)
-- ========================================================

INSERT INTO products (name, category, price, unit, stock)
SELECT * FROM (
    SELECT 'Premium Basmati Rice' AS name, 'Grains' AS category, 120.00 AS price, 'kg' AS unit, 500 AS stock UNION ALL
    SELECT 'Organic Whole Wheat', 'Grains', 45.00, 'kg', 1000 UNION ALL
    SELECT 'Pure Mustard Oil', 'Oils', 180.00, 'Litre', 200 UNION ALL
    SELECT 'Fresh Turmeric Powder', 'Spices', 250.00, 'kg', 150 UNION ALL
    SELECT 'Natural Forest Honey', 'Sweeteners', 450.00, 'kg', 80 UNION ALL
    SELECT 'Roasted Groundnuts', 'Snacks', 140.00, 'kg', 300 UNION ALL
    SELECT 'A2 Desi Cow Ghee', 'Dairy', 1200.00, 'kg', 40 UNION ALL
    SELECT 'Organic Jaggery Blocks', 'Sweeteners', 90.00, 'kg', 250
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM products LIMIT 1);

INSERT INTO events (title, date, location, type, description)
SELECT * FROM (
    SELECT 'Organic Farming Workshop' AS title, '2026-03-25' AS date, 'Agri Hub, Bangalore' AS location, 'Workshop' AS type, 'Learn the basics of transition to organic farming.' AS description UNION ALL
    SELECT 'Smart Irrigation Seminar', '2026-04-05', 'Community Center, Pune', 'Seminar', 'Using IoT sensors for water conservation.' UNION ALL
    SELECT 'Rural Tech Expo 2026', '2026-04-12', 'Vigyan Bhavan, Delhi', 'Expo', 'Showcasing the latest in rural technology.'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM events LIMIT 1);

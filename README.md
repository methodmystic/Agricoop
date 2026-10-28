# 🌾 AgriCoop — Agricultural Platform (Java JDBC & MySQL)

AgriCoop is a comprehensive agricultural web platform that connects farming communities with fair markets, predictive agronomy tools, and direct commerce. It eliminates exploitative intermediaries by allowing farmers to list produce directly, receive orders, register farmlands, and leverage AI-assisted crop diagnostics.

---

## 🏗️ Architecture & Tech Stack

AgriCoop runs on a full-stack **Java JDBC + MySQL** architecture:

- **Backend**: Java 17, Embedded HTTP Server (`com.sun.net.httpserver`)
- **Database Connectivity**: Pure Java Database Connectivity (JDBC) using `com.mysql.cj.jdbc.Driver`
- **Database**: MySQL 8.x (`agricoop_db`)
- **Security & Hashing**: BCrypt (`jbcrypt`) for salted password hashing
- **Data Serialization**: Google Gson for fast JSON payload handling
- **Frontend**: Responsive Tailwind CSS UI, Vanilla JavaScript (`fetch` REST client)
- **Containerization & Deployment**: Docker, Docker Compose, Maven

```
┌────────────────────────────────────────────────────────┐
│                   Frontend (Web App)                   │
│   Tailwind CSS • Responsive HTML5 • Vanilla JavaScript  │
└───────────────────────────▲────────────────────────────┘
                            │ REST API (JSON)
┌───────────────────────────▼────────────────────────────┐
│              Java HTTP Server (Port 3000)              │
│  AuthHandler • ProductHandler • OrderHandler • etc.    │
└───────────────────────────▲────────────────────────────┘
                            │ Pure JDBC (PreparedStatement)
┌───────────────────────────▼────────────────────────────┐
│                    MySQL Database                      │
│   users • registrations • products • orders • events   │
└────────────────────────────────────────────────────────┘
```

---

## 🗄️ Database Schema (`database/schema.sql`)

The application database is named `agricoop_db` and includes 6 core relational tables:

1. **`users`**: Farmer & buyer authentication accounts with BCrypt-hashed passwords.
2. **`registrations`**: Farm profiles, GPS/village locations, crop varieties, acreage.
3. **`products`**: Marketplace items with prices, stock levels, categories, and units.
4. **`orders`**: Customer orders linked with product details, buyer email, and statuses.
5. **`contact_messages`**: Community feedback, farmer inquiries, support tickets.
6. **`events`**: Agronomy workshops, seminars, and rural expos.

---

## 🚀 Getting Started Locally

### Prerequisites
- **Java Development Kit (JDK 17+)**
- **Apache Maven 3.8+**
- **MySQL Server 8.x** running on port `3306`

### 1. Database Setup
Log in to your MySQL terminal or GUI (phpMyAdmin / MySQL Workbench):
```sql
SOURCE database/schema.sql;
```
*(Alternatively, the Java backend automatically runs `CREATE DATABASE IF NOT EXISTS` and creates/seeds all tables on startup).*

### 2. Configure Database Credentials
Edit `server-java/src/main/resources/db.properties` or set environment variables:
```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/agricoop_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root
server.port=3000
```

### 3. Run the Server
On Windows:
```cmd
run-server.bat
```

Or using Maven from terminal:
```bash
mvn -f server-java/pom.xml compile
java -cp "server-java/target/classes;server-java/lib/*" com.agricoop.AgriCoopServer
```

Visit the application in your browser:
👉 **`http://localhost:3000`**

---

## 🐳 Running with Docker & Docker Compose

Deploy the entire stack (MySQL 8 + Java JDBC App) with a single command:

```bash
docker compose up --build
```

- MySQL will initialize with `database/schema.sql` automatically.
- The Java application will wait for MySQL health check, compile, and bind to port `3000`.
- Access the web interface at **`http://localhost:3000`**.

---

## 📡 REST API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/signup` | Register a new user with BCrypt hashed password |
| `POST` | `/api/auth/login` | Authenticate user against MySQL database |
| `GET` | `/api/products` | Retrieve all marketplace products |
| `PUT` | `/api/products/{id}` | Update product pricing or inventory stock |
| `POST` | `/api/orders` | Place a new purchase order |
| `GET` | `/api/orders/{email}` | Retrieve orders for a specific user |
| `POST` | `/api/register` | Register a new farm & acreage |
| `GET` | `/api/registrations` | Fetch all registered farms |
| `POST` | `/api/contact` | Submit contact / support message |
| `GET` | `/api/events` | List upcoming agricultural workshops |
| `GET` | `/api/stats` | Aggregate dashboard statistics & crop breakdown |
| `POST` | `/api/plant-doctor` | AI crop disease diagnostics & verification |

---

## ☁️ Production Deployment Options

1. **Dockerized VPS (AWS EC2, DigitalOcean, Hetzner)**:
   Clone repo and run `docker compose up -d`. Use Nginx or Caddy as reverse proxy with SSL (`certbot`).
2. **Cloud Platforms (Railway / Render)**:
   - Provision a managed MySQL database instance.
   - Deploy `Dockerfile` with environment variables:
     - `PORT=3000`
     - `DB_URL=jdbc:mysql://<mysql-host>:<port>/<db>?useSSL=true`
     - `DB_USER=<user>`
     - `DB_PASSWORD=<password>`

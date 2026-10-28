# 🌾 AgriCoop — Next-Gen Agricultural Cooperative Platform

[![Production Deployment](https://img.shields.io/badge/Production-Live%20on%20Vercel-black?logo=vercel)](https://agricoop-market.vercel.app)
[![Live Mirror](https://img.shields.io/badge/Mirror-Netlify-00C7B7?logo=netlify&logoColor=white)](https://agricooop.netlify.app)
[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![MySQL 8.0](https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-38B2AC?logo=tailwind-css&logoColor=white)](https://tailwindcss.com/)

> 🌐 **Live Production App**: **[https://agricoop-market.vercel.app](https://agricoop-market.vercel.app)**  
> 🔗 **Alternative Live Mirror**: **[https://agricooop.netlify.app](https://agricooop.netlify.app)**

---

## 📖 Overview

**AgriCoop** is a farmer-first web platform connecting agricultural communities with direct markets, predictive agronomy tools, and fair-trade commerce. It eliminates exploitative intermediaries by allowing farmers to list produce directly, receive verified orders, register farmlands, and leverage AI-assisted crop disease diagnostics.

---

## 🌟 Key Features

* **Direct Digital Marketplace**: Eliminates middlemen by enabling farmers to list agricultural produce directly to buyers with transparent pricing.
* **AI Plant Doctor & Crop Diagnostics**: Instant leaf and crop condition screening to identify diseases and recommend organic or chemical interventions.
* **Farm Registry & Acreage Management**: Comprehensive farm profiling for agricultural compliance, resource allocation, and government subsidies.
* **Predictive Agronomy & Soil Intelligence**: Actionable insights on soil hydration, local weather trends, and optimal harvest windows.
* **Community & Knowledge Hub**: Educational workshops, smart irrigation seminars, and rural expos for empowered farming communities.

---

## 🏗️ Technology Stack

* **Frontend**: HTML5, Tailwind CSS, Responsive UI, Vanilla JavaScript
* **Backend**: Java 17, Multi-threaded HTTP Server
* **Database Connectivity**: Java Database Connectivity (JDBC)
* **Database**: MySQL Relational Database (`agricoop_db`)
* **Security**: Salted BCrypt password hashing & parameterized SQL execution
* **Cloud & Hosting**: Vercel & Docker containerization

---

## 🚀 Local Quickstart

### Prerequisites
* **Java Development Kit (JDK 17+)**
* **Apache Maven 3.8+**
* **MySQL Server 8.x**

### 1. Database Initialization
Import the initialization schema into MySQL:
```bash
mysql -u <username> -p < database/schema.sql
```
*(The backend also automatically verifies and initializes required tables on startup).*

### 2. Run the Application
On Windows, start the application using the launcher:
```cmd
run-server.bat
```

Or run via Maven from terminal:
```bash
mvn -f server-java/pom.xml compile
java -cp "server-java/target/classes;server-java/lib/*" com.agricoop.AgriCoopServer
```

Access the platform locally at:
👉 **`http://localhost:3000`**

---

## ⚡ Production Deployment

### 🌐 Live URLs
* **Primary (Vercel)**: [https://agricoop-market.vercel.app](https://agricoop-market.vercel.app)
* **Secondary (Netlify)**: [https://agricooop.netlify.app](https://agricooop.netlify.app)

### Deploying to Vercel
The project includes a production [`vercel.json`](./vercel.json) configured to publish the client app with clean URL routing.

1. Push your changes to GitHub (automatic CI/CD triggers on `main` branch).
2. Or deploy directly using the CLI:
```bash
npx vercel --prod
```
or run the included Windows script:
```cmd
deploy.bat
```

---

## 🐳 Docker Deployment

For containerized environments, run the entire stack using Docker Compose:

```bash
docker compose up --build -d
```

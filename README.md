# 🌾 AgriCoop — Next-Gen Agricultural Cooperative Platform

AgriCoop is a farmer-first web platform connecting agricultural communities with direct markets, predictive agronomy tools, and fair-trade commerce. It eliminates exploitative intermediaries by allowing farmers to list produce directly, receive verified orders, register farmlands, and leverage AI-assisted crop disease diagnostics.

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
* **Database**: MySQL Relational Database
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

Access the platform at:
👉 **`http://localhost:3000`**

---

## ⚡ Deployment on Vercel

The AgriCoop web application is configured for deployment on **Vercel**:

### Automatic Deployment
1. Connect your GitHub repository to [Vercel](https://vercel.com).
2. Set the Framework Preset to **Other** (configured via `vercel.json`).
3. Deploy directly from the `main` branch.

### Manual CLI Deployment
Deploy to production directly from your terminal:
```bash
npx vercel --prod
```
or run:
```cmd
deploy.bat
```

---

## 🐳 Docker Deployment

For containerized environments, run the entire stack using Docker Compose:

```bash
docker compose up --build -d
```

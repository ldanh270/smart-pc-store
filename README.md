<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Jakarta%20EE-6.0-1572B6?style=for-the-badge&logo=jakarta&logoColor=white" alt="Jakarta EE"/>
  <img src="https://img.shields.io/badge/Hibernate-6.4.4-59666C?style=for-the-badge&logo=hibernate&logoColor=white" alt="Hibernate"/>
  <img src="https://img.shields.io/badge/SQL%20Server-2019+-CC2927?style=for-the-badge&logo=microsoftsqlserver&logoColor=white" alt="SQL Server"/>
  <img src="https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
</p>

# 🖥️ Smart PC Store

> A modern, full-featured e-commerce web application for PC components and computer systems built
> with Java Servlet, Jakarta EE, and JPA/Hibernate.

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Technology Stack](#-technology-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
    - [Prerequisites](#prerequisites)
    - [Installation](#installation)
    - [Configuration](#configuration)
    - [Database Setup](#database-setup)
    - [Build & Deploy](#build--deploy)
- [API Reference](#-api-reference)
- [Security](#-security)
- [Testing](#-testing)
- [Contributing](#-contributing)
- [License](#-license)
- [Team](#-team)

---

## 🎯 Overview

**Smart PC Store** is a comprehensive e-commerce platform designed for buying and selling PC
components and computer systems. The application provides a robust backend infrastructure built on
Jakarta EE standards with a clean, layered architecture following industry best practices.

### Key Highlights

- 🔐 **Secure Authentication** — JWT-based access tokens with refresh token rotation
- 🗄️ **Robust Data Layer** — JPA/Hibernate ORM with Microsoft SQL Server
- 📦 **Inventory Management** — Real-time stock tracking with transaction history
- 📊 **Analytics Ready** — Built-in revenue tracking and demand/price forecasting models
- 🛒 **Complete E-commerce Flow** — From cart management to order processing and payments

---

## ✨ Features

### 🔐 Authentication & Authorization

- User registration with input validation
- Secure login with JWT access tokens
- Refresh token mechanism for session persistence
- Password hashing using BCrypt

### 🛍️ Product Management

- Product catalog with categories
- Supplier management with price history tracking
- Inventory tracking with transaction logs

### 🛒 Shopping Experience

- Shopping cart functionality
- Order placement and management
- Multiple payment method support
- Order status tracking

### 📈 Analytics & Forecasting

- Daily revenue aggregation
- Price forecasting models
- Demand prediction capabilities

### 👥 User Management

- User profile management
- Address and contact information
- Order history

---

## 🛠️ Technology Stack

| Category               | Technology              | Version     |
|------------------------|-------------------------|-------------|
| **Language**           | Java                    | 17          |
| **Framework**          | Jakarta Servlet API     | 6.0.0       |
| **ORM**                | Hibernate Core          | 6.4.4.Final |
| **Persistence**        | Jakarta Persistence API | 3.1.0       |
| **Database**           | Microsoft SQL Server    | 2019+       |
| **Build Tool**         | Apache Maven            | 3.x         |
| **Authentication**     | JJWT (JSON Web Token)   | 0.11.5      |
| **Password Hashing**   | jBCrypt                 | 0.4         |
| **JSON Processing**    | Google Gson             | 2.10.1      |
| **Environment Config** | dotenv-java             | 3.0.0       |
| **Testing**            | JUnit                   | 4.13.1      |
| **Server**             | Apache Tomcat           | 10.x+       |

---

## 🏗️ Architecture

The application follows a **layered architecture** pattern ensuring separation of concerns and
maintainability:

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT (Browser/API)                 │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                  PRESENTATION LAYER                     │
│  ┌───────────────┐    ┌───────────────┐                 │
│  │   Servlets    │◄──►│  Controllers  │                 │
│  │ (URL Routing) │    │ (Request      │                 │
│  │               │    │  Handling)    │                 │
│  └───────────────┘    └───────────────┘                 │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                   SERVICE LAYER                         │
│  ┌───────────────────────────────────────────────────┐  │
│  │               Business Logic                      │  │
│  │     • AuthService • ProductService • etc.         │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                DATA ACCESS LAYER (DAO)                  │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐  │
│  │ GenericDao  │  │  UserDao    │  │  SessionDao     │  │
│  │  (Base)     │  │             │  │                 │  │
│  └─────────────┘  └─────────────┘  └─────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│              PERSISTENCE LAYER (JPA/Hibernate)          │
│  ┌───────────────────────────────────────────────────┐  │
│  │             Entity Manager + Entities             │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────┐
│                    DATABASE LAYER                       │
│  ┌───────────────────────────────────────────────────┐  │
│  │              Microsoft SQL Server                 │  │
│  └───────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### Design Patterns Used

- **DAO Pattern** — Abstracts data access logic from business logic
- **DTO Pattern** — Data Transfer Objects for clean API contracts
- **Service Layer Pattern** — Encapsulates business logic
- **MVC Pattern** — Separation of Model, View, and Controller

---

## 📁 Project Structure

```
smart-pc-store/
├── 📂 src/
│   ├── 📂 main/
│   │   ├── 📂 java/
│   │   │   ├── 📂 configs/              # Application configurations
│   │   │   │   ├── DatabaseConfig.java  # Database connection settings
│   │   │   │   ├── JwtConfig.java       # JWT token configuration
│   │   │   │   ├── Regex.java           # Validation patterns
│   │   │   │   └── UrlConfig.java       # URL routing configuration
│   │   │   │
│   │   │   ├── 📂 controllers/          # Request handlers
│   │   │   │   └── AuthController.java  # Authentication controller
│   │   │   │
│   │   │   ├── 📂 dao/                  # Data Access Objects
│   │   │   │   ├── GenericDao.java      # Base DAO with CRUD operations
│   │   │   │   ├── JPAUtil.java         # JPA EntityManager utility
│   │   │   │   ├── SessionDao.java      # Session data access
│   │   │   │   └── UserDao.java         # User data access
│   │   │   │
│   │   │   ├── 📂 dto/                  # Data Transfer Objects
│   │   │   │   ├── 📂 auth/
│   │   │   │   │   ├── AuthResponse.java
│   │   │   │   │   ├── LoginDto.java
│   │   │   │   │   └── RegisterDto.java
│   │   │   │   ├── 📂 product/
│   │   │   │   └── 📂 user/
│   │   │   │       └── UserDto.java
│   │   │   │
│   │   │   ├── 📂 entities/             # JPA Entity classes
│   │   │   │   ├── Cart.java
│   │   │   │   ├── CartItem.java
│   │   │   │   ├── Category.java
│   │   │   │   ├── DemandForecast.java
│   │   │   │   ├── InventoryTransaction.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   ├── Payment.java
│   │   │   │   ├── PriceForecast.java
│   │   │   │   ├── Product.java
│   │   │   │   ├── PurchaseOrder.java
│   │   │   │   ├── PurchaseOrderItem.java
│   │   │   │   ├── RevenueDaily.java
│   │   │   │   ├── Session.java
│   │   │   │   ├── Supplier.java
│   │   │   │   ├── SupplierPriceHistory.java
│   │   │   │   └── User.java
│   │   │   │
│   │   │   ├── 📂 filters/              # Servlet filters
│   │   │   │
│   │   │   ├── 📂 services/             # Business logic layer
│   │   │   │   └── AuthService.java
│   │   │   │
│   │   │   ├── 📂 servlets/             # HTTP endpoint handlers
│   │   │   │   ├── AuthServlet.java     # /auth/* endpoints
│   │   │   │   └── DefaultServlet.java  # Default routes
│   │   │   │
│   │   │   └── 📂 utils/                # Utility classes
│   │   │       ├── EnvHelper.java       # Environment variable helper
│   │   │       ├── HttpUtil.java        # HTTP request/response utilities
│   │   │       ├── JwtUtil.java         # JWT token utilities
│   │   │       └── 📂 validate/
│   │   │           └── AuthValidate.java
│   │   │
│   │   ├── 📂 resources/
│   │   │   └── 📂 META-INF/
│   │   │       └── persistence.xml      # JPA configuration
│   │   │
│   │   └── 📂 webapp/
│   │       ├── 📂 META-INF/
│   │       ├── 📂 WEB-INF/
│   │       └── index.jsp
│   │
│   └── 📂 test/                         # Unit tests
│
├── 📂 plan/                             # Project planning files
│   ├── schema.sql                       # Database schema
│   ├── data.sql                         # Sample data
│   └── TasksList.drawio                 # Task diagram
│
├── .env                                 # Environment variables
├── .gitignore                           # Git ignore rules
├── pom.xml                              # Maven configuration
└── README.md                            # This file
```

---

## 🗄️ Database Schema

The application uses a comprehensive relational database schema designed for e-commerce operations:

### Entity Relationship Diagram

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    Users     │───┬───│   Sessions   │       │  Categories  │
└──────────────┘   │   └──────────────┘       └──────────────┘
       │           │                                 │
       │           │                                 │
       ▼           │                                 ▼
┌──────────────┐   │   ┌──────────────┐       ┌──────────────┐
│    Carts     │   │   │   Suppliers  │───────│   Products   │
└──────────────┘   │   └──────────────┘       └──────────────┘
       │           │          │                      │
       ▼           │          ▼                      │
┌──────────────┐   │   ┌────────────────────┐        │
│  CartItems   │   │   │SupplierPriceHistory│        │
└──────────────┘   │   └────────────────────┘        │
                   │                                 │
       ┌───────────┘                                 │
       │                                             │
       ▼                                             ▼
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│    Orders    │───────│  OrderItems  │───────│ InventoryTxn │
└──────────────┘       └──────────────┘       └──────────────┘
       │
       ▼
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   Payments   │       │ RevenueDaily │       │  Forecasts   │
└──────────────┘       └──────────────┘       └──────────────┘
```

### Core Tables

| Table                    | Description                                   |
|--------------------------|-----------------------------------------------|
| `Users`                  | User accounts with authentication credentials |
| `Sessions`               | Active user sessions with refresh tokens      |
| `Categories`             | Product categorization                        |
| `Products`               | Product catalog with pricing and inventory    |
| `Suppliers`              | Vendor information and lead times             |
| `SupplierPriceHistories` | Historical import pricing data                |
| `Carts`                  | User shopping carts                           |
| `CartItems`              | Items in shopping carts                       |
| `Orders`                 | Customer orders                               |
| `OrderItems`             | Line items in orders                          |
| `Payments`               | Payment transactions                          |
| `InventoryTransactions`  | Stock movement history                        |
| `PurchaseOrders`         | Supplier purchase orders                      |
| `PurchaseOrderItems`     | Items in purchase orders                      |
| `RevenueDaily`           | Daily revenue aggregations                    |
| `DemandForecasts`        | Product demand predictions                    |
| `PriceForecasts`         | Product price predictions                     |

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed on your system:

| Software                 | Version      | Download                                              |
|--------------------------|--------------|-------------------------------------------------------|
| **Java JDK**             | 17 or higher | [Download](https://adoptium.net/)                     |
| **Apache Maven**         | 3.8+         | [Download](https://maven.apache.org/download.cgi)     |
| **Microsoft SQL Server** | 2019+        | [Download](https://www.microsoft.com/sql-server)      |
| **Apache Tomcat**        | 10.1+        | [Download](https://tomcat.apache.org/download-10.cgi) |

### Installation

1. **Clone the Repository**

   ```bash
   git clone https://github.com/ldanh270/smart-pc-store.git
   cd smart-pc-store
   ```

2. **Install Dependencies**

   ```bash
   mvn clean install
   ```

### Configuration

1. **Environment Variables**

   Create a `.env` file in the project root:

   ```env
   # Authentication
   ACCESS_TOKEN_SECRET=your_secure_256_bit_hex_secret_key_here

   # Optional: Override default database settings
   # DB_URL=jdbc:sqlserver://localhost:1433;databaseName=SMART_PC_STORE
   # DB_USER=sa
   # DB_PASSWORD=your_password
   ```

   > ⚠️ **Important:** Generate a secure secret key for production. Use a 256-bit (64 hex
   > characters) random string.

2. **JPA Configuration**

   Update `src/main/resources/META-INF/persistence.xml` with your database credentials:

   ```xml
   <property name="jakarta.persistence.jdbc.url"
             value="jdbc:sqlserver://localhost:1433;databaseName=SMART_PC_STORE;encrypt=true;trustServerCertificate=true"/>
   <property name="jakarta.persistence.jdbc.user" value="your_username"/>
   <property name="jakarta.persistence.jdbc.password" value="your_password"/>
   ```

### Database Setup

1. **Connect to SQL Server** using your preferred client (SSMS, Azure Data Studio, etc.)

2. **Execute the Schema Script**

   Run the SQL script located at `plan/schema.sql` to create the database and all tables:

   ```sql
   -- This script creates the SMART_PC_STORE database and all required tables
   -- See plan/schema.sql for the complete script
   ```

3. **(Optional) Load Sample Data**

   ```sql
   -- Execute plan/data.sql for sample data
   ```

### Build & Deploy

1. **Build the WAR File**

   ```bash
   mvn clean package
   ```

   The WAR file will be generated at: `target/smart-pc-store.war`

2. **Deploy to Tomcat**
    - Copy `smart-pc-store.war` to your Tomcat's `webapps/` directory
    - Or use your IDE's server integration

3. **Access the Application**

   ```
   http://localhost:8080/smart-pc-store
   ```

---

## 📡 API Reference

### Base URL

```
http://localhost:8080/smart-pc-store
```

### Authentication Endpoints

| Method | Endpoint        | Description                      |
|--------|-----------------|----------------------------------|
| `POST` | `/auth/signup`  | Register a new user              |
| `POST` | `/auth/login`   | Authenticate user and get tokens |
| `POST` | `/auth/refresh` | Refresh access token (WIP)       |

#### Register User

```http
POST /auth/signup
Content-Type: application/json

{
  "username": "johndoe",
  "password": "SecurePass123!",
  "fullName": "John Doe",
  "email": "john.doe@example.com"
}
```

**Response (201 Created):**

```json
{
  "message": "Register successfully"
}
```

#### Login

```http
POST /auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**

```json
{
  "success": true,
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "user": {
    "id": 1,
    "username": "johndoe",
    "fullName": "John Doe",
    "email": "john.doe@example.com",
    "phone": null,
    "address": null,
    "status": "active"
  }
}
```

### Error Responses

| Status Code | Description                                |
|-------------|--------------------------------------------|
| `400`       | Bad Request - Invalid input or JSON format |
| `401`       | Unauthorized - Invalid credentials         |
| `404`       | Not Found - Endpoint not found             |
| `409`       | Conflict - User already exists             |
| `500`       | Internal Server Error                      |

---

## 🔐 Security

### Authentication Flow

```
┌────────────┐     ┌─────────────┐     ┌──────────────┐
│   Client   │────►│   Server    │────►│   Database   │
└────────────┘     └─────────────┘     └──────────────┘
      │                   │                    │
      │  1. POST /login   │                    │
      │───────────────────►                    │
      │                   │  2. Validate user  │
      │                   │────────────────────►
      │                   │                    │
      │                   │◄────────────────────
      │                   │  3. Create tokens  │
      │                   │                    │
      │◄───────────────────                    │
      │  4. Return tokens │                    │
      │                   │                    │
      │  5. API Request   │                    │
      │  + Bearer Token   │                    │
      │───────────────────►                    │
      │                   │  6. Validate JWT   │
      │                   │────────────────────►
      │                   │                    │
      │◄───────────────────                    │
      │  7. Response      │                    │
```

### Security Features

- **Password Hashing** — BCrypt with automatic salt generation
- **JWT Tokens** — HS256 signed access tokens with configurable expiration
- **Refresh Tokens** — UUID-based tokens stored in database with expiration
- **Input Validation** — Server-side validation for all user inputs
- **SQL Injection Prevention** — JPA/Hibernate parameterized queries

### Best Practices

- Store `ACCESS_TOKEN_SECRET` securely (environment variable)
- Use HTTPS in production
- Rotate refresh tokens on each use
- Set appropriate CORS policies
- Implement rate limiting for authentication endpoints

---

## 🧪 Testing

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=AuthServiceTest

# Generate test coverage report
mvn test jacoco:report
```

### Test Structure

```
src/test/
├── java/
│   ├── dao/
│   │   └── UserDaoTest.java
│   ├── services/
│   │   └── AuthServiceTest.java
│   └── utils/
│       └── JwtUtilTest.java
```

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the Repository**

2. **Create a Feature Branch**

   ```bash
   git checkout -b feature/amazing-feature
   ```

3. **Commit Your Changes**

   ```bash
   git commit -m 'Add amazing feature'
   ```

4. **Push to the Branch**

   ```bash
   git push origin feature/amazing-feature
   ```

5. **Open a Pull Request**

### Code Style Guidelines

- Follow Java naming conventions
- Write meaningful commit messages
- Include JavaDoc for public methods
- Write unit tests for new features
- Keep methods focused and small

---

## 📄 License

This project is developed for **educational purposes**. All rights reserved.

---

## 👥 Team

<table>
  <tr>
    <td align="center">
      <a href="https://github.com/ldanh270">
        <img src="https://github.com/ldanh270.png" width="100px;" alt="ldanh270"/><br />
        <sub><b>ldanh270</b></sub>
      </a><br />
      <sub>Developer</sub>
    </td>
  </tr>
</table>

---

<p align="center">
  <b>⭐ If you find this project useful, please give it a star! ⭐</b>
</p>

<p align="center">
  Made with ❤️ using Java & Jakarta EE
</p>

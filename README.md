<p align="center">
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 17"/>
  <img src="https://img.shields.io/badge/Jakarta%20EE-6.0-1572B6?style=for-the-badge&logo=jakarta&logoColor=white" alt="Jakarta EE"/>
  <img src="https://img.shields.io/badge/Hibernate-6.4.4-59666C?style=for-the-badge&logo=hibernate&logoColor=white" alt="Hibernate"/>
  <img src="https://img.shields.io/badge/SQL%20Server-2019+-CC2927?style=for-the-badge&logo=microsoftsqlserver&logoColor=white" alt="SQL Server"/>
  <img src="https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven"/>
</p>

# 🖥️ Smart PC Store API

> A modern, enterprise-grade RESTful API backend for a PC components and customized systems
> e-commerce platform. Built on Jakarta EE standards leveraging pure Java Servlets, JPA/Hibernate,
> and Microsoft SQL Server.

---

## 📖 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Technology Stack](#-technology-stack)
- [Software Architecture](#-software-architecture)
- [Project Structure](#-project-structure)
- [Database Schema](#-database-schema)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Security](#-security)
- [Testing](#-testing)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Overview

**Smart PC Store** provides a highly scalable and robust backend infrastructure to an advanced
e-commerce platform catered towards PC building enthusiasts. Incorporating an explicit layered
architecture, it separates routing, business logics, and data persistence clearly, making it fully
compliant with real-world industry standards.

### Core Strengths

- 🔐 **Enhanced Security** — Stateful refresh token rotation paired with stateless JWT access
  tokens, wrapped with BCrypt password encoding.
- 🗄️ **Robust Data Persistence** — Comprehensive JPA/Hibernate mappings representing advanced
  relationship types against a Microsoft SQL Server database.
- 🏢 **B2B & Inventory Engine** — Elaborate Supplier Management system supporting quotations,
  purchase orders, price histories, and strict transaction logging.
- 📊 **Business Intelligence** — Built-in supplier analytics, daily revenue aggregation, and
  forecasting endpoints to support business decisions.
- 🛒 **Full E-commerce Lifecycle** — Handles complex state changes from shopping cart modifications,
  checkout, and inventory decrementing to payment integrations.

---

## ✨ Key Features

### 🛡️ Authentication & Authorization

- Robust registration flows with payload validation.
- Secure standard login generating access and refresh tokens.
- Refresh token persistence preventing invalid token reuse.

### 🛍️ Client Shopping Experience

- Product catalogs fetching with categorization.
- Active cart manipulation tied tightly to user sessions.
- Clean checkout processes translating carts into fulfillable orders.
- Native payment processing module.

### 🏢 Supplier & Procurement Management

- Complete vendor profiles (`SupplierController`).
- Flexible quotation requests mapping supplier prices histories (`SupplierQuotationController`).
- Strict B2B purchase order (PO) generation to restock inventory (`PurchaseController`).
- Supplier performance tracking and business analytics (`SupplierAnalyticsController`).

### ⚙️ Administrative Controls

- Product, category, and user CRUD panels.
- Order dispute and status tracking.
- Precise ledger mechanisms via the `InventoryTransaction` table preventing dead stock issues.

---

## 🛠️ Technology Stack

| Category              | Technology / Library      | Version     | Purpose                                   |
| --------------------- | ------------------------- | ----------- | ----------------------------------------- |
| **Core Language**     | Java SE                   | 17          | Base language syntax                      |
| **Web Framework**     | Jakarta Servlet API       | 6.0.0       | HTTP Request/Response handling, Filters   |
| **ORM & Persistence** | Hibernate Core + JPA      | 6.4.4.Final | Database mapping and transaction handling |
| **Relational DB**     | Microsoft SQL Server      | 2019+       | Main data storage                         |
| **Build & Deploy**    | Apache Maven & Tomcat     | 3.x / 10.x+ | Dependency management and server runtime  |
| **Security (Auth)**   | JJWT (JSON Web Token)     | 0.11.5      | Auth tokenization                         |
| **Security (Crypto)** | jBCrypt                   | 0.4         | Hashing user passwords securely           |
| **Utilities**         | Google Gson & dotenv-java | Latest      | JSON parsing and env variable handling    |
| **Automated Testing** | JUnit                     | 4.13.1      | Assuring business logic layer stability   |

---

## 🏗️ Software Architecture

This application strictly implements an **N-Tier Architecture** emphasizing clear Domain-Driven
boundary limits:

```text
CLIENT REQUESTS -> 🌐 HTTP Routes (Servlets/Controllers)
                      ↓
BUSINESS RULES  -> 🧠 Service Layer (DTOs & Validation)
                      ↓
DATA ACCESS     -> 🗃️ DAO Layer (Generic Interfaces)
                      ↓
PERSISTENCE     -> 🛡️ JPA/Hibernate Entities
                      ↓
DATABASE        -> 💽 SQL Server
```

Key Design Patterns enforced across the codebase:

- **Singleton & Factories** through Custom `JPAUtil` handlers.
- **DAO Pattern** leveraging a powerful generic base class (`GenericDao`).
- **DTO Pattern** completely stripping presentation APIs from database internal entities formatting.

---

## 📁 Project Structure

```text
smart-pc-store/
├── 📂 src/main/java/
│   ├── 📂 configs/        # App-wide configurations (DB, JWT, Regex validations)
│   ├── 📂 controllers/    # Request dispatchers (Auth, Product, Orders, Suppliers, etc.)
│   ├── 📂 dao/            # Data Access Objects (CRUD base logics)
│   ├── 📂 dto/            # Data Transfer Objects tailored per entity
│   ├── 📂 entities/       # Hibernate mapped Java Models
│   ├── 📂 filters/        # Pre-execution Servlet Interceptors (CORS, Auth)
│   ├── 📂 services/       # Top-level transactional business logic computations
│   ├── 📂 servlets/       # Raw HTTP endpoint mapping definitions
│   └── 📂 utils/          # Encryption, environment, and String utilities
├── 📂 src/main/resources/
│   └── 📂 META-INF/
│       └── persistence.xml # Database persistence unit declarations
├── 📂 test/               # Unit testing directories
├── 📂 plan/               # Project database schemas and design documents
├── .env                   # Environment variables (Ignored by Git)
└── pom.xml                # Maven project object model
```

---

## 🗄️ Database Schema

The backbone relational database incorporates more than 15 optimized tables handling immense B2B/B2C
hybrid loads:

### Core Tables Snapshot

- **Core Entities:** `Users`, `Sessions`, `Categories`, `Products`
- **Shopping Flow:** `Carts`, `CartItems`, `Orders`, `OrderItems`, `Payments`
- **Supply Operations:** `Suppliers`, `SupplierPriceHistories`, `PurchaseOrders`,
  `PurchaseOrderItems`
- **Internal Logs & Stats:** `InventoryTransactions`, `RevenueDaily`, `DemandForecasts`,
  `PriceForecasts`

See the explicit schema definition within [plan/schema.sql](./plan/schema.sql).

---

## 🚀 Getting Started

Ensure you have installed:

- [Java JDK 17+](https://adoptium.net/)
- [Apache Maven 3.8+](https://maven.apache.org/download.cgi)
- [Microsoft SQL Server 2019+](https://www.microsoft.com/sql-server)
- [Apache Tomcat 10.1+](https://tomcat.apache.org/download-10.cgi)

### Installation & Setup

1. **Clone the Repository**

   ```bash
   git clone https://github.com/ldanh270/smart-pc-store.git
   cd smart-pc-store
   ```

2. **Configure Environment Variables** Create a `.env` file at the root folder:

   ```env
   # JWT Configuration
   ACCESS_TOKEN_SECRET=your_secure_256_bit_hex_secret_key_here
   ```

3. **Configure Database Connection** Update `src/main/resources/META-INF/persistence.xml` with your
   SQL server details if needed:

   ```xml
   <property name="jakarta.persistence.jdbc.url" value="jdbc:sqlserver://localhost:1433;databaseName=SMART_PC_STORE;encrypt=true;trustServerCertificate=true"/>
   <property name="jakarta.persistence.jdbc.user" value="sa"/>
   <property name="jakarta.persistence.jdbc.password" value="YourPasswordHere"/>
   ```

4. **Initialize the Database** Import and execute the `plan/schema.sql` directly inside your SQL
   Server instance, and optionally `plan/data.sql` to populate sample testing data.

5. **Build & Deploy**
   ```bash
   mvn clean package
   ```
   Deploy the `target/smart-pc-store.war` to your local Apache Tomcat's `webapps` folder and boot up
   the server.

---

## 📡 API Reference

Base REST URL format: `http://localhost:8080/smart-pc-store`

_(A detailed Postman / OpenAPI collection is heavily recommended for viewing complex endpoints.
Below is a subset.)_

### Core Endpoints

| Resource      | Methods                        | Purpose                                                          |
| ------------- | ------------------------------ | ---------------------------------------------------------------- |
| `/auth/*`     | `POST`                         | User sign up, login, and token refreshes.                        |
| `/products`   | `GET`, `POST`, `PUT`, `DELETE` | Managing the public system catalog and product stock details.    |
| `/categories` | `GET`, `POST`, `PUT`, `DELETE` | Hierarchical management for computer part typologies.            |
| `/cart`       | `GET`, `POST`, `DELETE`        | Managing active user shopping intentions.                        |
| `/orders`     | `GET`, `POST`, `PUT`           | Processing checkout actions and modifying fulfillment sequences. |
| `/suppliers`  | `GET`, `POST`, `PUT`, `DELETE` | Tracking B2B manufacturers and vendor relationships.             |
| `/purchases`  | `GET`, `POST`                  | Issuing Purchase Orders internally to replenish main inventory.  |
| `/analytics`  | `GET`                          | Generating data blocks mapping out supplier activities.          |

---

## 🔐 Security

This application features rigorous enterprise-grade security practices:

1. **Separation of Tokens:** Client access utilizes `HS256` JWT while refresh cycles rely on UUID
   strings tightly verified against persistence states.
2. **Password Cryptography:** BCrypt hashes integrated deeply into the User Service logic intercept
   plain texts inherently at registration.
3. **Hibernate Prepared Statements:** Entity management guarantees zero SQL-injection
   vulnerabilities natively through ORM encapsulation parameters.
4. **Servlet Filtering:** All private routes mandatorily pass through a core JWT validation `Filter`
   validating bearer prefixes seamlessly.

---

## 🧪 Testing

The repository establishes a structured unit-testing baseline primarily validating core `Services`
and Data Access Objects using **JUnit**.

To execute the test suite:

```bash
mvn clean test
```

To run tests alongside coverage generation plugins (if configured):

```bash
mvn test jacoco:report
```

---

## 🤝 Contributing

We welcome educational and structural contributions to the codebase:

1. Fork this repository.
2. Form a descriptive feature branch (`git checkout -b feature/Implement-Stripe`).
3. Commit logically coherent changes (`git commit -m "Add Stripe SDK dependency"`).
4. Push your changes securely to the fork (`git push origin feature/Implement-Stripe`).
5. Open a Pull Request detailing what bug/feature was resolved.

### Style Guide

- Consistent descriptive JavaDoc labeling for Services.
- Separation of DTOs — _Never leak Entities into Controller responses._
- Proper English commit messages.

---

## 📄 License

This repository is strictly provisioned for **educational and portfolio purposes**. Original
codebase rights belong strictly to the author.

---

<p align="center">
  <b>⭐ Provided this project inspired your own architectures, please leave a star! ⭐</b><br/>
  Made with ❤️ focusing on modern Java Enterprise Engineering
</p>

<div align="center">

# Smart PC Store

**Enterprise-grade E-commerce Backend System**

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://adoptium.net/)
[![Jakarta EE](https://img.shields.io/badge/Jakarta%20EE-6.0-1572B6?style=flat&logo=jakarta&logoColor=white)](https://jakarta.ee/)
[![Hibernate](https://img.shields.io/badge/Hibernate-6.4.4-59666C?style=flat&logo=hibernate&logoColor=white)](https://hibernate.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-336791?style=flat&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=flat&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=flat&logo=docker&logoColor=white)](https://www.docker.com/)
[![CI](https://github.com/ldanh270/smart-pc-store/actions/workflows/ci.yml/badge.svg)](https://github.com/ldanh270/smart-pc-store/actions/workflows/ci.yml)
[![Security](https://github.com/ldanh270/smart-pc-store/actions/workflows/security.yml/badge.svg)](https://github.com/ldanh270/smart-pc-store/actions/workflows/security.yml)
[![Docker Release](https://github.com/ldanh270/smart-pc-store/actions/workflows/docker-release.yml/badge.svg)](https://github.com/ldanh270/smart-pc-store/actions/workflows/docker-release.yml)
 [![License](https://img.shields.io/badge/License-MIT-green.svg?style=flat)](LICENSE)

**A comprehensive RESTful API backend for PC component e-commerce platform built with Jakarta EE, Hibernate ORM, and PostgreSQL**

[Features](#features) • [Quick Start](#quick-start) • [Documentation](#documentation) • [CI/CD](#cicd) • [Architecture](#architecture) • [Contributing](#contributing)

</div>

---

## Overview

Smart PC Store is a production-ready backend API for PC components e-commerce platform. Built with enterprise Java technologies, it provides comprehensive features for product management, order processing, inventory control, and B2B supplier operations.

### Key Highlights

**Robust Security**
- JWT authentication with refresh token rotation
- BCrypt password hashing with adaptive work factor
- Role-based access control (RBAC)
- SQL injection prevention through JPA/Hibernate

**Scalable Architecture**
- N-Tier layered architecture (Presentation, Business Logic, Data Access, Persistence)
- DAO pattern with generic base class
- DTO pattern for API versioning and security
- Transaction management with ACID compliance

**Complete E-commerce Features**
- Product catalog with categories and search
- Shopping cart with persistent storage
- Order management with status tracking
- Payment processing (multiple methods supported)
- Supplier management and purchase orders
- Inventory transaction logging

**Production Ready**
- Docker containerization support
- Comprehensive error handling
- Logging and monitoring ready
- Database migrations support
- API documentation included

---

## Features

### Core Functionality

- **User Authentication & Authorization**
  - JWT-based authentication with access and refresh tokens
  - BCrypt password encryption
  - Role-based access control (Admin, Staff, Customer)
  - Session management with automatic cleanup

- **Product Management**
  - CRUD operations for products and categories
  - Hierarchical category structure
  - Product search and filtering
  - Stock management
  - Supplier association

- **Shopping & Orders**
  - Persistent shopping cart per user
  - Order processing with status tracking
  - Multiple payment methods (COD, Bank Transfer, E-Wallet)
  - Order history and details

- **B2B Operations**
  - Supplier management
  - Purchase order creation and tracking
  - Inventory transaction logging
  - Stock replenishment automation

- **Additional Features**
  - Web crawler for product data collection
  - Revenue analytics (daily aggregation)
  - Automated session cleanup
  - CORS support for frontend integration

---

## Technology Stack

| Category | Technology | Purpose |
|----------|------------|---------|
| **Language** | Java 17 LTS | Modern Java features, long-term support |
| **Framework** | Jakarta EE 6.0 | Servlet API, enterprise standards |
| **ORM** | Hibernate 6.4.4 | Object-relational mapping, JPA implementation |
| **Database** | PostgreSQL 15+ | Relational database, ACID compliance |
| **Build Tool** | Maven 3.x | Dependency management, build automation |
| **Server** | Apache Tomcat 10.1 | Servlet container |
| **Security** | JJWT 0.11.5 + BCrypt | JWT authentication, password hashing |
| **JSON** | Google Gson 2.10.1 | JSON serialization/deserialization |
| **Container** | Docker | Containerization, deployment |

---

## Quick Start

### Prerequisites

- Java JDK 17+
- Apache Maven 3.8+
- PostgreSQL 13+
- Apache Tomcat 10.1+

### Installation

```bash
# Clone repository
git clone https://github.com/ldanh270/smart-pc-store.git
cd smart-pc-store

# Configure database
createdb smart_pc_store
psql -d smart_pc_store -f plan/schema.sql

# Configure environment
cp .env.example .env
# Edit .env with your JWT secret and database credentials

# Build project
mvn clean package

# Deploy to Tomcat
cp target/smart-pc-store.war $TOMCAT_HOME/webapps/

# Start server
$TOMCAT_HOME/bin/startup.sh
```

### Docker Deployment

```bash
# Using Docker Compose
docker-compose up -d

# Or build and run manually
docker build -t smart-pc-store .
docker run -p 8080:8080 smart-pc-store
```

### Verify Installation

```bash
curl http://localhost:8080/smart-pc-store/
```

For detailed installation instructions, see [Installation Guide](docs/INSTALLATION.md).

---

## Documentation

Comprehensive documentation is available in the `docs/` directory:

- **[Installation Guide](docs/INSTALLATION.md)** - Detailed setup instructions for development and production
- **[API Documentation](docs/API_DOCUMENTATION.md)** - Complete REST API reference with examples
- **[Architecture Guide](docs/ARCHITECTURE.md)** - System architecture, design patterns, and best practices
- **[Database Schema](docs/DATABASE.md)** - Database design, relationships, and migrations
- **[Security Guide](docs/SECURITY.md)** - Security measures, authentication, and best practices
- **[CI/CD Guide](docs/CI_CD.md)** - GitHub Actions pipelines, secrets, and deployment setup
- **[Contributing Guide](docs/CONTRIBUTING.md)** - Guidelines for contributing to the project

---

## CI/CD

The project includes a complete GitHub Actions pipeline:

- **CI** (`.github/workflows/ci.yml`) - Maven build and test on push/PR
- **Security** (`.github/workflows/security.yml`) - Dependency Review and CodeQL
- **Qodana Code Quality** (`.github/workflows/qodana_code_quality.yml`) - JetBrains static analysis with annotations
- **Docker Release** (`.github/workflows/docker-release.yml`) - Build, scan, and publish Docker image to GHCR
- **Deploy** (`.github/workflows/deploy.yml`) - Manual deployment to `staging`/`production` using GitHub Environments

For setup details (required secrets, environments, deployment flow), see **[CI/CD Guide](docs/CI_CD.md)**.

---

## Architecture

The system follows **N-Tier Architecture** with clear separation of concerns:

```
Client Layer (React/Angular/Mobile)
         ↓
Presentation Layer (Servlets, Controllers, Filters)
         ↓
Business Logic Layer (Services, DTOs)
         ↓
Data Access Layer (DAOs, GenericDao)
         ↓
Persistence Layer (JPA/Hibernate, Entities)
         ↓
Database Layer (PostgreSQL)
```

### Design Patterns

- **MVC Pattern**: Model-View-Controller for request handling
- **DAO Pattern**: Generic data access with reusable CRUD operations
- **DTO Pattern**: Data transfer objects for API versioning and security
- **Singleton Pattern**: EntityManagerFactory management
- **Factory Pattern**: Object creation encapsulation
- **Filter Chain Pattern**: Cross-cutting concerns (CORS, Auth, RBAC)
- **Service Layer Pattern**: Business logic and transaction management

For detailed architecture information, see [Architecture Guide](docs/ARCHITECTURE.md).

---

## Project Structure

```
smart-pc-store/
├── docs/                          # Documentation
│   ├── INSTALLATION.md
│   ├── API_DOCUMENTATION.md
│   ├── ARCHITECTURE.md
│   ├── DATABASE.md
│   ├── SECURITY.md
│   └── CONTRIBUTING.md
├── plan/                          # Database schema and planning
│   ├── schema.sql
│   └── data.sql
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── configs/          # Configuration classes
│   │   │   ├── controllers/      # Request handlers
│   │   │   ├── crawler/          # Web crawler for data collection
│   │   │   ├── dao/              # Data access objects
│   │   │   ├── dto/              # Data transfer objects
│   │   │   ├── entities/         # JPA entities
│   │   │   ├── filters/          # Servlet filters
│   │   │   ├── listeners/        # Application listeners
│   │   │   ├── services/         # Business logic
│   │   │   ├── servlets/         # HTTP endpoints
│   │   │   └── utils/            # Utility classes
│   │   ├── resources/
│   │   │   └── META-INF/
│   │   │       └── persistence.xml
│   │   └── webapp/
│   │       └── WEB-INF/
│   └── test/                     # Unit and integration tests
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## API Endpoints

### Authentication
- `POST /auth/signup` - Register new user
- `POST /auth/login` - User login
- `POST /auth/refresh` - Refresh access token
- `POST /auth/logout` - User logout

### Products
- `GET /products` - List products (with pagination, search, filters)
- `GET /products/{id}` - Get product details
- `POST /products` - Create product (Admin/Staff)
- `PUT /products/{id}` - Update product (Admin/Staff)
- `DELETE /products/{id}` - Delete product (Admin)

### Shopping Cart
- `GET /cart` - View cart
- `POST /cart/add` - Add item to cart
- `PUT /cart/items/{id}` - Update cart item quantity
- `DELETE /cart/items/{id}` - Remove item from cart
- `DELETE /cart/clear` - Clear cart

### Orders
- `POST /orders/checkout` - Create order from cart
- `GET /orders/my-orders` - Get user's orders
- `GET /orders/{id}` - Get order details
- `PUT /orders/{id}/status` - Update order status (Admin/Staff)
- `PUT /orders/{id}/cancel` - Cancel order

### Categories
- `GET /categories` - List all categories
- `POST /categories` - Create category (Admin)

### Suppliers
- `GET /suppliers` - List suppliers (Admin/Staff)
- `POST /suppliers` - Create supplier (Admin)

### Purchase Orders
- `POST /purchase-orders` - Create purchase order (Admin/Staff)
- `GET /purchase-orders` - List purchase orders (Admin/Staff)

For complete API documentation with request/response examples, see [API Documentation](docs/API_DOCUMENTATION.md).

---

## Security

### Authentication
- JWT-based authentication with HS256 algorithm
- Dual-token approach: Access Token (15 min) + Refresh Token (7 days)
- Automatic token rotation to prevent reuse attacks
- HTTP-only cookies for refresh tokens

### Authorization
- Role-Based Access Control (RBAC)
- Three roles: ADMIN, STAFF, CUSTOMER
- Endpoint-level permission checks
- Resource ownership validation

### Data Protection
- BCrypt password hashing with work factor 12
- SQL injection prevention via JPA parameterized queries
- Input validation on all endpoints
- XSS prevention through output encoding
- CORS configuration for trusted origins

### Best Practices
- HTTPS enforcement (production)
- Security headers (HSTS, CSP, X-Frame-Options)
- Session management with automatic cleanup
- Comprehensive security logging
- Regular dependency vulnerability scanning

For detailed security information, see [Security Guide](docs/SECURITY.md).

---

## Testing

```bash
# Run all tests
mvn clean test

# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html

# Build without tests
mvn clean package -DskipTests
```

### Test Structure

- **Unit Tests**: Service and DAO layer testing
- **Integration Tests**: Database integration testing
- **API Tests**: HTTP endpoint testing (Postman/cURL)
- **Coverage Goal**: 80%+ for critical business logic

---

## Contributing

We welcome contributions! Please see our [Contributing Guide](docs/CONTRIBUTING.md) for details on:

- Code of Conduct
- Development workflow
- Coding standards
- Commit message conventions
- Pull request process
- Testing requirements

### Quick Contribution Steps

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Make your changes and add tests
4. Commit using conventional commits (`git commit -m "feat: add feature"`)
5. Push to your fork (`git push origin feature/your-feature`)
6. Open a Pull Request

---

## License

This project is created for **educational and portfolio purposes**.

### Usage Terms

**Allowed:**
- Study and learn from the code
- Use for personal/educational projects
- Fork for your own development

**Not Allowed:**
- Commercial use without permission
- Redistribute as your own work
- Remove copyright notices

---

## Authors

**Lê Đức Anh**
- Role: Lead Developer, Project Manager, Code Reviewer
- Email: ducanhle.dn@gmail.com
- GitHub: [@ldanh270](https://github.com/ldanh270)

**Dương Duy Vinh**
- Role: AI Engineer, Co-Developer
- Email: ducanhle.dn@gmail.com
- GitHub: [@cybervinh2077](https://github.com/cybervinh2077)

**Tạ Thị Bích Loan**
- Role: Designer, Co-Developer
- Email: ducanhle.dn@gmail.com
- GitHub: [@bloan-7105](https://github.com/bloan-7105)

**Nguyễn Ánh Tuyết**
- Role: Database Administrator, Co-Developer
- Email: ducanhle.dn@gmail.com
- GitHub: [@Nguyen-Anh-Tuyet](https://github.com/Nguyen-Anh-Tuyet)
---

## Acknowledgments

Built with:
- [Jakarta EE](https://jakarta.ee/) - Enterprise Java platform
- [Hibernate ORM](https://hibernate.org/) - Object-relational mapping
- [PostgreSQL](https://www.postgresql.org/) - Open source database
- [Apache Tomcat](https://tomcat.apache.org/) - Servlet container
- [Maven](https://maven.apache.org/) - Build automation

---

## Support

- **Issues**: [GitHub Issues](https://github.com/ldanh270/smart-pc-store/issues)
- **Documentation**: See `docs/` directory
- **Email**: [Contact via GitHub]

---

<div align="center">

**If this project helped you, please give it a star ⭐**

Made with Java, Jakarta EE, Hibernate & PostgreSQL

© 2026 Smart PC Store. All rights reserved.

</div>

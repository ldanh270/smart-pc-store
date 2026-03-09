# System Architecture

## Overview

Smart PC Store follows a **N-Tier Architecture** pattern with clear separation of concerns across multiple layers. This architecture ensures maintainability, scalability, and testability of the application.

## Architecture Layers

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│               (React/Angular/Mobile App/Postman)                │
└─────────────────────────────┬───────────────────────────────────┘
                              │ HTTP/HTTPS Requests
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
│   ┌────────────────────────────────────────────────────────┐    │
│   │  Servlets (HTTP Endpoint Mapping)                      │    │
│   │  - AuthServlet, ProductServlet, OrderServlet...        │    │
│   └────────────────────────┬───────────────────────────────┘    │
│                            │                                    │
│   ┌────────────────────────▼───────────────────────────────┐    │
│   │  Controllers (Request/Response Handler)                │    │
│   │  - AuthController, ProductController...                │    │
│   │  - Parse JSON, Validate Input, Call Services           │    │
│   └────────────────────────┬───────────────────────────────┘    │
│                            │                                    │
│   ┌────────────────────────▼───────────────────────────────┐    │
│   │  Filters (Cross-cutting Concerns)                      │    │
│   │  - CorsFilter, JwtAuthenticationFilter                 │    │
│   │  - RoleAuthorizationFilter                             │    │
│   └────────────────────────────────────────────────────────┘    │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                     BUSINESS LOGIC LAYER                        │
│   ┌────────────────────────────────────────────────────────┐    │
│   │  Services (Business Rules & Transactions)              │    │
│   │  - AuthService, ProductService, OrderService           │    │
│   │  - CartService, PurchaseService, PaymentService        │    │
│   │  - Transaction Management, Business Validation         │    │
│   └────────────────────────┬───────────────────────────────┘    │
│                            │                                    │
│   ┌────────────────────────▼───────────────────────────────┐    │
│   │  DTOs (Data Transfer Objects)                          │    │
│   │  - Request DTOs, Response DTOs                         │    │
│   │  - Isolation between API and Database                  │    │
│   └────────────────────────────────────────────────────────┘    │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                   DATA ACCESS LAYER (DAO)                       │
│   ┌────────────────────────────────────────────────────────┐    │
│   │  GenericDao<T> (Base CRUD Operations)                  │    │
│   │  - create(), findById(), findAll(), update(), delete() │    │
│   └────────────────────────┬───────────────────────────────┘    │
│                            │                                    │
│   ┌────────────────────────▼───────────────────────────────┐    │
│   │  Specific DAOs (Extended Operations)                   │    │
│   │  - UserDao, ProductDao, OrderDao, CartDao...           │    │
│   │  - Custom queries, Complex joins                       │    │
│   └────────────────────────────────────────────────────────┘    │
└─────────────────────────────┬───────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                    PERSISTENCE LAYER (ORM)                      │
│   ┌────────────────────────────────────────────────────────┐    │
│   │  JPA/Hibernate (ORM Framework)                         │    │
│   │  - Entity Management, Lazy/Eager Loading               │    │
│   │  - Relationship Mapping (OneToMany, ManyToOne...)      │    │
│   │  - Transaction Management, Caching                     │    │
│   └────────────────────────┬───────────────────────────────┘    │
│                            │                                    │
│   ┌────────────────────────▼───────────────────────────────┐    │
│   │  Entities (JPA Annotated Classes)                      │    │
│   │  - User, Product, Order, Cart, Category...             │    │
│   │  - Annotations: @Entity, @Table, @Column, @Id...       │    │
│   └────────────────────────────────────────────────────────┘    │
└─────────────────────────────┬───────────────────────────────────┘
                              │ JDBC
                              ↓
┌─────────────────────────────────────────────────────────────────┐
│                       DATABASE LAYER                            │
│                      PostgreSQL Database                        │
│   - Tables: Users, Products, Orders, Categories...              │
│   - Constraints, Indexes, Foreign Keys                          │
│   - Stored Procedures, Triggers                                 │
└─────────────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### 1. Presentation Layer

**Servlets**
- Map HTTP endpoints to URLs using `@WebServlet` annotations
- Handle HTTP methods (GET, POST, PUT, DELETE)
- Route requests to appropriate controllers
- Minimal business logic

**Controllers**
- Parse request body (JSON → DTO)
- Validate input data
- Call service layer methods
- Format responses (DTO → JSON)
- Handle exceptions and error responses

**Filters**
- Cross-cutting concerns (CORS, Authentication, Authorization)
- Execute before/after request processing
- Chainable and reusable

### 2. Business Logic Layer

**Services**
- Implement business rules and workflows
- Coordinate between multiple DAOs
- Manage transactions
- Perform business validations
- Transform data between DTOs and Entities

**DTOs (Data Transfer Objects)**
- Request DTOs: Receive data from client
- Response DTOs: Send data to client
- Isolate API structure from database structure
- Enable API versioning

### 3. Data Access Layer

**GenericDao**
- Base class with CRUD operations using Java Generics
- Reusable across all entities
- Transaction management
- Query optimization

**Specific DAOs**
- Extend GenericDao for specific entities
- Implement custom queries
- Complex joins and aggregations
- Entity-specific operations

### 4. Persistence Layer

**JPA/Hibernate**
- Object-Relational Mapping (ORM)
- Automatic SQL generation
- Entity lifecycle management
- Lazy/Eager loading strategies
- Second-level caching

**Entities**
- Plain Old Java Objects (POJOs) with JPA annotations
- Map to database tables
- Define relationships between entities
- Validation constraints

### 5. Database Layer

**PostgreSQL**
- Relational database management system
- ACID compliance
- Constraints and indexes for data integrity
- Connection pooling for performance

## Design Patterns

### 1. MVC Pattern (Model-View-Controller)

- **Model**: Entities (User, Product, Order...)
- **View**: JSON Response (no traditional view in REST API)
- **Controller**: Controllers handle requests and orchestrate services

### 2. DAO Pattern (Data Access Object)

```java
public class GenericDao<T> {
    private Class<T> entityClass;
    
    public T create(T entity) { /* ... */ }
    public T findById(Object id) { /* ... */ }
    public List<T> findAll() { /* ... */ }
    public T update(T entity) { /* ... */ }
    public void delete(T entity) { /* ... */ }
}

public class UserDao extends GenericDao<User> {
    public User findByUsername(String username) { /* ... */ }
    public List<User> findByRole(String role) { /* ... */ }
}
```

**Benefits**:
- Separates data access logic from business logic
- Reusable CRUD operations
- Easy to test and mock

### 3. DTO Pattern (Data Transfer Object)

```java
// Request DTO
public class LoginRequestDto {
    private String username;
    private String password;
    // getters, setters, validation
}

// Response DTO
public class LoginResponseDto {
    private UserDto user;
    private String accessToken;
    private String refreshToken;
    // getters, setters
}

// Entity (never exposed to client)
@Entity
@Table(name = "users")
public class User {
    @Id
    private UUID id;
    private String username;
    private String passwordHash;  // Hidden from API
    // other fields, relationships
}
```

**Benefits**:
- Security: Hide sensitive entity fields (passwordHash)
- Flexibility: Different representations for different APIs
- Versioning: Support multiple API versions

### 4. Singleton Pattern

```java
public class JPAUtil {
    private static EntityManagerFactory emf;
    
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (JPAUtil.class) {
                if (emf == null) {
                    emf = Persistence.createEntityManagerFactory("smart-pc-store");
                }
            }
        }
        return emf;
    }
    
    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }
}
```

**Benefits**:
- Resource efficiency (single EMF instance)
- Centralized configuration
- Thread-safe double-checked locking

### 5. Factory Pattern

```java
public class EntityManager getEntityManager() {
    return JPAUtil.getEntityManager();
}
```

**Benefits**:
- Encapsulation of object creation
- Flexible and extensible

### 6. Filter Chain Pattern

```java
@WebFilter("/*")
public class CorsFilter implements Filter { /* ... */ }

@WebFilter("/api/*")
public class JwtAuthenticationFilter implements Filter { /* ... */ }

@WebFilter("/admin/*")
public class RoleAuthorizationFilter implements Filter { /* ... */ }
```

**Benefits**:
- Modular cross-cutting concerns
- Reusable logic
- Easy to add/remove filters

### 7. Service Layer Pattern

```java
public class OrderService {
    private OrderDao orderDao;
    private ProductDao productDao;
    private CartDao cartDao;
    
    @Transactional
    public Order createOrder(OrderRequestDto dto) {
        // Business logic
        // Coordinate multiple DAOs
        // Manage transaction
    }
}
```

**Benefits**:
- Transaction management
- Business rule centralization
- Coordination between multiple DAOs

## Request Flow Example

### User Login Flow

```
1. Client sends POST /auth/login with {username, password}
   ↓
2. CorsFilter: Add CORS headers
   ↓
3. AuthServlet: Maps /auth/* requests
   ↓
4. AuthController.handleLogin():
   - Parse JSON body to LoginRequestDto
   - Validate input (AuthValidate.validateLogin)
   ↓
5. AuthService.login():
   - Find user by username (UserDao.findByUsername)
   - Verify password (BCrypt.checkpw)
   - Generate Access Token (JwtUtil.generateAccessToken)
   - Generate Refresh Token (UUID.randomUUID)
   - Save session (SessionDao.create)
   ↓
6. Response: LoginResponseDto {user, accessToken, refreshToken}
   ↓
7. Client stores tokens for subsequent requests
```

### Order Creation Flow (with Transaction)

```
1. Client sends POST /orders/checkout
   ↓
2. JwtAuthenticationFilter: Validate access token
   ↓
3. OrderServlet → OrderController
   ↓
4. OrderService.createOrder():
   BEGIN TRANSACTION
   ├─ Validate cart has items
   ├─ Create Order entity (OrderDao.create)
   ├─ Create OrderDetails from CartItems
   ├─ Update product stock (ProductDao.update)
   ├─ Create InventoryTransactions
   ├─ Clear cart (CartDao.clearCart)
   └─ Create Payment record
   COMMIT TRANSACTION
   ↓
5. Return OrderResponseDto
```

## Transaction Management

### Declarative Transactions (JPA)

```java
EntityManager em = JPAUtil.getEntityManager();
try {
    em.getTransaction().begin();
    
    // Multiple database operations
    Order order = orderDao.create(orderData);
    orderDetailDao.create(detailData);
    productDao.update(product);
    
    em.getTransaction().commit();
} catch (Exception e) {
    if (em.getTransaction().isActive()) {
        em.getTransaction().rollback();
    }
    throw e;
} finally {
    em.close();
}
```

**ACID Properties**:
- **Atomicity**: All operations succeed or all fail
- **Consistency**: Database remains in valid state
- **Isolation**: Concurrent transactions don't interfere
- **Durability**: Committed changes are permanent

## Error Handling Strategy

### Layered Exception Handling

```
Controller Layer:
├─ Catch validation errors → 400 Bad Request
├─ Catch authentication errors → 401 Unauthorized
├─ Catch authorization errors → 403 Forbidden
├─ Catch not found errors → 404 Not Found
└─ Catch all other exceptions → 500 Internal Server Error

Service Layer:
├─ Throw business logic exceptions
└─ Let transaction rollback on exception

DAO Layer:
├─ Handle persistence exceptions
└─ Convert to domain exceptions
```

### Example Error Handling

```java
@WebServlet("/products/*")
public class ProductServlet extends HttpServlet {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            controller.handleCreateProduct(req, resp);
        } catch (ValidationException e) {
            HttpUtil.sendJson(resp, 400, new ErrorResponse(e.getErrors()));
        } catch (AuthenticationException e) {
            HttpUtil.sendJson(resp, 401, new ErrorResponse(e.getMessage()));
        } catch (AuthorizationException e) {
            HttpUtil.sendJson(resp, 403, new ErrorResponse(e.getMessage()));
        } catch (NotFoundException e) {
            HttpUtil.sendJson(resp, 404, new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error", e);
            HttpUtil.sendJson(resp, 500, new ErrorResponse("Internal server error"));
        }
    }
}
```

## Security Architecture

### Authentication Flow

```
1. User login → Generate JWT Access Token (15 min)
2. Generate UUID Refresh Token (7 days)
3. Store Refresh Token in database (Sessions table)
4. Return both tokens to client
5. Client stores Access Token in memory
6. Client stores Refresh Token in HTTP-only cookie

Subsequent Requests:
1. Client sends Access Token in Authorization header
2. JwtAuthenticationFilter validates token
3. Extract userId from token
4. Proceed with request

Token Refresh:
1. Client sends Refresh Token (from cookie)
2. Server validates against database
3. Generate new Access Token + new Refresh Token
4. Delete old Refresh Token (rotation)
5. Return new tokens
```

### Authorization Flow

```
1. JwtAuthenticationFilter extracts userId
2. Load user from database (with role)
3. RoleAuthorizationFilter checks permissions
4. Allow or deny based on role
```

**Permission Matrix**:
- **ADMIN**: Full access to all endpoints
- **STAFF**: Product/Order/Supplier management
- **CUSTOMER**: Own profile, cart, orders

## Scalability Considerations

### Horizontal Scaling

- **Stateless Design**: JWT tokens enable stateless authentication
- **Load Balancing**: Multiple application instances behind load balancer
- **Database Connection Pooling**: Reuse connections efficiently
- **Caching**: Redis/Memcached for frequently accessed data

### Vertical Scaling

- **JVM Tuning**: Optimize heap size, garbage collection
- **Database Optimization**: Indexes, query optimization
- **Connection Pool Sizing**: Match application load

### Performance Optimization

- **Lazy Loading**: Load related entities only when needed
- **Batch Operations**: Bulk inserts/updates
- **Query Optimization**: Use indexes, avoid N+1 queries
- **Caching Strategy**: Second-level cache for entities

## Technology Stack

| Layer | Technology | Purpose |
|-------|------------|---------|
| Presentation | Jakarta Servlet 6.0 | HTTP request handling |
| Business Logic | Pure Java | Business rules implementation |
| Persistence | Hibernate 6.4.4 + JPA 3.1 | ORM, transaction management |
| Database | PostgreSQL 15 | Data storage |
| Authentication | JJWT 0.11.5 + BCrypt | JWT tokens, password hashing |
| Build | Maven 3.x | Dependency management, build automation |
| Server | Apache Tomcat 10.1 | Servlet container |
| Containerization | Docker | Deployment, scalability |

## Best Practices

### Code Organization

1. **Package by Feature**: Group related classes together
2. **Consistent Naming**: Follow Java conventions
3. **Separation of Concerns**: Each class has single responsibility
4. **Dependency Injection**: Pass dependencies through constructors

### Development Guidelines

1. **Test First**: Write tests before implementation (TDD)
2. **Keep Methods Short**: < 50 lines per method
3. **Avoid God Classes**: Break large classes into smaller ones
4. **Use Interfaces**: Program to interfaces, not implementations
5. **Handle Exceptions**: Proper exception handling and logging

### Database Guidelines

1. **Use Transactions**: Wrap multiple operations in transactions
2. **Optimize Queries**: Use indexes, avoid N+1 queries
3. **Normalize Data**: Follow 3NF normalization
4. **Use Foreign Keys**: Enforce referential integrity
5. **Regular Backups**: Automated backup strategy

---

## Related Documentation

- [Installation Guide](./INSTALLATION.md)
- [API Documentation](./API_DOCUMENTATION.md)
- [Security Guide](./SECURITY.md)
- [Database Schema](./DATABASE.md)
- [Contributing Guide](./CONTRIBUTING.md)

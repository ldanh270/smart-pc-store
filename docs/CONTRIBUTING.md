# Contributing Guide

## Welcome

Thank you for your interest in contributing to Smart PC Store! This document provides guidelines and instructions for contributing to the project.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Commit Guidelines](#commit-guidelines)
- [Pull Request Process](#pull-request-process)
- [CI/CD Checks](#8-pass-cicd-checks)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

---

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive experience for everyone. We expect all contributors to:

- Be respectful and professional
- Accept constructive criticism gracefully
- Focus on what is best for the community
- Show empathy towards other community members

### Unacceptable Behavior

- Harassment, discrimination, or offensive comments
- Trolling or insulting remarks
- Publishing others' private information
- Any conduct that would be inappropriate in a professional setting

---

## Getting Started

### Prerequisites

Before you begin, ensure you have:

- Java JDK 17+
- Apache Maven 3.8+
- PostgreSQL 13+
- Git
- IDE (IntelliJ IDEA, Eclipse, or VS Code)

### Fork and Clone

1. **Fork the repository** on GitHub
2. **Clone your fork** locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/smart-pc-store.git
   cd smart-pc-store
   ```
3. **Add upstream remote:**
   ```bash
   git remote add upstream https://github.com/ldanh270/smart-pc-store.git
   ```
4. **Verify remotes:**
   ```bash
   git remote -v
   # origin    https://github.com/YOUR_USERNAME/smart-pc-store.git (fetch)
   # origin    https://github.com/YOUR_USERNAME/smart-pc-store.git (push)
   # upstream  https://github.com/ldanh270/smart-pc-store.git (fetch)
   # upstream  https://github.com/ldanh270/smart-pc-store.git (push)
   ```

### Setup Development Environment

1. **Install dependencies:**
   ```bash
   mvn clean install
   ```

2. **Configure database:**
   - Create database `smart_pc_store`
   - Run `plan/schema.sql`
   - Update `persistence.xml` with your credentials

3. **Configure environment:**
   - Copy `.env.example` to `.env`
   - Set `ACCESS_TOKEN_SECRET` and other variables

4. **Build and run:**
   ```bash
   mvn clean package
   # Deploy to Tomcat or run with IDE
   ```

---

## Development Workflow

### 1. Create a Branch

Always create a new branch for your work:

```bash
# Update your local main branch
git checkout main
git pull upstream main

# Create feature branch
git checkout -b feature/your-feature-name

# Or for bug fixes
git checkout -b bugfix/issue-description
```

**Branch Naming Convention:**
- `feature/` - New features (e.g., `feature/payment-gateway`)
- `bugfix/` - Bug fixes (e.g., `bugfix/cart-null-pointer`)
- `hotfix/` - Critical production fixes (e.g., `hotfix/security-vulnerability`)
- `refactor/` - Code refactoring (e.g., `refactor/dao-layer`)
- `docs/` - Documentation updates (e.g., `docs/api-documentation`)
- `test/` - Test additions/updates (e.g., `test/service-layer`)

### 2. Make Changes

- Write clean, readable code
- Follow project coding standards
- Add comments for complex logic
- Update documentation if needed
- Write/update tests

### 3. Test Your Changes

```bash
# Run all tests
mvn clean test

# Build project
mvn clean package

# Test manually
# Deploy to Tomcat and verify functionality
```

### 4. Commit Changes

```bash
# Stage your changes
git add .

# Commit with descriptive message
git commit -m "feat(auth): implement two-factor authentication"
```

### 5. Keep Your Branch Updated

```bash
# Fetch latest changes from upstream
git fetch upstream

# Rebase your branch on upstream/main
git rebase upstream/main

# If conflicts occur, resolve them and continue:
git rebase --continue
```

### 6. Push to Your Fork

```bash
git push origin feature/your-feature-name
```

### 7. Create Pull Request

1. Go to your fork on GitHub
2. Click "New Pull Request"
3. Select your branch
4. Fill in the PR template
5. Submit for review

### 8. Pass CI/CD Checks

All pull requests must pass required GitHub Actions workflows before merge:

- `CI` workflow (`.github/workflows/ci.yml`)
- `Security` workflow (`.github/workflows/security.yml`)

For release and deployment details, see [CI/CD Guide](CI_CD.md).

---

## Coding Standards

### Java Code Style

#### Naming Conventions

```java
// Classes: PascalCase
public class ProductService { }

// Interfaces: PascalCase with descriptive names
public interface PaymentGateway { }

// Methods: camelCase, verb-based
public void createOrder() { }
public Order findOrderById(UUID id) { }

// Variables: camelCase, descriptive
private String username;
private List<Product> products;

// Constants: UPPER_SNAKE_CASE
public static final int MAX_RETRY_ATTEMPTS = 3;
private static final String DEFAULT_ROLE = "CUSTOMER";

// Packages: lowercase, singular
package dao;
package services;
package controllers;
```

#### Code Organization

```java
public class ProductService {
    // 1. Static fields
    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    
    // 2. Instance fields
    private final ProductDao productDao;
    private final CategoryDao categoryDao;
    
    // 3. Constructor
    public ProductService(ProductDao productDao, CategoryDao categoryDao) {
        this.productDao = productDao;
        this.categoryDao = categoryDao;
    }
    
    // 4. Public methods
    public Product createProduct(ProductRequestDto dto) {
        // Implementation
    }
    
    // 5. Protected/package methods
    protected void validateProduct(Product product) {
        // Implementation
    }
    
    // 6. Private methods
    private void notifyStockChange(Product product) {
        // Implementation
    }
    
    // 7. Nested classes/enums (if any)
    private static class ProductValidator {
        // Implementation
    }
}
```

#### Method Guidelines

```java
// ✅ Good: Short, focused methods
public Order createOrder(OrderRequestDto dto) {
    validateOrderRequest(dto);
    Order order = buildOrderFromDto(dto);
    order = orderDao.create(order);
    sendOrderConfirmation(order);
    return order;
}

// ❌ Bad: Long, doing too much
public Order createOrder(OrderRequestDto dto) {
    // 200 lines of code...
}

// ✅ Good: Clear return types
public Optional<User> findUserById(UUID id) {
    return Optional.ofNullable(userDao.findById(id));
}

// ✅ Good: Descriptive parameter names
public void updateProductStock(UUID productId, int quantityChange) {
    // Implementation
}

// ❌ Bad: Unclear parameter names
public void update(UUID id, int q) {
    // Implementation
}
```

#### JavaDoc Comments

```java
/**
 * Creates a new product in the system.
 * 
 * @param dto Product data transfer object containing product details
 * @return Created product entity with generated ID
 * @throws ValidationException if product data is invalid
 * @throws DuplicateException if product with same name exists
 */
public Product createProduct(ProductRequestDto dto) 
        throws ValidationException, DuplicateException {
    // Implementation
}
```

### Exception Handling

```java
// ✅ Good: Specific exceptions
try {
    product = productDao.findById(id);
} catch (EntityNotFoundException e) {
    logger.error("Product not found: {}", id, e);
    throw new NotFoundException("Product with id " + id + " not found");
} catch (PersistenceException e) {
    logger.error("Database error", e);
    throw new ServiceException("Failed to retrieve product", e);
}

// ❌ Bad: Catching general Exception
try {
    product = productDao.findById(id);
} catch (Exception e) {
    // What went wrong?
}

// ✅ Good: Proper resource cleanup
EntityManager em = JPAUtil.getEntityManager();
try {
    em.getTransaction().begin();
    // Operations
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

### Dependency Injection

```java
// ✅ Good: Constructor injection
public class OrderService {
    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final CartDao cartDao;
    
    public OrderService(OrderDao orderDao, ProductDao productDao, CartDao cartDao) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.cartDao = cartDao;
    }
}

// ❌ Bad: Direct instantiation
public class OrderService {
    private OrderDao orderDao = new OrderDao();
}
```

---

## Commit Guidelines

### Commit Message Format

We follow the [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Type

Must be one of:

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation only changes
- **style**: Code style (formatting, missing semicolons, etc.)
- **refactor**: Code refactoring (no functional changes)
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **build**: Build system or external dependencies
- **ci**: CI configuration changes
- **chore**: Other changes (maintenance tasks)

#### Scope

Optional, specifies the affected component:
- `auth` - Authentication module
- `product` - Product management
- `cart` - Shopping cart
- `order` - Order processing
- `payment` - Payment handling
- `dao` - Data access layer
- `api` - API endpoints

#### Subject

- Use imperative mood ("add" not "added" or "adds")
- Don't capitalize first letter
- No period at the end
- Limit to 72 characters

#### Examples

```bash
# Feature
feat(auth): implement two-factor authentication

# Bug fix
fix(cart): resolve duplicate item issue when adding products

# Documentation
docs(readme): update installation instructions

# Refactoring
refactor(dao): optimize database queries in ProductDao

# Test
test(order): add integration tests for checkout process

# Multiple paragraphs
feat(payment): integrate Stripe payment gateway

Implemented Stripe payment gateway with the following features:
- Credit card processing
- Webhook handling for payment status
- Refund support

Closes #123
```

### Commit Best Practices

1. **Atomic commits**: One logical change per commit
2. **Meaningful messages**: Explain what and why, not how
3. **Present tense**: "add feature" not "added feature"
4. **Reference issues**: "Closes #123" or "Fixes #456"

---

## Pull Request Process

### Before Submitting

- [ ] Code compiles without errors
- [ ] All tests pass (`mvn clean test`)
- [ ] Code follows project style guidelines
- [ ] New code has test coverage
- [ ] Documentation updated (if applicable)
- [ ] Commits follow commit guidelines
- [ ] Branch is up-to-date with main

### PR Title

Follow commit message format:
```
feat(auth): implement JWT refresh token rotation
fix(cart): resolve null pointer exception in cart service
docs(api): add examples for order endpoints
```

### PR Description Template

```markdown
## Description
Brief description of the changes

## Type of Change
- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Documentation update

## Related Issues
Fixes #123
Closes #456

## Changes Made
- Added JWT refresh token rotation
- Updated AuthService to handle token refresh
- Added tests for token refresh flow
- Updated API documentation

## Testing
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing performed

## Screenshots (if applicable)
[Add screenshots here]

## Checklist
- [ ] My code follows the project's style guidelines
- [ ] I have performed a self-review of my code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] New and existing unit tests pass locally with my changes
```

### Review Process

1. **Automated Checks**: CI/CD pipeline runs tests
2. **Code Review**: Maintainers review your code
3. **Feedback**: Address review comments
4. **Approval**: At least one maintainer approves
5. **Merge**: Maintainer merges your PR

### Responding to Feedback

```bash
# Make requested changes
git add .
git commit -m "refactor: address review comments"

# Push to your branch
git push origin feature/your-feature-name

# PR automatically updates
```

---

## Testing Guidelines

### Test Structure

```
src/test/java/
├── services/
│   ├── AuthServiceTest.java
│   ├── ProductServiceTest.java
│   └── OrderServiceTest.java
├── dao/
│   ├── UserDaoTest.java
│   └── ProductDaoTest.java
└── utils/
    ├── JwtUtilTest.java
    └── ValidationUtilTest.java
```

### Writing Tests

```java
public class ProductServiceTest {
    
    private ProductService productService;
    private ProductDao productDao;
    
    @Before
    public void setUp() {
        productDao = mock(ProductDao.class);
        productService = new ProductService(productDao);
    }
    
    @Test
    public void testCreateProduct_Success() {
        // Arrange
        ProductRequestDto dto = new ProductRequestDto();
        dto.setProductName("Test Product");
        dto.setPrice(99.99);
        
        Product savedProduct = new Product();
        savedProduct.setId(1);
        savedProduct.setProductName("Test Product");
        
        when(productDao.create(any(Product.class))).thenReturn(savedProduct);
        
        // Act
        Product result = productService.createProduct(dto);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId().intValue());
        assertEquals("Test Product", result.getProductName());
        verify(productDao, times(1)).create(any(Product.class));
    }
    
    @Test(expected = ValidationException.class)
    public void testCreateProduct_InvalidData() {
        // Arrange
        ProductRequestDto dto = new ProductRequestDto();
        // Missing required fields
        
        // Act
        productService.createProduct(dto);
        
        // Assert - exception expected
    }
}
```

### Test Coverage

Aim for:
- **Unit Tests**: 80%+ coverage for services and DAOs
- **Integration Tests**: Critical user flows
- **Edge Cases**: Boundary conditions, null values, invalid input

```bash
# Run tests with coverage
mvn clean test jacoco:report

# View coverage report
open target/site/jacoco/index.html
```

---

## Documentation

### When to Update Documentation

Update documentation when you:
- Add new features
- Change API endpoints
- Modify configuration
- Update dependencies
- Fix bugs that affect usage

### Documentation Files

- **README.md**: Overview, quick start
- **docs/INSTALLATION.md**: Detailed installation guide
- **docs/API_DOCUMENTATION.md**: API reference
- **docs/ARCHITECTURE.md**: System architecture
- **docs/DATABASE.md**: Database schema
- **docs/SECURITY.md**: Security measures

### Code Comments

```java
// ✅ Good: Explain why, not what
// Calculate discount based on customer tier to encourage bulk purchases
double discount = calculateTierDiscount(customer);

// ❌ Bad: Stating the obvious
// Set the username
user.setUsername(username);

// ✅ Good: Document complex algorithms
/**
 * Implements the Levenshtein distance algorithm to find similar product names.
 * This helps suggest alternative products when exact matches aren't found.
 * 
 * Time complexity: O(m*n) where m and n are string lengths
 * Space complexity: O(m*n) for the DP table
 */
private int calculateLevenshteinDistance(String s1, String s2) {
    // Implementation
}
```

---

## Areas for Contribution

### High Priority

- [ ] Payment gateway integration (Stripe, PayPal, VNPay)
- [ ] Email notification system (order confirmation, shipping updates)
- [ ] Product reviews and ratings
- [ ] Advanced search with Elasticsearch
- [ ] Real-time inventory alerts

### Medium Priority

- [ ] Wishlist functionality
- [ ] Product recommendations engine
- [ ] Admin dashboard (React/Vue frontend)
- [ ] Mobile app (React Native/Flutter)
- [ ] Multi-language support (i18n)

### Documentation

- [ ] API documentation with Swagger/OpenAPI
- [ ] User guides and tutorials
- [ ] Video tutorials
- [ ] Architecture diagrams
- [ ] Performance optimization guides

### Testing

- [ ] Increase test coverage to 90%+
- [ ] Performance testing with JMeter
- [ ] Security testing (penetration testing)
- [ ] Load testing
- [ ] End-to-end testing

### Infrastructure

- [ ] Docker Compose production configuration
- [ ] Kubernetes deployment manifests
- [ ] CI/CD pipeline with GitHub Actions
- [ ] Monitoring with Prometheus/Grafana
- [ ] Log aggregation with ELK stack

---

## Getting Help

### Resources

- **Documentation**: Check `docs/` directory
- **Issues**: Search existing GitHub issues
- **Discussions**: Use GitHub Discussions for questions

### Contact

- **GitHub Issues**: Report bugs or request features
- **Email**: [your-email@example.com]
- **Discord/Slack**: [Link to community chat]

---

## License

By contributing, you agree that your contributions will be licensed under the same license as the project.

---

## Recognition

Contributors will be recognized in:
- README.md contributors section
- Release notes for significant contributions
- Special mentions for outstanding contributions

Thank you for contributing to Smart PC Store!

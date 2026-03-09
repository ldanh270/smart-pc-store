# Security Guide

## Overview

Smart PC Store implements enterprise-grade security measures across multiple layers to protect user data, prevent unauthorized access, and ensure system integrity.

## Security Layers

### 1. Authentication

#### JWT-Based Authentication

The system uses a dual-token approach for secure authentication:

**Access Token (Short-lived)**
- Algorithm: HS256 (HMAC with SHA-256)
- Expiration: 15 minutes
- Storage: Client-side (memory/localStorage)
- Content: userId, username, role, expiration time
- Purpose: Authorize API requests

**Refresh Token (Long-lived)**
- Format: UUID v4
- Expiration: 7 days
- Storage: HTTP-only cookie + database
- Purpose: Obtain new access tokens

#### Token Flow

```
┌─────────────┐                           ┌─────────────┐
│   Client    │                           │   Server    │
└─────┬───────┘                           └──────┬──────┘
      │                                          │
      │  1. POST /auth/login                     │
      │  {username, password}                    │
      ├─────────────────────────────────────────>│
      │                                          │
      │                                   2. Verify credentials
      │                                   3. Generate tokens
      │                                   4. Store refresh token in DB
      │                                          │
      │  5. Response:                            │
      │  {accessToken, refreshToken}             │
      │  Set-Cookie: refreshToken=...            │
      │<─────────────────────────────────────────┤
      │                                          │
      │  6. API Request                          │
      │  Authorization: Bearer {accessToken}     │
      ├─────────────────────────────────────────>│
      │                                          │
      │                                    7. Validate JWT
      │                                    8. Extract userId
      │                                    9. Process request
      │                                          │
      │  10. Response                            │
      │<─────────────────────────────────────────┤
      │                                          │
```

#### Token Refresh Flow

```
┌─────────────┐                           ┌─────────────┐
│   Client    │                           │   Server    │
└─────┬───────┘                           └──────┬──────┘
      │                                          │
      │  1. Access token expired                 │
      │                                          │
      │  2. POST /auth/refresh                   │
      │  Cookie: refreshToken=...                │
      ├─────────────────────────────────────────>│
      │                                          │
      │                                   3. Validate refresh token
      │                                   4. Check token in database
      │                                   5. Generate new access token
      │                                   6. Generate new refresh token
      │                                   7. Delete old refresh token
      │                                   8. Store new refresh token
      │                                          │
      │  9. Response:                            │
      │  {accessToken, refreshToken}             │
      │  Set-Cookie: refreshToken=...            │
      │<─────────────────────────────────────────┤
      │                                          │
```

#### Implementation

**JwtUtil.java**
```java
public class JwtUtil {
    private static final String SECRET_KEY = System.getenv("ACCESS_TOKEN_SECRET");
    private static final long ACCESS_TOKEN_EXPIRY = 15 * 60 * 1000; // 15 minutes
    
    public static String generateAccessToken(UUID userId, String username, String role) {
        return Jwts.builder()
            .setSubject(userId.toString())
            .claim("username", username)
            .claim("role", role)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY))
            .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
            .compact();
    }
    
    public static UUID getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(SECRET_KEY)
            .parseClaimsJws(token)
            .getBody();
        return UUID.fromString(claims.getSubject());
    }
}
```

**Benefits of Dual-Token Approach:**
- Short-lived access tokens minimize exposure window
- Refresh tokens can be revoked server-side
- Token rotation prevents reuse attacks
- HTTP-only cookies protect against XSS

---

### 2. Password Security

#### BCrypt Hashing

All passwords are hashed using BCrypt with configurable work factor.

```java
// Registration - Hash password
String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
user.setPasswordHash(hashedPassword);

// Login - Verify password
boolean isValid = BCrypt.checkpw(plainPassword, user.getPasswordHash());
```

**BCrypt Features:**
- **Adaptive hashing**: Work factor can be increased as hardware improves
- **Automatic salt generation**: Each password has unique salt
- **Slow by design**: Mitigates brute-force attacks (12 rounds ≈ 250ms)
- **Rainbow table resistant**: Salting prevents precomputed attacks

#### Password Policy

**Enforced Requirements:**
- Minimum 8 characters
- At least 1 uppercase letter (A-Z)
- At least 1 lowercase letter (a-z)
- At least 1 digit (0-9)
- Optional: Special characters (!@#$%^&*)

**Validation Regex:**
```java
public static final Pattern PASSWORD_PATTERN = Pattern.compile(
    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
);
```

**Additional Recommendations:**
- Password expiration (90 days)
- Password history (prevent reuse of last 5 passwords)
- Account lockout after 5 failed attempts
- CAPTCHA after 3 failed attempts

---

### 3. SQL Injection Prevention

#### Parameterized Queries

All database queries use JPA/Hibernate with parameterized queries.

**Safe Query Examples:**

```java
// ✅ SAFE - Named parameter
TypedQuery<User> query = em.createQuery(
    "SELECT u FROM User u WHERE u.username = :username", 
    User.class
);
query.setParameter("username", username);

// ✅ SAFE - Positional parameter
TypedQuery<Product> query = em.createQuery(
    "SELECT p FROM Product p WHERE p.categoryId = ?1 AND p.price < ?2",
    Product.class
);
query.setParameter(1, categoryId);
query.setParameter(2, maxPrice);

// ✅ SAFE - Criteria API
CriteriaBuilder cb = em.getCriteriaBuilder();
CriteriaQuery<User> cq = cb.createQuery(User.class);
Root<User> root = cq.from(User.class);
cq.where(cb.equal(root.get("email"), email));
```

**Dangerous Patterns (Avoided):**

```java
// ❌ DANGEROUS - String concatenation
String sql = "SELECT * FROM users WHERE username = '" + username + "'";

// ❌ DANGEROUS - String format
String sql = String.format("SELECT * FROM products WHERE id = %s", productId);
```

**Hibernate Protection:**
- Automatically escapes special characters
- Uses prepared statements under the hood
- Validates parameter types
- Prevents multiple statement execution

---

### 4. Cross-Site Scripting (XSS) Prevention

#### Input Sanitization

```java
public static String sanitizeHtml(String input) {
    if (input == null) return null;
    
    // Remove dangerous HTML tags and attributes
    return input
        .replaceAll("<script[^>]*>.*?</script>", "")
        .replaceAll("<iframe[^>]*>.*?</iframe>", "")
        .replaceAll("javascript:", "")
        .replaceAll("on\\w+\\s*=", "");
}
```

#### Output Encoding

Always encode output when rendering user-generated content:

```java
// In DTOs, ensure sensitive fields are not included
public class UserResponseDto {
    private UUID id;
    private String username;
    private String displayName;
    // passwordHash is NOT included
}
```

#### Content Security Policy

```java
response.setHeader("Content-Security-Policy", 
    "default-src 'self'; " +
    "script-src 'self'; " +
    "style-src 'self' 'unsafe-inline'; " +
    "img-src 'self' data: https:; " +
    "font-src 'self'; " +
    "connect-src 'self'"
);
```

---

### 5. Cross-Site Request Forgery (CSRF) Protection

#### SameSite Cookie Attribute

```java
Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
refreshTokenCookie.setHttpOnly(true);
refreshTokenCookie.setSecure(true); // HTTPS only
refreshTokenCookie.setPath("/");
refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
refreshTokenCookie.setAttribute("SameSite", "Strict");
```

**SameSite Options:**
- **Strict**: Cookie only sent in first-party context
- **Lax**: Cookie sent on top-level navigation
- **None**: Cookie sent in all contexts (requires Secure)

#### CSRF Token (For Form Submissions)

```java
// Generate CSRF token
String csrfToken = UUID.randomUUID().toString();
session.setAttribute("csrfToken", csrfToken);

// Validate CSRF token
String sessionToken = (String) session.getAttribute("csrfToken");
String requestToken = request.getParameter("csrfToken");
if (!sessionToken.equals(requestToken)) {
    throw new SecurityException("Invalid CSRF token");
}
```

---

### 6. CORS (Cross-Origin Resource Sharing)

#### CorsFilter Implementation

```java
@WebFilter("/*")
public class CorsFilter implements Filter {
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // Allow specific origins (whitelist)
        String origin = httpRequest.getHeader("Origin");
        if (isAllowedOrigin(origin)) {
            httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        }
        
        httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        httpResponse.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
        httpResponse.setHeader("Access-Control-Max-Age", "3600");
        
        // Handle preflight requests
        if ("OPTIONS".equals(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isAllowedOrigin(String origin) {
        List<String> allowedOrigins = Arrays.asList(
            "http://localhost:3000",
            "https://yourdomain.com"
        );
        return allowedOrigins.contains(origin);
    }
}
```

**Production Configuration:**
- Use environment-specific origin whitelist
- Never use wildcard (*) with credentials
- Validate Origin header against whitelist
- Log unauthorized origin attempts

---

### 7. Role-Based Access Control (RBAC)

#### Role Hierarchy

```
ADMIN (Full access)
  ↓
STAFF (Product/Order management)
  ↓
CUSTOMER (Own profile and orders)
```

#### RoleAuthorizationFilter

```java
@WebFilter("/admin/*")
public class RoleAuthorizationFilter implements Filter {
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        UUID userId = (UUID) request.getAttribute("userId");
        
        // Load user with role
        UserDao userDao = new UserDao();
        User user = userDao.findById(userId);
        
        // Check permission
        if ("ADMIN".equals(user.getRole())) {
            chain.doFilter(request, response);
        } else {
            HttpUtil.sendJson(response, 403, 
                new ErrorResponse("Insufficient permissions"));
        }
    }
}
```

#### Permission Matrix

| Endpoint Pattern | ADMIN | STAFF | CUSTOMER |
|------------------|-------|-------|----------|
| `/auth/*` | Public | Public | Public |
| `/products` (GET) | ✅ | ✅ | ✅ |
| `/products` (POST/PUT/DELETE) | ✅ | ✅ | ❌ |
| `/categories` (GET) | ✅ | ✅ | ✅ |
| `/categories` (POST/PUT/DELETE) | ✅ | ❌ | ❌ |
| `/cart/*` | ✅ | ✅ | ✅ (own only) |
| `/orders` (GET) | ✅ (all) | ✅ (all) | ✅ (own only) |
| `/orders` (POST) | ✅ | ✅ | ✅ |
| `/orders/*/status` (PUT) | ✅ | ✅ | ❌ |
| `/suppliers/*` | ✅ | ✅ | ❌ |
| `/purchase-orders/*` | ✅ | ✅ | ❌ |
| `/users/*` | ✅ | ❌ | ❌ |

---

### 8. Session Management

#### Session Security

```java
// Session configuration
session.setMaxInactiveInterval(30 * 60); // 30 minutes

// Session fixation protection
String oldSessionId = session.getId();
request.changeSessionId();
logger.info("Session ID changed from {} to {}", oldSessionId, session.getId());

// Session cleanup on logout
session.invalidate();
```

#### Automatic Session Cleanup

```java
@WebListener
public class SessionCleanupContextListener implements ServletContextListener {
    private ScheduledExecutorService scheduler;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        scheduler = Executors.newScheduledThreadPool(1);
        
        // Run cleanup every hour
        scheduler.scheduleAtFixedRate(() -> {
            SessionDao sessionDao = new SessionDao();
            int deleted = sessionDao.deleteExpiredSessions();
            logger.info("Cleaned up {} expired sessions", deleted);
        }, 0, 1, TimeUnit.HOURS);
    }
}
```

---

### 9. HTTPS & Transport Security

#### SSL/TLS Configuration

**Tomcat server.xml:**
```xml
<Connector port="8443" protocol="HTTP/1.1"
           maxThreads="150" SSLEnabled="true"
           scheme="https" secure="true">
    <SSLHostConfig>
        <Certificate certificateKeystoreFile="${catalina.home}/conf/keystore.jks"
                     certificateKeystorePassword="changeit"
                     type="RSA" />
    </SSLHostConfig>
</Connector>
```

**Force HTTPS (web.xml):**
```xml
<security-constraint>
    <web-resource-collection>
        <web-resource-name>Entire Application</web-resource-name>
        <url-pattern>/*</url-pattern>
    </web-resource-collection>
    <user-data-constraint>
        <transport-guarantee>CONFIDENTIAL</transport-guarantee>
    </user-data-constraint>
</security-constraint>
```

#### Security Headers

```java
public class SecurityHeadersFilter implements Filter {
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        // HSTS - Force HTTPS
        httpResponse.setHeader("Strict-Transport-Security", 
            "max-age=31536000; includeSubDomains; preload");
        
        // Prevent MIME type sniffing
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        
        // Clickjacking protection
        httpResponse.setHeader("X-Frame-Options", "DENY");
        
        // XSS protection
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Content Security Policy
        httpResponse.setHeader("Content-Security-Policy", 
            "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'");
        
        // Referrer policy
        httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions policy
        httpResponse.setHeader("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=()");
        
        chain.doFilter(request, response);
    }
}
```

---

### 10. Rate Limiting

#### Implementation (Recommended)

```java
@WebFilter("/*")
public class RateLimitFilter implements Filter {
    private Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        String clientIP = request.getRemoteAddr();
        TokenBucket bucket = buckets.computeIfAbsent(clientIP, 
            k -> new TokenBucket(100, 60)); // 100 requests per minute
        
        if (bucket.tryConsume()) {
            chain.doFilter(request, response);
        } else {
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setHeader("Retry-After", "60");
            HttpUtil.sendJson(httpResponse, 429, 
                new ErrorResponse("Rate limit exceeded"));
        }
    }
}
```

**Production Recommendations:**
- Use Redis for distributed rate limiting
- Different limits for different endpoints
- Higher limits for authenticated users
- Implement exponential backoff
- Use Nginx/Apache for infrastructure-level rate limiting

---

### 11. Logging & Monitoring

#### Security Event Logging

```java
// Failed login attempts
logger.warn("Failed login attempt for user: {} from IP: {}", 
    username, request.getRemoteAddr());

// Unauthorized access
logger.warn("Unauthorized access attempt to {} by user: {}", 
    request.getRequestURI(), userId);

// Token validation failures
logger.warn("Invalid JWT token from IP: {}", request.getRemoteAddr());

// Suspicious activities
logger.error("Potential SQL injection detected: {}", suspiciousInput);
logger.error("Potential XSS attack detected: {}", suspiciousInput);
```

#### Security Metrics to Monitor

1. **Authentication Events:**
   - Failed login attempts (potential brute-force)
   - Successful logins from new IP addresses
   - Token refresh patterns

2. **Authorization Events:**
   - Access denied (403 Forbidden)
   - Privilege escalation attempts

3. **Anomalies:**
   - Unusual request patterns
   - Spike in error rates
   - Abnormal API usage

4. **Infrastructure:**
   - Failed database connections
   - High CPU/memory usage
   - Network errors

---

### 12. Dependency Security

#### Vulnerability Scanning

```bash
# Maven dependency check
mvn org.owasp:dependency-check-maven:check

# Update dependencies
mvn versions:display-dependency-updates

# Security audit with Snyk
snyk test
```

#### Secure Dependencies

**Keep up-to-date:**
- Regular updates for security patches
- Monitor CVE databases
- Subscribe to security advisories
- Use automated tools (Dependabot, Snyk)

**Current Secure Versions:**
- Java 17 LTS (latest security updates)
- Hibernate 6.4.4.Final
- PostgreSQL JDBC 42.7.2
- JJWT 0.11.5
- BCrypt 0.4

---

## Security Checklist

### Development

- [x] JWT authentication with token rotation
- [x] BCrypt password hashing (work factor: 12)
- [x] Parameterized queries (SQL injection prevention)
- [x] Input validation (server-side)
- [x] Output encoding (XSS prevention)
- [x] CORS configuration with origin whitelist
- [x] Role-based access control (RBAC)
- [x] Session management with auto-cleanup
- [x] HTTP-only cookies for sensitive tokens
- [ ] CSRF protection (implement if using forms)
- [ ] Rate limiting (recommended for production)

### Deployment

- [ ] HTTPS/TLS enabled
- [ ] Security headers configured
- [ ] Firewall rules configured
- [ ] Database credentials secured
- [ ] Environment variables for secrets
- [ ] Regular security audits
- [ ] Automated vulnerability scanning
- [ ] Intrusion detection system (IDS)
- [ ] Web Application Firewall (WAF)
- [ ] DDoS protection

### Operational

- [ ] Security logging enabled
- [ ] Log aggregation and monitoring
- [ ] Incident response plan
- [ ] Regular backups
- [ ] Disaster recovery plan
- [ ] Security awareness training
- [ ] Penetration testing
- [ ] Compliance audits (GDPR, PCI-DSS if applicable)

---

## Security Incident Response

### Incident Response Plan

1. **Detection**: Identify security incident through monitoring/alerts
2. **Containment**: Isolate affected systems
3. **Eradication**: Remove threat and close vulnerabilities
4. **Recovery**: Restore systems to normal operation
5. **Post-Incident**: Document lessons learned, improve defenses

### Common Incidents

**Brute Force Attack:**
```bash
# Detect: High number of failed login attempts from single IP
# Response: Implement account lockout, add CAPTCHA, ban IP
```

**SQL Injection Attempt:**
```bash
# Detect: Suspicious patterns in logs (UNION, DROP, etc.)
# Response: Validate all parameterized queries, add WAF rules
```

**XSS Attack:**
```bash
# Detect: Script tags in user input
# Response: Sanitize input, encode output, CSP headers
```

---

## Compliance

### OWASP Top 10 Coverage

| Risk | Mitigation |
|------|------------|
| A01: Broken Access Control | RBAC, JWT authentication, authorization filters |
| A02: Cryptographic Failures | BCrypt hashing, HTTPS, secure tokens |
| A03: Injection | Parameterized queries, input validation |
| A04: Insecure Design | Secure architecture, threat modeling |
| A05: Security Misconfiguration | Secure defaults, security headers |
| A06: Vulnerable Components | Regular updates, vulnerability scanning |
| A07: Authentication Failures | Strong passwords, MFA (recommended), token rotation |
| A08: Software & Data Integrity | Code signing, dependency verification |
| A09: Logging & Monitoring | Comprehensive logging, alerting |
| A10: SSRF | Input validation, URL whitelisting |

---

## Additional Resources

- [OWASP Cheat Sheets](https://cheatsheetseries.owasp.org/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc8725)
- [PostgreSQL Security](https://www.postgresql.org/docs/current/security.html)
- [Hibernate Security Guide](https://hibernate.org/orm/documentation/)

---

## Related Documentation

- [Installation Guide](./INSTALLATION.md)
- [API Documentation](./API_DOCUMENTATION.md)
- [Architecture Guide](./ARCHITECTURE.md)
- [Database Schema](./DATABASE.md)
- [Contributing Guide](./CONTRIBUTING.md)

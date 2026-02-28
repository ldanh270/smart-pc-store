🛒 Cart Module — Smart PC Store

============================================================
ARCHITECTURE
============================================================

CartServlet → CartController → CartService → DAO (CartDao, CartItemDao, GenericDao<Product>)

| Layer      | File                     | Responsibility                             |
| ---------- | ------------------------ | ------------------------------------------ |
| Servlet    | CartServlet.java         | Receive HTTP requests, route to Controller |
| Controller | CartController.java      | Parse request, extract JWT, call Service   |
| Service    | CartService.java         | Business logic, transaction management     |
| Entity     | CartItem.java, Cart.java | JPA entity mapping to database             |
| DTO        | CartItemResponseDto.java | Response object returned to client         |

============================================================
FIXES IMPLEMENTED
============================================================

1. Shared EntityManager → thread-unsafe
   Fix: Create EM per request using try-with-resources

2. CartItemResponseDto could throw NPE when price = null
   Fix: Added null-check, return subtotal = 0

3. addToCart did not validate productId = null
   Fix: Throw "Product ID is required" if null

4. Response missing stockQuantity
   Fix: Added stockQuantity field to DTO

5. doPost returned 400 without message
   Fix: Use HttpUtil.sendJson instead of sendError

6. No functionality to clear entire cart
   Fix: Added clearCart() + DELETE /cart/

7. JwtUtil.getUserIdFromAuthorizationHeader not implemented
   Fix: Implemented JWT parsing from Bearer header

8. AuthServlet used singleton EntityManager
   Fix: Changed to per-request EntityManager

9. .env file not found when running Tomcat
   Fix: Copied .env into src/main/resources/

============================================================
API REFERENCE
============================================================

Base URL:
http://localhost:8080/smart-pc-store

All Cart APIs require header:
Authorization: Bearer <accessToken>

---

## AUTH

POST /auth/signup
Body: {username, password, fullName, email}
Response: 201 Created

POST /auth/login
Body: {username, password}
Response: 200 OK + {accessToken, refreshToken}

POST /auth/logout
Body: {refreshToken}
Response: 204 No Content

---

## CART

GET /cart/
Description: Get current user's cart

POST /cart/add
Body: {productId, quantity}
Description: Add product to cart

PUT /cart/items/{id}
Body: {quantity}
Description: Update item quantity

DELETE /cart/items/{id}
Description: Remove a cart item

DELETE /cart/
Description: Clear entire cart

Sample GET /cart/ Response:

[
{
"cartItemId": 1,
"productId": 5,
"productName": "AMD Ryzen 5 5600X",
"price": 4990000,
"quantity": 2,
"subtotal": 9980000,
"stockQuantity": 15
}
]

============================================================
STATUS CODES
============================================================

200 OK - Success
201 Created - Item added successfully
400 Bad Request - Business error (insufficient stock, invalid input, etc.)
401 Unauthorized - Missing or invalid token

============================================================
ENVIRONMENT SETUP
============================================================

Requirements:

- Java 17+
- Apache Tomcat 10.1+
- SQL Server
- .env file in src/main/resources/

.env must contain at least:
ACCESS_TOKEN_SECRET=<string with at least 64 characters>

Run project:

1. Clean & Build project (Maven)
2. Deploy WAR to Tomcat
3. Access http://localhost:8080/smart-pc-store

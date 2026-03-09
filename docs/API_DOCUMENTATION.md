# API Documentation

## Base URL

```
http://localhost:8080/smart-pc-store
```

**Content-Type**: `application/json` for all requests  
**Authentication**: Bearer Token in Authorization header (except public endpoints)

## Table of Contents

- [Authentication](#authentication)
- [Products](#products)
- [Categories](#categories)
- [Shopping Cart](#shopping-cart)
- [Orders](#orders)
- [Suppliers](#suppliers)
- [Purchase Orders](#purchase-orders)
- [User Management](#user-management)
- [Error Responses](#error-responses)

---

## Authentication

### Register User

```http
POST /auth/signup
```

**Request Body:**
```json
{
  "username": "john_doe",
  "email": "john@example.com",
  "password": "SecurePass123!",
  "displayName": "John Doe",
  "phone": "0901234567",
  "address": "123 Main Street, HCM City"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "user": {
      "id": "uuid-string",
      "username": "john_doe",
      "email": "john@example.com",
      "displayName": "John Doe",
      "role": "CUSTOMER",
      "status": "ACTIVE"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "uuid-refresh-token"
  }
}
```

### Login

```http
POST /auth/login
```

**Request Body:**
```json
{
  "username": "john_doe",
  "password": "SecurePass123!"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "user": {
      "id": "uuid",
      "username": "john_doe",
      "email": "john@example.com",
      "displayName": "John Doe",
      "role": "CUSTOMER"
    },
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "uuid-refresh-token"
  }
}
```

**Set-Cookie Header:**
```
refreshToken=uuid-refresh-token; HttpOnly; Secure; Path=/; Max-Age=604800
```

### Refresh Token

```http
POST /auth/refresh
```

**Headers:**
```
Cookie: refreshToken=uuid-refresh-token
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "new-access-token",
    "refreshToken": "new-refresh-token"
  }
}
```

### Logout

```http
POST /auth/logout
```

**Headers:**
```
Cookie: refreshToken=uuid-refresh-token
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Logged out successfully"
}
```

---

## Products

### Get All Products

```http
GET /products?page=1&limit=20&categoryId=1&search=laptop&sort=price&order=asc
```

**Query Parameters:**
- `page`: Page number (default: 1)
- `limit`: Items per page (default: 20)
- `categoryId`: Filter by category ID (optional)
- `search`: Search by product name (optional)
- `sort`: Sort field (price, name, createdAt) (optional)
- `order`: Sort order (asc, desc) (optional)

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "products": [
      {
        "id": 1,
        "productName": "Intel Core i9-13900K",
        "description": "24-core Desktop Processor",
        "price": 589.99,
        "stock": 50,
        "imageUrl": "https://example.com/image.jpg",
        "category": {
          "id": 1,
          "categoryName": "CPU"
        },
        "supplier": {
          "id": 1,
          "supplierName": "Intel"
        },
        "status": "AVAILABLE"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 5,
      "totalItems": 100,
      "itemsPerPage": 20
    }
  }
}
```

### Get Product by ID

```http
GET /products/{id}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": 1,
    "productName": "Intel Core i9-13900K",
    "description": "24-core, 32-thread Desktop Processor with integrated graphics",
    "price": 589.99,
    "stock": 50,
    "imageUrl": "https://example.com/image.jpg",
    "category": {
      "id": 1,
      "categoryName": "CPU",
      "parentId": null
    },
    "supplier": {
      "id": 1,
      "supplierName": "Intel Corporation",
      "contactInfo": "contact@intel.com"
    },
    "status": "AVAILABLE",
    "createdAt": "2024-01-15T10:30:00Z"
  }
}
```

### Create Product

**Authentication Required: ADMIN/STAFF**

```http
POST /products
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "productName": "AMD Ryzen 9 7950X",
  "description": "16-core, 32-thread Desktop Processor",
  "price": 699.99,
  "stock": 30,
  "imageUrl": "https://example.com/ryzen.jpg",
  "categoryId": 1,
  "supplierId": 2
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Product created successfully",
  "data": {
    "id": 101,
    "productName": "AMD Ryzen 9 7950X",
    "price": 699.99,
    "stock": 30
  }
}
```

### Update Product

**Authentication Required: ADMIN/STAFF**

```http
PUT /products/{id}
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "price": 649.99,
  "stock": 45
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Product updated successfully"
}
```

### Delete Product

**Authentication Required: ADMIN**

```http
DELETE /products/{id}
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Product deleted successfully"
}
```

---

## Categories

### Get All Categories

```http
GET /categories
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "categoryName": "CPU",
      "description": "Central Processing Units",
      "imageUrl": "https://example.com/cpu-icon.jpg",
      "status": true,
      "parentId": null,
      "productCount": 45
    },
    {
      "id": 2,
      "categoryName": "GPU",
      "description": "Graphics Processing Units",
      "imageUrl": "https://example.com/gpu-icon.jpg",
      "status": true,
      "parentId": null,
      "productCount": 38
    }
  ]
}
```

### Create Category

**Authentication Required: ADMIN**

```http
POST /categories
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "categoryName": "Storage",
  "description": "SSDs and Hard Drives",
  "imageUrl": "https://example.com/storage-icon.jpg",
  "parentId": null
}
```

---

## Shopping Cart

### View Cart

**Authentication Required**

```http
GET /cart
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "cartId": "uuid",
    "items": [
      {
        "id": "item-uuid",
        "product": {
          "id": 1,
          "productName": "Intel Core i9-13900K",
          "price": 589.99,
          "imageUrl": "https://example.com/image.jpg"
        },
        "quantity": 2,
        "priceAtAdd": 589.99,
        "subtotal": 1179.98
      }
    ],
    "totalItems": 3,
    "totalAmount": 2450.00
  }
}
```

### Add to Cart

**Authentication Required**

```http
POST /cart/add
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "productId": 1,
  "quantity": 2
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Product added to cart",
  "data": {
    "cartItemId": "uuid",
    "quantity": 2
  }
}
```

### Update Cart Item

**Authentication Required**

```http
PUT /cart/items/{itemId}
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "quantity": 5
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Cart item updated"
}
```

### Remove from Cart

**Authentication Required**

```http
DELETE /cart/items/{itemId}
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Item removed from cart"
}
```

### Clear Cart

**Authentication Required**

```http
DELETE /cart/clear
Authorization: Bearer {access_token}
```

---

## Orders

### Create Order / Checkout

**Authentication Required**

```http
POST /orders/checkout
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "shippingAddress": "123 Main Street, District 1, HCM City",
  "shippingPhone": "0901234567",
  "notes": "Please deliver in the morning",
  "paymentMethod": "COD"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Order created successfully",
  "data": {
    "orderId": "uuid",
    "orderDate": "2024-03-09T14:30:00Z",
    "status": "PENDING",
    "totalAmount": 2450.00,
    "items": [
      {
        "productName": "Intel Core i9-13900K",
        "quantity": 2,
        "unitPrice": 589.99,
        "subtotal": 1179.98
      }
    ]
  }
}
```

### Get User Orders

**Authentication Required**

```http
GET /orders/my-orders?page=1&limit=10&status=PENDING
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "orders": [
      {
        "id": "uuid",
        "orderDate": "2024-03-09T14:30:00Z",
        "status": "SHIPPING",
        "totalAmount": 2450.00,
        "itemCount": 3
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 2,
      "totalItems": 15
    }
  }
}
```

### Get Order Details

**Authentication Required**

```http
GET /orders/{orderId}
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "id": "uuid",
    "orderDate": "2024-03-09T14:30:00Z",
    "status": "SHIPPING",
    "totalAmount": 2450.00,
    "shippingAddress": "123 Main Street, District 1, HCM City",
    "shippingPhone": "0901234567",
    "notes": "Please deliver in the morning",
    "items": [
      {
        "product": {
          "id": 1,
          "productName": "Intel Core i9-13900K",
          "imageUrl": "https://example.com/image.jpg"
        },
        "quantity": 2,
        "unitPrice": 589.99,
        "subtotal": 1179.98
      }
    ],
    "payment": {
      "paymentMethod": "COD",
      "paymentStatus": "PENDING",
      "amount": 2450.00
    }
  }
}
```

### Update Order Status

**Authentication Required: ADMIN/STAFF**

```http
PUT /orders/{orderId}/status
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "status": "CONFIRMED"
}
```

**Valid Statuses**: PENDING → PROCESSING → CONFIRMED → SHIPPING → DELIVERED → COMPLETED

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Order status updated to CONFIRMED"
}
```

### Cancel Order

**Authentication Required**

```http
PUT /orders/{orderId}/cancel
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Order cancelled successfully"
}
```

---

## Suppliers

### Get All Suppliers

**Authentication Required: ADMIN/STAFF**

```http
GET /suppliers
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "supplierName": "Intel Corporation",
      "contactInfo": "2200 Mission College Blvd, Santa Clara, CA",
      "leadTimeDays": 14,
      "productCount": 45
    }
  ]
}
```

### Create Supplier

**Authentication Required: ADMIN**

```http
POST /suppliers
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "supplierName": "ASUS Technology",
  "contactInfo": "support@asus.com, +886-2-2894-3447",
  "leadTimeDays": 10
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Supplier created successfully",
  "data": {
    "id": 5,
    "supplierName": "ASUS Technology"
  }
}
```

---

## Purchase Orders

### Create Purchase Order

**Authentication Required: ADMIN/STAFF**

```http
POST /purchase-orders
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "supplierId": 1,
  "expectedDeliveryDate": "2024-03-25",
  "items": [
    {
      "productId": 1,
      "quantity": 100,
      "unitCost": 450.00
    },
    {
      "productId": 5,
      "quantity": 50,
      "unitCost": 850.00
    }
  ],
  "notes": "Urgent order for Q2 stock"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Purchase order created successfully",
  "data": {
    "purchaseOrderId": "uuid",
    "totalCost": 87500.00,
    "status": "PENDING"
  }
}
```

### Get Purchase Orders

**Authentication Required: ADMIN/STAFF**

```http
GET /purchase-orders?status=PENDING&page=1&limit=20
Authorization: Bearer {access_token}
```

### Update Purchase Order Status

**Authentication Required: ADMIN/STAFF**

```http
PUT /purchase-orders/{id}/status
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "status": "CONFIRMED"
}
```

**Valid Statuses**: PENDING → CONFIRMED → RECEIVED → CANCELLED

---

## User Management

### Get All Users

**Authentication Required: ADMIN**

```http
GET /users?page=1&limit=20&role=CUSTOMER&status=ACTIVE
Authorization: Bearer {access_token}
```

**Response (200 OK):**
```json
{
  "success": true,
  "data": {
    "users": [
      {
        "id": "uuid",
        "username": "john_doe",
        "email": "john@example.com",
        "displayName": "John Doe",
        "role": "CUSTOMER",
        "status": "ACTIVE",
        "createdAt": "2024-01-15T10:30:00Z"
      }
    ],
    "pagination": {
      "currentPage": 1,
      "totalPages": 5,
      "totalItems": 100
    }
  }
}
```

### Update User Role

**Authentication Required: ADMIN**

```http
PUT /users/{userId}/role
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "role": "STAFF"
}
```

**Valid Roles**: CUSTOMER, STAFF, ADMIN

**Response (200 OK):**
```json
{
  "success": true,
  "message": "User role updated to STAFF"
}
```

### Get User Profile

**Authentication Required**

```http
GET /profile
Authorization: Bearer {access_token}
```

### Update User Profile

**Authentication Required**

```http
PUT /profile
Authorization: Bearer {access_token}
```

**Request Body:**
```json
{
  "displayName": "John Smith",
  "phone": "0901234567",
  "address": "456 New Address, HCM City"
}
```

---

## Error Responses

### 400 Bad Request
```json
{
  "success": false,
  "error": "Validation failed",
  "details": [
    "Email is required",
    "Password must be at least 8 characters"
  ]
}
```

### 401 Unauthorized
```json
{
  "success": false,
  "error": "Unauthorized",
  "message": "Invalid or expired token"
}
```

### 403 Forbidden
```json
{
  "success": false,
  "error": "Forbidden",
  "message": "You don't have permission to access this resource"
}
```

### 404 Not Found
```json
{
  "success": false,
  "error": "Not found",
  "message": "Product with id 999 not found"
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "error": "Internal server error",
  "message": "An unexpected error occurred"
}
```

---

## Testing with cURL

### Example: Register and Login Flow

```bash
# 1. Register new user
curl -X POST http://localhost:8080/smart-pc-store/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test123!",
    "displayName": "Test User",
    "phone": "0901234567",
    "address": "123 Test Street"
  }'

# 2. Login
curl -X POST http://localhost:8080/smart-pc-store/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "Test123!"
  }' \
  -c cookies.txt

# 3. Get products (with access token from login response)
curl -X GET http://localhost:8080/smart-pc-store/products \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE"

# 4. Add to cart
curl -X POST http://localhost:8080/smart-pc-store/cart/add \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN_HERE" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

## Postman Collection

For easier testing, import the Postman collection (if available) or create environment variables:

**Environment Variables:**
```
BASE_URL: http://localhost:8080/smart-pc-store
ACCESS_TOKEN: {{access_token}}
REFRESH_TOKEN: {{refresh_token}}
```

---

## Rate Limits

**Current Implementation**: No rate limiting  
**Recommended for Production**: 
- 100 requests per minute per IP for public endpoints
- 1000 requests per minute for authenticated users
- Implement using Redis or Nginx rate limiting

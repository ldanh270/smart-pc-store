# 🛒 Cart Module — Smart PC Store

## Kiến trúc

```
CartServlet  →  CartController  →  CartService  →  DAO (CartDao, CartItemDao, GenericDao<Product>)
```

| Lớp        | File                         | Vai trò                                 |
| ---------- | ---------------------------- | --------------------------------------- |
| Servlet    | `CartServlet.java`           | Nhận HTTP request, route đến Controller |
| Controller | `CartController.java`        | Parse request, parse JWT, gọi Service   |
| Service    | `CartService.java`           | Business logic, transaction             |
| Entity     | `CartItem.java`, `Cart.java` | JPA entity mapping DB                   |
| DTO        | `CartItemResponseDto.java`   | Response trả về client                  |

---

## Những fix đã thực hiện

| #   | Vấn đề                                                    | Fix                                       |
| --- | --------------------------------------------------------- | ----------------------------------------- |
| 1   | `EntityManager` dùng chung → thread-unsafe                | EM per-request trong `try-with-resources` |
| 2   | `CartItemResponseDto` có thể NPE khi `price = null`       | Thêm null-check, trả `subtotal = 0`       |
| 3   | `addToCart` không validate `productId = null`             | Throw `"Product ID is required"` nếu null |
| 4   | Response không có `stockQuantity`                         | Thêm field `stockQuantity` vào DTO        |
| 5   | `doPost` trả 400 không có message                         | Dùng `HttpUtil.sendJson` thay `sendError` |
| 6   | Không có chức năng xóa toàn bộ giỏ                        | Thêm `clearCart()` + `DELETE /cart/`      |
| 7   | `JwtUtil.getUserIdFromAuthorizationHeader` chưa implement | Implement parse JWT từ Bearer header      |
| 8   | `AuthServlet` dùng EM singleton                           | Fix EM per-request giống CartServlet      |
| 9   | `.env` không được tìm thấy khi chạy Tomcat                | Copy `.env` vào `src/main/resources/`     |

---

## API Reference

Base URL: `http://localhost:8080/smart-pc-store`

> **Tất cả Cart API đều yêu cầu Header:**
> `Authorization: Bearer <accessToken>`

### Auth (lấy token trước)

| Method | Endpoint       | Body                                    | Response                            |
| ------ | -------------- | --------------------------------------- | ----------------------------------- |
| POST   | `/auth/signup` | `{username, password, fullName, email}` | 201 Created                         |
| POST   | `/auth/login`  | `{username, password}`                  | 200 + `{accessToken, refreshToken}` |
| POST   | `/auth/logout` | `{refreshToken}`                        | 204 No Content                      |

### Cart

| Method | Endpoint           | Body                    | Mô tả                 |
| ------ | ------------------ | ----------------------- | --------------------- |
| GET    | `/cart/`           | —                       | Lấy giỏ hàng của user |
| POST   | `/cart/add`        | `{productId, quantity}` | Thêm sản phẩm vào giỏ |
| PUT    | `/cart/items/{id}` | `{quantity}`            | Cập nhật số lượng     |
| DELETE | `/cart/items/{id}` | —                       | Xóa 1 sản phẩm        |
| DELETE | `/cart/`           | —                       | Xóa toàn bộ giỏ       |

#### GET /cart/ — Response mẫu

```json
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
```

#### Status Codes

| Code             | Ý nghĩa                                                             |
| ---------------- | ------------------------------------------------------------------- |
| 200 OK           | Thành công                                                          |
| 201 Created      | Thêm sản phẩm thành công                                            |
| 400 Bad Request  | Lỗi business (không đủ hàng, productId null, item không tồn tại...) |
| 401 Unauthorized | Token thiếu hoặc sai format                                         |

---

## Hướng dẫn test với Postman

### Bước 1 — Signup (nếu chưa có tài khoản)

```
POST {{BASE_URL}}/auth/signup
Headers:  Content-Type: application/json
Body:
{
  "username": "testuser",
  "password": "Test@12345",
  "fullName": "Test User",
  "email": "test@example.com"
}
→ Expect: 201 Created
```

### Bước 2 — Login lấy token

```
POST {{BASE_URL}}/auth/login
Headers:  Content-Type: application/json
Body:
{
  "username": "testuser",
  "password": "Test@12345"
}
→ Expect: 200 OK + accessToken trong response
   Copy accessToken để dùng ở các bước sau
```

### Bước 3 — Xem giỏ hàng (giỏ rỗng)

```
GET {{BASE_URL}}/cart/
Headers:  Authorization: Bearer <accessToken>
→ Expect: 200 OK + []
```

### Bước 4 — Thêm sản phẩm

`
POST {{BASE_URL}}/cart/add
Headers: Authorization: Bearer <accessToken>
Content-Type: application/json
Body:
{
"productId": 1,
"quantity": 2
}
→ Expect: 201 Created + "Product added to cart successfully"
Lưu ý: productId phải tồn tại trong DB

```

### Bước 5 — Xem giỏ, lấy cartItemId
```

GET {{BASE_URL}}/cart/
Headers: Authorization: Bearer <accessToken>
→ Expect: 200 OK + danh sách items có cartItemId
Copy cartItemId để dùng ở bước 6, 7

```

### Bước 6 — Cập nhật số lượng
```

PUT {{BASE_URL}}/cart/items/{cartItemId}
ex: {{BASE_URL}}/cart/items/1
Headers: Authorization: Bearer <accessToken>
Content-Type: application/json
Body:
{
"quantity": 5
}
→ Expect: 200 OK + "Cart item updated successfully"

```

### Bước 7 — Xóa 1 sản phẩm
```

DELETE {{BASE_URL}}/cart/items/{cartItemId}
Headers: Authorization: Bearer <accessToken>
→ Expect: 200 OK + "Cart item removed successfully"

```

### Bước 8 — Xóa toàn bộ giỏ
```

DELETE {{BASE_URL}}/cart/
Headers: Authorization: Bearer <accessToken>
→ Expect: 200 OK + "Cart cleared successfully"

```

### Test edge cases

| Test case | Request | Expect |
|---|---|---|
| Thêm khi hết hàng | POST /cart/add với qty > stock | 400 "Not enough stock" |
| productId không tồn tại | POST /cart/add với productId = 9999 | 400 "Product not found" |
| productId null | POST /cart/add thiếu productId | 400 "Product ID is required" |
| quantity = 0 | PUT /cart/items/{id} với qty = 0 | 200, item bị tự xóa |
| Không có token | GET /cart/ thiếu Authorization | 400 "Missing or invalid Authorization header" |

---

## Setup môi trường

### Yêu cầu
- Java 17+
- Apache Tomcat 10.1+
- SQL Server
- File `.env` ở `src/main/resources/` (có `ACCESS_TOKEN_SECRET`)

### Chạy project
1. Clean & Build project (Maven)
2. Deploy WAR lên Tomcat
3. Truy cập `http://localhost:8080/smart-pc-store`

### File .env
File `.env` phải có ít nhất:
```

ACCESS_TOKEN_SECRET=<chuỗi ít nhất 64 ký tự>

```

```

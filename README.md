# 🖥️ Smart PC Store

Dự án web application bán linh kiện và máy tính sử dụng Java Servlet với Jakarta EE.

## 📋 Mô tả

Smart PC Store là một ứng dụng web thương mại điện tử cho phép người dùng duyệt, tìm kiếm và mua sắm
các sản phẩm linh kiện máy tính và PC.

## 🛠️ Công nghệ sử dụng

| Công nghệ           | Phiên bản |
| ------------------- | --------- |
| Java                | 17        |
| Jakarta Servlet API | 6.0.0     |
| Maven               | 3.x       |
| SQL Server          | -         |
| JUnit               | 4.11      |

## 📁 Cấu trúc dự án

```
smart-pc-store/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controllers/    # Servlet controllers
│   │   │   ├── dao/            # Data Access Objects
│   │   │   ├── models/         # Entity models
│   │   │   └── services/       # Business logic services
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       └── index.jsp
│   └── test/                   # Unit tests
├── .env                        # Environment variables
├── pom.xml                     # Maven configuration
└── README.md
```

## ⚙️ Cấu hình

### Biến môi trường

Tạo file `.env` trong thư mục gốc với nội dung sau:

```env
DB_DRIVER_NAME=com.microsoft.sqlserver.jdbc.SQLServerDriver
DB_URL=jdbc:sqlserver://localhost:1433;databaseName=your_database;encrypt=false;trustServerCertificate=true
DB_USER=your_username
DB_PASSWORD=your_password
```

### Yêu cầu hệ thống

- **JDK 17** hoặc cao hơn
- **Apache Maven 3.x**
- **SQL Server**
- **Apache Tomcat 10.x** hoặc server hỗ trợ Jakarta Servlet 6.0

## 🚀 Hướng dẫn cài đặt

### 1. Clone repository

```bash
git clone https://github.com/ldanh270/smart-pc-store.git
cd smart-pc-store
```

### 2. Cài đặt dependencies

```bash
mvn clean install
```

### 3. Cấu hình database

1. Tạo database trong SQL Server
2. Cập nhật thông tin kết nối trong file `.env`
3. Chạy các script SQL để tạo bảng (nếu có)

### 4. Build và deploy

```bash
# Build file WAR
mvn clean package

# Deploy file WAR lên Tomcat hoặc server
# File WAR nằm tại: target/smart-pc-store.war
```

### 5. Chạy ứng dụng

Sau khi deploy, truy cập ứng dụng tại:

```
http://localhost:8080/smart-pc-store
```

## 📖 API Endpoints

| Method | Endpoint | Mô tả         |
| ------ | -------- | ------------- |
| GET    | `/hello` | Test endpoint |

## 🧪 Chạy tests

```bash
mvn test
```

## 👥 Nhóm phát triển

- **ldanh270** - Developer

## 📄 License

Dự án này được phát triển cho mục đích học tập.

---

⭐ Nếu dự án hữu ích, hãy cho chúng tôi một star!

# Installation Guide

## System Requirements

| Tool | Version | Download Link |
|------|---------|---------------|
| Java JDK | 17 LTS or higher | [Adoptium Temurin](https://adoptium.net/) |
| Apache Maven | 3.8.x or higher | [Maven Download](https://maven.apache.org/download.cgi) |
| PostgreSQL | 13+ (recommended 15+) | [PostgreSQL Download](https://www.postgresql.org/download/) |
| Apache Tomcat | 10.1.x | [Tomcat 10 Download](https://tomcat.apache.org/download-10.cgi) |
| Git | Latest | [Git Download](https://git-scm.com/) |
| Docker | Latest (Optional) | [Docker Desktop](https://www.docker.com/products/docker-desktop/) |

## Verify Installation

```bash
# Check Java version
java -version
# Output: openjdk version "17.0.x"

# Check Maven
mvn -version
# Output: Apache Maven 3.x.x

# Check PostgreSQL
psql --version
# Output: psql (PostgreSQL) 15.x

# Check Git
git --version
# Output: git version 2.x.x
```

## Installation Steps

### Step 1: Clone Repository

```bash
git clone https://github.com/ldanh270/smart-pc-store.git
cd smart-pc-store
```

### Step 2: Configure Database

#### 2.1. Create PostgreSQL Database

```bash
# Connect to PostgreSQL
psql -U postgres

# Create database
CREATE DATABASE smart_pc_store;

# Create user (optional)
CREATE USER smartpc_admin WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE smart_pc_store TO smartpc_admin;

# Exit psql
\q
```

#### 2.2. Import Database Schema

```bash
# Import schema.sql
psql -U postgres -d smart_pc_store -f plan/schema.sql

# Import sample data (optional)
psql -U postgres -d smart_pc_store -f plan/data.sql
```

**Alternative: Using GUI tools (pgAdmin/DBeaver)**
1. Connect to PostgreSQL server
2. Create new database `smart_pc_store`
3. Execute `plan/schema.sql`
4. Execute `plan/data.sql` (optional)

### Step 3: Configure Persistence Unit

Edit `src/main/resources/META-INF/persistence.xml`:

```xml
<properties>
    <property name="jakarta.persistence.jdbc.driver" value="org.postgresql.Driver"/>
    <property name="jakarta.persistence.jdbc.url" 
              value="jdbc:postgresql://localhost:5432/smart_pc_store"/>
    <property name="jakarta.persistence.jdbc.user" value="postgres"/>
    <property name="jakarta.persistence.jdbc.password" value="your_password_here"/>

    <property name="hibernate.dialect" value="org.hibernate.dialect.PostgreSQLDialect"/>
    <property name="hibernate.hbm2ddl.auto" value="update"/>
    <property name="hibernate.show_sql" value="true"/>
    <property name="hibernate.format_sql" value="true"/>
</properties>
```

**Configuration values to change:**
- `jdbc.url`: Database host and port (default: `localhost:5432`)
- `jdbc.user`: Your PostgreSQL username
- `jdbc.password`: Your PostgreSQL password

### Step 4: Configure Environment Variables

Create `.env` file in project root:

```bash
# Create .env file
touch .env  # Linux/Mac
type nul > .env  # Windows
```

Add the following content to `.env`:

```env
# JWT Configuration
ACCESS_TOKEN_SECRET=your_256_bit_secret_key_here_please_change_this_to_a_secure_random_string

# Example: Generate random secret key
# ACCESS_TOKEN_SECRET=9a3f8b2c7d1e6f5a4b3c2d1e9f8a7b6c5d4e3f2a1b0c9d8e7f6a5b4c3d2e1f0a

# Database Configuration (optional, if not in persistence.xml)
DB_HOST=localhost
DB_PORT=5432
DB_NAME=smart_pc_store
DB_USER=postgres
DB_PASSWORD=your_password

# Application Configuration
APP_PORT=8080
APP_ENV=development
```

**Generate secure secret key (256-bit):**

```bash
# Using PowerShell (Windows)
powershell -Command "[System.Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))"

# Using OpenSSL (Linux/Mac)
openssl rand -hex 32

# Or use online tool
# https://www.allkeysgenerator.com/Random/Security-Encryption-Key-Generator.aspx
```

### Step 5: Build Project

```bash
# Clean and build project
mvn clean package

# Output: target/smart-pc-store.war
```

**Maven will:**
1. Download all dependencies from `pom.xml`
2. Compile source code (`.java` → `.class`)
3. Run tests (skip with `-DskipTests`)
4. Package into `smart-pc-store.war`

### Step 6: Deploy to Apache Tomcat

#### Option 1: Manual Deployment

```bash
# Copy WAR file to Tomcat webapps
cp target/smart-pc-store.war /path/to/tomcat/webapps/

# Start Tomcat
cd /path/to/tomcat/bin
./startup.sh  # Linux/Mac
startup.bat   # Windows

# Tomcat will automatically extract and deploy WAR file
```

#### Option 2: IDE Deployment (IntelliJ IDEA)

1. **File → Project Structure → Artifacts**
2. **Add → Web Application: Archive → From module 'smart-pc-store'**
3. **Run → Edit Configurations → Add New Configuration → Tomcat Server → Local**
4. **Configure Tomcat home directory**
5. **Deployment tab → Add → smart-pc-store:war**
6. **Run** to start server

#### Option 3: Eclipse Deployment

1. **Window → Preferences → Server → Runtime Environments → Add → Apache Tomcat 10.1**
2. **Right-click project → Properties → Targeted Runtimes → Apache Tomcat 10.1**
3. **Right-click project → Run As → Run on Server**

### Step 7: Verify Deployment

Open browser and navigate to:

```
http://localhost:8080/smart-pc-store
```

**Test API endpoints:**

```bash
# Health check
curl http://localhost:8080/smart-pc-store/

# Test authentication endpoint
curl -X POST http://localhost:8080/smart-pc-store/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Expected response:**
```json
{
  "success": true,
  "data": {
    "user": {...},
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "uuid-string-here"
  }
}
```

## Docker Deployment

### Build and Run with Docker

```bash
# Build Docker image
docker build -t smart-pc-store:latest .

# Run container
docker run -d \
  -p 8080:8080 \
  --name smart-pc-store-app \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=smart_pc_store \
  -e DB_USER=postgres \
  -e DB_PASSWORD=your_password \
  -e ACCESS_TOKEN_SECRET=your_secret_key \
  smart-pc-store:latest

# View logs
docker logs -f smart-pc-store-app

# Stop container
docker stop smart-pc-store-app

# Remove container
docker rm smart-pc-store-app
```

### Docker Compose (Recommended)

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: smart-pc-store-db
    environment:
      POSTGRES_DB: smart_pc_store
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./plan/schema.sql:/docker-entrypoint-initdb.d/01-schema.sql
      - ./plan/data.sql:/docker-entrypoint-initdb.d/02-data.sql
    networks:
      - app-network

  app:
    build: .
    container_name: smart-pc-store-app
    ports:
      - "8080:8080"
    environment:
      DB_HOST: postgres
      DB_PORT: 5432
      DB_NAME: smart_pc_store
      DB_USER: postgres
      DB_PASSWORD: postgres
      ACCESS_TOKEN_SECRET: your_256_bit_secret_key_here
    depends_on:
      - postgres
    networks:
      - app-network

volumes:
  postgres_data:

networks:
  app-network:
    driver: bridge
```

**Run with Docker Compose:**

```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop all services
docker-compose down

# Stop and remove volumes
docker-compose down -v
```

## Troubleshooting

### Error: "java.lang.ClassNotFoundException: org.postgresql.Driver"

**Cause**: Maven hasn't downloaded PostgreSQL driver

**Solution**:
```bash
mvn dependency:resolve
mvn clean package
```

### Error: "Connection refused: connect"

**Cause**: PostgreSQL server not running or incorrect configuration

**Solution**:
```bash
# Check PostgreSQL service
# Windows: Services.msc → PostgreSQL
# Linux: sudo systemctl status postgresql

# Test connection
psql -U postgres -h localhost -p 5432
```

### Error: "Port 8080 already in use"

**Cause**: Port 8080 is being used by another process

**Solution**:
```bash
# Find process using port 8080
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8080
kill -9 <PID>

# Or change port in Tomcat server.xml
```

### Error: "Unable to create EntityManagerFactory"

**Cause**: Incorrect persistence.xml configuration or database doesn't exist

**Solution**:
1. Verify `persistence.xml` configuration
2. Check database name, username, password
3. Ensure PostgreSQL is running
4. Verify driver dependency in `pom.xml`

## Development Mode

For development with hot reload:

```bash
# Using Maven Tomcat plugin
mvn tomcat7:run

# Or using Spring Boot DevTools (if integrated)
mvn spring-boot:run
```

## Production Deployment Tips

1. **Change hibernate.hbm2ddl.auto to `validate`** in production
2. **Use connection pooling** (HikariCP recommended)
3. **Enable HTTPS** with SSL certificate
4. **Set environment-specific configs** (.env.production)
5. **Enable logging** with Log4j/SLF4J
6. **Monitor performance** with APM tools
7. **Regular database backups**
8. **Use reverse proxy** (Nginx/Apache) in front of Tomcat

## Next Steps

After successful installation, refer to:
- [API Documentation](./API_DOCUMENTATION.md) - Learn about available endpoints
- [Architecture Guide](./ARCHITECTURE.md) - Understand system design
- [Security Guide](./SECURITY.md) - Review security measures
- [Contributing Guide](./CONTRIBUTING.md) - Start contributing

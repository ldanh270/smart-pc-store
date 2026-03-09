# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app

# Copy the pom.xml first to cache Maven dependencies
COPY pom.xml .

# Go offline to download all dependencies. This layer is cached unless pom.xml changes.
RUN mvn dependency:go-offline -B

# Copy the source code
COPY src ./src

# Build the application (skipping tests for a faster build phase in Docker)
RUN mvn clean package -DskipTests

# Stage 2: Run the application on Tomcat 10
# Using Tomcat 10 because the project uses Jakarta Servlet API 6.0 and Java 17
FROM tomcat:10.1-jdk17

# Remove default Tomcat apps to keep the container clean and secure
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the built WAR file from the build stage to Tomcat's webapps directory
# Renaming to ROOT.war makes it available at the root URL (/)
COPY --from=build /app/target/smart-pc-store.war /usr/local/tomcat/webapps/ROOT.war

# Expose Tomcat's default port
EXPOSE 8080

# Start Tomcat server
CMD ["catalina.sh", "run"]

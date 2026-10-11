# ============================================================
# Stage 1: Build the Spring Boot application using Maven
# ============================================================
FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy pom.xml first to take advantage of Docker layer caching for dependencies
COPY pom.xml .

# Download dependencies offline
RUN mvn dependency:go-offline -B || true

# Copy application source code
COPY src ./src

# Package the application (skip tests in docker build; tested locally)
RUN mvn clean package -DskipTests -B

# ============================================================
# Stage 2: Production Runtime image
# ============================================================
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Create persistent storage directory for SQLite database on Render persistent disk
RUN mkdir -p /var/data

# Copy the packaged JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Configure default runtime environment variables
# Render automatically injects the PORT environment variable
ENV PORT=8080
ENV DB_PATH=/var/data/restaurant_queue.db
ENV JAVA_OPTS="-Djava.awt.headless=true -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Expose default port
EXPOSE 8080

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

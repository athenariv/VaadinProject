# ===== Build stage =====
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY mvnw .
COPY mvnw.cmd .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies first (cacheable layer)
RUN ./mvnw dependency:go-offline -B

COPY src src

# Build the application (skip tests)
RUN ./mvnw package -DskipTests -Pproduction -B

# ===== Runtime stage =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built jar
COPY --from=builder /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads/profiles

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

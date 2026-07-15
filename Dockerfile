# Stage 1: Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml first for layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the built JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

USER appuser

# Environment variables (override at runtime via docker run -e)
# These map to the placeholders in application.properties
ENV SERVER_PORT=8080
ENV DB_URL=jdbc:postgresql://localhost:5433/rentaya
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=rootroot
ENV DB_DDL_AUTO=update
ENV JWT_SECRET=rentaya-local-development-secret-key-2026
ENV JWT_EXPIRATION=86400
ENV SEED_DATA_ENABLED=false
ENV CORS_ALLOWED_ORIGINS=https://rentaya-web.onrender.com

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

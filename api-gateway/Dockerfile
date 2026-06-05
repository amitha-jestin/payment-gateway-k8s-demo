# ── Stage 1: Build ──────────────────────────────────────────────────────────
# Official image with Maven 3.9 + JDK 21 pre-installed — no manual setup needed
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy parent POM first so the child module can resolve it
COPY pom.xml                  ./pom.xml
COPY api-gateway/pom.xml           ./api-gateway/pom.xml
COPY api-gateway/src               ./api-gateway/src
COPY user-service/pom.xml        ./user-service/pom.xml
COPY user-service/src            ./user-service/src
COPY order-service/pom.xml       ./order-service/pom.xml
COPY order-service/src           ./order-service/src
COPY payment-service/pom.xml     ./payment-service/pom.xml
COPY payment-service/src         ./payment-service/src


# Install parent to local repo, then build the module (Spring Boot repackage runs here)
RUN mvn -pl api-gateway -am package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
# Slim JRE-only image — no JDK or Maven in the final image
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
# Copy the repackaged (executable) jar by explicit name to avoid wildcard picking a non-executable file
COPY --from=builder /build/api-gateway/target/api-gateway-1.0.0.jar /app/app.jar

# Create non-root user after copying artifacts so ownership is predictable
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

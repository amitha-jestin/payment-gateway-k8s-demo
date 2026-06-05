# ── Stage 1: Build ──────────────────────────────────────────────────────────
# Official image with Maven 3.9 + JDK 21 pre-installed — no manual setup needed
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copy parent POM first so the child module can resolve it
COPY pom.xml                  ./pom.xml
COPY order-service/pom.xml           ./order-service/pom.xml
COPY order-service/src               ./order-service/src

# Install parent to local repo, then build the module (Spring Boot repackage runs here)
RUN mvn -f pom.xml install -N -q &&     cd order-service && mvn package -DskipTests -q

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
# Slim JRE-only image — no JDK or Maven in the final image
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

WORKDIR /app
COPY --from=builder /build/order-service/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

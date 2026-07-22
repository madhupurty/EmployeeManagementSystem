# =============================================================================
# DOCKERFILE - Employee Management System
# =============================================================================
#
# This Dockerfile creates a container image for the Spring Boot application.
# Uses multi-stage build for smaller final image size.
#
# BUILD STAGES:
# 1. Build Stage: Compiles the application using Maven
# 2. Runtime Stage: Runs the application using lightweight JRE
#
# USAGE:
#   Build:  docker build -t ems-app .
#   Run:    docker run -p 8080:8080 ems-app
#
# =============================================================================

# =============================================================================
# STAGE 1: BUILD
# =============================================================================
# Use Maven with JDK 21 for building the application
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
# Dependencies are cached unless pom.xml changes
COPY pom.xml .

# Download dependencies (cached if pom.xml unchanged)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application (skip tests for faster builds)
# Tests should be run in CI/CD pipeline before building image
RUN mvn clean package -DskipTests -B

# =============================================================================
# STAGE 2: RUNTIME
# =============================================================================
# Use lightweight JRE Alpine image for running the application
FROM eclipse-temurin:21-jre-alpine

# Labels for image metadata
LABEL maintainer="EMS Team <ems@company.com>"
LABEL version="1.0"
LABEL description="Employee Management System Spring Boot Application"

# Set working directory
WORKDIR /app

# Create non-root user for security
# Running as root inside containers is a security risk
RUN addgroup -S ems && adduser -S ems -G ems

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R ems:ems /app

# Switch to non-root user
USER ems

# Expose the application port
EXPOSE 8080

# Health check - verify application is running
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM optimization flags for containers
# -XX:+UseContainerSupport: Respect container memory limits
# -XX:MaxRAMPercentage=75.0: Use 75% of container memory for heap
# -Djava.security.egd: Faster random number generation
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Run the application
# Using shell form to allow variable expansion
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

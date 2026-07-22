# Phase 6: Deployment Implementation

## Overview

Phase 6 implements comprehensive deployment solutions for the Employee Management System including **Docker**, **Docker Compose**, production configuration, and deployment scripts.

---

## Table of Contents

1. [Technologies Used](#technologies-used)
2. [File Structure](#file-structure)
3. [Docker Setup](#docker-setup)
4. [Docker Compose](#docker-compose)
5. [Production Configuration](#production-configuration)
6. [Deployment Scripts](#deployment-scripts)
7. [AWS EC2 Deployment](#aws-ec2-deployment)
8. [Environment Variables](#environment-variables)
9. [Health Checks & Monitoring](#health-checks--monitoring)
10. [Security Considerations](#security-considerations)
11. [Troubleshooting](#troubleshooting)
12. [Interview Questions](#interview-questions)

---

## Technologies Used

| Technology | Purpose |
|------------|---------|
| **Docker** | Containerization |
| **Docker Compose** | Multi-container orchestration |
| **MySQL 8.0** | Production database |
| **Spring Boot Actuator** | Health checks and monitoring |
| **Alpine Linux** | Lightweight container base image |

---

## File Structure

```
EmployeeManagementSystem/
├── Dockerfile                          # Multi-stage Docker build
├── docker-compose.yml                  # Multi-container setup
├── .dockerignore                       # Docker build exclusions
├── .env.example                        # Environment template
├── deploy.bat                          # Windows deployment script
├── deploy.sh                           # Linux/Mac deployment script
├── docker/
│   └── mysql/
│       └── init/
│           └── 01-init.sql             # MySQL initialization
└── src/main/resources/
    └── application-prod.properties     # Production configuration
```

---

## Docker Setup

### Dockerfile Explained

Our Dockerfile uses a **multi-stage build** for optimal image size:

```dockerfile
# Stage 1: BUILD - Compile with Maven
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B       # Cache dependencies
COPY src ./src
RUN mvn clean package -DskipTests -B   # Build JAR

# Stage 2: RUNTIME - Run with lightweight JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S ems && adduser -S ems -G ems  # Non-root user
COPY --from=build /app/target/*.jar app.jar
USER ems                                # Security: run as non-root
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s \
    CMD wget --spider http://localhost:8080/actuator/health
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Why Multi-Stage Build?

| Aspect | Single Stage | Multi-Stage |
|--------|--------------|-------------|
| Image Size | ~800MB+ | ~300MB |
| Contains | JDK + Maven + Source | JRE only |
| Security | More attack surface | Minimal dependencies |
| Build Time | Faster rebuilds | Cached layers |

### Build Commands

```bash
# Build the image
docker build -t ems-app .

# Build with no cache
docker build --no-cache -t ems-app .

# Build with specific tag
docker build -t ems-app:1.0.0 .
```

### Run Commands

```bash
# Run standalone (needs external MySQL)
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/employee_management_db \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=root \
  ems-app
```

---

## Docker Compose

### Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Network                        │
│                    (ems-network)                        │
│                                                         │
│  ┌─────────────────┐       ┌─────────────────────┐     │
│  │   ems-mysql     │       │      ems-app        │     │
│  │   (MySQL 8.0)   │◄─────►│  (Spring Boot App)  │     │
│  │   Port: 3307    │       │    Port: 8080       │     │
│  └─────────────────┘       └─────────────────────┘     │
│          │                          │                   │
│          ▼                          ▼                   │
│   mysql_data volume          Health Checks              │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### Services

#### MySQL Service

```yaml
ems-mysql:
  image: mysql:8.0
  environment:
    MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    MYSQL_DATABASE: employee_management_db
    MYSQL_USER: emsuser
    MYSQL_PASSWORD: ${MYSQL_PASSWORD}
  volumes:
    - mysql_data:/var/lib/mysql           # Persistent data
    - ./docker/mysql/init:/docker-entrypoint-initdb.d  # Init scripts
  healthcheck:
    test: ["CMD", "mysqladmin", "ping"]
    interval: 10s
    retries: 5
```

#### Application Service

```yaml
ems-app:
  build: .
  environment:
    SPRING_PROFILES_ACTIVE: prod
    SPRING_DATASOURCE_URL: jdbc:mysql://ems-mysql:3306/employee_management_db
  depends_on:
    ems-mysql:
      condition: service_healthy    # Wait for healthy DB
  healthcheck:
    test: ["CMD", "wget", "--spider", "http://localhost:8080/actuator/health"]
```

### Docker Compose Commands

```bash
# Start all services
docker-compose up -d

# Start with rebuild
docker-compose up -d --build

# View logs
docker-compose logs -f

# View specific service logs
docker-compose logs -f ems-app

# Stop services
docker-compose down

# Stop and remove volumes
docker-compose down -v

# Check status
docker-compose ps

# Execute command in container
docker-compose exec ems-app sh
docker-compose exec ems-mysql mysql -u root -p
```

---

## Production Configuration

### application-prod.properties

Key production settings:

```properties
# Database connection pool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# Disable SQL logging
spring.jpa.show-sql=false

# Hide error details from clients
server.error.include-message=never
server.error.include-stacktrace=never

# Enable compression
server.compression.enabled=true

# Actuator for health checks
management.endpoints.web.exposure.include=health,info,metrics
```

### Spring Profiles

| Profile | Purpose | Activation |
|---------|---------|------------|
| default | Development with local MySQL | No config needed |
| test | Testing with H2 | `-Dspring.profiles.active=test` |
| prod | Production in Docker | `SPRING_PROFILES_ACTIVE=prod` |

---

## Deployment Scripts

### Windows (deploy.bat)

```batch
deploy.bat start    # Start services
deploy.bat stop     # Stop services
deploy.bat build    # Build and start
deploy.bat logs     # View logs
deploy.bat status   # Check status
deploy.bat clean    # Remove everything
```

### Linux/Mac (deploy.sh)

```bash
chmod +x deploy.sh  # Make executable (first time)
./deploy.sh start   # Start services
./deploy.sh stop    # Stop services
./deploy.sh build   # Build and start
./deploy.sh logs    # View logs
./deploy.sh status  # Check status
./deploy.sh clean   # Remove everything
```

---

## AWS EC2 Deployment

### Prerequisites

1. AWS Account
2. EC2 Instance (t2.medium recommended)
3. Security Group with ports 22, 80, 443, 8080 open
4. SSH key pair

### Step-by-Step Deployment

#### 1. Launch EC2 Instance

```bash
# Amazon Linux 2 or Ubuntu 22.04
# Instance type: t2.medium (2 vCPU, 4GB RAM)
# Storage: 20GB minimum
```

#### 2. Connect to Instance

```bash
ssh -i your-key.pem ec2-user@your-ec2-ip
```

#### 3. Install Docker

```bash
# Amazon Linux 2
sudo yum update -y
sudo yum install -y docker
sudo service docker start
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Logout and login again for group changes
exit
ssh -i your-key.pem ec2-user@your-ec2-ip
```

#### 4. Clone and Deploy

```bash
# Clone repository
git clone https://github.com/your-repo/EmployeeManagementSystem.git
cd EmployeeManagementSystem

# Create environment file
cp .env.example .env
nano .env  # Edit with secure values

# Deploy
docker-compose up -d --build

# Check status
docker-compose ps
docker-compose logs -f
```

#### 5. Configure Security Group

| Type | Port | Source | Description |
|------|------|--------|-------------|
| SSH | 22 | Your IP | SSH access |
| HTTP | 80 | 0.0.0.0/0 | Web traffic |
| HTTPS | 443 | 0.0.0.0/0 | Secure web |
| Custom TCP | 8080 | 0.0.0.0/0 | Application |

### Access Application

- Application: `http://your-ec2-ip:8080`
- Swagger UI: `http://your-ec2-ip:8080/swagger-ui.html`
- Health Check: `http://your-ec2-ip:8080/actuator/health`

---

## Environment Variables

### Required Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password | `SecureRootPass123!` |
| `MYSQL_DATABASE` | Database name | `employee_management_db` |
| `MYSQL_USER` | Application DB user | `emsuser` |
| `MYSQL_PASSWORD` | Application DB password | `SecureAppPass123!` |
| `JWT_SECRET` | JWT signing key (Base64) | `base64-encoded-key` |
| `JWT_EXPIRATION` | Token expiry (ms) | `86400000` |

### Generate JWT Secret

```bash
# Linux/Mac
openssl rand -base64 64

# Or use online: https://generate.plus/en/base64
```

### .env File Example

```env
MYSQL_ROOT_PASSWORD=SecureRootPass123!
MYSQL_DATABASE=employee_management_db
MYSQL_USER=emsuser
MYSQL_PASSWORD=SecureAppPass123!
JWT_SECRET=YourBase64EncodedSecretKeyHere==
JWT_EXPIRATION=86400000
```

⚠️ **Never commit `.env` file to version control!**

---

## Health Checks & Monitoring

### Actuator Endpoints

| Endpoint | Description |
|----------|-------------|
| `/actuator/health` | Application health |
| `/actuator/health/liveness` | Kubernetes liveness probe |
| `/actuator/health/readiness` | Kubernetes readiness probe |
| `/actuator/info` | Application info |
| `/actuator/metrics` | Metrics (authenticated) |

### Health Check Response

```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

### Docker Health Checks

```bash
# Check container health
docker inspect --format='{{.State.Health.Status}}' ems-app

# View health check logs
docker inspect --format='{{json .State.Health}}' ems-app | jq
```

---

## Security Considerations

### 1. Non-Root User
Container runs as non-root user `ems`:
```dockerfile
RUN addgroup -S ems && adduser -S ems -G ems
USER ems
```

### 2. Secrets Management
- Never hardcode secrets in code
- Use environment variables
- Consider AWS Secrets Manager for production

### 3. Network Security
- Containers communicate on internal network
- Only necessary ports exposed
- MySQL not exposed to host in production

### 4. Image Security
- Use official base images
- Keep images updated
- Scan for vulnerabilities:
```bash
docker scan ems-app
```

### 5. Production Checklist
- [ ] Strong passwords in `.env`
- [ ] JWT secret is unique and complex
- [ ] HTTPS enabled (use reverse proxy)
- [ ] Database not exposed externally
- [ ] Logging configured properly
- [ ] Regular backups scheduled

---

## Troubleshooting

### Common Issues

#### 1. Container won't start
```bash
# Check logs
docker-compose logs ems-app

# Common fix: Wait for MySQL
docker-compose restart ems-app
```

#### 2. Database connection refused
```bash
# Check MySQL is healthy
docker-compose ps ems-mysql

# Check MySQL logs
docker-compose logs ems-mysql

# Verify connection
docker-compose exec ems-mysql mysql -u root -p
```

#### 3. Out of memory
```bash
# Check container resources
docker stats

# Increase memory limit in docker-compose.yml
deploy:
  resources:
    limits:
      memory: 2G
```

#### 4. Permission denied
```bash
# Fix file permissions
chmod +x deploy.sh
```

#### 5. Port already in use
```bash
# Find process using port
netstat -ano | findstr :8080  # Windows
lsof -i :8080                 # Linux/Mac

# Change port in docker-compose.yml
ports:
  - "8081:8080"
```

### Useful Debug Commands

```bash
# Enter container shell
docker-compose exec ems-app sh

# View real-time logs
docker-compose logs -f --tail=100

# Check network
docker network inspect ems-network

# Restart single service
docker-compose restart ems-app

# Rebuild single service
docker-compose up -d --build ems-app
```

---

## Interview Questions

### Q1: What is Docker and why use it?
**Answer:** Docker is a containerization platform that packages applications with their dependencies into isolated containers. Benefits:
- Consistent environments (dev = prod)
- Isolation between applications
- Easy deployment and scaling
- Version control for infrastructure

### Q2: Explain multi-stage Docker builds
**Answer:** Multi-stage builds use multiple FROM statements to create intermediate images. Benefits:
- Smaller final image (only runtime, not build tools)
- Separation of build and runtime concerns
- Improved security (fewer attack vectors)
- Cached layers for faster rebuilds

### Q3: What is Docker Compose?
**Answer:** Docker Compose is a tool for defining and running multi-container Docker applications. Using a YAML file, you configure services, networks, and volumes, then start everything with one command.

### Q4: How do containers communicate?
**Answer:** Containers on the same Docker network can communicate using:
- Container name as hostname (e.g., `ems-mysql`)
- Docker's internal DNS resolution
- Network bridges

### Q5: Why run as non-root user in container?
**Answer:** Security principle of least privilege:
- Limits damage if container is compromised
- Prevents container escape attacks
- Required by some orchestration platforms (OpenShift)

### Q6: What is `depends_on` with `condition: service_healthy`?
**Answer:** It ensures a service starts only after its dependency is fully ready (not just started). Without health condition, the app might start before MySQL is accepting connections.

### Q7: How to persist data in Docker?
**Answer:** Use Docker volumes:
```yaml
volumes:
  - mysql_data:/var/lib/mysql  # Named volume
  - ./data:/app/data           # Bind mount
```

### Q8: Difference between COPY and ADD in Dockerfile?
**Answer:**
- `COPY`: Simple file/directory copy
- `ADD`: Can also extract archives and fetch URLs
- Best practice: Use `COPY` unless you need `ADD` features

### Q9: What is Docker health check?
**Answer:** A command that Docker runs periodically to check container health. Containers can be:
- `starting`: Still in start period
- `healthy`: Health check passing
- `unhealthy`: Health check failing

### Q10: How to handle secrets in Docker?
**Answer:**
1. Environment variables (`.env` file)
2. Docker secrets (Swarm mode)
3. External secret managers (AWS Secrets Manager, Vault)
4. Never hardcode in Dockerfile or images

---

## Quick Reference

### Start Application
```bash
# Windows
deploy.bat start

# Linux/Mac
./deploy.sh start
```

### Access Points
| Service | URL |
|---------|-----|
| Application | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| API Docs | http://localhost:8080/api-docs |
| Health Check | http://localhost:8080/actuator/health |

### Common Commands
```bash
docker-compose up -d        # Start
docker-compose down         # Stop
docker-compose logs -f      # Logs
docker-compose ps           # Status
docker-compose up -d --build  # Rebuild
```

---

## Summary

Phase 6 implemented:

✅ Multi-stage Dockerfile for optimized images  
✅ Docker Compose for multi-container orchestration  
✅ Production configuration (application-prod.properties)  
✅ Environment variable management  
✅ Health checks with Spring Actuator  
✅ Deployment scripts (Windows & Linux)  
✅ AWS EC2 deployment guide  
✅ Security best practices  
✅ Comprehensive documentation

The Employee Management System is now ready for production deployment! 🚀

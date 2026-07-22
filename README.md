# 🏢 Employee Management System

> **Enterprise-grade Backend Application built with Java 21 & Spring Boot 3**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Option 1: Run with Docker (Recommended)](#option-1-run-with-docker-recommended)
  - [Option 2: Run Locally](#option-2-run-locally)
- [API Documentation](#api-documentation)
- [API Endpoints](#api-endpoints)
- [Authentication](#authentication)
- [Testing](#testing)
- [Deployment](#deployment)
- [Project Phases](#project-phases)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

The **Employee Management System** is a comprehensive enterprise backend application designed to manage employee lifecycle, departments, leave requests, and attendance tracking. Built following industry best practices with a focus on security, scalability, and maintainability.

---

## Features

### Core Modules
- ✅ **Employee Management** - CRUD operations with pagination, sorting, and search
- ✅ **Department Management** - Department hierarchy with manager assignment
- ✅ **Leave Management** - Leave request workflow with approval/rejection
- ✅ **Attendance Tracking** - Check-in/out with working hours calculation

### Security
- 🔐 **JWT Authentication** - Stateless token-based authentication
- 👥 **Role-Based Access Control** - ADMIN, HR, EMPLOYEE roles
- 🛡️ **Method-Level Security** - @PreAuthorize annotations

### Quality & DevOps
- 📝 **API Documentation** - Swagger/OpenAPI 3.0
- 🧪 **Comprehensive Testing** - Unit, Controller, Integration tests
- 🐳 **Docker Ready** - Multi-stage builds with Docker Compose
- 📊 **Health Monitoring** - Spring Actuator endpoints
- 📋 **Audit Trail** - Automatic tracking of changes

---

## Tech Stack

| Category | Technology |
|----------|------------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.2 |
| **Security** | Spring Security 6, JWT |
| **Database** | MySQL 8.0 |
| **ORM** | Spring Data JPA, Hibernate |
| **Build Tool** | Maven |
| **Testing** | JUnit 5, Mockito, MockMvc |
| **Documentation** | Swagger/OpenAPI 3.0 |
| **Containerization** | Docker, Docker Compose |
| **Logging** | SLF4J, Logback |

---

## Project Structure

```
EmployeeManagementSystem/
├── src/
│   ├── main/
│   │   ├── java/com/ems/
│   │   │   ├── config/          # Configuration classes
│   │   │   ├── controller/      # REST Controllers
│   │   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── entity/          # JPA Entities
│   │   │   ├── exception/       # Custom Exceptions
│   │   │   ├── repository/      # Data Access Layer
│   │   │   ├── security/        # JWT & Security
│   │   │   └── service/         # Business Logic
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-prod.properties
│   │       └── docs/            # Documentation
│   └── test/                    # Test Classes
├── docker/
│   └── mysql/init/              # MySQL init scripts
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## Getting Started

### Prerequisites

**For Docker deployment:**
- [Docker](https://www.docker.com/get-started) (v20.10+)
- [Docker Compose](https://docs.docker.com/compose/install/) (v2.0+)

**For local development:**
- [Java 21](https://adoptium.net/) (JDK)
- [Maven](https://maven.apache.org/download.cgi) (v3.8+)
- [MySQL](https://dev.mysql.com/downloads/mysql/) (v8.0)
- [Git](https://git-scm.com/downloads)

---

### Option 1: Run with Docker (Recommended)

This is the easiest way to run the project with all dependencies.

#### Step 1: Clone the repository
```bash
git clone https://github.com/madhupurty/EmployeeManagementSystem.git
cd EmployeeManagementSystem
```

#### Step 2: Create environment file
```bash
# Copy the example environment file
copy .env.example .env        # Windows
# cp .env.example .env        # Linux/Mac

# Edit .env file with your settings (optional)
```

#### Step 3: Run with Docker Compose
```bash
# Build and start all services
docker-compose up -d --build

# Check status
docker-compose ps

# View logs
docker-compose logs -f app
```

#### Step 4: Access the application
- **API Base URL:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **Health Check:** http://localhost:8080/actuator/health

#### Stop the application
```bash
docker-compose down

# To remove volumes (database data)
docker-compose down -v
```

---

### Option 2: Run Locally

#### Step 1: Clone the repository
```bash
git clone https://github.com/madhupurty/EmployeeManagementSystem.git
cd EmployeeManagementSystem
```

#### Step 2: Setup MySQL Database
```sql
-- Login to MySQL
mysql -u root -p

-- Create database
CREATE DATABASE employee_management_system;

-- Create user (optional)
CREATE USER 'ems_user'@'localhost' IDENTIFIED BY 'ems_password';
GRANT ALL PRIVILEGES ON employee_management_system.* TO 'ems_user'@'localhost';
FLUSH PRIVILEGES;
```

#### Step 3: Configure application.properties
```properties
# src/main/resources/application.properties

spring.datasource.url=jdbc:mysql://localhost:3306/employee_management_system
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

#### Step 4: Build the project
```bash
# Windows
mvnw.cmd clean install

# Linux/Mac
./mvnw clean install

# Or if Maven is installed globally
mvn clean install
```

#### Step 5: Run the application
```bash
# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run

# Or run the JAR file
java -jar target/EmployeeManagementSystem-0.0.1-SNAPSHOT.jar
```

#### Step 6: Access the application
- **API Base URL:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html

---

## API Documentation

Once the application is running, access the interactive API documentation:

**Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

![Swagger UI](https://via.placeholder.com/800x400?text=Swagger+UI+Screenshot)

---

## API Endpoints

### Authentication
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/auth/register` | Register new user | Public |
| POST | `/api/auth/login` | Login and get JWT | Public |

### Employees
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/employees` | Get all employees (paginated) | All Roles |
| GET | `/api/employees/{id}` | Get employee by ID | All Roles |
| POST | `/api/employees` | Create new employee | ADMIN, HR |
| PUT | `/api/employees/{id}` | Update employee | ADMIN, HR |
| DELETE | `/api/employees/{id}` | Delete employee | ADMIN |
| GET | `/api/employees/search` | Search employees | All Roles |

### Departments
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/departments` | Get all departments | All Roles |
| GET | `/api/departments/{id}` | Get department by ID | All Roles |
| POST | `/api/departments` | Create department | ADMIN |
| PUT | `/api/departments/{id}` | Update department | ADMIN |
| DELETE | `/api/departments/{id}` | Delete department | ADMIN |

### Leave Management
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| GET | `/api/leaves` | Get all leave requests | ADMIN, HR |
| POST | `/api/leaves` | Apply for leave | All Roles |
| PUT | `/api/leaves/{id}/approve` | Approve leave | ADMIN, HR |
| PUT | `/api/leaves/{id}/reject` | Reject leave | ADMIN, HR |

### Attendance
| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/attendance/check-in` | Check in | All Roles |
| POST | `/api/attendance/check-out` | Check out | All Roles |
| GET | `/api/attendance/employee/{id}` | Get attendance | All Roles |

---

## Authentication

### Register a new user
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@company.com",
    "password": "admin123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'
```

### Login to get JWT token
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "admin123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "username": "admin",
  "role": "ADMIN"
}
```

### Use token in requests
```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Testing

### Run all tests
```bash
mvn test
```

### Run specific test class
```bash
mvn test -Dtest=EmployeeServiceImplTest
```

### Run integration tests
```bash
mvn test -Dtest=IntegrationTest
```

### Generate test coverage report
```bash
mvn jacoco:report
# Report available at: target/site/jacoco/index.html
```

---

## Deployment

### Docker Deployment (Local/Server)
```bash
# Build and run
docker-compose up -d --build

# Scale application
docker-compose up -d --scale app=3
```

### AWS EC2 Deployment
See [PHASE6_DEPLOYMENT.md](src/main/resources/docs/PHASE6_DEPLOYMENT.md) for detailed AWS deployment instructions.

---

## Project Phases

| Phase | Description | Status |
|-------|-------------|--------|
| Phase 1 | Foundation (Spring Boot, Architecture, Logging) | ✅ Complete |
| Phase 2 | Employee Module (CRUD, Pagination, Validation) | ✅ Complete |
| Phase 3 | Security (JWT, Spring Security, RBAC) | ✅ Complete |
| Phase 4 | Advanced Features (Dept, Leave, Attendance, Audit) | ✅ Complete |
| Phase 5 | Testing (JUnit 5, Mockito, Integration) | ✅ Complete |
| Phase 6 | Deployment (Docker, Docker Compose, AWS) | ✅ Complete |

---

## 📤 Upload to GitHub

### First Time Setup

#### Step 1: Create a new repository on GitHub
1. Go to [GitHub](https://github.com) and login
2. Click the **+** icon → **New repository**
3. Repository name: `EmployeeManagementSystem`
4. Description: `Enterprise Employee Management System - Spring Boot 3, JWT, Docker`
5. Keep it **Public** or **Private**
6. **DON'T** initialize with README (we already have one)
7. Click **Create repository**

#### Step 2: Initialize Git and push
```bash
# Navigate to project directory
cd C:\Users\madhu.purty\Desktop\SPRING_FRAMEWORK\EmployeeManagementSystem

# Initialize git repository
git init

# Add all files
git add .

# Create first commit
git commit -m "Initial commit: Employee Management System - Complete Implementation"

# Add remote repository (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/madhupurty/EmployeeManagementSystem.git

# Push to GitHub
git branch -M main
git push -u origin main
```

### Subsequent Updates
```bash
# Check status
git status

# Add changes
git add .

# Commit with message
git commit -m "Your commit message"

# Push to GitHub
git push
```

---

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Author

**Madhu Purty**

- GitHub: [@madhupurty](https://github.com/madhupurty)
- LinkedIn: [Madhu Purty](https://linkedin.com/in/madhupurty)

---

## 🙏 Acknowledgments

- Spring Boot Documentation
- Spring Security Reference
- Docker Documentation
- The amazing open-source community

---

<p align="center">
  Made with ❤️ using Spring Boot
</p>

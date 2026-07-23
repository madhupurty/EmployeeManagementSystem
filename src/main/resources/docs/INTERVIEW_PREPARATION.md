# 🎯 Employee Management System - Interview Preparation Guide

> **For: 5.8 Years Experienced Java Developer**
> **Project: Enterprise Employee Management System**

---

## 📋 Table of Contents

1. [How to Brief Explain the Project](#how-to-brief-explain-the-project)
2. [Quick Project Summary 30 Seconds Pitch](#quick-project-summary-30-seconds-pitch)
3. [Detailed Project Explanation 2-3 Minutes](#detailed-project-explanation-2-3-minutes)
4. [Interview Questions by Category](#interview-questions-by-category)
   - [Project Overview Questions](#1-project-overview-questions)
   - [Architecture and Design Questions](#2-architecture-and-design-questions)
   - [Spring Boot Questions](#3-spring-boot-questions)
   - [Spring Security and JWT Questions](#4-spring-security-and-jwt-questions)
   - [Database and JPA Questions](#5-database-and-jpa-questions)
   - [REST API Design Questions](#6-rest-api-design-questions)
   - [Exception Handling Questions](#7-exception-handling-questions)
   - [Testing Questions](#8-testing-questions)
   - [Docker and Deployment Questions](#9-docker-and-deployment-questions)
   - [Performance and Optimization Questions](#10-performance-and-optimization-questions)
   - [Scenario-Based Questions](#11-scenario-based-questions)
   - [Code-Level Questions](#12-code-level-questions)
   - [Behavioral Experience Questions](#13-behavioral-experience-questions)

---

## How to Brief Explain the Project

### Quick Project Summary 30 Seconds Pitch

> "I developed an **Enterprise Employee Management System** - a production-ready backend application using **Java 21** and **Spring Boot 3**. The system handles employee lifecycle management including **CRUD operations**, **department management**, **leave requests**, and **attendance tracking**. I implemented **JWT-based authentication** with **role-based access control** supporting three roles: Admin, HR, and Employee. The application follows a **layered architecture** with proper separation of concerns, includes comprehensive **unit and integration tests** using JUnit 5 and Mockito, and is **containerized using Docker** for easy deployment. The APIs are documented using **Swagger/OpenAPI**."

---

### Detailed Project Explanation 2-3 Minutes

> "Let me walk you through the Employee Management System I developed. This is an **enterprise-grade backend application** built using **Java 21** and **Spring Boot 3**.
>
> **Problem Statement:** Organizations need a centralized system to manage employee data, track attendance, handle leave requests, and maintain department hierarchies with proper access controls.
>
> **Architecture:** I followed a **layered architecture pattern** with clear separation:
> - **Controller Layer** - REST APIs handling HTTP requests
> - **Service Layer** - Business logic implementation
> - **Repository Layer** - Data access using Spring Data JPA
> - **Entity Layer** - Domain models mapped to MySQL database
>
> **Key Modules:**
> 1. **Employee Module** - Complete CRUD with pagination, sorting, and search functionality
> 2. **Department Module** - Department management with manager assignment
> 3. **Leave Management** - Leave request workflow with approval/rejection process
> 4. **Attendance Module** - Check-in/out tracking with working hours calculation
>
> **Security Implementation:** I implemented **Spring Security with JWT** authentication. The system supports **three roles**:
> - **ADMIN** - Full system access
> - **HR** - Can manage employees and approve leaves
> - **EMPLOYEE** - Can view their data and apply for leaves
>
> Each API endpoint is secured with **@PreAuthorize** annotations for method-level security.
>
> **Technical Highlights:**
> - **Audit Trail** - Using JPA Auditing for tracking created/modified timestamps and users
> - **Global Exception Handling** - Centralized error handling with custom exceptions
> - **Input Validation** - Bean Validation with custom validators
> - **API Documentation** - Swagger/OpenAPI 3.0 with JWT support
>
> **Testing:** I wrote comprehensive tests:
> - **Unit Tests** using JUnit 5 and Mockito for service layer
> - **Controller Tests** using MockMvc with security context
> - **Integration Tests** with H2 in-memory database
>
> **Deployment:** The application is containerized using **Docker** with multi-stage builds to optimize image size. I created a **Docker Compose** setup that orchestrates the application with MySQL database, including health checks and proper networking.
>
> This project demonstrates my ability to design and implement **production-ready enterprise applications** following industry best practices."

---

## Interview Questions by Category

---

## 1. Project Overview Questions

### Q1: Can you give a brief overview of your project?
**Answer:** *(Use the 30-second pitch above)*

### Q2: What was the business requirement for this project?
**Answer:**
> "The requirement was to build a centralized employee management system that could:
> - Manage employee lifecycle (hiring, updates, termination)
> - Handle organizational hierarchy through departments
> - Track employee attendance and working hours
> - Manage leave requests with an approval workflow
> - Provide role-based access for different user types
> - Be scalable and deployable on cloud infrastructure"

### Q3: What was your role in this project?
**Answer:**
> "I was the **sole developer** responsible for end-to-end implementation including:
> - System design and architecture decisions
> - Database schema design
> - Backend API development
> - Security implementation
> - Writing unit and integration tests
> - Docker containerization and deployment setup
> - API documentation"

### Q4: How long did it take to complete this project?
**Answer:**
> "The project was developed in **6 phases** over approximately **4-6 weeks**:
> - Phase 1: Foundation setup (2-3 days)
> - Phase 2: Employee module (1 week)
> - Phase 3: Security implementation (1 week)
> - Phase 4: Advanced features (1-2 weeks)
> - Phase 5: Testing (1 week)
> - Phase 6: Deployment setup (2-3 days)"

### Q5: What challenges did you face during development?
**Answer:**
> "Key challenges included:
> 1. **JWT Token Management** - Handling token expiration gracefully and implementing refresh token logic
> 2. **Role-Based Security** - Designing granular permissions that don't become too complex
> 3. **Audit Fields** - Implementing automatic population of createdBy/updatedBy without circular dependencies
> 4. **Leave Workflow** - Handling edge cases like overlapping leave requests and leave balance validation
> 5. **Docker Networking** - Ensuring proper communication between app and database containers"

### Q6: If you had to do this project again, what would you do differently?
**Answer:**
> "I would consider:
> 1. **Implementing CQRS** for better read/write separation at scale
> 2. **Adding Redis** for caching frequently accessed data
> 3. **Event-Driven Architecture** using Kafka for audit logs
> 4. **Implementing refresh tokens** for better security
> 5. **Adding rate limiting** to prevent API abuse"

---

## 2. Architecture and Design Questions

### Q7: Explain the architecture of your application.
**Answer:**
> "I followed a **Layered Architecture** pattern:
> ```
> Client → Controller → Service → Repository → Database
> ```
> 
> **Layers:**
> - **Controller Layer**: REST endpoints, request/response handling, input validation
> - **Service Layer**: Business logic, transaction management
> - **Repository Layer**: Data access, JPA queries
> - **Entity Layer**: Domain models, JPA mappings
> 
> **Cross-cutting concerns:**
> - **Security**: JWT filter intercepting all requests
> - **Exception Handling**: Global exception handler
> - **Logging**: SLF4J with Logback
> - **Auditing**: JPA Auditing for timestamps"

### Q8: Why did you choose layered architecture over microservices?
**Answer:**
> "For this project, layered architecture was more appropriate because:
> 1. **Single deployment unit** - Easier to manage for a focused domain
> 2. **Simpler development** - No need for service discovery, API gateway
> 3. **Transaction management** - Easier to maintain ACID properties
> 4. **Team size** - Single developer, no need for team boundaries
> 
> However, the code is **modular enough** to extract into microservices if needed. Each module (Employee, Department, Leave, Attendance) has its own service and can be separated."

### Q9: How would you convert this to microservices?
**Answer:**
> "I would:
> 1. **Identify bounded contexts**: Employee, Department, Leave, Attendance, Auth
> 2. **Create separate services** for each with their own database
> 3. **Add API Gateway** (Spring Cloud Gateway) for routing
> 4. **Service Discovery** using Eureka or Consul
> 5. **Inter-service communication** via REST or messaging (Kafka/RabbitMQ)
> 6. **Distributed tracing** using Sleuth and Zipkin
> 7. **Centralized configuration** using Spring Cloud Config"

### Q10: Explain SOLID principles and how you applied them.
**Answer:**
> "**S - Single Responsibility:**
> - Each service class handles one module (EmployeeService, DepartmentService)
> - Separate DTOs for request and response
>
> **O - Open/Closed:**
> - Service interfaces allow new implementations without modifying existing code
> - Global exception handler can handle new exceptions without changes
>
> **L - Liskov Substitution:**
> - Service implementations can be swapped (useful for testing with mocks)
>
> **I - Interface Segregation:**
> - Separate interfaces for different services
> - UserDetails interface implementation in User entity
>
> **D - Dependency Inversion:**
> - Controllers depend on service interfaces, not implementations
> - Constructor injection with @Autowired"

### Q11: What design patterns did you use?
**Answer:**
> "1. **Repository Pattern** - Data access abstraction via Spring Data JPA
> 2. **DTO Pattern** - Separate request/response objects from entities
> 3. **Builder Pattern** - Using Lombok @Builder for object creation
> 4. **Factory Pattern** - JwtUtil creating tokens
> 5. **Strategy Pattern** - Different authentication strategies
> 6. **Template Method** - JPA repository methods
> 7. **Filter Pattern** - JWT authentication filter in security chain
> 8. **Singleton Pattern** - Spring beans are singletons by default"

### Q12: How did you ensure loose coupling in your code?
**Answer:**
> "1. **Dependency Injection** - Using constructor injection
> 2. **Interface-based design** - Services implement interfaces
> 3. **DTO usage** - Controllers don't expose entities directly
> 4. **Event-driven** - Could use ApplicationEvents for cross-module communication
> 5. **Configuration externalization** - Properties files for environment-specific configs"

---

## 3. Spring Boot Questions

### Q13: Why did you choose Spring Boot 3 over Spring Boot 2?
**Answer:**
> "Spring Boot 3 offers:
> 1. **Java 17+ baseline** - Access to modern Java features (records, pattern matching)
> 2. **Jakarta EE 9+** - Updated namespace (javax → jakarta)
> 3. **Native compilation support** - GraalVM native images
> 4. **Improved observability** - Micrometer integration
> 5. **Spring Security 6** - Simplified security configuration
> 6. **Better performance** - Optimized startup time"

### Q14: Explain Spring Boot auto-configuration.
**Answer:**
> "Auto-configuration automatically configures beans based on:
> 1. **Classpath dependencies** - Adding spring-boot-starter-data-jpa auto-configures DataSource, EntityManager
> 2. **@ConditionalOnClass** - Configures if class is present
> 3. **@ConditionalOnMissingBean** - Only if bean not already defined
> 4. **Application properties** - Customizes auto-configured beans
>
> Example: Adding `spring-boot-starter-security` automatically:
> - Enables security filters
> - Creates default login page
> - Generates random password"

### Q15: What Spring Boot starters did you use?
**Answer:**
> "```xml
> spring-boot-starter-web        - REST APIs, embedded Tomcat
> spring-boot-starter-data-jpa   - JPA, Hibernate
> spring-boot-starter-security   - Spring Security
> spring-boot-starter-validation - Bean Validation
> spring-boot-starter-actuator   - Health checks, metrics
> spring-boot-starter-test       - JUnit 5, Mockito
> ```"

### Q16: How does @SpringBootApplication work?
**Answer:**
> "@SpringBootApplication is a meta-annotation combining:
> 1. **@Configuration** - Marks class as configuration source
> 2. **@EnableAutoConfiguration** - Enables auto-configuration
> 3. **@ComponentScan** - Scans for components in current and sub-packages
>
> It bootstraps the application by:
> - Creating ApplicationContext
> - Registering beans
> - Starting embedded server"

### Q17: Explain different bean scopes in Spring.
**Answer:**
> "1. **Singleton** (default) - One instance per Spring container
> 2. **Prototype** - New instance every time requested
> 3. **Request** - One instance per HTTP request
> 4. **Session** - One instance per HTTP session
> 5. **Application** - One instance per ServletContext
>
> In my project, all services are singleton scope for performance."

### Q18: How do you handle different environments (dev, prod)?
**Answer:**
> "Using **Spring Profiles**:
> ```properties
> # application.properties (default)
> spring.profiles.active=dev
>
> # application-dev.properties
> spring.datasource.url=jdbc:mysql://localhost:3306/ems_dev
>
> # application-prod.properties
> spring.datasource.url=${DATABASE_URL}
> ```
>
> Activated via:
> - Environment variable: `SPRING_PROFILES_ACTIVE=prod`
> - Command line: `--spring.profiles.active=prod`
> - Docker: Set in docker-compose.yml"

### Q19: What is the difference between @Component, @Service, @Repository, @Controller?
**Answer:**
> "All are specializations of @Component for **component scanning**:
>
> | Annotation | Purpose | Special Behavior |
> |------------|---------|------------------|
> | @Component | Generic bean | None |
> | @Service | Business logic | None (semantic) |
> | @Repository | Data access | Exception translation |
> | @Controller | Web controller | Request mapping |
> | @RestController | REST API | @Controller + @ResponseBody |
>
> @Repository adds **automatic exception translation** - converts database exceptions to Spring's DataAccessException hierarchy."

### Q20: Explain @Transactional annotation.
**Answer:**
> "@Transactional provides declarative transaction management:
>
> **Attributes:**
> - **propagation** - REQUIRED (default), REQUIRES_NEW, NESTED
> - **isolation** - READ_COMMITTED, SERIALIZABLE
> - **readOnly** - Optimization hint
> - **rollbackFor** - Exceptions to rollback
> - **timeout** - Transaction timeout
>
> **In my project:**
> ```java
> @Transactional
> public EmployeeResponseDTO createEmployee(EmployeeRequestDTO request) {
>     // All operations in single transaction
> }
>
> @Transactional(readOnly = true)
> public Page<EmployeeResponseDTO> getAllEmployees(Pageable pageable) {
>     // Read-only transaction for queries
> }
> ```"

---

## 4. Spring Security and JWT Questions

### Q21: Explain your security implementation.
**Answer:**
> "I implemented **stateless JWT-based authentication**:
>
> **Flow:**
> 1. User sends credentials to `/api/auth/login`
> 2. Server validates credentials against database
> 3. Server generates JWT token with user details and roles
> 4. Client stores token and sends in `Authorization: Bearer <token>` header
> 5. JwtAuthenticationFilter validates token on each request
> 6. SecurityContextHolder stores authenticated user
>
> **Components:**
> - **JwtUtil** - Token generation, validation, extraction
> - **JwtAuthenticationFilter** - OncePerRequestFilter for JWT validation
> - **CustomUserDetailsService** - Loads user from database
> - **SecurityConfig** - Security filter chain configuration"

### Q22: How does JWT work? Explain its structure.
**Answer:**
> "JWT (JSON Web Token) has three parts separated by dots:
>
> **Header.Payload.Signature**
>
> ```
> eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGVzIjoiQURNSU4iLCJleHAiOjE2NTk5MjcxMjN9.abc123signature
> ```
>
> **Header:** Algorithm and token type
> ```json
> {\"alg\": \"HS256\", \"typ\": \"JWT\"}
> ```
>
> **Payload:** Claims (data)
> ```json
> {\"sub\": \"admin\", \"roles\": \"ADMIN\", \"exp\": 1659927123}
> ```
>
> **Signature:** HMACSHA256(base64(header) + \".\" + base64(payload), secret)"

### Q23: Why JWT over session-based authentication?
**Answer:**
> "**JWT Advantages:**
> 1. **Stateless** - No server-side session storage
> 2. **Scalable** - Works across multiple servers without session replication
> 3. **Mobile-friendly** - Easy to use with mobile apps
> 4. **Cross-domain** - Works with CORS
> 5. **Self-contained** - Contains user info, reducing database calls
>
> **Session Disadvantages:**
> 1. Server memory for sessions
> 2. Session replication in clusters
> 3. Not suitable for mobile apps"

### Q24: How do you handle JWT expiration?
**Answer:**
> "**Current Implementation:**
> - Token expires after configured time (24 hours)
> - Expired token returns 401 Unauthorized
> - User must login again
>
> **Better Approach (Enhancement):**
> ```
> Access Token: Short-lived (15-30 minutes)
> Refresh Token: Long-lived (7 days), stored in HttpOnly cookie
>
> Flow:
> 1. Access token expires
> 2. Client sends refresh token to /api/auth/refresh
> 3. Server validates refresh token
> 4. Server issues new access token
> ```"

### Q25: Explain role-based access control in your project.
**Answer:**
> "I implemented RBAC with three roles:
>
> | Role | Permissions |
> |------|-------------|
> | ADMIN | All operations on all resources |
> | HR | Create, Read, Update employees; Approve leaves |
> | EMPLOYEE | Read own data; Apply for leaves |
>
> **Implementation:**
> ```java
> // Method-level security
> @PreAuthorize(\"hasRole('ADMIN')\")
> public void deleteEmployee(Long id) { }
>
> @PreAuthorize(\"hasAnyRole('ADMIN', 'HR')\")
> public void updateEmployee(Long id, EmployeeRequestDTO dto) { }
>
> @PreAuthorize(\"hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')\")
> public EmployeeResponseDTO getEmployee(Long id) { }
> ```"

### Q26: How do you secure your endpoints?
**Answer:**
> "**Two levels of security:**
>
> **1. URL-based (SecurityConfig):**
> ```java
> http.authorizeHttpRequests(auth -> auth
>     .requestMatchers(\"/api/auth/**\").permitAll()
>     .requestMatchers(\"/swagger-ui/**\").permitAll()
>     .requestMatchers(\"/actuator/health\").permitAll()
>     .anyRequest().authenticated()
> );
> ```
>
> **2. Method-based (@PreAuthorize):**
> ```java
> @PreAuthorize(\"hasRole('ADMIN')\")
> @DeleteMapping(\"/{id}\")
> public ResponseEntity<Void> deleteEmployee(@PathVariable Long id)
> ```"

### Q27: What is the security filter chain?
**Answer:**
> "Spring Security uses a chain of filters:
>
> ```
> Request → SecurityFilterChain → Controller
>
> Filters in order:
> 1. CorsFilter
> 2. CsrfFilter (disabled for stateless)
> 3. UsernamePasswordAuthenticationFilter
> 4. JwtAuthenticationFilter (custom)
> 5. ExceptionTranslationFilter
> 6. FilterSecurityInterceptor
> ```
>
> My JwtAuthenticationFilter runs before UsernamePasswordAuthenticationFilter to validate JWT tokens."

### Q28: How do you handle CORS?
**Answer:**
> "CORS (Cross-Origin Resource Sharing) configured in SecurityConfig:
>
> ```java
> @Bean
> public CorsConfigurationSource corsConfigurationSource() {
>     CorsConfiguration config = new CorsConfiguration();
>     config.setAllowedOrigins(Arrays.asList(\"http://localhost:3000\"));
>     config.setAllowedMethods(Arrays.asList(\"GET\", \"POST\", \"PUT\", \"DELETE\"));
>     config.setAllowedHeaders(Arrays.asList(\"*\"));
>     config.setAllowCredentials(true);
>     
>     UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
>     source.registerCorsConfiguration(\"/**\", config);
>     return source;
> }
> ```"

### Q29: What is CSRF and why did you disable it?
**Answer:**
> "**CSRF (Cross-Site Request Forgery):**
> An attack where malicious site tricks user's browser into making requests to your site using user's session.
>
> **Why disabled:**
> - JWT is stateless - no session cookies
> - Token sent in Authorization header, not cookies
> - CSRF only exploits cookie-based auth
>
> ```java
> http.csrf(csrf -> csrf.disable())
> ```
>
> **For cookie-based JWT:**
> Would need to enable CSRF protection."

### Q30: How do you store passwords securely?
**Answer:**
> "Using **BCryptPasswordEncoder**:
>
> ```java
> @Bean
> public PasswordEncoder passwordEncoder() {
>     return new BCryptPasswordEncoder();
> }
>
> // Registration
> user.setPassword(passwordEncoder.encode(request.getPassword()));
>
> // Login - Spring Security automatically compares using BCrypt
> authenticationManager.authenticate(
>     new UsernamePasswordAuthenticationToken(username, password)
> );
> ```
>
> **BCrypt features:**
> - One-way hashing
> - Built-in salt
> - Configurable work factor
> - Resistant to rainbow tables"

---

## 5. Database and JPA Questions

### Q31: Why did you choose MySQL?
**Answer:**
> "MySQL was chosen because:
> 1. **Relational data** - Employee, Department relationships
> 2. **ACID compliance** - Transaction integrity
> 3. **Mature ecosystem** - Tools, documentation, community
> 4. **Performance** - Optimized for read-heavy workloads
> 5. **Cost-effective** - Open source
> 6. **Industry standard** - Widely used in enterprise"

### Q32: Explain your database schema design.
**Answer:**
> "**Tables:**
> ```
> users (id, username, email, password, role, first_name, last_name)
> employees (id, first_name, last_name, email, department, salary, status, hire_date)
> departments (id, name, code, description, manager_id)
> leave_requests (id, employee_id, type, status, start_date, end_date, reason)
> attendances (id, employee_id, date, check_in, check_out, status, working_hours)
> ```
>
> **Relationships:**
> - Employee ↔ Department (Many-to-One)
> - Employee ↔ LeaveRequest (One-to-Many)
> - Employee ↔ Attendance (One-to-Many)
> - Department ↔ Manager (Many-to-One self-ref via Employee)"

### Q33: What is the difference between JPA and Hibernate?
**Answer:**
> "**JPA (Java Persistence API):**
> - Specification/standard for ORM
> - Defines interfaces and annotations
> - Part of Jakarta EE
>
> **Hibernate:**
> - Implementation of JPA specification
> - Provides additional features beyond JPA
> - Most popular JPA implementation
>
> **Spring Data JPA:**
> - Abstraction over JPA
> - Repository interfaces with query methods
> - Reduces boilerplate code
>
> In my project: `Spring Data JPA → JPA API → Hibernate → JDBC → MySQL`"

### Q34: Explain entity relationships in your project.
**Answer:**
> "**One-to-Many (Department → Employees):**
> ```java
> @Entity
> public class Department {
>     @OneToMany(mappedBy = \"departmentEntity\")
>     private List<Employee> employees;
> }
>
> @Entity
> public class Employee {
>     @ManyToOne(fetch = FetchType.LAZY)
>     @JoinColumn(name = \"department_id\")
>     private Department departmentEntity;
> }
> ```
>
> **FetchType:**
> - LAZY (default for collections) - Load when accessed
> - EAGER - Load immediately"

### Q35: What is N+1 problem and how do you solve it?
**Answer:**
> "**N+1 Problem:**
> When fetching parent entities, each child collection triggers a separate query.
>
> ```java
> // 1 query for departments
> List<Department> depts = departmentRepository.findAll();
> // N queries for employees (one per department)
> for (Department d : depts) {
>     d.getEmployees().size(); // Triggers query
> }
> ```
>
> **Solutions:**
> 1. **JOIN FETCH:**
> ```java
> @Query(\"SELECT d FROM Department d JOIN FETCH d.employees\")
> List<Department> findAllWithEmployees();
> ```
>
> 2. **@EntityGraph:**
> ```java
> @EntityGraph(attributePaths = {\"employees\"})
> List<Department> findAll();
> ```
>
> 3. **@BatchSize:**
> ```java
> @BatchSize(size = 20)
> @OneToMany
> private List<Employee> employees;
> ```"

### Q36: How does pagination work in Spring Data JPA?
**Answer:**
> "Spring Data JPA provides Pageable interface:
>
> **Repository:**
> ```java
> Page<Employee> findAll(Pageable pageable);
> ```
>
> **Service:**
> ```java
> Pageable pageable = PageRequest.of(
>     pageNumber,    // 0-indexed
>     pageSize,      // items per page
>     Sort.by(\"lastName\").ascending()
> );
> Page<Employee> page = repository.findAll(pageable);
> ```
>
> **Page object contains:**
> - `getContent()` - List of items
> - `getTotalElements()` - Total count
> - `getTotalPages()` - Total pages
> - `hasNext()` / `hasPrevious()` - Navigation"

### Q37: Explain JPA Auditing in your project.
**Answer:**
> "I used JPA Auditing for automatic timestamp tracking:
>
> **Configuration:**
> ```java
> @Configuration
> @EnableJpaAuditing
> public class JpaAuditConfig {
>     @Bean
>     public AuditorAware<String> auditorProvider() {
>         return () -> Optional.ofNullable(
>             SecurityContextHolder.getContext().getAuthentication()
>         ).map(Authentication::getName);
>     }
> }
> ```
>
> **Base Entity:**
> ```java
> @MappedSuperclass
> @EntityListeners(AuditingEntityListener.class)
> public abstract class BaseEntity {
>     @CreatedDate
>     private LocalDateTime createdAt;
>     
>     @LastModifiedDate
>     private LocalDateTime updatedAt;
>     
>     @CreatedBy
>     private String createdBy;
>     
>     @LastModifiedBy
>     private String updatedBy;
> }
> ```"

### Q38: What are the different types of JPA queries?
**Answer:**
> "**1. Derived Queries (Method naming):**
> ```java
> List<Employee> findByDepartment(String department);
> List<Employee> findByStatusAndDepartment(String status, String dept);
> ```
>
> **2. JPQL Queries:**
> ```java
> @Query(\"SELECT e FROM Employee e WHERE e.salary > :salary\")
> List<Employee> findHighEarners(@Param(\"salary\") BigDecimal salary);
> ```
>
> **3. Native SQL:**
> ```java
> @Query(value = \"SELECT * FROM employees WHERE department = ?\", nativeQuery = true)
> List<Employee> findByDeptNative(String dept);
> ```
>
> **4. Criteria API:**
> ```java
> CriteriaBuilder cb = entityManager.getCriteriaBuilder();
> CriteriaQuery<Employee> query = cb.createQuery(Employee.class);
> // Dynamic query building
> ```"

### Q39: How do you handle database transactions?
**Answer:**
> "**Transaction Management:**
>
> **1. Declarative (Annotation-based):**
> ```java
> @Transactional
> public void transferEmployee(Long empId, Long newDeptId) {
>     Employee emp = employeeRepository.findById(empId).orElseThrow();
>     Department dept = departmentRepository.findById(newDeptId).orElseThrow();
>     emp.setDepartmentEntity(dept);
>     // Auto-commits on method completion
>     // Auto-rollbacks on RuntimeException
> }
> ```
>
> **2. Propagation:**
> - REQUIRED (default) - Use existing or create new
> - REQUIRES_NEW - Always create new
> - NESTED - Nested transaction with savepoints
>
> **3. Isolation Levels:**
> - READ_COMMITTED - Prevents dirty reads
> - REPEATABLE_READ - Prevents non-repeatable reads
> - SERIALIZABLE - Full isolation"

### Q40: Difference between save() and saveAndFlush()?
**Answer:**
> "**save():**
> - Persists entity to persistence context
> - Actual SQL may be deferred until transaction commit
> - More efficient for batch operations
>
> **saveAndFlush():**
> - Persists and immediately executes SQL
> - Useful when you need ID immediately
> - Forces synchronization with database
>
> ```java
> Employee emp = repository.save(employee);      // SQL may be pending
> Employee emp = repository.saveAndFlush(emp);   // SQL executed immediately
> ```"

---

## 6. REST API Design Questions

### Q41: What REST conventions did you follow?
**Answer:**
> "**URL Design:**
> - Nouns for resources: `/api/employees`, `/api/departments`
> - Plural form: `/api/employees` not `/api/employee`
> - Hierarchical: `/api/departments/{deptId}/employees`
>
> **HTTP Methods:**
> - GET - Read (idempotent)
> - POST - Create
> - PUT - Full update
> - PATCH - Partial update
> - DELETE - Remove
>
> **Status Codes:**
> - 200 OK - Success
> - 201 Created - Resource created
> - 204 No Content - Delete success
> - 400 Bad Request - Validation error
> - 401 Unauthorized - Authentication required
> - 403 Forbidden - Insufficient permissions
> - 404 Not Found - Resource not found
> - 500 Internal Server Error"

### Q42: How did you design your API responses?
**Answer:**
> "**Consistent Response Structure:**
>
> **Success Response:**
> ```json
> {
>   \"success\": true,
>   \"data\": {
>     \"id\": 1,
>     \"firstName\": \"John\",
>     \"lastName\": \"Doe\"
>   },
>   \"message\": \"Employee created successfully\"
> }
> ```
>
> **Error Response:**
> ```json
> {
>   \"success\": false,
>   \"error\": {
>     \"code\": \"VALIDATION_ERROR\",
>     \"message\": \"Validation failed\",
>     \"details\": [
>       {\"field\": \"email\", \"message\": \"Invalid email format\"}
>     ]
>   },
>   \"timestamp\": \"2024-01-15T10:30:00Z\"
> }
> ```
>
> **Paginated Response:**
> ```json
> {
>   \"content\": [...],
>   \"page\": 0,
>   \"size\": 10,
>   \"totalElements\": 100,
>   \"totalPages\": 10
> }
> ```"

### Q43: What is the difference between PUT and PATCH?
**Answer:**
> "**PUT - Full Update:**
> - Replaces entire resource
> - Must send all fields
> - Idempotent
>
> ```json
> PUT /api/employees/1
> {
>   \"firstName\": \"John\",
>   \"lastName\": \"Doe\",
>   \"email\": \"john@email.com\",
>   \"department\": \"IT\",
>   \"salary\": 75000
> }
> ```
>
> **PATCH - Partial Update:**
> - Updates only specified fields
> - Send only changed fields
>
> ```json
> PATCH /api/employees/1
> {
>   \"salary\": 80000
> }
> ```
>
> My project uses PUT for full updates."

### Q44: Why use DTOs instead of entities directly?
**Answer:**
> "**Problems with exposing entities:**
> 1. **Security** - May expose sensitive fields (password)
> 2. **Coupling** - API tied to database schema
> 3. **Circular references** - JSON serialization issues
> 4. **Over-fetching** - Sending more data than needed
> 5. **Validation** - Different validation for create/update
>
> **Benefits of DTOs:**
> ```java
> // Request DTO - Only fields client can set
> public class EmployeeRequestDTO {
>     private String firstName;
>     private String lastName;
>     // No id, createdAt, etc.
> }
>
> // Response DTO - Only fields client needs
> public class EmployeeResponseDTO {
>     private Long id;
>     private String fullName;
>     // Computed fields, formatted data
> }
> ```"

### Q45: How do you handle API versioning?
**Answer:**
> "**Common Approaches:**
>
> **1. URL Path (Recommended):**
> ```
> /api/v1/employees
> /api/v2/employees
> ```
>
> **2. Request Header:**
> ```
> Accept: application/vnd.company.v1+json
> ```
>
> **3. Query Parameter:**
> ```
> /api/employees?version=1
> ```
>
> **My Implementation:**
> ```java
> @RestController
> @RequestMapping(\"/api/v1/employees\")
> public class EmployeeController { }
> ```
>
> URL versioning is clearest and easiest to test."

### Q46: How did you document your APIs?
**Answer:**
> "Using **Swagger/OpenAPI 3.0** with springdoc-openapi:
>
> **Configuration:**
> ```java
> @Configuration
> public class OpenApiConfig {
>     @Bean
>     public OpenAPI customOpenAPI() {
>         return new OpenAPI()
>             .info(new Info()
>                 .title(\"Employee Management API\")
>                 .version(\"1.0\"))
>             .addSecurityItem(new SecurityRequirement()
>                 .addList(\"Bearer Authentication\"))
>             .components(new Components()
>                 .addSecuritySchemes(\"Bearer Authentication\",
>                     new SecurityScheme()
>                         .type(SecurityScheme.Type.HTTP)
>                         .scheme(\"bearer\")
>                         .bearerFormat(\"JWT\")));
>     }
> }
> ```
>
> **Access:** http://localhost:8080/swagger-ui.html"

---

## 7. Exception Handling Questions

### Q47: How did you implement global exception handling?
**Answer:**
> "Using **@RestControllerAdvice**:
>
> ```java
> @RestControllerAdvice
> public class GlobalExceptionHandler {
>     
>     @ExceptionHandler(ResourceNotFoundException.class)
>     public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
>         ErrorResponse error = new ErrorResponse(
>             HttpStatus.NOT_FOUND.value(),
>             ex.getMessage(),
>             LocalDateTime.now()
>         );
>         return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
>     }
>     
>     @ExceptionHandler(MethodArgumentNotValidException.class)
>     public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
>         Map<String, String> errors = new HashMap<>();
>         ex.getBindingResult().getFieldErrors().forEach(error -> 
>             errors.put(error.getField(), error.getDefaultMessage())
>         );
>         // Return validation errors
>     }
>     
>     @ExceptionHandler(Exception.class)
>     public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
>         // Log error, return generic message
>     }
> }
> ```"

### Q48: What custom exceptions did you create?
**Answer:**
> "**Custom Exceptions:**
>
> ```java
> // Resource not found
> public class ResourceNotFoundException extends RuntimeException {
>     public ResourceNotFoundException(String resource, String field, Object value) {
>         super(String.format(\"%s not found with %s: '%s'\", resource, field, value));
>     }
> }
>
> // Business logic violation
> public class BadRequestException extends RuntimeException { }
>
> // Duplicate resource
> public class DuplicateResourceException extends RuntimeException { }
>
> // Authentication failure
> public class AuthenticationException extends RuntimeException { }
> ```
>
> **Usage:**
> ```java
> Employee emp = repository.findById(id)
>     .orElseThrow(() -> new ResourceNotFoundException(\"Employee\", \"id\", id));
> ```"

### Q49: Why use RuntimeException over checked exceptions?
**Answer:**
> "**RuntimeException (Unchecked):**
> - No need to declare in method signature
> - Cleaner code
> - Spring's @Transactional rolls back on RuntimeException by default
> - Modern best practice for business exceptions
>
> **Checked Exception:**
> - Forces handling at compile time
> - Can clutter code with try-catch
> - Good for recoverable errors
>
> **My approach:** Custom exceptions extend RuntimeException, handled globally by @RestControllerAdvice."

---

## 8. Testing Questions

### Q50: What testing strategy did you use?
**Answer:**
> "**Testing Pyramid:**
> ```
>        /\\      E2E Tests (few)
>       /  \\     
>      /----\\    Integration Tests (some)
>     /      \\   
>    /--------\\  Unit Tests (many)
> ```
>
> **My Implementation:**
> 1. **Unit Tests** - Service layer with mocked dependencies
> 2. **Controller Tests** - MockMvc with security context
> 3. **Integration Tests** - Full stack with H2 database"

### Q51: Explain your unit testing approach.
**Answer:**
> "**Using JUnit 5 + Mockito:**
>
> ```java
> @ExtendWith(MockitoExtension.class)
> class EmployeeServiceImplTest {
>     
>     @Mock
>     private EmployeeRepository employeeRepository;
>     
>     @InjectMocks
>     private EmployeeServiceImpl employeeService;
>     
>     @Test
>     @DisplayName(\"Should create employee successfully\")
>     void createEmployee_Success() {
>         // Given
>         EmployeeRequestDTO request = createRequest();
>         when(employeeRepository.existsByEmail(anyString())).thenReturn(false);
>         when(employeeRepository.save(any())).thenReturn(employee);
>         
>         // When
>         EmployeeResponseDTO result = employeeService.createEmployee(request);
>         
>         // Then
>         assertNotNull(result);
>         assertEquals(\"John\", result.getFirstName());
>         verify(employeeRepository).save(any());
>     }
> }
> ```"

### Q52: What is the difference between @Mock and @InjectMocks?
**Answer:**
> "**@Mock:**
> - Creates a mock object
> - All methods return default values (null, 0, false)
> - Behavior defined using when().thenReturn()
>
> **@InjectMocks:**
> - Creates real instance of the class
> - Injects @Mock objects into its dependencies
>
> ```java
> @Mock
> EmployeeRepository repository;  // Fake repository
>
> @InjectMocks
> EmployeeServiceImpl service;    // Real service with fake repo injected
> ```"

### Q53: How do you test controllers?
**Answer:**
> "**Using MockMvc with @WebMvcTest:**
>
> ```java
> @WebMvcTest(EmployeeController.class)
> class EmployeeControllerTest {
>     
>     @Autowired
>     private MockMvc mockMvc;
>     
>     @MockBean
>     private EmployeeService employeeService;
>     
>     @Test
>     @WithMockUser(roles = \"ADMIN\")
>     void getEmployee_ReturnsEmployee() throws Exception {
>         when(employeeService.getEmployeeById(1L)).thenReturn(employeeDTO);
>         
>         mockMvc.perform(get(\"/api/employees/1\")
>                 .contentType(MediaType.APPLICATION_JSON))
>             .andExpect(status().isOk())
>             .andExpect(jsonPath(\"$.firstName\").value(\"John\"));
>     }
> }
> ```"

### Q54: How do you write integration tests?
**Answer:**
> "**Using @SpringBootTest with H2:**
>
> ```java
> @SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
> @ActiveProfiles(\"test\")
> @TestMethodOrder(OrderAnnotation.class)
> class IntegrationTest {
>     
>     @Autowired
>     private TestRestTemplate restTemplate;
>     
>     @Test
>     @Order(1)
>     void registerAndLogin() {
>         // Register
>         ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
>             \"/api/auth/register\",
>             registerRequest,
>             AuthResponse.class
>         );
>         assertEquals(HttpStatus.OK, response.getStatusCode());
>         assertNotNull(response.getBody().getToken());
>     }
> }
> ```
>
> **Test Properties (H2):**
> ```properties
> spring.datasource.url=jdbc:h2:mem:testdb
> spring.jpa.hibernate.ddl-auto=create-drop
> ```"

### Q55: What is @MockBean vs @Mock?
**Answer:**
> "**@Mock (Mockito):**
> - Pure Mockito annotation
> - Works with @ExtendWith(MockitoExtension.class)
> - Not Spring-aware
>
> **@MockBean (Spring Boot Test):**
> - Spring Boot test annotation
> - Replaces bean in ApplicationContext
> - Works with @SpringBootTest, @WebMvcTest
>
> ```java
> // Unit test - pure Mockito
> @ExtendWith(MockitoExtension.class)
> class ServiceTest {
>     @Mock EmployeeRepository repo;
> }
>
> // Spring test - replaces bean in context
> @WebMvcTest(EmployeeController.class)
> class ControllerTest {
>     @MockBean EmployeeService service;
> }
> ```"

### Q56: How do you test security?
**Answer:**
> "**Using @WithMockUser:**
>
> ```java
> @Test
> @WithMockUser(username = \"admin\", roles = \"ADMIN\")
> void adminCanDeleteEmployee() throws Exception {
>     mockMvc.perform(delete(\"/api/employees/1\"))
>         .andExpect(status().isNoContent());
> }
>
> @Test
> @WithMockUser(username = \"user\", roles = \"EMPLOYEE\")
> void employeeCannotDeleteEmployee() throws Exception {
>     mockMvc.perform(delete(\"/api/employees/1\"))
>         .andExpect(status().isForbidden());
> }
>
> @Test
> void unauthenticatedCannotAccess() throws Exception {
>     mockMvc.perform(get(\"/api/employees\"))
>         .andExpect(status().isUnauthorized());
> }
> ```"

---

## 9. Docker and Deployment Questions

### Q57: Explain your Docker setup.
**Answer:**
> "**Multi-stage Dockerfile:**
>
> ```dockerfile
> # Stage 1: Build
> FROM maven:3.9-eclipse-temurin-21 AS build
> WORKDIR /app
> COPY pom.xml .
> RUN mvn dependency:go-offline  # Cache dependencies
> COPY src ./src
> RUN mvn package -DskipTests
>
> # Stage 2: Run
> FROM eclipse-temurin:21-jre-alpine
> WORKDIR /app
> COPY --from=build /app/target/*.jar app.jar
> EXPOSE 8080
> ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]
> ```
>
> **Benefits:**
> - Small image size (~300MB vs 800MB+)
> - No build tools in final image
> - Cached dependency layer"

### Q58: Explain your Docker Compose setup.
**Answer:**
> "**docker-compose.yml:**
>
> ```yaml
> services:
>   app:
>     build: .
>     ports:
>       - \"8080:8080\"
>     depends_on:
>       mysql:
>         condition: service_healthy
>     environment:
>       - SPRING_PROFILES_ACTIVE=prod
>       - DATABASE_URL=jdbc:mysql://mysql:3306/ems
>     
>   mysql:
>     image: mysql:8.0
>     environment:
>       - MYSQL_DATABASE=ems
>       - MYSQL_ROOT_PASSWORD=secret
>     volumes:
>       - mysql_data:/var/lib/mysql
>       - ./docker/mysql/init:/docker-entrypoint-initdb.d
>     healthcheck:
>       test: mysqladmin ping -h localhost
> ```
>
> **Features:**
> - Health checks ensure MySQL is ready
> - Persistent volume for data
> - Init scripts for schema"

### Q59: How do you handle secrets in Docker?
**Answer:**
> "**Never hardcode secrets in Dockerfile/code!**
>
> **Options:**
> 1. **Environment Variables:**
> ```yaml
> environment:
>   - DB_PASSWORD=${DB_PASSWORD}  # From .env file
> ```
>
> 2. **Docker Secrets (Swarm):**
> ```yaml
> secrets:
>   db_password:
>     file: ./secrets/db_password.txt
> ```
>
> 3. **External Secret Management:**
> - AWS Secrets Manager
> - HashiCorp Vault
> - Kubernetes Secrets
>
> **My approach:** .env.example as template, actual .env in .gitignore"

### Q60: How would you deploy to AWS EC2?
**Answer:**
> "**Steps:**
>
> 1. **Provision EC2:**
>    - Amazon Linux 2 / Ubuntu
>    - t2.micro (free tier) or t2.small
>    - Security group: ports 22, 80, 443, 8080
>
> 2. **Install Docker:**
>    ```bash
>    sudo yum install docker -y
>    sudo systemctl start docker
>    ```
>
> 3. **Deploy Application:**
>    ```bash
>    git clone <repo>
>    cd EmployeeManagementSystem
>    docker-compose up -d
>    ```
>
> 4. **Configure Domain/SSL:**
>    - Route 53 for DNS
>    - ACM for SSL certificate
>    - Nginx reverse proxy or ALB
>
> 5. **Monitoring:**
>    - CloudWatch for logs
>    - Actuator endpoints for health"

### Q61: What is the difference between CMD and ENTRYPOINT?
**Answer:**
> "**CMD:**
> - Default command to run
> - Can be overridden: `docker run image <new-cmd>`
>
> **ENTRYPOINT:**
> - Main process of container
> - Not easily overridden
> - CMD arguments appended
>
> **Best Practice:**
> ```dockerfile
> ENTRYPOINT [\"java\", \"-jar\", \"app.jar\"]
> CMD [\"--spring.profiles.active=prod\"]
>
> # Results in: java -jar app.jar --spring.profiles.active=prod
> ```"

---

## 10. Performance and Optimization Questions

### Q62: How would you optimize database queries?
**Answer:**
> "**1. Indexing:**
> ```java
> @Table(indexes = {
>     @Index(name = \"idx_employee_email\", columnList = \"email\"),
>     @Index(name = \"idx_employee_dept\", columnList = \"department\")
> })
> ```
>
> **2. Pagination:**
> ```java
> Page<Employee> findAll(Pageable pageable);
> ```
>
> **3. Fetch Optimization:**
> ```java
> @Query(\"SELECT e FROM Employee e JOIN FETCH e.department\")
> ```
>
> **4. Projections (only needed fields):**
> ```java
> interface EmployeeSummary {
>     String getFirstName();
>     String getLastName();
> }
> ```
>
> **5. Query Caching:**
> ```java
> @QueryHints(@QueryHint(name = \"org.hibernate.cacheable\", value = \"true\"))
> ```"

### Q63: How would you implement caching?
**Answer:**
> "**Using Spring Cache with Redis:**
>
> ```java
> @Configuration
> @EnableCaching
> public class CacheConfig {
>     @Bean
>     public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
>         return RedisCacheManager.builder(factory)
>             .cacheDefaults(RedisCacheConfiguration.defaultCacheConfig()
>                 .entryTtl(Duration.ofMinutes(10)))
>             .build();
>     }
> }
>
> @Service
> public class EmployeeServiceImpl {
>     
>     @Cacheable(value = \"employees\", key = \"#id\")
>     public EmployeeResponseDTO getEmployeeById(Long id) { }
>     
>     @CacheEvict(value = \"employees\", key = \"#id\")
>     public void updateEmployee(Long id, EmployeeRequestDTO dto) { }
>     
>     @CacheEvict(value = \"employees\", allEntries = true)
>     public void deleteEmployee(Long id) { }
> }
> ```"

### Q64: How do you handle connection pooling?
**Answer:**
> "**Using HikariCP (Spring Boot default):**
>
> ```properties
> spring.datasource.hikari.maximum-pool-size=10
> spring.datasource.hikari.minimum-idle=5
> spring.datasource.hikari.idle-timeout=300000
> spring.datasource.hikari.connection-timeout=20000
> spring.datasource.hikari.max-lifetime=1200000
> ```
>
> **Tuning:**
> - **maximum-pool-size:** 10-20 for typical apps
> - **minimum-idle:** Keep some connections ready
> - **connection-timeout:** Fail fast if no connection available"

### Q65: How would you make your API handle high load?
**Answer:**
> "**Strategies:**
>
> 1. **Horizontal Scaling:**
>    - Multiple app instances behind load balancer
>    - Stateless JWT enables this
>
> 2. **Caching:**
>    - Redis for frequently accessed data
>    - CDN for static content
>
> 3. **Database Optimization:**
>    - Read replicas
>    - Connection pooling
>    - Query optimization
>
> 4. **Async Processing:**
>    - @Async for non-critical operations
>    - Message queues for background jobs
>
> 5. **Rate Limiting:**
>    - Prevent API abuse
>    - Bucket4j or Spring Cloud Gateway
>
> 6. **Circuit Breaker:**
>    - Resilience4j for fault tolerance"

---

## 11. Scenario-Based Questions

### Q66: An employee tries to delete another employee but gets 403. How do you debug?
**Answer:**
> "**Debugging Steps:**
>
> 1. **Check logs:**
>    ```
>    DEBUG o.s.s.w.a.i.FilterSecurityInterceptor - Authorization failed
>    ```
>
> 2. **Verify user role:**
>    - JWT token decode (jwt.io)
>    - Check roles claim
>
> 3. **Verify endpoint security:**
>    ```java
>    @PreAuthorize(\"hasRole('ADMIN')\")
>    @DeleteMapping(\"/{id}\")
>    ```
>
> 4. **Check role prefix:**
>    - Roles must be ROLE_ADMIN in DB
>    - Or configure: `hasRole('ADMIN')` checks for ROLE_ADMIN
>
> 5. **Test with correct role:**
>    - Login as ADMIN user
>    - Use new token"

### Q67: The application is slow. How do you identify the bottleneck?
**Answer:**
> "**Debugging Steps:**
>
> 1. **Enable SQL logging:**
>    ```properties
>    spring.jpa.show-sql=true
>    logging.level.org.hibernate.stat=DEBUG
>    ```
>
> 2. **Check for N+1 queries:**
>    - Look for repeated SELECT statements
>    - Fix with JOIN FETCH or @EntityGraph
>
> 3. **Actuator metrics:**
>    - `/actuator/metrics/http.server.requests`
>    - `/actuator/metrics/hikaricp.connections`
>
> 4. **Profiling:**
>    - Java Flight Recorder
>    - VisualVM
>
> 5. **APM Tools:**
>    - New Relic, Datadog
>    - Distributed tracing"

### Q68: How would you handle a sudden traffic spike?
**Answer:**
> "**Immediate Actions:**
>
> 1. **Scale horizontally:**
>    - Add more container instances
>    - Auto-scaling if on cloud
>
> 2. **Enable caching:**
>    - Cache frequently accessed data
>
> 3. **Optimize database:**
>    - Add read replicas
>    - Increase connection pool
>
> 4. **Rate limiting:**
>    - Prevent abuse
>    - Return 429 Too Many Requests
>
> **Long-term:**
> - Load testing with JMeter
> - Auto-scaling policies
> - CDN for static content
> - Queue non-critical operations"

### Q69: A critical bug is found in production. What's your approach?
**Answer:**
> "**Incident Response:**
>
> 1. **Assess Impact:**
>    - How many users affected?
>    - Data corruption risk?
>
> 2. **Immediate Mitigation:**
>    - Feature flag to disable
>    - Rollback if necessary
>
> 3. **Root Cause Analysis:**
>    - Check logs (ELK, CloudWatch)
>    - Reproduce locally
>
> 4. **Fix and Test:**
>    - Write failing test first
>    - Fix code
>    - Test thoroughly
>
> 5. **Deploy:**
>    - Hotfix branch
>    - Deploy to staging
>    - Deploy to production
>
> 6. **Post-mortem:**
>    - Document incident
>    - Prevent recurrence"

### Q70: How would you add a new module (e.g., Payroll)?
**Answer:**
> "**Steps:**
>
> 1. **Design:**
>    - Entity: Payroll (employeeId, salary, bonus, deductions)
>    - API endpoints
>    - Business rules
>
> 2. **Implementation:**
>    ```
>    entity/Payroll.java
>    repository/PayrollRepository.java
>    service/PayrollService.java
>    service/impl/PayrollServiceImpl.java
>    controller/PayrollController.java
>    dto/PayrollRequestDTO.java
>    dto/PayrollResponseDTO.java
>    ```
>
> 3. **Security:**
>    - Define role permissions
>    - Add @PreAuthorize
>
> 4. **Testing:**
>    - Unit tests for service
>    - Controller tests
>    - Integration tests
>
> 5. **Documentation:**
>    - Update Swagger
>    - Update README"

---

## 12. Code-Level Questions

### Q71: Walk me through the code flow when creating an employee.
**Answer:**
> "**Request Flow:**
>
> ```
> POST /api/employees
> {\"firstName\": \"John\", \"lastName\": \"Doe\", \"email\": \"john@email.com\"}
>
> 1. JwtAuthenticationFilter
>    - Extracts JWT from header
>    - Validates token
>    - Sets SecurityContext
>
> 2. EmployeeController.createEmployee()
>    - @Valid validates request DTO
>    - Calls service
>
> 3. EmployeeServiceImpl.createEmployee()
>    - Checks email doesn't exist
>    - Maps DTO to Entity
>    - Saves via repository
>    - Maps Entity to Response DTO
>
> 4. EmployeeRepository.save()
>    - JPA/Hibernate generates INSERT
>    - Auto-generates ID
>    - JPA Auditing sets createdAt, createdBy
>
> 5. Response
>    - 201 Created
>    - Response body with created employee
> ```"

### Q72: How does your JWT filter work?
**Answer:**
> "```java
> @Component
> public class JwtAuthenticationFilter extends OncePerRequestFilter {
>     
>     @Override
>     protected void doFilterInternal(HttpServletRequest request,
>             HttpServletResponse response, FilterChain chain) {
>         
>         // 1. Extract token from header
>         String header = request.getHeader(\"Authorization\");
>         if (header == null || !header.startsWith(\"Bearer \")) {
>             chain.doFilter(request, response);
>             return;
>         }
>         
>         String token = header.substring(7);
>         
>         // 2. Validate token
>         if (!jwtUtil.validateToken(token)) {
>             chain.doFilter(request, response);
>             return;
>         }
>         
>         // 3. Extract username and load user
>         String username = jwtUtil.extractUsername(token);
>         UserDetails user = userDetailsService.loadUserByUsername(username);
>         
>         // 4. Set authentication in context
>         UsernamePasswordAuthenticationToken auth = 
>             new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
>         SecurityContextHolder.getContext().setAuthentication(auth);
>         
>         chain.doFilter(request, response);
>     }
> }
> ```"

### Q73: Explain your DTO to Entity mapping.
**Answer:**
> "**Manual Mapping (used in project):**
>
> ```java
> // Request to Entity
> private Employee mapToEntity(EmployeeRequestDTO dto) {
>     Employee employee = new Employee();
>     employee.setFirstName(dto.getFirstName());
>     employee.setLastName(dto.getLastName());
>     employee.setEmail(dto.getEmail());
>     return employee;
> }
>
> // Entity to Response
> private EmployeeResponseDTO mapToDTO(Employee entity) {
>     EmployeeResponseDTO dto = new EmployeeResponseDTO();
>     dto.setId(entity.getId());
>     dto.setFullName(entity.getFirstName() + \" \" + entity.getLastName());
>     return dto;
> }
> ```
>
> **Alternative - MapStruct:**
> ```java
> @Mapper(componentModel = \"spring\")
> public interface EmployeeMapper {
>     Employee toEntity(EmployeeRequestDTO dto);
>     EmployeeResponseDTO toDTO(Employee entity);
> }
> ```"

---

## 13. Behavioral Experience Questions

### Q74: Why did you choose to build this project?
**Answer:**
> "I built this project to:
> 1. **Demonstrate full-stack backend skills** - Not just basic CRUD
> 2. **Practice enterprise patterns** - Security, testing, deployment
> 3. **Stay current** - Java 21, Spring Boot 3
> 4. **Create portfolio piece** - Show potential employers
> 5. **Learn by doing** - Solidify concepts through implementation"

### Q75: What did you learn from this project?
**Answer:**
> "**Technical:**
> - Deep understanding of Spring Security and JWT
> - JPA Auditing for tracking changes
> - Docker multi-stage builds
> - Testing patterns (BDD, MockMvc)
>
> **Soft Skills:**
> - Breaking large projects into phases
> - Writing documentation
> - Thinking about security from the start
> - Importance of proper error handling"

### Q76: How do you stay updated with new technologies?
**Answer:**
> "1. **Official docs** - Spring blog, Java release notes
> 2. **YouTube** - Conference talks, tutorials
> 3. **GitHub** - Explore popular repos
> 4. **Building projects** - Like this EMS
> 5. **Online communities** - Reddit, Stack Overflow
> 6. **Courses** - Udemy, Pluralsight"

### Q77: Tell me about a challenging bug you fixed.
**Answer:**
> "**Challenge:** JWT tokens were being invalidated unexpectedly.
>
> **Investigation:**
> - Checked token expiration - correct
> - Validated signature - correct
> - Found issue: Server restart generated new secret key!
>
> **Solution:**
> - Externalized JWT secret to properties file
> - Used environment variable in production
> - Added graceful handling for invalid tokens
>
> **Lesson:** Never hardcode secrets, externalize configuration."

---

## 🎯 Quick Reference Cards

### Card 1: Project Elevator Pitch (30 seconds)
```
Enterprise Employee Management System built with Java 21 + Spring Boot 3.
Features: Employee CRUD, Department management, Leave/Attendance tracking.
Security: JWT authentication with role-based access (Admin/HR/Employee).
Quality: Unit tests, Integration tests, Docker deployment, Swagger docs.
```

### Card 2: Architecture Summary
```
Layered Architecture:
├── Controller (REST APIs)
├── Service (Business Logic)
├── Repository (Data Access)
└── Entity (Domain Models)

Cross-cutting: Security (JWT), Exception Handling, Logging, Auditing
```

### Card 3: Tech Stack Quick List
```
Backend:  Java 21, Spring Boot 3, Spring Security, JWT
Database: MySQL, Spring Data JPA, Hibernate
Testing:  JUnit 5, Mockito, MockMvc, H2
DevOps:   Docker, Docker Compose, Maven
Docs:     Swagger/OpenAPI 3.0
```

### Card 4: Key Numbers to Remember
```
- 6 Phases of development
- 4 Modules (Employee, Department, Leave, Attendance)
- 3 Roles (ADMIN, HR, EMPLOYEE)
- 77+ potential interview questions covered
- ~40 Java files created
- ~300MB Docker image (multi-stage)
```

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
- [JWT.io](https://jwt.io/)
- [Docker Documentation](https://docs.docker.com/)
- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)

---

> **Good Luck with your interviews! 🍀**
> 
> Remember: Confidence comes from preparation. You've built this project, you understand it deeply.

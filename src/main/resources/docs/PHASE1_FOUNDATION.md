# Phase 1 - Foundation Setup

## Overview
Phase 1 establishes the project foundation with Spring Boot 3, Maven, MySQL, layered architecture, exception handling, and logging.

## Project Structure
```
EmployeeManagementSystem/
+-- pom.xml
+-- src/main/java/com/ems/
    +-- EmployeeManagementSystemApplication.java
    +-- config/
    +-- controller/
    +-- service/impl/
    +-- repository/
    +-- entity/
    +-- dto/
        +-- ApiResponse.java
    +-- exception/
        +-- GlobalExceptionHandler.java
        +-- ResourceNotFoundException.java
        +-- BadRequestException.java
        +-- ErrorDetails.java
    +-- util/
        +-- AppConstants.java
+-- src/main/resources/
    +-- application.properties
    +-- logback-spring.xml
    +-- docs/PHASE1_FOUNDATION.md
```


## Key Annotations Explained

### @SpringBootApplication
Combines @Configuration, @EnableAutoConfiguration, @ComponentScan
- @Configuration: Marks class as bean definition source
- @EnableAutoConfiguration: Auto-configures based on classpath
- @ComponentScan: Scans for @Component, @Service, @Repository, @Controller

### @RestControllerAdvice
Global exception handler for all REST controllers
- Combines @ControllerAdvice and @ResponseBody
- Centralizes exception handling logic

### @ExceptionHandler
Marks method to handle specific exception types

### @ResponseStatus
Sets HTTP status code for exceptions

## Spring Boot Internals

### How Auto-Configuration Works
1. Spring Boot reads META-INF/spring/AutoConfiguration.imports
2. Loads auto-config classes based on @Conditional annotations
3. @ConditionalOnClass - Only if class present on classpath
4. @ConditionalOnMissingBean - Only if user hasnt defined bean

### Application Startup Flow
1. main() calls SpringApplication.run()
2. Creates ApplicationContext (IoC container)
3. Component scanning finds beans
4. Auto-configuration runs
5. Embedded Tomcat starts
6. Application ready on port 8080

## Interview Questions - Phase 1

### Q1: What is Spring Boot and why use it?
A: Spring Boot is an opinionated framework built on Spring that simplifies configuration through auto-configuration, embedded servers, and starter dependencies.

### Q2: What does @SpringBootApplication do?
A: Combines @Configuration, @EnableAutoConfiguration, and @ComponentScan. Marks the main class and enables auto-configuration.

### Q3: Explain the layered architecture in your project.
A: Controller (REST endpoints) -> Service (business logic) -> Repository (data access) -> Entity (JPA models). DTOs for data transfer.

### Q4: Why use GlobalExceptionHandler?
A: Centralizes exception handling, provides consistent error responses, separates error logic from business logic.

### Q5: What is the difference between JPA and Hibernate?
A: JPA is a specification defining ORM interfaces. Hibernate is an implementation of JPA.

### Q6: Explain HikariCP.
A: High-performance JDBC connection pool. Default in Spring Boot 2+. Manages database connections efficiently.

### Q7: What is the purpose of application.properties?
A: Externalized configuration for database, server, logging. Allows different configs per environment.

### Q8: What is ddl-auto=update?
A: Hibernate auto-updates schema based on entities. Options: none, validate, update, create, create-drop.

### Q9: Why use Lombok?
A: Reduces boilerplate code. Generates getters, setters, constructors at compile time via annotation processing.

### Q10: Why is @Data bad for JPA entities?
A: Generates equals/hashCode using all fields, causing issues with lazy loading and proxies. Use @Getter/@Setter separately.

### Q11: What is a fat JAR?
A: JAR containing application code AND all dependencies. Created by Spring Boot Maven plugin. Run with java -jar.

### Q12: Explain Maven dependency scopes.
A: compile (default, all phases), provided (compile only), runtime (not compile, runtime), test (test only).

### Q13: How does Spring Boot auto-configuration work?
A: Reads AutoConfiguration.imports, uses @Conditional annotations to configure beans based on classpath classes.

### Q14: What is the IoC container?
A: ApplicationContext manages bean lifecycle, dependency injection. Creates, wires, and destroys beans.

### Q15: Why use @RestControllerAdvice?
A: Combines @ControllerAdvice and @ResponseBody. Handles exceptions globally and returns JSON responses.

## Best Practices Followed
- Layered Architecture (Controller/Service/Repository)
- Centralized Exception Handling
- Externalized Configuration
- Proper Logging with Logback
- Standard API Response Format
- Validation Ready

## TODO Next Steps - Phase 2
- Create Employee Entity
- Create EmployeeRepository
- Create EmployeeService
- Create EmployeeController with CRUD APIs
- Add Pagination, Sorting, Searching
- Add Bean Validation

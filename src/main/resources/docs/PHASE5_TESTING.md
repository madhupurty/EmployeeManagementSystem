# Phase 5: Testing Implementation

## Overview

Phase 5 implements comprehensive testing for the Employee Management System using **JUnit 5**, **Mockito**, and **Spring Boot Test**. This includes unit tests, controller tests, and integration tests.

---

## Table of Contents

1. [Testing Technologies](#testing-technologies)
2. [Test Structure](#test-structure)
3. [Unit Tests](#unit-tests)
4. [Controller Tests](#controller-tests)
5. [Integration Tests](#integration-tests)
6. [Test Configuration](#test-configuration)
7. [Running Tests](#running-tests)
8. [Best Practices](#best-practices)
9. [Interview Questions](#interview-questions)

---

## Testing Technologies

| Technology | Purpose |
|------------|---------|
| **JUnit 5** | Testing framework (assertions, lifecycle) |
| **Mockito** | Mocking framework for unit tests |
| **Spring Boot Test** | Spring testing support |
| **MockMvc** | Testing controllers without starting server |
| **H2 Database** | In-memory database for integration tests |
| **AssertJ** | Fluent assertion library |
| **Spring Security Test** | Security testing utilities |

### Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

---

## Test Structure

```
src/test/
├── java/
│   └── com/
│       └── ems/
│           ├── service/
│           │   ├── EmployeeServiceImplTest.java
│           │   ├── DepartmentServiceImplTest.java
│           │   └── AuthServiceImplTest.java
│           ├── controller/
│           │   └── EmployeeControllerTest.java
│           └── integration/
│               └── IntegrationTest.java
└── resources/
    └── application-test.properties
```

---

## Unit Tests

### What is Unit Testing?

Unit tests test a single unit (class/method) in **isolation**. Dependencies are mocked to focus only on the logic being tested.

### Key Annotations

| Annotation | Purpose |
|------------|---------|
| `@ExtendWith(MockitoExtension.class)` | Enables Mockito annotations |
| `@Mock` | Creates a mock object |
| `@InjectMocks` | Injects mocks into the class being tested |
| `@BeforeEach` | Runs before each test method |
| `@Nested` | Groups related tests |
| `@DisplayName` | Human-readable test name |

### Example: Employee Service Test

```java
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Unit Tests")
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @Test
    @DisplayName("Should create employee successfully")
    void createEmployee_WhenEmailIsUnique_ShouldReturnCreatedEmployee() {
        // Given (Arrange)
        given(employeeRepository.existsByEmail(anyString())).willReturn(false);
        given(employeeRepository.save(any(Employee.class))).willReturn(employee);

        // When (Act)
        EmployeeResponseDTO result = employeeService.createEmployee(requestDTO);

        // Then (Assert)
        assertThat(result).isNotNull();
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }
}
```

### BDD Style Testing (Given-When-Then)

```java
// Given: Setup test data and mock behavior
given(repository.findById(1L)).willReturn(Optional.of(entity));

// When: Execute the method being tested
Result result = service.getById(1L);

// Then: Verify the outcome
assertThat(result).isNotNull();
verify(repository, times(1)).findById(1L);
```

### Mockito Matchers

| Matcher | Usage |
|---------|-------|
| `any()` | Matches any object |
| `anyLong()` | Matches any Long |
| `anyString()` | Matches any String |
| `eq(value)` | Matches specific value |
| `argThat(predicate)` | Custom matcher |

### Verification Methods

```java
verify(mock, times(1)).method();     // Called exactly once
verify(mock, never()).method();       // Never called
verify(mock, atLeast(2)).method();   // Called at least twice
verify(mock, atMost(3)).method();    // Called at most 3 times
```

---

## Controller Tests

### What is Controller Testing?

Controller tests verify HTTP layer behavior including:
- Request/response handling
- Status codes
- JSON structure
- Validation
- Security (authentication/authorization)

### Key Annotations

| Annotation | Purpose |
|------------|---------|
| `@WebMvcTest(Controller.class)` | Loads only web layer |
| `@MockBean` | Creates mock in Spring context |
| `@WithMockUser` | Provides mock authentication |

### Example: Employee Controller Test

```java
@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Unit Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create employee when ADMIN")
    void createEmployee_WhenAdmin_ShouldReturn201() throws Exception {
        // Given
        given(employeeService.createEmployee(any())).willReturn(responseDTO);

        // When & Then
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.firstName", is("John")));
    }
}
```

### MockMvc Request Builders

```java
// GET request
mockMvc.perform(get("/api/employees"))

// GET with path variable
mockMvc.perform(get("/api/employees/{id}", 1))

// GET with query params
mockMvc.perform(get("/api/employees/search")
        .param("keyword", "John")
        .param("page", "0"))

// POST with body
mockMvc.perform(post("/api/employees")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonContent))

// PUT/DELETE
mockMvc.perform(put("/api/employees/1").with(csrf()).content(...))
mockMvc.perform(delete("/api/employees/1").with(csrf()))
```

### Security Testing

```java
// Test with mock user
@WithMockUser(roles = "ADMIN")

// Test with specific user
@WithMockUser(username = "admin", roles = {"ADMIN"})

// Test authentication required
@Test
void request_WhenNotAuthenticated_ShouldReturn401() {
    mockMvc.perform(get("/api/employees"))
           .andExpect(status().isUnauthorized());
}

// Test authorization
@Test
@WithMockUser(roles = "EMPLOYEE")
void delete_WhenEmployee_ShouldReturn403() {
    mockMvc.perform(delete("/api/employees/1"))
           .andExpect(status().isForbidden());
}
```

---

## Integration Tests

### What is Integration Testing?

Integration tests verify that multiple components work together correctly. They use:
- Real Spring context
- Real database (H2 in-memory)
- Real HTTP flow

### Key Annotations

| Annotation | Purpose |
|------------|---------|
| `@SpringBootTest` | Loads full application context |
| `@AutoConfigureMockMvc` | Configures MockMvc |
| `@ActiveProfiles("test")` | Uses test profile |
| `@TestMethodOrder` | Orders test execution |

### Example: Integration Test

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static String adminToken;

    @Test
    @Order(1)
    @DisplayName("Should register admin user")
    void registerAdmin_ShouldSucceed() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract token for later tests
        adminToken = extractToken(result);
    }

    @Test
    @Order(2)
    @DisplayName("Should create employee with token")
    void createEmployee_WithToken_ShouldSucceed() throws Exception {
        mockMvc.perform(post("/api/employees")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated());
    }
}
```

---

## Test Configuration

### application-test.properties

```properties
# H2 in-memory database
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop

# JWT (for testing)
jwt.secret=dGVzdFNlY3JldEtleUZvclRlc3Rpbmc=
jwt.expiration=3600000

# Reduce logging noise
logging.level.root=WARN
```

---

## Running Tests

### Run All Tests

```bash
# Maven
mvn test

# With coverage report
mvn test jacoco:report
```

### Run Specific Test Class

```bash
mvn test -Dtest=EmployeeServiceImplTest
```

### Run Tests Matching Pattern

```bash
mvn test -Dtest="*ServiceImplTest"
```

### Run Integration Tests Only

```bash
mvn test -Dtest="*IntegrationTest"
```

---

## Best Practices

### 1. Test Naming Convention

```java
// Pattern: methodName_scenario_expectedResult
void createEmployee_WhenEmailExists_ShouldThrowException()
void getById_WhenNotFound_ShouldReturn404()
```

### 2. AAA Pattern (Arrange-Act-Assert)

```java
@Test
void testMethod() {
    // Arrange - Setup test data
    Employee employee = new Employee();
    given(repository.findById(1L)).willReturn(Optional.of(employee));
    
    // Act - Execute method
    EmployeeDTO result = service.getById(1L);
    
    // Assert - Verify result
    assertThat(result).isNotNull();
}
```

### 3. Use @Nested for Grouping

```java
@Nested
@DisplayName("Create Employee Tests")
class CreateEmployeeTests {
    @Test void success_case() {}
    @Test void duplicate_email_case() {}
}

@Nested
@DisplayName("Delete Employee Tests")
class DeleteEmployeeTests {
    @Test void success_case() {}
    @Test void not_found_case() {}
}
```

### 4. Test Both Happy and Unhappy Paths

```java
// Happy path
@Test
void create_WhenValid_ShouldSucceed() { ... }

// Unhappy paths
@Test
void create_WhenEmailExists_ShouldThrowException() { ... }

@Test
void create_WhenInvalidData_ShouldReturn400() { ... }
```

### 5. Don't Test Framework Code

Focus on YOUR business logic, not Spring's functionality.

---

## Interview Questions

### Q1: What is the difference between Unit and Integration tests?
**Answer:**
| Aspect | Unit Test | Integration Test |
|--------|-----------|-----------------|
| Scope | Single class/method | Multiple components |
| Dependencies | Mocked | Real |
| Speed | Fast | Slower |
| Database | None | In-memory (H2) |
| Purpose | Test logic isolation | Test component interaction |

### Q2: What is Mockito and why use it?
**Answer:** Mockito is a mocking framework that creates fake objects to replace real dependencies. Benefits:
- Isolate class under test
- Control behavior of dependencies
- Verify interactions
- No need for real database/services

### Q3: Explain @Mock vs @MockBean
**Answer:**
- `@Mock`: Pure Mockito annotation, creates mock outside Spring context
- `@MockBean`: Spring Boot annotation, replaces/adds bean in Spring context

### Q4: What is MockMvc?
**Answer:** MockMvc is a Spring test utility that simulates HTTP requests without starting a server. It:
- Tests controller layer
- Verifies request/response handling
- Tests validation and security
- Is faster than actual HTTP calls

### Q5: How do you test security?
**Answer:**
```java
// Mock user with role
@WithMockUser(roles = "ADMIN")

// Test unauthorized access
mockMvc.perform(get("/api/protected"))
       .andExpect(status().isUnauthorized());

// Test forbidden access
@WithMockUser(roles = "EMPLOYEE")
@Test
void adminOnly_WhenEmployee_ShouldReturn403() {
    mockMvc.perform(delete("/api/employees/1"))
           .andExpect(status().isForbidden());
}
```

### Q6: What is BDD style testing?
**Answer:** BDD (Behavior-Driven Development) uses Given-When-Then format:
- **Given**: Setup preconditions and mocks
- **When**: Execute the action being tested
- **Then**: Verify the expected outcome

### Q7: Why use H2 for testing?
**Answer:**
- In-memory: No external database needed
- Fast: Creates/drops on each test run
- Compatible: Supports SQL syntax
- Isolated: Each test starts fresh

### Q8: What annotations enable JPA testing?
**Answer:**
- `@DataJpaTest`: Loads only JPA components
- `@AutoConfigureTestDatabase`: Configures test database
- `@Transactional`: Rolls back after each test

### Q9: How to verify mock interactions?
**Answer:**
```java
// Verify called once
verify(repository).save(any());

// Verify never called
verify(repository, never()).delete(any());

// Verify call count
verify(repository, times(2)).findById(anyLong());

// Verify argument
verify(repository).save(argThat(emp -> 
    emp.getEmail().equals("test@test.com")
));
```

### Q10: What is @TestMethodOrder?
**Answer:** Orders test method execution. Useful for integration tests where order matters:
```java
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class Tests {
    @Test @Order(1) void first() {}
    @Test @Order(2) void second() {}
}
```

---

## Test Coverage Summary

| Layer | Test File | Coverage |
|-------|-----------|----------|
| Service | EmployeeServiceImplTest | CRUD, Search, Validation |
| Service | DepartmentServiceImplTest | CRUD, Manager ops |
| Service | AuthServiceImplTest | Register, Login |
| Controller | EmployeeControllerTest | HTTP, Security |
| Integration | IntegrationTest | Full flow |

---

## Summary

Phase 5 implemented:

✅ JUnit 5 unit tests with Mockito  
✅ BDD-style testing (Given-When-Then)  
✅ Service layer tests  
✅ Controller tests with MockMvc  
✅ Security testing with @WithMockUser  
✅ Integration tests with H2 database  
✅ Test configuration for isolated testing  

---

## Next Steps (Phase 6)

- Docker containerization
- Docker Compose for multi-container setup
- AWS EC2 deployment
- Swagger/OpenAPI documentation refinement

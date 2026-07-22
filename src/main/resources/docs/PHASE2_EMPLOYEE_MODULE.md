# Phase 2 - Employee Module

## Overview
Phase 2 implements the complete Employee CRUD (Create, Read, Update, Delete) module with pagination, sorting, searching, and Bean Validation.

---

## Project Structure After Phase 2

```
EmployeeManagementSystem/
├── pom.xml
├── src/main/java/com/ems/
│   ├── EmployeeManagementSystemApplication.java
│   ├── config/
│   ├── controller/
│   │   └── EmployeeController.java          ✅ NEW
│   ├── service/
│   │   ├── EmployeeService.java             ✅ NEW
│   │   └── impl/
│   │       └── EmployeeServiceImpl.java     ✅ NEW
│   ├── repository/
│   │   └── EmployeeRepository.java          ✅ NEW
│   ├── entity/
│   │   └── Employee.java                    ✅ NEW
│   ├── dto/
│   │   ├── ApiResponse.java
│   │   ├── EmployeeRequestDTO.java          ✅ NEW
│   │   ├── EmployeeResponseDTO.java         ✅ NEW
│   │   └── PagedResponseDTO.java            ✅ NEW
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java      ✅ UPDATED
│   │   ├── ResourceNotFoundException.java
│   │   ├── BadRequestException.java
│   │   └── ErrorDetails.java
│   └── util/
│       └── AppConstants.java
├── src/main/resources/
│   ├── application.properties
│   ├── logback-spring.xml
│   └── docs/
│       ├── PHASE1_FOUNDATION.md
│       └── PHASE2_EMPLOYEE_MODULE.md        ✅ NEW
```

---

## API Endpoints Summary

| Method | Endpoint | Description | Status Code |
|--------|----------|-------------|-------------|
| POST | `/api/employees` | Create new employee | 201 Created |
| PUT | `/api/employees/{id}` | Update employee | 200 OK |
| GET | `/api/employees/{id}` | Get employee by ID | 200 OK |
| GET | `/api/employees/code/{code}` | Get employee by code | 200 OK |
| DELETE | `/api/employees/{id}` | Delete employee | 200 OK |
| PATCH | `/api/employees/{id}/deactivate` | Soft delete (deactivate) | 200 OK |
| GET | `/api/employees` | Get all (paginated) | 200 OK |
| GET | `/api/employees/department/{dept}` | Filter by department | 200 OK |
| GET | `/api/employees/status/{status}` | Filter by status | 200 OK |
| GET | `/api/employees/search?keyword=x` | Search employees | 200 OK |
| GET | `/api/employees/search/advanced` | Multi-filter search | 200 OK |
| GET | `/api/employees/departments` | Get all departments | 200 OK |
| GET | `/api/employees/designations` | Get all designations | 200 OK |
| GET | `/api/employees/manager/{id}` | Get by manager | 200 OK |
| GET | `/api/employees/check-email?email=x` | Check email exists | 200 OK |

---

## Key Annotations Explained

### Entity Layer (@Entity)

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@Entity` | Marks class as JPA entity (maps to table) | `@Entity public class Employee` |
| `@Table` | Configures table name, constraints, indexes | `@Table(name="employees")` |
| `@Id` | Marks primary key field | `@Id private Long id;` |
| `@GeneratedValue` | Auto-generate primary key | `@GeneratedValue(strategy=IDENTITY)` |
| `@Column` | Configure column (name, length, nullable) | `@Column(name="first_name", nullable=false)` |
| `@Enumerated` | Map Java enum to database | `@Enumerated(EnumType.STRING)` |
| `@CreationTimestamp` | Auto-set timestamp on create | Audit field |
| `@UpdateTimestamp` | Auto-set timestamp on update | Audit field |

### Repository Layer (@Repository)

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@Repository` | Marks class as repository + exception translation | Interface level |
| `@Query` | Custom JPQL/SQL query | `@Query("SELECT e FROM Employee e...")` |
| `@Param` | Bind method parameter to query | `@Param("keyword") String keyword` |
| `@Modifying` | Required for UPDATE/DELETE queries | `@Modifying @Query("UPDATE...")` |

### Service Layer (@Service)

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@Service` | Marks class as service component | Class level |
| `@Transactional` | Manage transaction boundaries | Method or class level |
| `@Transactional(readOnly=true)` | Optimize for read operations | Query methods |

### Controller Layer (@RestController)

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@RestController` | REST controller (returns JSON) | Class level |
| `@RequestMapping` | Base URL path | `@RequestMapping("/api/employees")` |
| `@GetMapping` | Handle GET requests | `@GetMapping("/{id}")` |
| `@PostMapping` | Handle POST requests | `@PostMapping` |
| `@PutMapping` | Handle PUT requests | `@PutMapping("/{id}")` |
| `@DeleteMapping` | Handle DELETE requests | `@DeleteMapping("/{id}")` |
| `@PatchMapping` | Handle PATCH requests | `@PatchMapping("/{id}/deactivate")` |
| `@PathVariable` | Extract value from URL path | `@PathVariable Long id` |
| `@RequestParam` | Extract query parameter | `@RequestParam String keyword` |
| `@RequestBody` | Deserialize request body to object | `@RequestBody EmployeeDTO dto` |
| `@Valid` | Trigger Bean Validation | `@Valid @RequestBody DTO dto` |

### Validation Annotations

| Annotation | Purpose | Example |
|------------|---------|---------|
| `@NotBlank` | Not null, not empty, not whitespace | Required strings |
| `@NotNull` | Not null (allows empty) | Required objects |
| `@Size` | String length constraint | `@Size(min=2, max=50)` |
| `@Email` | Valid email format | Email fields |
| `@Pattern` | Custom regex validation | Phone numbers |
| `@Past` | Date must be in past | Date of birth |
| `@PastOrPresent` | Date in past or today | Date of joining |
| `@Positive` | Number > 0 | IDs |
| `@DecimalMin` | Minimum decimal value | Salary |
| `@Digits` | Precision and scale | `@Digits(integer=8, fraction=2)` |

---

## Spring Boot Internals

### How Spring Data JPA Works

1. **Interface Declaration**: You declare repository interface extending JpaRepository
2. **Proxy Creation**: Spring creates a proxy implementation at runtime
3. **Method Parsing**: Spring parses method names to generate queries
   - `findByEmail` → `SELECT * FROM employees WHERE email = ?`
   - `findByDepartmentAndStatus` → `SELECT * FROM ... WHERE department = ? AND status = ?`
4. **Query Execution**: Proxy delegates to EntityManager
5. **Result Mapping**: JPA maps ResultSet to Entity objects

### How @Transactional Works

1. **Proxy Creation**: Spring creates a proxy around your service bean
2. **Method Interception**: When @Transactional method is called:
   - Proxy gets database connection from pool
   - Proxy starts transaction (`BEGIN`)
   - Proxy calls your actual method
3. **Commit/Rollback**:
   - Success → `COMMIT`
   - RuntimeException → `ROLLBACK`
   - Checked Exception → `COMMIT` (unless configured otherwise)
4. **Connection Return**: Connection returned to pool

### How Bean Validation Works

1. **Request Arrives**: Controller receives HTTP request
2. **Deserialization**: Jackson converts JSON to DTO object
3. **Validation Trigger**: `@Valid` triggers Hibernate Validator
4. **Constraint Check**: Each field's annotations are validated
5. **Error Collection**: All violations collected into BindingResult
6. **Exception Thrown**: If errors exist, MethodArgumentNotValidException thrown
7. **Handler Intercepts**: GlobalExceptionHandler catches and formats response

---

## Best Practices Implemented

### 1. DTO Pattern
- **Request DTO**: Contains only input fields with validation
- **Response DTO**: Contains output fields (includes ID, timestamps)
- **Benefit**: Separation between API contract and database schema

### 2. Service Interface + Implementation
- **Interface**: Defines contract (what the service does)
- **Implementation**: Contains logic (how it does it)
- **Benefit**: Loose coupling, easier testing, swappable implementations

### 3. Constructor Injection
```java
@RequiredArgsConstructor
public class EmployeeServiceImpl {
    private final EmployeeRepository employeeRepository;
}
```
- **Benefit**: Immutable dependencies, easier testing, no null issues

### 4. Pagination with PagedResponseDTO
- **Convert Spring's Page**: To custom response format
- **1-indexed pages**: More intuitive for API consumers
- **Include metadata**: totalPages, totalElements, first, last

### 5. Comprehensive Exception Handling
- **Specific handlers**: For each exception type
- **Consistent format**: All errors follow same structure
- **Logging**: All errors logged with appropriate level

### 6. Audit Fields
- **createdAt/updatedAt**: Auto-managed timestamps
- **createdBy/updatedBy**: For user tracking (Phase 3)

---

## Sample API Requests & Responses

### Create Employee
```http
POST /api/employees
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@company.com",
  "phoneNumber": "9876543210",
  "department": "Engineering",
  "designation": "Software Engineer",
  "salary": 75000.00,
  "dateOfBirth": "1990-05-15",
  "dateOfJoining": "2024-01-10",
  "address": "123 Main Street",
  "city": "Bangalore",
  "state": "Karnataka",
  "country": "India",
  "zipCode": "560001"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Employee created successfully",
  "data": {
    "id": 1,
    "employeeCode": "EMP-20240115-0001",
    "firstName": "John",
    "lastName": "Doe",
    "fullName": "John Doe",
    "email": "john.doe@company.com",
    "department": "Engineering",
    "designation": "Software Engineer",
    "salary": 75000.00,
    "status": "ACTIVE",
    "createdAt": "2024-01-15T10:30:00"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Get All with Pagination
```http
GET /api/employees?page=1&size=10&sortBy=firstName&sortDir=asc
```

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [...],
    "pageNumber": 1,
    "pageSize": 10,
    "totalElements": 45,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

### Search Employees
```http
GET /api/employees/search?keyword=john&page=1&size=10
```

### Validation Error Response
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "message": "Validation Failed",
  "details": "Invalid input data",
  "validationErrors": {
    "firstName": "First name is required",
    "email": "Please provide a valid email address"
  }
}
```

---

## Interview Questions - Phase 2

### Q1: Explain the layered architecture in your project.
**Answer:**
```
Controller Layer → Service Layer → Repository Layer → Database
     ↓                   ↓                 ↓
   DTOs           Business Logic      Entities
   HTTP         Transaction Mgmt      SQL Queries
   Validation    DTO Conversion       JPA/Hibernate
```

The layers are:
1. **Controller**: Handles HTTP requests, validation, returns responses
2. **Service**: Contains business logic, transaction management, DTO conversion
3. **Repository**: Data access layer, interfaces with database
4. **Entity**: JPA entities mapped to database tables

**Why layering?**
- Separation of concerns
- Each layer has single responsibility
- Easy to test each layer independently
- Can change one layer without affecting others

---

### Q2: Why use DTOs instead of Entity directly in API?
**Answer:**
1. **Security**: Entity may have sensitive fields (password, internal IDs)
2. **Decoupling**: API contract independent of database schema
3. **Different validation**: Create vs Update may need different rules
4. **Performance**: Don't expose lazy-loaded relationships
5. **Flexibility**: Can add computed fields (fullName) in response

---

### Q3: How does Spring Data JPA generate queries from method names?
**Answer:**
Spring parses method name at startup:
- `findBy` → SELECT query
- `deleteBy` → DELETE query
- Field names → WHERE clause columns
- `And/Or` → Multiple conditions
- `OrderBy` → ORDER BY clause
- `IgnoreCase` → Case-insensitive comparison

Example:
```java
findByDepartmentAndStatusOrderByFirstNameAsc(String dept, Status status)
```
Generates:
```sql
SELECT * FROM employees 
WHERE department = ? AND status = ?
ORDER BY first_name ASC
```

---

### Q4: Explain @Transactional annotation.
**Answer:**
@Transactional manages database transactions:
- **Starts transaction** before method execution
- **Commits** if method completes successfully
- **Rollbacks** if RuntimeException thrown

Key attributes:
- `readOnly=true`: Optimizes for read operations
- `rollbackFor`: Specify which exceptions cause rollback
- `propagation`: How transactions interact (REQUIRED, REQUIRES_NEW)
- `isolation`: Transaction isolation level

**Important**: Doesn't work for:
- Private methods
- Self-invocation (this.method())

---

### Q5: What is the N+1 query problem?
**Answer:**
N+1 problem occurs with lazy loading:

```java
// 1 query to get employees
List<Employee> employees = repository.findAll();

// N queries - one for each employee's department
for (Employee e : employees) {
    e.getDepartment().getName(); // Triggers SQL query!
}
```

**Solutions:**
1. **Join Fetch**: `@Query("SELECT e FROM Employee e JOIN FETCH e.department")`
2. **@EntityGraph**: Define fetch plan
3. **DTOs**: Project only needed fields
4. **Batch fetching**: `@BatchSize(size=50)`

---

### Q6: Difference between JPQL and native SQL?
**Answer:**

| JPQL | Native SQL |
|------|------------|
| Uses entity/field names | Uses table/column names |
| Database independent | Database specific |
| Object-oriented | SQL syntax |
| `SELECT e FROM Employee e` | `SELECT * FROM employees` |
| Better for portability | Better for complex queries |

---

### Q7: How does pagination work in Spring Data JPA?
**Answer:**
1. Create `Pageable` object with page number, size, sort
2. Pass to repository method
3. Spring adds `LIMIT` and `OFFSET` to SQL
4. Returns `Page<Entity>` with content and metadata

```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("firstName"));
Page<Employee> page = repository.findAll(pageable);

// Generated SQL:
// SELECT * FROM employees ORDER BY first_name LIMIT 10 OFFSET 0
```

---

### Q8: Why use BigDecimal for salary instead of Double?
**Answer:**
- **Double** uses binary floating-point: `0.1 + 0.2 = 0.30000000004`
- **BigDecimal** uses decimal: `0.1 + 0.2 = 0.3` exactly

For financial calculations, **precision matters**. BigDecimal provides exact decimal arithmetic.

---

### Q9: Explain @Valid and Bean Validation flow.
**Answer:**
1. Request arrives at controller
2. Jackson deserializes JSON to DTO
3. `@Valid` triggers Hibernate Validator
4. Each field annotation is checked (@NotBlank, @Email, etc.)
5. All errors collected
6. If errors exist → `MethodArgumentNotValidException`
7. `GlobalExceptionHandler` catches and returns 400 response

---

### Q10: What is @RestControllerAdvice?
**Answer:**
`@RestControllerAdvice` = `@ControllerAdvice` + `@ResponseBody`

- **@ControllerAdvice**: Global exception handler for all controllers
- **@ResponseBody**: Return values serialized to JSON

It intercepts exceptions from any controller and provides consistent error responses.

---

### Q11: Constructor injection vs Field injection - which is better?
**Answer:**
**Constructor injection is better:**

| Constructor Injection | Field Injection |
|----------------------|-----------------|
| Dependencies are required | Dependencies can be null |
| Immutable (final fields) | Mutable |
| Easy to test (pass mocks) | Needs reflection |
| Fails fast on startup | Fails late at runtime |
| Spring team recommended | Discouraged |

---

### Q12: Why create separate Request and Response DTOs?
**Answer:**
1. **Request DTO**: Has validation annotations, no ID, no timestamps
2. **Response DTO**: Has ID, timestamps, computed fields, no validation

Benefits:
- Different validation needs
- Clear API documentation
- Security (control what's exposed)
- Flexibility to change independently

---

### Q13: How would you implement soft delete?
**Answer:**
Instead of deleting record, update status to INACTIVE:

```java
@Modifying
@Query("UPDATE Employee e SET e.status = 'INACTIVE' WHERE e.id = :id")
int softDeleteEmployee(@Param("id") Long id);
```

Benefits:
- Data is preserved for auditing
- Can be restored if needed
- Maintains referential integrity

---

### Q14: What HTTP status codes should different operations return?
**Answer:**

| Operation | Status Code | Reason |
|-----------|-------------|--------|
| GET success | 200 OK | Resource found |
| GET not found | 404 Not Found | Resource doesn't exist |
| POST success | 201 Created | New resource created |
| PUT success | 200 OK | Resource updated |
| DELETE success | 200 OK or 204 No Content | Resource deleted |
| Validation error | 400 Bad Request | Invalid input |
| Server error | 500 Internal Server Error | Unexpected error |

---

### Q15: How do you handle validation errors?
**Answer:**
1. Use `@Valid` on @RequestBody parameter
2. Validation annotations on DTO fields (@NotBlank, @Email)
3. `GlobalExceptionHandler` catches `MethodArgumentNotValidException`
4. Extract field errors and return structured response:

```json
{
  "message": "Validation Failed",
  "validationErrors": {
    "firstName": "First name is required",
    "email": "Invalid email format"
  }
}
```

---

## Database Schema

After running the application, Hibernate creates:

```sql
CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_code VARCHAR(20) NOT NULL UNIQUE,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone_number VARCHAR(15),
    department VARCHAR(50),
    designation VARCHAR(100),
    salary DECIMAL(10,2),
    date_of_birth DATE,
    date_of_joining DATE,
    address VARCHAR(255),
    city VARCHAR(50),
    state VARCHAR(50),
    country VARCHAR(50),
    zip_code VARCHAR(10),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    manager_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    
    INDEX idx_employee_email (email),
    INDEX idx_employee_department (department),
    INDEX idx_employee_status (status)
);
```

---

## Testing the APIs

### Using cURL

```bash
# Create Employee
curl -X POST http://localhost:8080/api/employees \
  -H "Content-Type: application/json" \
  -d '{"firstName":"John","lastName":"Doe","email":"john@example.com"}'

# Get All Employees
curl http://localhost:8080/api/employees?page=1&size=10

# Search Employees
curl "http://localhost:8080/api/employees/search?keyword=john"

# Update Employee
curl -X PUT http://localhost:8080/api/employees/1 \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Jane","lastName":"Doe"}'

# Delete Employee
curl -X DELETE http://localhost:8080/api/employees/1
```

### Using Postman
1. Import the collection (create one for your project)
2. Set base URL: `http://localhost:8080`
3. Test each endpoint with sample data

---

## What's Next - Phase 3 Preview

Phase 3 will add **Spring Security with JWT Authentication**:

1. User and Role entities
2. JWT token generation and validation
3. Login/Register endpoints
4. Role-based access control
   - ADMIN: Full access
   - HR: Manage employees
   - EMPLOYEE: View only
5. Password encryption with BCrypt
6. Secure all employee endpoints

---

## Quick Reference

```java
// Create Pageable
Pageable pageable = PageRequest.of(page, size, Sort.by(sortDir.equals("desc") 
    ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy));

// Entity to DTO
EmployeeResponseDTO dto = EmployeeResponseDTO.fromEntity(employee);

// DTO to Entity
Employee entity = EmployeeResponseDTO.toEntity(requestDTO);

// Throw not found
throw new ResourceNotFoundException("Employee", "id", id);

// Return success response
return ResponseEntity.ok(ApiResponse.success("Message", data));

// Return created response
return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Created", data));
```

---

*Phase 2 completed successfully! Ready for Phase 3 - Security with JWT.*

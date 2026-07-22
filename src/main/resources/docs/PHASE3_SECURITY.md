# Phase 3: Security Implementation

## Overview

Phase 3 implements comprehensive security for the Employee Management System using **Spring Security** and **JWT (JSON Web Token)** authentication with **Role-Based Access Control (RBAC)**.

---

## Table of Contents

1. [Technologies Used](#technologies-used)
2. [Architecture Overview](#architecture-overview)
3. [File Structure](#file-structure)
4. [JWT Authentication Flow](#jwt-authentication-flow)
5. [Role-Based Access Control](#role-based-access-control)
6. [Implementation Details](#implementation-details)
7. [API Endpoints](#api-endpoints)
8. [Configuration](#configuration)
9. [Testing the Security](#testing-the-security)
10. [Interview Questions](#interview-questions)

---

## Technologies Used

| Technology | Purpose |
|------------|---------|
| Spring Security | Authentication and Authorization framework |
| JWT (jjwt) | Token-based stateless authentication |
| BCrypt | Password hashing algorithm |
| Spring Boot 3 | Framework version with Jakarta EE |

### Dependencies Added (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENT                                          │
│                    (Frontend/Postman/Swagger)                               │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ HTTP Request
                                    │ (Authorization: Bearer <JWT>)
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         SPRING SECURITY FILTER CHAIN                         │
│  ┌─────────────────┐  ┌──────────────────────┐  ┌───────────────────────┐  │
│  │   CORS Filter   │→ │   JwtAuthFilter      │→ │ UsernamePassword      │  │
│  │                 │  │ (Extract & Validate) │  │ AuthenticationFilter  │  │
│  └─────────────────┘  └──────────────────────┘  └───────────────────────┘  │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ If JWT Valid
                                    │ Set Authentication in SecurityContext
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                           SECURITY CONTEXT                                   │
│                    (Holds Authentication Object)                            │
│                    - Principal (User)                                       │
│                    - Authorities (ROLE_ADMIN, ROLE_HR, ROLE_EMPLOYEE)       │
└─────────────────────────────────────────────────────────────────────────────┘
                                    │
                                    │ @PreAuthorize Checks
                                    ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CONTROLLER                                      │
│                         (Process Request)                                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## File Structure

```
src/main/java/com/ems/
├── config/
│   ├── SecurityConfig.java          # Main security configuration
│   └── OpenApiConfig.java           # Swagger JWT configuration
├── security/
│   ├── JwtUtil.java                 # JWT token utility (generate, validate)
│   ├── JwtAuthenticationFilter.java # Filter to process JWT on each request
│   └── CustomUserDetailsService.java # Loads user from database
├── entity/
│   ├── User.java                    # User entity (implements UserDetails)
│   └── Role.java                    # Role enum (ADMIN, HR, EMPLOYEE)
├── repository/
│   └── UserRepository.java          # User database operations
├── dto/
│   ├── LoginRequest.java            # Login request DTO
│   ├── RegisterRequest.java         # Registration request DTO
│   └── AuthResponse.java            # Authentication response DTO
├── service/
│   ├── AuthService.java             # Authentication service interface
│   └── impl/
│       └── AuthServiceImpl.java     # Authentication service implementation
└── controller/
    └── AuthController.java          # Authentication endpoints
```

---

## JWT Authentication Flow

### Registration Flow

```
1. Client sends POST /api/auth/register
   {
     "username": "john.doe",
     "email": "john@company.com",
     "password": "password123",
     "firstName": "John",
     "lastName": "Doe",
     "role": "EMPLOYEE"
   }

2. Server validates input
3. Server checks username/email uniqueness
4. Server hashes password with BCrypt
5. Server saves user to database
6. Server generates JWT token
7. Server returns AuthResponse with token
```

### Login Flow

```
1. Client sends POST /api/auth/login
   {
     "usernameOrEmail": "john.doe",
     "password": "password123"
   }

2. AuthenticationManager validates credentials
   - Loads user via UserDetailsService
   - Compares password using BCrypt
3. If valid: Generate JWT token
4. Return AuthResponse with token and user info
```

### Protected Request Flow

```
1. Client sends request with header:
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...

2. JwtAuthenticationFilter intercepts request
3. Extract JWT from Authorization header
4. Extract username from JWT
5. Load user from database
6. Validate JWT (signature, expiration)
7. If valid: Set Authentication in SecurityContext
8. Request proceeds to controller
9. @PreAuthorize checks role permissions
10. Controller processes request
```

---

## Role-Based Access Control

### Role Hierarchy

| Role | Description | Permissions |
|------|-------------|-------------|
| **ADMIN** | System Administrator | Full access - all operations |
| **HR** | Human Resources | Create, Read, Update employees |
| **EMPLOYEE** | Regular Employee | Read own data, limited access |

### Endpoint Permissions

| Endpoint | Method | ADMIN | HR | EMPLOYEE |
|----------|--------|-------|-----|----------|
| `/api/auth/**` | ALL | ✅ | ✅ | ✅ (Public) |
| `/api/employees` | GET | ✅ | ✅ | ✅ |
| `/api/employees` | POST | ✅ | ✅ | ❌ |
| `/api/employees/{id}` | GET | ✅ | ✅ | ✅ |
| `/api/employees/{id}` | PUT | ✅ | ✅ | ❌ |
| `/api/employees/{id}` | DELETE | ✅ | ❌ | ❌ |
| `/api/employees/{id}/deactivate` | PATCH | ✅ | ✅ | ❌ |
| `/api/employees/search` | GET | ✅ | ✅ | ✅ |
| `/api/employees/search/advanced` | GET | ✅ | ✅ | ❌ |
| `/api/employees/status/{status}` | GET | ✅ | ✅ | ❌ |

---

## Implementation Details

### 1. User Entity (User.java)

The User entity implements Spring Security's `UserDetails` interface:

```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String username;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;  // BCrypt hashed
    
    @Enumerated(EnumType.STRING)
    private Role role;
    
    private boolean enabled = true;
    private boolean accountNonLocked = true;
    
    // UserDetails methods implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
    // ... other UserDetails methods
}
```

### 2. JWT Utility (JwtUtil.java)

Key operations:
- **generateToken()**: Creates JWT with username and roles
- **validateToken()**: Verifies signature and expiration
- **extractUsername()**: Gets username from token
- **extractClaims()**: Gets any claim from token

```java
// Token generation
public String generateToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", userDetails.getAuthorities().toString());
    return createToken(claims, userDetails.getUsername());
}

// Token creation with JJWT
private String createToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
            .claims(claims)
            .subject(subject)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(getSigningKey())
            .compact();
}
```

### 3. JWT Authentication Filter (JwtAuthenticationFilter.java)

Extends `OncePerRequestFilter` to process every request:

```java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response,
                                FilterChain filterChain) {
    // 1. Extract JWT from header
    String jwt = extractJwtFromRequest(request);
    
    // 2. Extract username
    String username = jwtUtil.extractUsername(jwt);
    
    // 3. Load user and validate
    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
    
    // 4. If valid, set authentication
    if (jwtUtil.validateToken(jwt, userDetails)) {
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities()
            );
        SecurityContextHolder.getContext().setAuthentication(authToken);
    }
    
    filterChain.doFilter(request, response);
}
```

### 4. Security Configuration (SecurityConfig.java)

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/employees/**").hasAnyRole("ADMIN", "HR")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## API Endpoints

### Authentication Endpoints (Public)

#### Register User
```
POST /api/auth/register
Content-Type: application/json

Request:
{
  "username": "john.doe",
  "email": "john.doe@company.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe",
  "role": "EMPLOYEE"
}

Response (201 Created):
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": 1,
    "username": "john.doe",
    "email": "john.doe@company.com",
    "fullName": "John Doe",
    "role": "EMPLOYEE"
  }
}
```

#### Login
```
POST /api/auth/login
Content-Type: application/json

Request:
{
  "usernameOrEmail": "john.doe",
  "password": "password123"
}

Response (200 OK):
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400000,
    "userId": 1,
    "username": "john.doe",
    "fullName": "John Doe",
    "role": "EMPLOYEE"
  }
}
```

### Using JWT Token

After login, include the token in all subsequent requests:

```
GET /api/employees
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

---

## Configuration

### application.properties

```properties
# JWT Configuration
jwt.secret=dGhpc0lzQVN1cGVyU2VjcmV0S2V5Rm9ySldUVG9rZW5HZW5lcmF0aW9uQW5kVmFsaWRhdGlvbjIwMjQ=
jwt.expiration=86400000
```

### JWT Secret Key

The secret key is Base64 encoded. For production:
1. Use a cryptographically secure random key
2. Store in environment variables
3. Never commit to version control

```bash
# Generate secure key (Linux/Mac)
openssl rand -base64 64
```

---

## Testing the Security

### Using Swagger UI

1. Open http://localhost:8080/swagger-ui.html
2. Call `POST /api/auth/register` or `POST /api/auth/login`
3. Copy the token from response
4. Click "Authorize" button
5. Enter: `Bearer <your-token>`
6. Now all protected endpoints will include the token

### Using Postman

1. **Register/Login** to get token
2. Add header to requests:
   - Key: `Authorization`
   - Value: `Bearer <your-token>`

### Using cURL

```bash
# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"admin123"}'

# Access protected endpoint
curl http://localhost:8080/api/employees \
  -H "Authorization: Bearer <token>"
```

---

## Interview Questions

### Q1: What is JWT and how does it work?
**Answer:** JWT (JSON Web Token) is a compact, URL-safe token format for securely transmitting information. It has three parts:
1. **Header**: Algorithm and token type
2. **Payload**: Claims (data like username, roles, expiration)
3. **Signature**: Verifies the token hasn't been tampered with

### Q2: Why use JWT over Session-based authentication?
**Answer:**
| JWT | Session |
|-----|---------|
| Stateless (no server storage) | Stateful (server stores session) |
| Scalable (works with load balancers) | Requires session replication |
| Self-contained (carries all info) | Requires DB lookup |
| Cross-domain friendly | Cookie-based (same domain) |

### Q3: Is JWT encrypted?
**Answer:** No! JWT is **encoded** (Base64), not encrypted. Anyone can decode and read the payload. The signature only ensures the token hasn't been modified. Never store sensitive data in JWT.

### Q4: What is the difference between Authentication and Authorization?
**Answer:**
- **Authentication**: WHO are you? (Verify identity - login)
- **Authorization**: WHAT can you do? (Verify permissions - roles)

### Q5: Why use BCrypt for password hashing?
**Answer:**
1. **Intentionally slow**: Protects against brute force
2. **Built-in salt**: Protects against rainbow tables
3. **Configurable work factor**: Can be made slower as hardware improves
4. MD5/SHA are too fast, making brute force easier

### Q6: What is CSRF and why did we disable it?
**Answer:** CSRF (Cross-Site Request Forgery) is an attack where malicious sites trick browsers into making requests with stored cookies. We disabled it because:
1. We use JWT in Authorization header, not cookies
2. Attackers can't steal the header like cookies
3. JWT provides authentication proof itself

### Q7: Explain the @PreAuthorize annotation.
**Answer:** `@PreAuthorize` is a method-level security annotation that checks permissions BEFORE the method executes:
```java
@PreAuthorize("hasRole('ADMIN')")  // Only ADMIN can access
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")  // ADMIN or HR
@PreAuthorize("#id == principal.id")  // User can only access own data
```

### Q8: What happens if JWT expires?
**Answer:**
1. JwtUtil.isTokenExpired() returns true
2. validateToken() returns false
3. Authentication is not set in SecurityContext
4. Request is treated as unauthenticated
5. Server returns 401 Unauthorized
6. Client must re-login to get new token

### Q9: Why implement UserDetails interface?
**Answer:** UserDetails is Spring Security's core interface for user information. By implementing it:
1. Spring Security can directly use our User entity
2. No need for adapter class
3. Control over credentials and authorities
4. Works with all Spring Security features

### Q10: How to handle token refresh?
**Answer:** Common approaches:
1. **Short-lived access token + Refresh token**: Access token expires quickly, refresh token lasts longer
2. **Sliding expiration**: Extend token on each valid request
3. **Re-login**: Simple approach - user logs in again

---

## Database Schema

The security module creates the following table:

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    role ENUM('ADMIN', 'HR', 'EMPLOYEE') NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    account_non_locked BOOLEAN DEFAULT TRUE,
    employee_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);

-- Indexes
CREATE INDEX idx_user_username ON users(username);
CREATE INDEX idx_user_email ON users(email);
```

---

## Next Steps (Phase 4)

- Department Module
- Leave Management
- Attendance Tracking
- Audit Fields
- Global Exception Handling enhancement
- DTO Mapping improvements

---

## Summary

Phase 3 implemented a complete security layer with:

✅ Spring Security integration  
✅ JWT token authentication  
✅ Role-based access control (ADMIN, HR, EMPLOYEE)  
✅ User registration and login  
✅ Password encryption with BCrypt  
✅ Method-level security with @PreAuthorize  
✅ Swagger JWT authentication support  
✅ Stateless session management  
✅ CORS configuration

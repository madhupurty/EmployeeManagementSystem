# Phase 4: Advanced Features Implementation

## Overview

Phase 4 implements advanced features for the Employee Management System including **Department Module**, **Leave Management**, **Attendance Tracking**, **Audit Fields**, and enhanced **Global Exception Handling**.

---

## Table of Contents

1. [Features Implemented](#features-implemented)
2. [File Structure](#file-structure)
3. [Department Module](#department-module)
4. [Leave Management](#leave-management)
5. [Attendance Tracking](#attendance-tracking)
6. [Audit Fields (BaseEntity)](#audit-fields-baseentity)
7. [Enhanced Exception Handling](#enhanced-exception-handling)
8. [API Endpoints Summary](#api-endpoints-summary)
9. [Database Schema](#database-schema)
10. [Interview Questions](#interview-questions)

---

## Features Implemented

| Feature | Description |
|---------|-------------|
| **Department Module** | Full CRUD for departments with manager assignment |
| **Leave Management** | Leave request, approval workflow, statistics |
| **Attendance Tracking** | Check-in/out, working hours, overtime tracking |
| **Audit Fields** | createdAt, updatedAt, createdBy, updatedBy |
| **Enhanced Exceptions** | Security exceptions, JWT exceptions |

---

## File Structure

```
src/main/java/com/ems/
├── config/
│   └── JpaAuditConfig.java           # JPA Auditing configuration
├── entity/
│   ├── BaseEntity.java               # Audit fields base class
│   ├── Department.java               # Department entity
│   ├── LeaveRequest.java             # Leave request entity
│   ├── LeaveType.java                # Leave type enum
│   ├── LeaveStatus.java              # Leave status enum
│   ├── Attendance.java               # Attendance entity
│   └── AttendanceStatus.java         # Attendance status enum
├── repository/
│   ├── DepartmentRepository.java     # Department data access
│   ├── LeaveRequestRepository.java   # Leave data access
│   └── AttendanceRepository.java     # Attendance data access
├── service/
│   ├── DepartmentService.java        # Department service interface
│   ├── LeaveService.java             # Leave service interface
│   ├── AttendanceService.java        # Attendance service interface
│   └── impl/
│       ├── DepartmentServiceImpl.java
│       ├── LeaveServiceImpl.java
│       └── AttendanceServiceImpl.java
├── controller/
│   ├── DepartmentController.java     # Department REST endpoints
│   ├── LeaveController.java          # Leave REST endpoints
│   └── AttendanceController.java     # Attendance REST endpoints
├── dto/
│   ├── DepartmentRequestDTO.java
│   ├── DepartmentResponseDTO.java
│   ├── LeaveRequestDTO.java
│   ├── LeaveResponseDTO.java
│   ├── LeaveActionDTO.java
│   ├── AttendanceRequestDTO.java
│   └── AttendanceResponseDTO.java
└── exception/
    └── GlobalExceptionHandler.java   # Enhanced with security exceptions
```

---

## Department Module

### Entity: Department

```java
@Entity
@Table(name = "departments")
public class Department extends BaseEntity {
    private Long id;
    private String code;          // Unique code (e.g., "IT", "HR")
    private String name;          // Full name
    private String description;
    private Employee manager;     // Department head
    private List<Employee> employees;
    private String location;
    private String contactEmail;
    private String contactPhone;
    private Double budget;
    private Boolean active;
}
```

### Department API Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/departments` | Create department | ADMIN, HR |
| PUT | `/api/departments/{id}` | Update department | ADMIN, HR |
| GET | `/api/departments/{id}` | Get by ID | ALL |
| GET | `/api/departments/code/{code}` | Get by code | ALL |
| GET | `/api/departments` | Get all (paginated) | ALL |
| GET | `/api/departments/active` | Get active departments | ALL |
| GET | `/api/departments/list` | Get all as list | ALL |
| GET | `/api/departments/search` | Search departments | ALL |
| DELETE | `/api/departments/{id}` | Delete department | ADMIN |
| PATCH | `/api/departments/{id}/deactivate` | Deactivate | ADMIN, HR |
| PATCH | `/api/departments/{id}/activate` | Activate | ADMIN, HR |
| PATCH | `/api/departments/{id}/manager/{empId}` | Assign manager | ADMIN, HR |
| DELETE | `/api/departments/{id}/manager` | Remove manager | ADMIN, HR |

### Example: Create Department

```json
POST /api/departments
Authorization: Bearer <token>

{
  "code": "IT",
  "name": "Information Technology",
  "description": "IT Department handles all tech operations",
  "managerId": 1,
  "location": "Building A, Floor 3",
  "contactEmail": "it@company.com",
  "contactPhone": "+1-555-0100",
  "budget": 500000.00,
  "active": true
}
```

---

## Leave Management

### Enums

**LeaveType:**
- `ANNUAL` - Annual/Vacation leave
- `SICK` - Sick leave
- `CASUAL` - Casual leave
- `MATERNITY` - Maternity leave
- `PATERNITY` - Paternity leave
- `BEREAVEMENT` - Bereavement leave
- `UNPAID` - Unpaid leave
- `COMP_OFF` - Compensatory off
- `WORK_FROM_HOME` - Work from home

**LeaveStatus:**
- `PENDING` - Awaiting approval
- `APPROVED` - Approved
- `REJECTED` - Rejected
- `CANCELLED` - Cancelled by employee

### Leave Request Entity

```java
@Entity
@Table(name = "leave_requests")
public class LeaveRequest extends BaseEntity {
    private Long id;
    private Employee employee;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private LeaveStatus status;
    private Employee approvedBy;
    private LocalDate actionDate;
    private String approverComments;
    private Boolean isHalfDay;
    private String halfDaySession;
}
```

### Leave Workflow

```
1. Employee creates leave request
   Status: PENDING

2. Manager/HR reviews request
   ↓
   ├── APPROVE → Status: APPROVED
   ├── REJECT → Status: REJECTED
   └── Employee can CANCEL if still PENDING → Status: CANCELLED
```

### Leave API Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/leaves` | Create leave request | ALL |
| PUT | `/api/leaves/{id}` | Update (pending only) | ALL |
| GET | `/api/leaves/{id}` | Get by ID | ALL |
| GET | `/api/leaves` | Get all (paginated) | ADMIN, HR |
| GET | `/api/leaves/employee/{id}` | Get by employee | ALL |
| GET | `/api/leaves/status/{status}` | Get by status | ADMIN, HR |
| GET | `/api/leaves/pending` | Get pending requests | ADMIN, HR |
| GET | `/api/leaves/date-range` | Get by date range | ADMIN, HR |
| DELETE | `/api/leaves/{id}` | Delete (pending only) | ADMIN, HR |
| PATCH | `/api/leaves/{id}/approve` | Approve request | ADMIN, HR |
| PATCH | `/api/leaves/{id}/reject` | Reject request | ADMIN, HR |
| PATCH | `/api/leaves/{id}/cancel` | Cancel request | ALL |
| GET | `/api/leaves/employee/{id}/used-days` | Get used days | ALL |
| GET | `/api/leaves/pending/count` | Get pending count | ADMIN, HR |

### Example: Apply for Leave

```json
POST /api/leaves
Authorization: Bearer <token>

{
  "employeeId": 1,
  "leaveType": "ANNUAL",
  "startDate": "2026-08-01",
  "endDate": "2026-08-05",
  "reason": "Family vacation",
  "isHalfDay": false
}
```

### Example: Approve Leave

```json
PATCH /api/leaves/1/approve
Authorization: Bearer <token>

{
  "comments": "Approved. Have a great vacation!"
}
```

---

## Attendance Tracking

### AttendanceStatus Enum

- `PRESENT` - Employee was present
- `ABSENT` - Employee was absent (unplanned)
- `ON_LEAVE` - Employee on approved leave
- `HALF_DAY` - Half day
- `WEEKEND` - Weekend/Non-working day
- `HOLIDAY` - Public holiday
- `WORK_FROM_HOME` - Working from home

### Attendance Entity

```java
@Entity
@Table(name = "attendance")
public class Attendance extends BaseEntity {
    private Long id;
    private Employee employee;
    private LocalDate attendanceDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private AttendanceStatus status;
    private Integer workingHoursMinutes;
    private Integer overtimeMinutes;
    private String remarks;
    private String checkInIp;
    private String checkOutIp;
    private String checkInLocation;
    private String checkOutLocation;
    private Boolean isLate;
    private Boolean isEarlyCheckout;
}
```

### Features

1. **Check-in/Check-out**: Employees can check in and out
2. **Working Hours**: Automatically calculated from check times
3. **Overtime**: Tracked when working > 8 hours
4. **Late Detection**: Flagged when check-in after 9:00 AM
5. **Early Checkout**: Flagged when checkout before 6:00 PM
6. **IP Tracking**: Records IP address of check-in/out
7. **Location**: Optional location tracking

### Attendance API Endpoints

| Method | Endpoint | Description | Access |
|--------|----------|-------------|--------|
| POST | `/api/attendance` | Record attendance | ADMIN, HR |
| PUT | `/api/attendance/{id}` | Update attendance | ADMIN, HR |
| POST | `/api/attendance/check-in/{empId}` | Check in | ALL |
| POST | `/api/attendance/check-out/{empId}` | Check out | ALL |
| GET | `/api/attendance/{id}` | Get by ID | ALL |
| GET | `/api/attendance/employee/{id}/date/{date}` | Get by emp & date | ALL |
| GET | `/api/attendance` | Get all (paginated) | ADMIN, HR |
| GET | `/api/attendance/employee/{id}` | Get by employee | ALL |
| GET | `/api/attendance/date/{date}` | Get by date | ADMIN, HR |
| GET | `/api/attendance/date-range` | Get by date range | ADMIN, HR |
| DELETE | `/api/attendance/{id}` | Delete | ADMIN |
| GET | `/api/attendance/stats/present-count` | Present count | ADMIN, HR |
| GET | `/api/attendance/stats/absent-count` | Absent count | ADMIN, HR |
| GET | `/api/attendance/stats/employee/{id}/working-hours` | Working hours | ALL |
| GET | `/api/attendance/stats/employee/{id}/overtime` | Overtime | ALL |
| GET | `/api/attendance/stats/employee/{id}/late-count` | Late count | ALL |

### Example: Employee Check-in

```json
POST /api/attendance/check-in/1
Authorization: Bearer <token>

Response:
{
  "success": true,
  "message": "Check-in successful",
  "data": {
    "id": 1,
    "employeeId": 1,
    "employeeName": "John Doe",
    "attendanceDate": "2026-07-22",
    "checkInTime": "09:15:00",
    "status": "PRESENT",
    "isLate": true
  }
}
```

---

## Audit Fields (BaseEntity)

### BaseEntity Class

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}
```

### JPA Audit Configuration

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditConfig {

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
            
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.of("SYSTEM");
            }
            return Optional.of(auth.getName());
        };
    }
}
```

### How It Works

1. Entity extends `BaseEntity`
2. When entity is saved (INSERT):
   - `createdAt` = current timestamp
   - `createdBy` = current authenticated user
3. When entity is updated:
   - `updatedAt` = current timestamp
   - `updatedBy` = current authenticated user

---

## Enhanced Exception Handling

### New Exception Handlers Added

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `AccessDeniedException` | 403 | User lacks permission |
| `AuthenticationException` | 401 | Authentication failed |
| `BadCredentialsException` | 401 | Invalid login credentials |
| `ExpiredJwtException` | 401 | JWT token expired |
| `MalformedJwtException` | 401 | Invalid JWT format |
| `SignatureException` | 401 | JWT signature invalid |

### Example Error Response

```json
{
  "timestamp": "2026-07-22T10:30:00",
  "message": "Access Denied: You don't have permission to access this resource",
  "details": "uri=/api/employees/1"
}
```

---

## Database Schema

### Departments Table

```sql
CREATE TABLE departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    manager_id BIGINT,
    location VARCHAR(200),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    budget DOUBLE,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    FOREIGN KEY (manager_id) REFERENCES employees(id)
);
```

### Leave Requests Table

```sql
CREATE TABLE leave_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    leave_type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_days INT NOT NULL,
    reason VARCHAR(500),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    approved_by_id BIGINT,
    action_date DATE,
    approver_comments VARCHAR(500),
    is_half_day BOOLEAN DEFAULT FALSE,
    half_day_session VARCHAR(20),
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    FOREIGN KEY (employee_id) REFERENCES employees(id),
    FOREIGN KEY (approved_by_id) REFERENCES employees(id)
);
```

### Attendance Table

```sql
CREATE TABLE attendance (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    status VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
    working_hours_minutes INT,
    overtime_minutes INT DEFAULT 0,
    remarks VARCHAR(500),
    check_in_ip VARCHAR(50),
    check_out_ip VARCHAR(50),
    check_in_location VARCHAR(200),
    check_out_location VARCHAR(200),
    is_late BOOLEAN DEFAULT FALSE,
    is_early_checkout BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    UNIQUE KEY uk_attendance_employee_date (employee_id, attendance_date),
    FOREIGN KEY (employee_id) REFERENCES employees(id)
);
```

---

## Interview Questions

### Q1: What is @MappedSuperclass?
**Answer:** `@MappedSuperclass` marks a class whose fields should be inherited by child entities, but the class itself is NOT an entity. No table is created for this class - its fields are added to child entity tables.

### Q2: How does JPA Auditing work?
**Answer:** 
1. `@EnableJpaAuditing` enables auditing
2. `AuditingEntityListener` listens for entity lifecycle events
3. `AuditorAware` bean provides the current user
4. Before save: `@CreatedDate`, `@CreatedBy` are set
5. On update: `@LastModifiedDate`, `@LastModifiedBy` are updated

### Q3: Why use enums with @Enumerated(EnumType.STRING)?
**Answer:** 
- `EnumType.STRING`: Stores enum name ("PENDING", "APPROVED")
- `EnumType.ORDINAL`: Stores position (0, 1, 2) - AVOID!
- STRING is safer because adding/reordering enum values won't corrupt data

### Q4: What's the difference between @OneToMany and @ManyToOne?
**Answer:**
- `@ManyToOne`: Many entities reference one entity (Employee → Department)
- `@OneToMany`: One entity has many related entities (Department → Employees)
- The "Many" side owns the relationship (has the foreign key)

### Q5: Why use Optional in repository methods?
**Answer:**
- Clearly indicates that result may not exist
- Forces handling of null case (no NullPointerException)
- Provides functional methods like `orElseThrow()`, `map()`, `ifPresent()`

### Q6: How to handle overlapping leave requests?
**Answer:** Query checks for date overlap:
```sql
SELECT * FROM leave_requests 
WHERE employee_id = ? 
AND status NOT IN ('REJECTED', 'CANCELLED')
AND start_date <= ? AND end_date >= ?
```
If any results, overlap exists.

### Q7: Why track IP address in attendance?
**Answer:**
- Security: Detect if someone else is checking in for employee
- Audit: Know from where the action was performed
- Location verification: Can compare with office IP ranges

---

## Summary

Phase 4 implemented:

✅ **Department Module** - Full CRUD with manager assignment  
✅ **Leave Management** - Request, approval workflow, statistics  
✅ **Attendance Tracking** - Check-in/out, working hours, overtime  
✅ **Audit Fields** - BaseEntity with automatic timestamps and user tracking  
✅ **Enhanced Exception Handling** - Security exceptions added  
✅ **DTO Mapping** - Clean separation between entities and API  
✅ **Role-based Access** - Proper security on all endpoints

---

## Next Steps (Phase 5)

- JUnit 5 Testing
- Mockito for Unit Tests
- Integration Testing
- Test Coverage Reports

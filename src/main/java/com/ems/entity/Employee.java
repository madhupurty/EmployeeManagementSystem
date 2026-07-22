package com.ems.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * EMPLOYEE ENTITY - JPA Entity Class
 * =============================================================================
 * 
 * WHAT IS AN ENTITY?
 * ------------------
 * An Entity is a Java class that maps to a database table. Each instance of
 * this class represents a row in the 'employees' table.
 * 
 * WHY USE JPA ENTITIES?
 * ---------------------
 * 1. Object-Relational Mapping (ORM) - Maps Java objects to database tables
 * 2. No need to write SQL for basic CRUD operations
 * 3. Database-independent code (can switch MySQL to PostgreSQL easily)
 * 4. Automatic schema generation from Java classes
 * 
 * INTERVIEW QUESTION: What is the difference between JPA and Hibernate?
 * ANSWER: JPA (Java Persistence API) is a SPECIFICATION - it defines interfaces
 *         and annotations for ORM. Hibernate is an IMPLEMENTATION of JPA.
 *         Think of JPA as the interface and Hibernate as the class implementing it.
 * 
 * INTERVIEW QUESTION: Why not use @Data annotation on JPA entities?
 * ANSWER: @Data generates equals() and hashCode() using ALL fields. This causes
 *         problems with:
 *         1. Lazy-loaded associations (triggers unnecessary DB queries)
 *         2. Bidirectional relationships (infinite loops)
 *         3. Proxy objects in Hibernate
 *         Best practice: Use @Getter and @Setter separately.
 * 
 * =============================================================================
 */

/*
 * @Entity - Marks this class as a JPA entity (maps to a database table)
 * 
 * WHAT HAPPENS INTERNALLY:
 * 1. Hibernate scans for @Entity classes during startup
 * 2. Creates a mapping between this class and a database table
 * 3. Each field becomes a column (unless marked @Transient)
 * 4. Generates SQL queries based on this mapping
 */
@Entity

/*
 * @Table - Specifies the table name and constraints
 * 
 * name = "employees" - The table will be named 'employees' in database
 * 
 * uniqueConstraints - Defines unique constraints at table level
 *   - email must be unique (no two employees with same email)
 *   - employee_code must be unique (like EMP001, EMP002)
 * 
 * indexes - Creates database indexes for faster queries
 *   - Index on email for quick lookups
 *   - Index on department for filtering queries
 *   - Index on status for active/inactive filters
 * 
 * INTERVIEW QUESTION: Difference between unique constraint and index?
 * ANSWER: Unique constraint ENFORCES data integrity (no duplicates).
 *         Index IMPROVES query performance (faster searches).
 *         A unique constraint automatically creates an index.
 */
@Table(
    name = "employees",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_employee_email"),
        @UniqueConstraint(columnNames = "employee_code", name = "uk_employee_code")
    },
    indexes = {
        @Index(columnList = "email", name = "idx_employee_email"),
        @Index(columnList = "department", name = "idx_employee_department"),
        @Index(columnList = "status", name = "idx_employee_status")
    }
)

/*
 * LOMBOK ANNOTATIONS:
 * 
 * @Getter - Generates getter methods for all fields
 * @Setter - Generates setter methods for all fields
 * 
 * @NoArgsConstructor - Generates no-argument constructor
 *   REQUIRED by JPA/Hibernate! Hibernate uses reflection to create instances.
 * 
 * @AllArgsConstructor - Generates constructor with all fields as parameters
 *   Useful for creating test data or manual object creation.
 * 
 * @Builder - Implements Builder design pattern
 *   Allows fluent object creation:
 *   Employee emp = Employee.builder()
 *                          .firstName("John")
 *                          .lastName("Doe")
 *                          .email("john@example.com")
 *                          .build();
 * 
 * INTERVIEW QUESTION: Why use Builder pattern?
 * ANSWER: 
 *   1. Cleaner code when creating objects with many fields
 *   2. Immutability support (can omit setters if needed)
 *   3. Avoids telescoping constructor anti-pattern
 *   4. Self-documenting code (field names visible at call site)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    /*
     * @Id - Marks this field as the PRIMARY KEY
     * 
     * @GeneratedValue - Specifies how the primary key is generated
     * 
     * GenerationType.IDENTITY - Uses database AUTO_INCREMENT
     *   MySQL will automatically assign: 1, 2, 3, 4, ...
     * 
     * OTHER OPTIONS:
     * - GenerationType.AUTO - Let Hibernate decide (default)
     * - GenerationType.SEQUENCE - Uses database sequence (Oracle, PostgreSQL)
     * - GenerationType.TABLE - Uses a separate table for ID generation
     * - GenerationType.UUID - Generates UUID (for String primary keys)
     * 
     * INTERVIEW QUESTION: When to use IDENTITY vs SEQUENCE?
     * ANSWER: 
     *   - IDENTITY: Best for MySQL, SQL Server (uses AUTO_INCREMENT)
     *   - SEQUENCE: Best for Oracle, PostgreSQL (batch inserts are faster)
     *   - IDENTITY doesn't support batch inserts well because ID is generated
     *     after INSERT, not before.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Column - Customizes the column mapping
     * 
     * name = "employee_code" - Column name in database (snake_case convention)
     * nullable = false - NOT NULL constraint (required field)
     * unique = true - UNIQUE constraint (no duplicates)
     * length = 20 - VARCHAR(20) in database
     * 
     * NAMING CONVENTION:
     * - Java: camelCase (employeeCode)
     * - Database: snake_case (employee_code)
     * 
     * Spring's default naming strategy converts camelCase to snake_case
     * automatically, but explicit @Column(name=...) is clearer.
     */
    @Column(name = "employee_code", nullable = false, unique = true, length = 20)
    private String employeeCode;

    /*
     * Basic string fields with validation constraints
     * 
     * length = 50 - VARCHAR(50) - Limits storage space
     * nullable = false - Required field
     * 
     * NOTE: Database constraints (nullable) are different from
     * validation annotations (@NotBlank). Both should be used:
     * - Database constraint: Last line of defense
     * - Validation annotation: Catches errors early with nice messages
     */
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /*
     * Email field - crucial for employee communication
     * 
     * unique = true - No two employees can have same email
     * length = 100 - Emails can be long (user@subdomain.company.com)
     */
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    /*
     * Phone number stored as String (not number)
     * 
     * WHY STRING?
     * 1. Phone numbers can have leading zeros (0123456789)
     * 2. International format includes + symbol (+91 9876543210)
     * 3. May include dashes or spaces for readability
     * 4. No mathematical operations needed on phone numbers
     */
    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    /*
     * Department name - kept for backward compatibility and quick access.
     * The actual relationship is managed by departmentEntity field.
     */
    @Column(name = "department", length = 50)
    private String department;

    /*
     * Department Entity Relationship (Phase 4)
     * 
     * @ManyToOne: Many employees belong to one department
     * @JoinColumn: Creates foreign key 'department_id' in employees table
     * 
     * FetchType.LAZY: Don't load department until explicitly accessed
     *                 (Better performance - avoids unnecessary queries)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", referencedColumnName = "id")
    private Department departmentEntity;

    /*
     * Designation/Job Title
     * 
     * Examples: "Software Engineer", "Senior Developer", "Team Lead"
     */
    @Column(name = "designation", length = 100)
    private String designation;

    /*
     * @Column for decimal values (salary)
     * 
     * precision = 10 - Total number of digits (including decimals)
     * scale = 2 - Number of decimal places
     * 
     * Example: 12345678.90 (8 digits before decimal, 2 after)
     * 
     * WHY BigDecimal instead of Double?
     * 1. Double has floating-point precision issues (0.1 + 0.2 = 0.30000000004)
     * 2. BigDecimal provides exact decimal arithmetic
     * 3. CRITICAL for financial calculations (salary, tax, bonuses)
     * 
     * INTERVIEW QUESTION: Why use BigDecimal for salary?
     * ANSWER: Double/Float use binary floating-point which can't represent
     *         some decimal fractions exactly. For money, we need EXACT values.
     *         BigDecimal stores numbers as decimal, avoiding rounding errors.
     */
    @Column(name = "salary", precision = 10, scale = 2)
    private BigDecimal salary;

    /*
     * Date of birth - using LocalDate (not Date)
     * 
     * WHY LocalDate (Java 8+) instead of java.util.Date?
     * 1. Immutable (thread-safe)
     * 2. No time component (just date, which is what we need)
     * 3. Better API (plusDays(), minusMonths(), etc.)
     * 4. No timezone issues
     * 
     * java.util.Date is LEGACY and should be avoided in new code.
     */
    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    /*
     * Date of joining the company
     */
    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    /*
     * Address fields - could be a separate embedded object
     * For simplicity, keeping as separate fields
     * In a complex system, consider @Embedded Address class
     */
    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "city", length = 50)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    /*
     * @Enumerated - Maps Java enum to database
     * 
     * EnumType.STRING - Stores enum name as VARCHAR ("ACTIVE", "INACTIVE")
     * EnumType.ORDINAL - Stores enum position as INTEGER (0, 1, 2) - AVOID!
     * 
     * WHY STRING over ORDINAL?
     * If you add a new enum value in the middle, ORDINAL values shift!
     * Example: Adding "SUSPENDED" between ACTIVE(0) and INACTIVE(1)
     *          would corrupt existing data.
     * 
     * STRING is human-readable in database and safe to reorder.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default  // Sets default value when using builder
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    /*
     * Manager's employee ID - Self-referential relationship
     * 
     * For Phase 2, storing as simple Long.
     * In advanced implementation, this could be @ManyToOne self-reference:
     *   @ManyToOne
     *   @JoinColumn(name = "manager_id")
     *   private Employee manager;
     */
    @Column(name = "manager_id")
    private Long managerId;

    // ==========================================================================
    // AUDIT FIELDS - Automatically populated timestamps
    // ==========================================================================

    /*
     * @CreationTimestamp - Automatically sets timestamp when entity is CREATED
     * 
     * WHAT HAPPENS INTERNALLY:
     * 1. Hibernate intercepts the INSERT operation
     * 2. Before executing INSERT, sets this field to current timestamp
     * 3. Uses database timestamp or JVM timestamp based on configuration
     * 
     * updatable = false - This column should NOT change after creation
     * 
     * ALTERNATIVE: JPA's @PrePersist callback
     *   @PrePersist
     *   public void onCreate() {
     *       createdAt = LocalDateTime.now();
     *   }
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /*
     * @UpdateTimestamp - Automatically updates timestamp on every UPDATE
     * 
     * Every time Hibernate executes an UPDATE on this entity,
     * this field is automatically set to the current timestamp.
     * 
     * USEFUL FOR:
     * 1. Tracking when records were last modified
     * 2. Implementing optimistic locking with version
     * 3. Synchronization and caching strategies
     * 4. Audit trails and debugging
     * 
     * ALTERNATIVE: JPA's @PreUpdate callback
     *   @PreUpdate
     *   public void onUpdate() {
     *       updatedAt = LocalDateTime.now();
     *   }
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /*
     * Created by - stores the user who created this record
     * Will be populated by Spring Security in Phase 3
     * For now, can be set manually or left null
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /*
     * Updated by - stores the user who last modified this record
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    // ==========================================================================
    // NESTED ENUM - Employee Status
    // ==========================================================================

    /*
     * Enum defined inside the Entity class
     * 
     * WHY NESTED ENUM?
     * 1. Tightly coupled to Employee (status only makes sense for employees)
     * 2. Encapsulation (keeps related code together)
     * 3. Namespace clarity (Employee.EmployeeStatus vs standalone Status)
     * 
     * INTERVIEW QUESTION: How does @Enumerated work?
     * ANSWER: @Enumerated maps Java enums to database columns.
     *         EnumType.STRING stores the enum name ("ACTIVE").
     *         EnumType.ORDINAL stores the position (0, 1, 2) - NOT recommended.
     */
    public enum EmployeeStatus {
        ACTIVE,      // Currently employed
        INACTIVE,    // Left the company
        ON_LEAVE,    // On leave of absence
        TERMINATED,  // Employment terminated
        PROBATION    // In probation period
    }

    // ==========================================================================
    // CUSTOM METHODS - Business logic in entity (if minimal)
    // ==========================================================================

    /*
     * Returns full name of employee
     * 
     * Transient method - not persisted to database
     * Just a convenience method for display purposes
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /*
     * Checks if employee is currently active
     */
    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE;
    }
}

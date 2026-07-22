package com.ems.repository;

import com.ems.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * EMPLOYEE REPOSITORY - Data Access Layer
 * =============================================================================
 * 
 * WHAT IS A REPOSITORY?
 * ---------------------
 * Repository is a design pattern that abstracts data access logic.
 * It provides a collection-like interface for accessing domain objects.
 * 
 * SPRING DATA JPA MAGIC:
 * ----------------------
 * We DON'T write implementation for this interface!
 * Spring Data JPA generates the implementation at runtime based on:
 * 1. Method names (findByEmail → SELECT * FROM employees WHERE email = ?)
 * 2. @Query annotations (custom JPQL/SQL queries)
 * 3. Inherited methods from JpaRepository
 * 
 * INTERVIEW QUESTION: How does Spring Data JPA generate repository implementation?
 * ANSWER: Spring creates a proxy at runtime that:
 *   1. Parses method names to create queries (findByFirstName → WHERE first_name = ?)
 *   2. Uses @Query annotations for custom queries
 *   3. Implements JpaRepository methods (save, findById, delete, etc.)
 *   4. Handles transactions automatically
 * 
 * =============================================================================
 * INTERFACE HIERARCHY
 * =============================================================================
 * 
 * JpaRepository<Employee, Long> extends:
 *   └── PagingAndSortingRepository<Employee, Long>
 *       └── CrudRepository<Employee, Long>
 *           └── Repository<Employee, Long>
 * 
 * WHAT EACH PROVIDES:
 * - Repository: Marker interface (no methods)
 * - CrudRepository: save(), findById(), delete(), count(), existsById()
 * - PagingAndSortingRepository: findAll(Pageable), findAll(Sort)
 * - JpaRepository: flush(), saveAndFlush(), deleteInBatch(), getOne()
 * 
 * JpaSpecificationExecutor: Enables dynamic queries with Specifications
 * 
 * =============================================================================
 */

/*
 * @Repository - Marks this as a Spring-managed repository bean
 * 
 * WHAT IT DOES:
 * 1. Component scanning picks it up (@ComponentScan in main class)
 * 2. Exception translation - converts JPA exceptions to Spring's DataAccessException
 * 3. Enables dependency injection
 * 
 * NOTE: @Repository is OPTIONAL for interfaces extending JpaRepository
 * Spring Data JPA auto-detects them. We include it for clarity.
 * 
 * INTERVIEW QUESTION: What is the difference between @Component, @Service, @Repository?
 * ANSWER: All are specializations of @Component (all create Spring beans).
 *   - @Component: Generic component
 *   - @Service: Business logic layer (no special behavior, just semantic)
 *   - @Repository: Data access layer + exception translation to DataAccessException
 *   - @Controller/@RestController: Web layer
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    // ==========================================================================
    // DERIVED QUERY METHODS - Spring generates queries from method names
    // ==========================================================================

    /*
     * Find employee by email
     * 
     * METHOD NAME PARSING:
     * findByEmail → SELECT * FROM employees WHERE email = ?
     * 
     * Returns Optional<Employee> because email should be unique
     * Optional prevents NullPointerException if employee not found
     * 
     * INTERVIEW QUESTION: Why return Optional instead of null?
     * ANSWER: Optional forces callers to handle the "not found" case explicitly.
     *         Prevents NullPointerException and makes code more readable.
     *         Example: employee.orElseThrow(() -> new ResourceNotFoundException(...))
     */
    Optional<Employee> findByEmail(String email);

    /*
     * Find employee by employee code
     * 
     * EmployeeCode is a unique business identifier (e.g., EMP001)
     * Different from database ID
     */
    Optional<Employee> findByEmployeeCode(String employeeCode);

    /*
     * Check if email exists
     * 
     * existsBy... → Returns boolean
     * Used to check for duplicates before insert
     * More efficient than findByEmail (doesn't load entire entity)
     */
    boolean existsByEmail(String email);

    /*
     * Check if employee code exists
     */
    boolean existsByEmployeeCode(String employeeCode);

    /*
     * Check if email exists for a different employee (used in update)
     * 
     * existsByEmailAndIdNot → WHERE email = ? AND id != ?
     * 
     * WHY NEEDED?
     * When updating employee, we need to check if NEW email is unique
     * But we should exclude the current employee from the check
     * 
     * Example: Employee 1 has email "john@example.com"
     *          Updating Employee 1 with same email should be allowed
     *          But updating Employee 2 to use "john@example.com" should fail
     */
    boolean existsByEmailAndIdNot(String email, Long id);

    // ==========================================================================
    // FINDER METHODS - Return collections
    // ==========================================================================

    /*
     * Find all employees in a department
     * 
     * Returns List (multiple employees can be in same department)
     */
    List<Employee> findByDepartment(String department);

    /*
     * Find employees in department with pagination
     * 
     * Pageable parameter enables:
     * - Page number
     * - Page size
     * - Sorting
     * 
     * Returns Page<Employee> with metadata
     */
    Page<Employee> findByDepartment(String department, Pageable pageable);

    /*
     * Find all employees by status
     */
    List<Employee> findByStatus(Employee.EmployeeStatus status);

    /*
     * Find employees by status with pagination
     */
    Page<Employee> findByStatus(Employee.EmployeeStatus status, Pageable pageable);

    /*
     * Find employees by manager
     * 
     * All direct reports of a manager
     */
    List<Employee> findByManagerId(Long managerId);

    /*
     * Find employees by designation
     */
    List<Employee> findByDesignation(String designation);

    // ==========================================================================
    // COMPLEX DERIVED QUERIES - Combining conditions
    // ==========================================================================

    /*
     * Find by department AND status
     * 
     * And → WHERE department = ? AND status = ?
     * 
     * Other keywords:
     * - Or → WHERE x = ? OR y = ?
     * - Not → WHERE x != ?
     * - Like → WHERE x LIKE ?
     * - OrderBy → ORDER BY x ASC/DESC
     */
    List<Employee> findByDepartmentAndStatus(String department, Employee.EmployeeStatus status);

    /*
     * Find employees with first name starting with...
     * 
     * StartingWith → WHERE first_name LIKE 'value%'
     * EndingWith → WHERE first_name LIKE '%value'
     * Containing → WHERE first_name LIKE '%value%'
     */
    List<Employee> findByFirstNameStartingWithIgnoreCase(String prefix);

    /*
     * Find employees with salary in a range
     * 
     * Between → WHERE salary BETWEEN ? AND ?
     */
    List<Employee> findBySalaryBetween(BigDecimal minSalary, BigDecimal maxSalary);

    /*
     * Find employees earning more than a value
     * 
     * GreaterThan → WHERE salary > ?
     * GreaterThanEqual → WHERE salary >= ?
     * LessThan → WHERE salary < ?
     * LessThanEqual → WHERE salary <= ?
     */
    List<Employee> findBySalaryGreaterThanEqual(BigDecimal salary);

    // ==========================================================================
    // @Query ANNOTATION - Custom JPQL Queries
    // ==========================================================================

    /*
     * @Query - Define custom query using JPQL or native SQL
     * 
     * JPQL (Java Persistence Query Language):
     * - Object-oriented query language (uses entity names, not table names)
     * - Database-independent
     * - Syntax: SELECT e FROM Employee e (not SELECT * FROM employees)
     * 
     * :keyword → Named parameter, bound with @Param
     * 
     * INTERVIEW QUESTION: Difference between JPQL and native SQL?
     * ANSWER:
     *   JPQL: Object-oriented, uses entity/field names, database-independent
     *   Native SQL: Raw SQL, uses table/column names, database-specific
     *   
     *   Use JPQL when possible for portability.
     *   Use native SQL for database-specific features or complex queries.
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.department) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.designation) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Employee> searchEmployees(@Param("keyword") String keyword, Pageable pageable);

    /*
     * Search with multiple criteria
     * 
     * This is a more specific search allowing filtering by department and status
     * while also searching by keyword
     */
    @Query("SELECT e FROM Employee e WHERE " +
           "(LOWER(e.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:department IS NULL OR e.department = :department) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<Employee> searchEmployeesWithFilters(
            @Param("keyword") String keyword,
            @Param("department") String department,
            @Param("status") Employee.EmployeeStatus status,
            Pageable pageable);

    /*
     * Find all active employees ordered by join date (newest first)
     */
    @Query("SELECT e FROM Employee e WHERE e.status = 'ACTIVE' ORDER BY e.dateOfJoining DESC")
    List<Employee> findAllActiveEmployeesOrderByJoinDate();

    /*
     * Count employees by department
     * 
     * Aggregate function in JPQL
     */
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.department = :department")
    long countByDepartment(@Param("department") String department);

    /*
     * Get average salary by department
     */
    @Query("SELECT AVG(e.salary) FROM Employee e WHERE e.department = :department")
    BigDecimal getAverageSalaryByDepartment(@Param("department") String department);

    /*
     * Get all unique departments
     * 
     * DISTINCT keyword in JPQL
     */
    @Query("SELECT DISTINCT e.department FROM Employee e WHERE e.department IS NOT NULL")
    List<String> findAllDepartments();

    /*
     * Get all unique designations
     */
    @Query("SELECT DISTINCT e.designation FROM Employee e WHERE e.designation IS NOT NULL")
    List<String> findAllDesignations();

    // ==========================================================================
    // NATIVE SQL QUERIES
    // ==========================================================================

    /*
     * nativeQuery = true → Use raw SQL instead of JPQL
     * 
     * WHEN TO USE NATIVE QUERIES:
     * 1. Complex queries not expressible in JPQL
     * 2. Database-specific functions (MySQL's IFNULL, PostgreSQL's ARRAY_AGG)
     * 3. Performance optimization with database hints
     * 4. Legacy database compatibility
     * 
     * CAUTION: Native queries are NOT database-portable
     */
    @Query(value = "SELECT * FROM employees WHERE YEAR(date_of_joining) = :year", 
           nativeQuery = true)
    List<Employee> findEmployeesJoinedInYear(@Param("year") int year);

    /*
     * Native query for finding employees with birthday this month
     */
    @Query(value = "SELECT * FROM employees WHERE MONTH(date_of_birth) = MONTH(CURRENT_DATE)", 
           nativeQuery = true)
    List<Employee> findEmployeesWithBirthdayThisMonth();

    // ==========================================================================
    // MODIFYING QUERIES - Update/Delete operations
    // ==========================================================================

    /*
     * @Modifying - Required for UPDATE/DELETE queries
     * 
     * WHY @Modifying?
     * By default, @Query is for SELECT operations.
     * @Modifying tells Spring this query changes data.
     * 
     * clearAutomatically = true → Clears persistence context after update
     * This ensures subsequent queries fetch fresh data from database.
     * 
     * IMPORTANT: Modifying queries must be in a @Transactional method!
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Employee e SET e.status = :status WHERE e.id = :id")
    int updateEmployeeStatus(@Param("id") Long id, @Param("status") Employee.EmployeeStatus status);

    /*
     * Bulk update - set manager for multiple employees
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Employee e SET e.managerId = :managerId WHERE e.department = :department")
    int assignManagerToDepartment(@Param("managerId") Long managerId, 
                                   @Param("department") String department);

    /*
     * Soft delete - update status instead of deleting
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Employee e SET e.status = 'INACTIVE' WHERE e.id = :id")
    int softDeleteEmployee(@Param("id") Long id);

    /*
     * Bulk salary update
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Employee e SET e.salary = e.salary * :percentage WHERE e.department = :department")
    int updateSalaryByDepartment(@Param("department") String department, 
                                  @Param("percentage") BigDecimal percentage);
}

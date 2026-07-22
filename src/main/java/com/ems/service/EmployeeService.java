package com.ems.service;

import com.ems.dto.EmployeeRequestDTO;
import com.ems.dto.EmployeeResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.entity.Employee;

import java.util.List;

/**
 * =============================================================================
 * EMPLOYEE SERVICE INTERFACE - Business Logic Contract
 * =============================================================================
 * 
 * WHY USE INTERFACE + IMPLEMENTATION PATTERN?
 * --------------------------------------------
 * 1. ABSTRACTION: Hides implementation details from controllers
 * 2. LOOSE COUPLING: Controller depends on interface, not implementation
 * 3. TESTABILITY: Easy to mock for unit tests
 * 4. FLEXIBILITY: Can swap implementations without changing callers
 * 5. PROXY SUPPORT: Spring creates proxies for interfaces (needed for @Transactional)
 * 
 * DESIGN PRINCIPLE: "Program to an interface, not an implementation"
 * This is one of the SOLID principles (Dependency Inversion Principle)
 * 
 * INTERVIEW QUESTION: Why create separate interface and implementation?
 * ANSWER: Following Dependency Inversion Principle - high-level modules
 *         (Controller) should depend on abstractions (Service interface),
 *         not concrete implementations (ServiceImpl). This enables:
 *         - Easier testing with mocks
 *         - Multiple implementations (e.g., different DB strategies)
 *         - Better maintainability and loose coupling
 * 
 * =============================================================================
 * SERVICE LAYER RESPONSIBILITIES
 * =============================================================================
 * 
 * 1. BUSINESS LOGIC: Validation rules, calculations, workflows
 * 2. TRANSACTION MANAGEMENT: @Transactional boundaries
 * 3. DTO CONVERSION: Entity <-> DTO transformations
 * 4. ORCHESTRATION: Coordinate multiple repositories if needed
 * 5. EXCEPTION HANDLING: Throw business-meaningful exceptions
 * 
 * WHAT SERVICE SHOULD NOT DO:
 * - HTTP request/response handling (that's Controller's job)
 * - Direct database operations (that's Repository's job)
 * - Security authorization (that's Spring Security's job)
 * 
 * =============================================================================
 */
public interface EmployeeService {

    // ==========================================================================
    // CRUD OPERATIONS
    // ==========================================================================

    /**
     * Create a new employee
     * 
     * @param employeeRequestDTO The employee data to create
     * @return The created employee with generated ID and employee code
     * @throws BadRequestException if email already exists
     */
    EmployeeResponseDTO createEmployee(EmployeeRequestDTO employeeRequestDTO);

    /**
     * Update an existing employee
     * 
     * @param id The ID of the employee to update
     * @param employeeRequestDTO The new employee data
     * @return The updated employee
     * @throws ResourceNotFoundException if employee not found
     * @throws BadRequestException if email already exists for another employee
     */
    EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO employeeRequestDTO);

    /**
     * Get employee by ID
     * 
     * @param id The employee ID
     * @return The employee details
     * @throws ResourceNotFoundException if employee not found
     */
    EmployeeResponseDTO getEmployeeById(Long id);

    /**
     * Get employee by employee code
     * 
     * @param employeeCode The unique employee code (e.g., EMP001)
     * @return The employee details
     * @throws ResourceNotFoundException if employee not found
     */
    EmployeeResponseDTO getEmployeeByCode(String employeeCode);

    /**
     * Delete employee by ID
     * 
     * @param id The employee ID
     * @throws ResourceNotFoundException if employee not found
     */
    void deleteEmployee(Long id);

    /**
     * Soft delete (deactivate) employee
     * Sets status to INACTIVE instead of deleting
     * 
     * @param id The employee ID
     * @return The deactivated employee
     * @throws ResourceNotFoundException if employee not found
     */
    EmployeeResponseDTO deactivateEmployee(Long id);

    // ==========================================================================
    // PAGINATION AND SORTING
    // ==========================================================================

    /**
     * Get all employees with pagination and sorting
     * 
     * @param pageNo Page number (1-indexed)
     * @param pageSize Number of items per page
     * @param sortBy Field to sort by
     * @param sortDir Sort direction (asc or desc)
     * @return Paginated list of employees
     */
    PagedResponseDTO<EmployeeResponseDTO> getAllEmployees(int pageNo, int pageSize, 
                                                           String sortBy, String sortDir);

    /**
     * Get employees by department with pagination
     * 
     * @param department The department name
     * @param pageNo Page number (1-indexed)
     * @param pageSize Number of items per page
     * @param sortBy Field to sort by
     * @param sortDir Sort direction
     * @return Paginated list of employees in the department
     */
    PagedResponseDTO<EmployeeResponseDTO> getEmployeesByDepartment(String department, 
                                                                    int pageNo, int pageSize,
                                                                    String sortBy, String sortDir);

    /**
     * Get employees by status with pagination
     * 
     * @param status The employee status (ACTIVE, INACTIVE, etc.)
     * @param pageNo Page number
     * @param pageSize Page size
     * @param sortBy Sort field
     * @param sortDir Sort direction
     * @return Paginated list of employees with the given status
     */
    PagedResponseDTO<EmployeeResponseDTO> getEmployeesByStatus(String status,
                                                                int pageNo, int pageSize,
                                                                String sortBy, String sortDir);

    // ==========================================================================
    // SEARCH OPERATIONS
    // ==========================================================================

    /**
     * Search employees by keyword (matches firstName, lastName, email, department, designation)
     * 
     * @param keyword The search keyword
     * @param pageNo Page number
     * @param pageSize Page size
     * @param sortBy Sort field
     * @param sortDir Sort direction
     * @return Paginated search results
     */
    PagedResponseDTO<EmployeeResponseDTO> searchEmployees(String keyword,
                                                           int pageNo, int pageSize,
                                                           String sortBy, String sortDir);

    /**
     * Advanced search with multiple filters
     * 
     * @param keyword Search keyword (optional)
     * @param department Department filter (optional)
     * @param status Status filter (optional)
     * @param pageNo Page number
     * @param pageSize Page size
     * @param sortBy Sort field
     * @param sortDir Sort direction
     * @return Paginated filtered results
     */
    PagedResponseDTO<EmployeeResponseDTO> searchEmployeesWithFilters(String keyword,
                                                                      String department,
                                                                      String status,
                                                                      int pageNo, int pageSize,
                                                                      String sortBy, String sortDir);

    // ==========================================================================
    // UTILITY METHODS
    // ==========================================================================

    /**
     * Get all unique departments
     * 
     * @return List of department names
     */
    List<String> getAllDepartments();

    /**
     * Get all unique designations
     * 
     * @return List of designation names
     */
    List<String> getAllDesignations();

    /**
     * Get employees by manager
     * 
     * @param managerId The manager's employee ID
     * @return List of employees reporting to this manager
     */
    List<EmployeeResponseDTO> getEmployeesByManager(Long managerId);

    /**
     * Check if email exists
     * 
     * @param email The email to check
     * @return true if email exists, false otherwise
     */
    boolean isEmailExists(String email);

    /**
     * Check if employee code exists
     * 
     * @param employeeCode The code to check
     * @return true if code exists, false otherwise
     */
    boolean isEmployeeCodeExists(String employeeCode);
}

package com.ems.service.impl;

import com.ems.dto.EmployeeRequestDTO;
import com.ems.dto.EmployeeResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.entity.Employee;
import com.ems.exception.BadRequestException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EmployeeRepository;
import com.ems.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * EMPLOYEE SERVICE IMPLEMENTATION - Business Logic Layer
 * =============================================================================
 * 
 * This class contains all business logic for employee management.
 * It orchestrates between Controller and Repository layers.
 * 
 * =============================================================================
 * KEY ANNOTATIONS EXPLAINED
 * =============================================================================
 * 
 * @Service
 * --------
 * Marks this class as a Spring-managed service component.
 * 
 * WHAT HAPPENS INTERNALLY:
 * 1. Component scanning detects @Service annotation
 * 2. Spring creates a singleton bean of this class
 * 3. Bean is registered in ApplicationContext with name "employeeServiceImpl"
 * 4. Can be injected anywhere using @Autowired or constructor injection
 * 
 * WHY @Service AND NOT @Component?
 * Both create beans, but @Service adds semantic meaning:
 * - @Component: Generic component
 * - @Service: Business logic holder (clearer intent)
 * - Both work identically, but @Service is conventional for business layer
 * 
 * INTERVIEW QUESTION: What is the difference between @Component and @Service?
 * ANSWER: Functionally identical - both create Spring beans. @Service is a
 *         specialization of @Component that indicates the class holds
 *         business logic. It's a convention that improves code readability.
 * 
 * =============================================================================
 * @Transactional EXPLAINED
 * =============================================================================
 * 
 * @Transactional(readOnly = true) at class level:
 * - All methods are read-only by default
 * - Optimizes database operations (no flush, no write locks)
 * - Override with @Transactional for write operations
 * 
 * HOW SPRING TRANSACTIONS WORK:
 * 1. Spring creates a PROXY around the bean
 * 2. When method is called, proxy intercepts
 * 3. Proxy starts transaction (EntityManager.getTransaction().begin())
 * 4. Proxy calls actual method
 * 5. If success: commit transaction
 * 6. If exception: rollback transaction
 * 
 * INTERVIEW QUESTION: How does @Transactional work internally?
 * ANSWER: Spring creates a proxy that wraps the bean. When a @Transactional
 *         method is called, the proxy:
 *         1. Gets connection from pool
 *         2. Starts transaction
 *         3. Executes method
 *         4. Commits on success, rollbacks on RuntimeException
 *         5. Returns connection to pool
 * 
 * IMPORTANT: @Transactional doesn't work for private methods or
 * self-invocation (calling this.method() from within the class)!
 * 
 * =============================================================================
 */
@Service
@Transactional(readOnly = true)  // Default: read-only for query methods
@RequiredArgsConstructor  // Generates constructor for final fields (dependency injection)
public class EmployeeServiceImpl implements EmployeeService {

    /*
     * LOGGER - SLF4J Logger for this class
     * 
     * WHY USE LOGGING?
     * 1. Debugging: Track application flow
     * 2. Monitoring: Watch for errors in production
     * 3. Auditing: Log who did what when
     * 4. Performance: Track slow operations
     * 
     * LOG LEVELS (in order of severity):
     * - TRACE: Very detailed (every method entry/exit)
     * - DEBUG: Debugging info (variable values)
     * - INFO: General information (application events)
     * - WARN: Potential problems (deprecated features used)
     * - ERROR: Errors that need attention
     * 
     * In production, usually set to INFO or WARN
     * In development, can be DEBUG or TRACE
     */
    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    /*
     * DEPENDENCY INJECTION via Constructor
     * 
     * WHY CONSTRUCTOR INJECTION OVER @Autowired FIELD INJECTION?
     * 1. IMMUTABILITY: final fields can't be changed after construction
     * 2. TESTABILITY: Easy to pass mocks in tests
     * 3. REQUIRED DEPENDENCIES: Constructor fails if dependency is missing
     * 4. THREAD SAFETY: final fields are safely published
     * 5. BEST PRACTICE: Recommended by Spring team
     * 
     * @RequiredArgsConstructor generates:
     * public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
     *     this.employeeRepository = employeeRepository;
     * }
     * 
     * INTERVIEW QUESTION: Field vs Constructor injection?
     * ANSWER: Constructor injection is preferred because:
     *   - Ensures dependencies are required (not null)
     *   - Enables immutability (final fields)
     *   - Better for testing (easily pass mocks)
     *   - Spring team recommends it
     */
    private final EmployeeRepository employeeRepository;

    // ==========================================================================
    // CREATE EMPLOYEE
    // ==========================================================================

    /*
     * @Transactional without readOnly = read-write transaction
     * 
     * This method modifies data, so we need write transaction.
     * Override the class-level readOnly = true.
     */
    @Override
    @Transactional  // Write transaction (override class-level readOnly)
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        logger.info("Creating new employee with email: {}", requestDTO.getEmail());

        // Business validation: Check if email already exists
        if (employeeRepository.existsByEmail(requestDTO.getEmail())) {
            logger.warn("Employee creation failed: Email {} already exists", requestDTO.getEmail());
            throw new BadRequestException("Employee with email " + requestDTO.getEmail() + " already exists");
        }

        // Convert DTO to Entity
        Employee employee = EmployeeResponseDTO.toEntity(requestDTO);

        // Generate unique employee code
        String employeeCode = generateEmployeeCode();
        employee.setEmployeeCode(employeeCode);

        // Set default status if not provided
        if (employee.getStatus() == null) {
            employee.setStatus(Employee.EmployeeStatus.ACTIVE);
        }

        // Set default joining date if not provided
        if (employee.getDateOfJoining() == null) {
            employee.setDateOfJoining(LocalDate.now());
        }

        // Save to database
        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("Employee created successfully with ID: {} and code: {}", 
                    savedEmployee.getId(), savedEmployee.getEmployeeCode());

        // Convert Entity to Response DTO and return
        return EmployeeResponseDTO.fromEntity(savedEmployee);
    }

    // ==========================================================================
    // UPDATE EMPLOYEE
    // ==========================================================================

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {
        logger.info("Updating employee with ID: {}", id);

        // Find existing employee or throw exception
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Employee not found with ID: {}", id);
                    return new ResourceNotFoundException("Employee", "id", id);
                });

        // Check if new email conflicts with another employee
        if (requestDTO.getEmail() != null && 
            !requestDTO.getEmail().equals(employee.getEmail()) &&
            employeeRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            logger.warn("Email {} already exists for another employee", requestDTO.getEmail());
            throw new BadRequestException("Email " + requestDTO.getEmail() + " is already in use");
        }

        // Update entity with new values
        EmployeeResponseDTO.updateEntity(employee, requestDTO);

        // Save updated entity
        Employee updatedEmployee = employeeRepository.save(employee);
        logger.info("Employee updated successfully with ID: {}", id);

        return EmployeeResponseDTO.fromEntity(updatedEmployee);
    }

    // ==========================================================================
    // GET EMPLOYEE BY ID
    // ==========================================================================

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        logger.debug("Fetching employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Employee not found with ID: {}", id);
                    return new ResourceNotFoundException("Employee", "id", id);
                });

        return EmployeeResponseDTO.fromEntity(employee);
    }

    // ==========================================================================
    // GET EMPLOYEE BY CODE
    // ==========================================================================

    @Override
    public EmployeeResponseDTO getEmployeeByCode(String employeeCode) {
        logger.debug("Fetching employee with code: {}", employeeCode);

        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> {
                    logger.warn("Employee not found with code: {}", employeeCode);
                    return new ResourceNotFoundException("Employee", "employeeCode", employeeCode);
                });

        return EmployeeResponseDTO.fromEntity(employee);
    }

    // ==========================================================================
    // DELETE EMPLOYEE (Hard Delete)
    // ==========================================================================

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        logger.info("Deleting employee with ID: {}", id);

        // Check if employee exists
        if (!employeeRepository.existsById(id)) {
            logger.warn("Cannot delete: Employee not found with ID: {}", id);
            throw new ResourceNotFoundException("Employee", "id", id);
        }

        employeeRepository.deleteById(id);
        logger.info("Employee deleted successfully with ID: {}", id);
    }

    // ==========================================================================
    // DEACTIVATE EMPLOYEE (Soft Delete)
    // ==========================================================================

    @Override
    @Transactional
    public EmployeeResponseDTO deactivateEmployee(Long id) {
        logger.info("Deactivating employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Cannot deactivate: Employee not found with ID: {}", id);
                    return new ResourceNotFoundException("Employee", "id", id);
                });

        employee.setStatus(Employee.EmployeeStatus.INACTIVE);
        Employee deactivatedEmployee = employeeRepository.save(employee);
        
        logger.info("Employee deactivated successfully with ID: {}", id);
        return EmployeeResponseDTO.fromEntity(deactivatedEmployee);
    }

    // ==========================================================================
    // GET ALL EMPLOYEES (Paginated)
    // ==========================================================================

    /*
     * PAGINATION EXPLAINED
     * --------------------
     * 
     * PageRequest.of(pageNo, pageSize, sort):
     * - pageNo: 0-indexed page number (0 = first page)
     * - pageSize: Number of items per page
     * - sort: Sorting configuration
     * 
     * WHAT'S HAPPENING:
     * 1. Create Pageable object with page info
     * 2. Repository adds LIMIT and OFFSET to SQL query
     * 3. Returns Page<Employee> with content and metadata
     * 4. Convert to our custom PagedResponseDTO
     * 
     * SQL GENERATED (for page 1, size 10, sort by firstName ASC):
     * SELECT * FROM employees ORDER BY first_name ASC LIMIT 10 OFFSET 10
     */
    @Override
    public PagedResponseDTO<EmployeeResponseDTO> getAllEmployees(int pageNo, int pageSize,
                                                                  String sortBy, String sortDir) {
        logger.debug("Fetching employees - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                     pageNo, pageSize, sortBy, sortDir);

        // Create Pageable with sorting
        Pageable pageable = createPageable(pageNo, pageSize, sortBy, sortDir);

        // Execute paginated query
        Page<Employee> employeePage = employeeRepository.findAll(pageable);

        // Convert to response DTO
        return convertToPagedResponse(employeePage);
    }

    // ==========================================================================
    // GET EMPLOYEES BY DEPARTMENT (Paginated)
    // ==========================================================================

    @Override
    public PagedResponseDTO<EmployeeResponseDTO> getEmployeesByDepartment(String department,
                                                                           int pageNo, int pageSize,
                                                                           String sortBy, String sortDir) {
        logger.debug("Fetching employees by department: {}", department);

        Pageable pageable = createPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Employee> employeePage = employeeRepository.findByDepartment(department, pageable);

        return convertToPagedResponse(employeePage);
    }

    // ==========================================================================
    // GET EMPLOYEES BY STATUS (Paginated)
    // ==========================================================================

    @Override
    public PagedResponseDTO<EmployeeResponseDTO> getEmployeesByStatus(String status,
                                                                       int pageNo, int pageSize,
                                                                       String sortBy, String sortDir) {
        logger.debug("Fetching employees by status: {}", status);

        Employee.EmployeeStatus employeeStatus;
        try {
            employeeStatus = Employee.EmployeeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }

        Pageable pageable = createPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Employee> employeePage = employeeRepository.findByStatus(employeeStatus, pageable);

        return convertToPagedResponse(employeePage);
    }

    // ==========================================================================
    // SEARCH EMPLOYEES
    // ==========================================================================

    @Override
    public PagedResponseDTO<EmployeeResponseDTO> searchEmployees(String keyword,
                                                                  int pageNo, int pageSize,
                                                                  String sortBy, String sortDir) {
        logger.debug("Searching employees with keyword: {}", keyword);

        Pageable pageable = createPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Employee> employeePage = employeeRepository.searchEmployees(keyword, pageable);

        return convertToPagedResponse(employeePage);
    }

    // ==========================================================================
    // SEARCH WITH FILTERS
    // ==========================================================================

    @Override
    public PagedResponseDTO<EmployeeResponseDTO> searchEmployeesWithFilters(String keyword,
                                                                             String department,
                                                                             String status,
                                                                             int pageNo, int pageSize,
                                                                             String sortBy, String sortDir) {
        logger.debug("Searching employees - keyword: {}, department: {}, status: {}", 
                     keyword, department, status);

        Employee.EmployeeStatus employeeStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                employeeStatus = Employee.EmployeeStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status);
            }
        }

        Pageable pageable = createPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Employee> employeePage = employeeRepository.searchEmployeesWithFilters(
                keyword, department, employeeStatus, pageable);

        return convertToPagedResponse(employeePage);
    }

    // ==========================================================================
    // UTILITY METHODS
    // ==========================================================================

    @Override
    public List<String> getAllDepartments() {
        return employeeRepository.findAllDepartments();
    }

    @Override
    public List<String> getAllDesignations() {
        return employeeRepository.findAllDesignations();
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByManager(Long managerId) {
        logger.debug("Fetching employees reporting to manager ID: {}", managerId);

        return employeeRepository.findByManagerId(managerId)
                .stream()
                .map(EmployeeResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isEmailExists(String email) {
        return employeeRepository.existsByEmail(email);
    }

    @Override
    public boolean isEmployeeCodeExists(String employeeCode) {
        return employeeRepository.existsByEmployeeCode(employeeCode);
    }

    // ==========================================================================
    // PRIVATE HELPER METHODS
    // ==========================================================================

    /*
     * Creates Pageable object with sorting
     * 
     * WHY EXTRACT TO HELPER METHOD?
     * - DRY (Don't Repeat Yourself)
     * - Single place to change pagination logic
     * - Consistent sort direction handling
     */
    private Pageable createPageable(int pageNo, int pageSize, String sortBy, String sortDir) {
        // Convert 1-indexed page to 0-indexed (Spring uses 0-indexed)
        int zeroIndexedPage = pageNo > 0 ? pageNo - 1 : 0;

        // Determine sort direction
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                    ? Sort.by(sortBy).descending() 
                    : Sort.by(sortBy).ascending();

        return PageRequest.of(zeroIndexedPage, pageSize, sort);
    }

    /*
     * Converts Page<Employee> to PagedResponseDTO<EmployeeResponseDTO>
     * 
     * STREAM API USED:
     * .stream() - Create stream from list
     * .map() - Transform each Employee to EmployeeResponseDTO
     * .collect() - Collect results into list
     * 
     * EQUIVALENT TRADITIONAL CODE:
     * List<EmployeeResponseDTO> dtos = new ArrayList<>();
     * for (Employee emp : employeePage.getContent()) {
     *     dtos.add(EmployeeResponseDTO.fromEntity(emp));
     * }
     */
    private PagedResponseDTO<EmployeeResponseDTO> convertToPagedResponse(Page<Employee> employeePage) {
        List<EmployeeResponseDTO> content = employeePage.getContent()
                .stream()
                .map(EmployeeResponseDTO::fromEntity)
                .collect(Collectors.toList());

        return PagedResponseDTO.fromPage(employeePage, content);
    }

    /*
     * Generates unique employee code
     * 
     * FORMAT: EMP-YYYYMMDD-XXXX
     * Example: EMP-20240115-0001
     * 
     * This is a simple implementation. In production, you might want:
     * - Database sequence
     * - UUID-based codes
     * - Redis-based incrementing counter
     */
    private String generateEmployeeCode() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseCode = "EMP-" + datePrefix + "-";

        // Find the highest existing code for today and increment
        // Simple approach: count employees created today and add 1
        long count = employeeRepository.count();
        String sequence = String.format("%04d", count + 1);

        return baseCode + sequence;
    }
}

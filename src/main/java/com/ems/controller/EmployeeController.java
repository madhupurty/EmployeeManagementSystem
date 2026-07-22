package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.EmployeeRequestDTO;
import com.ems.dto.EmployeeResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.service.EmployeeService;
import com.ems.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * =============================================================================
 * EMPLOYEE CONTROLLER - REST API Endpoints
 * =============================================================================
 * 
 * This controller handles all HTTP requests related to Employee management.
 * It follows RESTful design principles and returns proper HTTP status codes.
 * 
 * =============================================================================
 * REST API DESIGN PRINCIPLES
 * =============================================================================
 * 
 * 1. RESOURCE-BASED URLs:
 *    - /api/employees (collection)
 *    - /api/employees/{id} (single resource)
 *    - NOT /api/getEmployee, /api/createEmployee (verbs in URL)
 * 
 * 2. HTTP METHODS = CRUD OPERATIONS:
 *    - GET: Read (retrieve data)
 *    - POST: Create (add new data)
 *    - PUT: Update (replace entire resource)
 *    - PATCH: Partial Update (modify specific fields)
 *    - DELETE: Delete (remove data)
 * 
 * 3. HTTP STATUS CODES:
 *    - 200 OK: Successful GET, PUT, PATCH
 *    - 201 Created: Successful POST
 *    - 204 No Content: Successful DELETE
 *    - 400 Bad Request: Validation error, bad input
 *    - 404 Not Found: Resource doesn't exist
 *    - 500 Internal Server Error: Server-side error
 * 
 * 4. CONSISTENT RESPONSE FORMAT:
 *    All responses wrapped in ApiResponse for consistency
 * 
 * INTERVIEW QUESTION: What are REST principles?
 * ANSWER: REST (Representational State Transfer) principles include:
 *   1. Stateless: Each request contains all info needed
 *   2. Client-Server: Separation of concerns
 *   3. Uniform Interface: Standard HTTP methods and URIs
 *   4. Resource-based: Everything is a resource with unique URI
 *   5. Layered System: Client doesn't know if it's talking to server or proxy
 * 
 * =============================================================================
 * ANNOTATIONS EXPLAINED
 * =============================================================================
 */

/*
 * @RestController = @Controller + @ResponseBody
 * 
 * @Controller: Marks class as Spring MVC controller (handles web requests)
 * @ResponseBody: Return values are serialized to JSON directly (not view names)
 * 
 * Without @ResponseBody, Spring would look for view templates.
 * With @RestController, every method returns data, not views.
 * 
 * INTERVIEW QUESTION: Difference between @Controller and @RestController?
 * ANSWER:
 *   @Controller: Returns view names (for Thymeleaf, JSP)
 *   @RestController: Returns data directly as JSON/XML
 *   
 *   @RestController = @Controller + @ResponseBody on every method
 */
@RestController

/*
 * @RequestMapping - Base URL for all endpoints in this controller
 * 
 * All methods will have URLs starting with /api/employees
 * Example:
 *   @GetMapping("/{id}") becomes GET /api/employees/{id}
 *   @PostMapping becomes POST /api/employees
 */
@RequestMapping("/api/employees")

/*
 * @RequiredArgsConstructor - Lombok generates constructor for final fields
 * 
 * This enables CONSTRUCTOR INJECTION:
 * public EmployeeController(EmployeeService employeeService) {
 *     this.employeeService = employeeService;
 * }
 */
@RequiredArgsConstructor

/*
 * @CrossOrigin - Enables CORS (Cross-Origin Resource Sharing)
 * 
 * WHAT IS CORS?
 * Browsers block requests from one domain to another (security).
 * Example: Frontend at localhost:3000 calling backend at localhost:8080
 * 
 * @CrossOrigin allows specific origins to make requests.
 * origins = "*" means any origin (useful for development, NOT for production!)
 * 
 * PRODUCTION: Configure specific origins in application.properties or
 * use a WebMvcConfigurer for global CORS configuration.
 */
@CrossOrigin(origins = "*")
@Tag(name = "Employee Management", description = "APIs for managing employee data")
@SecurityRequirement(name = "Bearer Authentication")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    /*
     * Dependency injection via constructor (best practice)
     * Service is injected by Spring from ApplicationContext
     */
    private final EmployeeService employeeService;

    // ==========================================================================
    // CREATE EMPLOYEE - POST /api/employees
    // ==========================================================================

    /*
     * @PostMapping - Handles HTTP POST requests
     * 
     * POST is used for CREATING new resources.
     * Request body contains the data to create.
     * 
     * @Valid - Triggers validation on EmployeeRequestDTO
     * 
     * VALIDATION FLOW:
     * 1. Client sends JSON in request body
     * 2. Jackson deserializes JSON to EmployeeRequestDTO
     * 3. @Valid triggers validation of @NotBlank, @Email, etc.
     * 4. If validation fails: MethodArgumentNotValidException thrown
     * 5. GlobalExceptionHandler catches and returns 400 response
     * 6. If validation passes: method body executes
     * 
     * @RequestBody - Deserializes JSON body to Java object
     * 
     * INTERVIEW QUESTION: What does @Valid do?
     * ANSWER: @Valid triggers Bean Validation on the annotated parameter.
     *         It checks all validation annotations (@NotBlank, @Email, etc.)
     *         on the DTO fields. If any validation fails, Spring throws
     *         MethodArgumentNotValidException before the method executes.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create a new employee", description = "Only ADMIN and HR can create employees")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> createEmployee(
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {
        
        logger.info("REST request to create employee: {}", requestDTO.getEmail());

        EmployeeResponseDTO createdEmployee = employeeService.createEmployee(requestDTO);

        /*
         * ResponseEntity - Wrapper for HTTP response
         * 
         * ResponseEntity.status(HttpStatus.CREATED) - Sets 201 Created status
         * .body() - Sets the response body
         * 
         * WHY 201 CREATED instead of 200 OK?
         * 201 indicates a new resource was created successfully.
         * Response should include the created resource.
         * 
         * ALTERNATIVE: Include Location header with URI of new resource
         * ResponseEntity.created(URI.create("/api/employees/" + id)).body(...)
         */
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee created successfully", createdEmployee));
    }

    // ==========================================================================
    // UPDATE EMPLOYEE - PUT /api/employees/{id}
    // ==========================================================================

    /*
     * @PutMapping("/{id}") - Handles HTTP PUT requests
     * 
     * PUT is used for UPDATING/REPLACING a resource.
     * The {id} is a path variable extracted from the URL.
     * 
     * @PathVariable - Extracts value from URL path
     * Example: PUT /api/employees/5 → id = 5
     * 
     * PUT vs PATCH:
     * - PUT: Replace entire resource (send all fields)
     * - PATCH: Partial update (send only changed fields)
     * 
     * Our implementation uses PUT but allows partial updates
     * (doesn't require all fields). This is pragmatic for simplicity.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update an employee", description = "Only ADMIN and HR can update employees")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDTO requestDTO) {
        
        logger.info("REST request to update employee with ID: {}", id);

        EmployeeResponseDTO updatedEmployee = employeeService.updateEmployee(id, requestDTO);

        return ResponseEntity.ok(ApiResponse.success("Employee updated successfully", updatedEmployee));
    }

    // ==========================================================================
    // GET EMPLOYEE BY ID - GET /api/employees/{id}
    // ==========================================================================

    /*
     * @GetMapping("/{id}") - Handles HTTP GET requests for single resource
     * 
     * GET is used for READING data. It should be:
     * - SAFE: Doesn't modify data
     * - IDEMPOTENT: Same result no matter how many times called
     * - CACHEABLE: Response can be cached
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get employee by ID", description = "All authenticated users can view employee details")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeById(@PathVariable Long id) {
        logger.debug("REST request to get employee by ID: {}", id);

        EmployeeResponseDTO employee = employeeService.getEmployeeById(id);

        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    // ==========================================================================
    // GET EMPLOYEE BY CODE - GET /api/employees/code/{employeeCode}
    // ==========================================================================

    @GetMapping("/code/{employeeCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get employee by code", description = "All authenticated users can view employee details")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> getEmployeeByCode(
            @PathVariable String employeeCode) {
        
        logger.debug("REST request to get employee by code: {}", employeeCode);

        EmployeeResponseDTO employee = employeeService.getEmployeeByCode(employeeCode);

        return ResponseEntity.ok(ApiResponse.success(employee));
    }

    // ==========================================================================
    // DELETE EMPLOYEE - DELETE /api/employees/{id}
    // ==========================================================================

    /*
     * @DeleteMapping("/{id}") - Handles HTTP DELETE requests
     * 
     * DELETE is used for REMOVING resources.
     * 
     * Response: 204 No Content (successful delete with no body)
     * Alternative: 200 OK with confirmation message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete an employee", description = "Only ADMIN can delete employees")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        logger.info("REST request to delete employee with ID: {}", id);

        employeeService.deleteEmployee(id);

        /*
         * Two common responses for DELETE:
         * 
         * 1. 204 No Content (no response body):
         *    return ResponseEntity.noContent().build();
         * 
         * 2. 200 OK with message (our approach - more informative):
         *    return ResponseEntity.ok(ApiResponse.success("Deleted"));
         */
        return ResponseEntity.ok(ApiResponse.success("Employee deleted successfully", null));
    }

    // ==========================================================================
    // DEACTIVATE EMPLOYEE - PATCH /api/employees/{id}/deactivate
    // ==========================================================================

    /*
     * Custom action endpoint - not strictly RESTful but practical
     * 
     * Alternative approaches:
     * 1. PATCH /api/employees/{id} with body {"status": "INACTIVE"}
     * 2. PUT /api/employees/{id}/status with body {"status": "INACTIVE"}
     * 3. Our approach: POST /api/employees/{id}/deactivate (action-based)
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Deactivate an employee", description = "Only ADMIN and HR can deactivate employees")
    public ResponseEntity<ApiResponse<EmployeeResponseDTO>> deactivateEmployee(@PathVariable Long id) {
        logger.info("REST request to deactivate employee with ID: {}", id);

        EmployeeResponseDTO deactivatedEmployee = employeeService.deactivateEmployee(id);

        return ResponseEntity.ok(ApiResponse.success("Employee deactivated successfully", deactivatedEmployee));
    }

    // ==========================================================================
    // GET ALL EMPLOYEES (Paginated) - GET /api/employees
    // ==========================================================================

    /*
     * PAGINATION PARAMETERS AS @RequestParam
     * 
     * @RequestParam - Extracts query parameters from URL
     * Example: GET /api/employees?page=1&size=10&sortBy=firstName&sortDir=asc
     * 
     * required = false: Parameter is optional
     * defaultValue: Used if parameter not provided
     * 
     * Using AppConstants for defaults ensures consistency across endpoints.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get all employees (paginated)", description = "All authenticated users can view employees")
    public ResponseEntity<ApiResponse<PagedResponseDTO<EmployeeResponseDTO>>> getAllEmployees(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        
        logger.debug("REST request to get all employees - page: {}, size: {}", page, size);

        PagedResponseDTO<EmployeeResponseDTO> response = employeeService.getAllEmployees(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================================================
    // GET EMPLOYEES BY DEPARTMENT - GET /api/employees/department/{department}
    // ==========================================================================

    @GetMapping("/department/{department}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get employees by department", description = "All authenticated users can view employees by department")
    public ResponseEntity<ApiResponse<PagedResponseDTO<EmployeeResponseDTO>>> getEmployeesByDepartment(
            @PathVariable String department,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        
        logger.debug("REST request to get employees by department: {}", department);

        PagedResponseDTO<EmployeeResponseDTO> response = 
                employeeService.getEmployeesByDepartment(department, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================================================
    // GET EMPLOYEES BY STATUS - GET /api/employees/status/{status}
    // ==========================================================================

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get employees by status", description = "ADMIN and HR can filter employees by status")
    public ResponseEntity<ApiResponse<PagedResponseDTO<EmployeeResponseDTO>>> getEmployeesByStatus(
            @PathVariable String status,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        
        logger.debug("REST request to get employees by status: {}", status);

        PagedResponseDTO<EmployeeResponseDTO> response = 
                employeeService.getEmployeesByStatus(status, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================================================
    // SEARCH EMPLOYEES - GET /api/employees/search?keyword=xxx
    // ==========================================================================

    /*
     * SEARCH ENDPOINT
     * 
     * Uses @RequestParam for search keyword and pagination.
     * Searches across multiple fields: firstName, lastName, email, department.
     * 
     * Example: GET /api/employees/search?keyword=john&page=1&size=10
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Search employees by keyword", description = "All authenticated users can search employees")
    public ResponseEntity<ApiResponse<PagedResponseDTO<EmployeeResponseDTO>>> searchEmployees(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        
        logger.debug("REST request to search employees with keyword: {}", keyword);

        PagedResponseDTO<EmployeeResponseDTO> response = 
                employeeService.searchEmployees(keyword, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================================================
    // ADVANCED SEARCH - GET /api/employees/search/advanced
    // ==========================================================================

    /*
     * Advanced search with multiple optional filters
     * 
     * Example: GET /api/employees/search/advanced?keyword=john&department=IT&status=ACTIVE
     * 
     * All filter parameters are optional (required = false)
     */
    @GetMapping("/search/advanced")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Advanced search with filters", description = "ADMIN and HR can perform advanced search")
    public ResponseEntity<ApiResponse<PagedResponseDTO<EmployeeResponseDTO>>> searchEmployeesAdvanced(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "department", required = false) String department,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir) {
        
        logger.debug("REST request for advanced search - keyword: {}, department: {}, status: {}", 
                     keyword, department, status);

        PagedResponseDTO<EmployeeResponseDTO> response = 
                employeeService.searchEmployeesWithFilters(keyword, department, status, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================================================
    // UTILITY ENDPOINTS
    // ==========================================================================

    /*
     * Get all unique departments - useful for dropdown filters
     */
    @GetMapping("/departments")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get all departments", description = "All authenticated users can view departments")
    public ResponseEntity<ApiResponse<List<String>>> getAllDepartments() {
        logger.debug("REST request to get all departments");

        List<String> departments = employeeService.getAllDepartments();

        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    /*
     * Get all unique designations - useful for dropdown filters
     */
    @GetMapping("/designations")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get all designations", description = "All authenticated users can view designations")
    public ResponseEntity<ApiResponse<List<String>>> getAllDesignations() {
        logger.debug("REST request to get all designations");

        List<String> designations = employeeService.getAllDesignations();

        return ResponseEntity.ok(ApiResponse.success(designations));
    }

    /*
     * Get employees by manager ID
     */
    @GetMapping("/manager/{managerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get employees by manager", description = "ADMIN and HR can view employees by manager")
    public ResponseEntity<ApiResponse<List<EmployeeResponseDTO>>> getEmployeesByManager(
            @PathVariable Long managerId) {
        
        logger.debug("REST request to get employees by manager ID: {}", managerId);

        List<EmployeeResponseDTO> employees = employeeService.getEmployeesByManager(managerId);

        return ResponseEntity.ok(ApiResponse.success(employees));
    }

    /*
     * Check if email exists - useful for real-time validation
     */
    @GetMapping("/check-email")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Check if email exists", description = "ADMIN and HR can check email availability")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        logger.debug("REST request to check if email exists: {}", email);

        boolean exists = employeeService.isEmailExists(email);

        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}

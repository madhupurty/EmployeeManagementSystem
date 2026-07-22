package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.DepartmentRequestDTO;
import com.ems.dto.DepartmentResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.service.DepartmentService;
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
 * DEPARTMENT CONTROLLER - REST API for Department Management
 * =============================================================================
 * 
 * ENDPOINTS:
 * ----------
 * POST   /api/departments              - Create department (ADMIN, HR)
 * PUT    /api/departments/{id}         - Update department (ADMIN, HR)
 * GET    /api/departments/{id}         - Get department by ID (All authenticated)
 * GET    /api/departments/code/{code}  - Get department by code (All authenticated)
 * GET    /api/departments              - Get all departments paginated (All authenticated)
 * GET    /api/departments/active       - Get active departments (All authenticated)
 * GET    /api/departments/list         - Get all active as list (All authenticated)
 * GET    /api/departments/search       - Search departments (All authenticated)
 * DELETE /api/departments/{id}         - Delete department (ADMIN only)
 * PATCH  /api/departments/{id}/deactivate - Deactivate (ADMIN, HR)
 * PATCH  /api/departments/{id}/activate   - Activate (ADMIN, HR)
 * PATCH  /api/departments/{id}/manager/{employeeId} - Assign manager (ADMIN, HR)
 * DELETE /api/departments/{id}/manager - Remove manager (ADMIN, HR)
 * 
 * =============================================================================
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Department Management", description = "APIs for managing departments")
@SecurityRequirement(name = "Bearer Authentication")
public class DepartmentController {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentController.class);

    private final DepartmentService departmentService;

    // ==========================================================================
    // CREATE
    // ==========================================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Create a new department", description = "Only ADMIN and HR can create departments")
    public ResponseEntity<ApiResponse<DepartmentResponseDTO>> createDepartment(
            @Valid @RequestBody DepartmentRequestDTO requestDTO) {
        
        logger.info("REST request to create department: {}", requestDTO.getCode());

        DepartmentResponseDTO createdDepartment = departmentService.createDepartment(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department created successfully", createdDepartment));
    }

    // ==========================================================================
    // UPDATE
    // ==========================================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update a department", description = "Only ADMIN and HR can update departments")
    public ResponseEntity<ApiResponse<DepartmentResponseDTO>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDTO requestDTO) {
        
        logger.info("REST request to update department with ID: {}", id);

        DepartmentResponseDTO updatedDepartment = departmentService.updateDepartment(id, requestDTO);

        return ResponseEntity.ok(ApiResponse.success("Department updated successfully", updatedDepartment));
    }

    // ==========================================================================
    // READ
    // ==========================================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get department by ID", description = "All authenticated users can view")
    public ResponseEntity<ApiResponse<DepartmentResponseDTO>> getDepartmentById(@PathVariable Long id) {
        logger.debug("REST request to get department by ID: {}", id);

        DepartmentResponseDTO department = departmentService.getDepartmentById(id);

        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get department by code", description = "All authenticated users can view")
    public ResponseEntity<ApiResponse<DepartmentResponseDTO>> getDepartmentByCode(@PathVariable String code) {
        logger.debug("REST request to get department by code: {}", code);

        DepartmentResponseDTO department = departmentService.getDepartmentByCode(code);

        return ResponseEntity.ok(ApiResponse.success(department));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get all departments (paginated)", description = "All authenticated users can view")
    public ResponseEntity<ApiResponse<PagedResponseDTO<DepartmentResponseDTO>>> getAllDepartments(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        
        logger.debug("REST request to get all departments - page: {}, size: {}", page, size);

        PagedResponseDTO<DepartmentResponseDTO> response = 
                departmentService.getAllDepartments(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get active departments (paginated)", description = "All authenticated users can view")
    public ResponseEntity<ApiResponse<PagedResponseDTO<DepartmentResponseDTO>>> getActiveDepartments(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        
        logger.debug("REST request to get active departments");

        PagedResponseDTO<DepartmentResponseDTO> response = 
                departmentService.getActiveDepartments(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get all active departments as list", description = "Useful for dropdowns")
    public ResponseEntity<ApiResponse<List<DepartmentResponseDTO>>> getAllActiveDepartmentsList() {
        logger.debug("REST request to get all active departments as list");

        List<DepartmentResponseDTO> departments = departmentService.getAllActiveDepartmentsList();

        return ResponseEntity.ok(ApiResponse.success(departments));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Search departments", description = "Search by name, code, or description")
    public ResponseEntity<ApiResponse<PagedResponseDTO<DepartmentResponseDTO>>> searchDepartments(
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION) String sortDir) {
        
        logger.debug("REST request to search departments with keyword: {}", keyword);

        PagedResponseDTO<DepartmentResponseDTO> response = 
                departmentService.searchDepartments(keyword, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==========================================================================
    // DELETE / STATUS
    // ==========================================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a department", description = "Only ADMIN can delete departments")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        logger.info("REST request to delete department with ID: {}", id);

        departmentService.deleteDepartment(id);

        return ResponseEntity.ok(ApiResponse.success("Department deleted successfully", null));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Deactivate a department", description = "ADMIN and HR can deactivate")
    public ResponseEntity<ApiResponse<DepartmentResponseDTO>> deactivateDepartment(@PathVariable Long id) {
        logger.info("REST request to deactivate department with ID: {}", id);

        DepartmentResponseDTO department = departmentService.deactivateDepartment(id);

        return ResponseEntity.ok(ApiResponse.success("Department deactivated successfully", department));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Activate a department", description = "ADMIN and HR can activate")
    public ResponseEntity<ApiResponse<DepartmentResponseDTO>> activateDepartment(@PathVariable Long id) {
        logger.info("REST request to activate department with ID: {}", id);

        DepartmentResponseDTO department = departmentService.activateDepartment(id);

        return ResponseEntity.ok(ApiResponse.success("Department activated successfully", department));
    }

    // ==========================================================================
    // MANAGER OPERATIONS
    // ==========================================================================

    @PatchMapping("/{departmentId}/manager/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Assign manager to department", description = "ADMIN and HR can assign managers")
    public ResponseEntity<ApiResponse<DepartmentResponseDTO>> assignManager(
            @PathVariable Long departmentId,
            @PathVariable Long employeeId) {
        
        logger.info("REST request to assign manager {} to department {}", employeeId, departmentId);

        DepartmentResponseDTO department = departmentService.assignManager(departmentId, employeeId);

        return ResponseEntity.ok(ApiResponse.success("Manager assigned successfully", department));
    }

    @DeleteMapping("/{departmentId}/manager")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Remove manager from department", description = "ADMIN and HR can remove managers")
    public ResponseEntity<ApiResponse<DepartmentResponseDTO>> removeManager(@PathVariable Long departmentId) {
        logger.info("REST request to remove manager from department {}", departmentId);

        DepartmentResponseDTO department = departmentService.removeManager(departmentId);

        return ResponseEntity.ok(ApiResponse.success("Manager removed successfully", department));
    }

    // ==========================================================================
    // VALIDATION
    // ==========================================================================

    @GetMapping("/check-code")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Check if department code exists", description = "Validation endpoint")
    public ResponseEntity<ApiResponse<Boolean>> checkCodeExists(@RequestParam String code) {
        logger.debug("REST request to check if department code exists: {}", code);

        boolean exists = departmentService.isDepartmentCodeExists(code);

        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @GetMapping("/check-name")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Check if department name exists", description = "Validation endpoint")
    public ResponseEntity<ApiResponse<Boolean>> checkNameExists(@RequestParam String name) {
        logger.debug("REST request to check if department name exists: {}", name);

        boolean exists = departmentService.isDepartmentNameExists(name);

        return ResponseEntity.ok(ApiResponse.success(exists));
    }
}

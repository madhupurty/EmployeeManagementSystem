package com.ems.controller;

import com.ems.dto.*;
import com.ems.entity.LeaveStatus;
import com.ems.entity.User;
import com.ems.service.LeaveService;
import com.ems.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * =============================================================================
 * LEAVE CONTROLLER - REST API for Leave Management
 * =============================================================================
 */
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Leave Management", description = "APIs for managing employee leave requests")
@SecurityRequirement(name = "Bearer Authentication")
public class LeaveController {

    private static final Logger logger = LoggerFactory.getLogger(LeaveController.class);

    private final LeaveService leaveService;

    // ==========================================================================
    // CREATE
    // ==========================================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Create a new leave request", description = "All authenticated users can apply for leave")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> createLeaveRequest(
            @Valid @RequestBody LeaveRequestDTO requestDTO) {
        
        logger.info("REST request to create leave request for employee: {}", requestDTO.getEmployeeId());

        LeaveResponseDTO createdRequest = leaveService.createLeaveRequest(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave request created successfully", createdRequest));
    }

    // ==========================================================================
    // UPDATE
    // ==========================================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Update a leave request", description = "Only pending requests can be updated")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> updateLeaveRequest(
            @PathVariable Long id,
            @Valid @RequestBody LeaveRequestDTO requestDTO) {
        
        logger.info("REST request to update leave request: {}", id);

        LeaveResponseDTO updatedRequest = leaveService.updateLeaveRequest(id, requestDTO);

        return ResponseEntity.ok(ApiResponse.success("Leave request updated successfully", updatedRequest));
    }

    // ==========================================================================
    // READ
    // ==========================================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get leave request by ID")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> getLeaveRequestById(@PathVariable Long id) {
        logger.debug("REST request to get leave request: {}", id);

        LeaveResponseDTO leaveRequest = leaveService.getLeaveRequestById(id);

        return ResponseEntity.ok(ApiResponse.success(leaveRequest));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get all leave requests (paginated)", description = "ADMIN and HR can view all")
    public ResponseEntity<ApiResponse<PagedResponseDTO<LeaveResponseDTO>>> getAllLeaveRequests(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "startDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        
        logger.debug("REST request to get all leave requests");

        PagedResponseDTO<LeaveResponseDTO> response = 
                leaveService.getAllLeaveRequests(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get leave requests by employee")
    public ResponseEntity<ApiResponse<PagedResponseDTO<LeaveResponseDTO>>> getLeaveRequestsByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "startDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        
        logger.debug("REST request to get leave requests for employee: {}", employeeId);

        PagedResponseDTO<LeaveResponseDTO> response = 
                leaveService.getLeaveRequestsByEmployee(employeeId, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get leave requests by status")
    public ResponseEntity<ApiResponse<PagedResponseDTO<LeaveResponseDTO>>> getLeaveRequestsByStatus(
            @PathVariable LeaveStatus status,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "startDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        
        logger.debug("REST request to get leave requests by status: {}", status);

        PagedResponseDTO<LeaveResponseDTO> response = 
                leaveService.getLeaveRequestsByStatus(status, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get pending leave requests", description = "For approval queue")
    public ResponseEntity<ApiResponse<PagedResponseDTO<LeaveResponseDTO>>> getPendingLeaveRequests(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        
        logger.debug("REST request to get pending leave requests");

        PagedResponseDTO<LeaveResponseDTO> response = 
                leaveService.getPendingLeaveRequests(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get leave requests by date range")
    public ResponseEntity<ApiResponse<List<LeaveResponseDTO>>> getLeaveRequestsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("REST request to get leave requests from {} to {}", startDate, endDate);

        List<LeaveResponseDTO> leaves = leaveService.getLeaveRequestsByDateRange(startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(leaves));
    }

    // ==========================================================================
    // DELETE
    // ==========================================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Delete a leave request", description = "Only pending requests can be deleted")
    public ResponseEntity<ApiResponse<Void>> deleteLeaveRequest(@PathVariable Long id) {
        logger.info("REST request to delete leave request: {}", id);

        leaveService.deleteLeaveRequest(id);

        return ResponseEntity.ok(ApiResponse.success("Leave request deleted successfully", null));
    }

    // ==========================================================================
    // APPROVAL WORKFLOW
    // ==========================================================================

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Approve a leave request", description = "ADMIN and HR can approve")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> approveLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveActionDTO actionDTO,
            @AuthenticationPrincipal User currentUser) {
        
        logger.info("REST request to approve leave request: {}", id);

        // Get approver ID from current user or their linked employee
        Long approverId = currentUser.getEmployee() != null ? 
                currentUser.getEmployee().getId() : 1L; // Default to 1 if no employee linked

        LeaveResponseDTO approvedRequest = leaveService.approveLeave(id, actionDTO, approverId);

        return ResponseEntity.ok(ApiResponse.success("Leave request approved successfully", approvedRequest));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Reject a leave request", description = "ADMIN and HR can reject")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> rejectLeave(
            @PathVariable Long id,
            @RequestBody(required = false) LeaveActionDTO actionDTO,
            @AuthenticationPrincipal User currentUser) {
        
        logger.info("REST request to reject leave request: {}", id);

        Long approverId = currentUser.getEmployee() != null ? 
                currentUser.getEmployee().getId() : 1L;

        LeaveResponseDTO rejectedRequest = leaveService.rejectLeave(id, actionDTO, approverId);

        return ResponseEntity.ok(ApiResponse.success("Leave request rejected", rejectedRequest));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Cancel a leave request", description = "Only pending requests can be cancelled")
    public ResponseEntity<ApiResponse<LeaveResponseDTO>> cancelLeave(@PathVariable Long id) {
        logger.info("REST request to cancel leave request: {}", id);

        LeaveResponseDTO cancelledRequest = leaveService.cancelLeave(id);

        return ResponseEntity.ok(ApiResponse.success("Leave request cancelled", cancelledRequest));
    }

    // ==========================================================================
    // STATISTICS
    // ==========================================================================

    @GetMapping("/employee/{employeeId}/used-days")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get used leave days for an employee in a year")
    public ResponseEntity<ApiResponse<Integer>> getUsedLeaveDays(
            @PathVariable Long employeeId,
            @RequestParam(defaultValue = "#{T(java.time.Year).now().getValue()}") int year) {
        
        logger.debug("REST request to get used leave days for employee: {} in year: {}", employeeId, year);

        Integer usedDays = leaveService.getUsedLeaveDays(employeeId, year);

        return ResponseEntity.ok(ApiResponse.success(usedDays));
    }

    @GetMapping("/pending/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get count of pending leave requests")
    public ResponseEntity<ApiResponse<Long>> getPendingLeaveCount() {
        logger.debug("REST request to get pending leave count");

        Long count = leaveService.getPendingLeaveCount();

        return ResponseEntity.ok(ApiResponse.success(count));
    }
}

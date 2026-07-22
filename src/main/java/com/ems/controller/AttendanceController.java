package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.AttendanceRequestDTO;
import com.ems.dto.AttendanceResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.service.AttendanceService;
import com.ems.util.AppConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * =============================================================================
 * ATTENDANCE CONTROLLER - REST API for Attendance Management
 * =============================================================================
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Attendance Management", description = "APIs for managing employee attendance")
@SecurityRequirement(name = "Bearer Authentication")
public class AttendanceController {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);

    private final AttendanceService attendanceService;

    // ==========================================================================
    // RECORD ATTENDANCE
    // ==========================================================================

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Record attendance manually", description = "ADMIN and HR can record attendance")
    public ResponseEntity<ApiResponse<AttendanceResponseDTO>> recordAttendance(
            @Valid @RequestBody AttendanceRequestDTO requestDTO) {
        
        logger.info("REST request to record attendance for employee: {}", requestDTO.getEmployeeId());

        AttendanceResponseDTO attendance = attendanceService.recordAttendance(requestDTO);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Attendance recorded successfully", attendance));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Update attendance record", description = "ADMIN and HR can update attendance")
    public ResponseEntity<ApiResponse<AttendanceResponseDTO>> updateAttendance(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequestDTO requestDTO) {
        
        logger.info("REST request to update attendance: {}", id);

        AttendanceResponseDTO attendance = attendanceService.updateAttendance(id, requestDTO);

        return ResponseEntity.ok(ApiResponse.success("Attendance updated successfully", attendance));
    }

    // ==========================================================================
    // CHECK-IN / CHECK-OUT
    // ==========================================================================

    @PostMapping("/check-in/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Employee check-in", description = "Record check-in time for today")
    public ResponseEntity<ApiResponse<AttendanceResponseDTO>> checkIn(
            @PathVariable Long employeeId,
            HttpServletRequest request) {
        
        logger.info("REST request for employee {} to check in", employeeId);

        String ip = getClientIp(request);
        AttendanceResponseDTO attendance = attendanceService.checkIn(employeeId, ip, null);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Check-in successful", attendance));
    }

    @PostMapping("/check-out/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Employee check-out", description = "Record check-out time for today")
    public ResponseEntity<ApiResponse<AttendanceResponseDTO>> checkOut(
            @PathVariable Long employeeId,
            HttpServletRequest request) {
        
        logger.info("REST request for employee {} to check out", employeeId);

        String ip = getClientIp(request);
        AttendanceResponseDTO attendance = attendanceService.checkOut(employeeId, ip, null);

        return ResponseEntity.ok(ApiResponse.success("Check-out successful", attendance));
    }

    // ==========================================================================
    // READ OPERATIONS
    // ==========================================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get attendance by ID")
    public ResponseEntity<ApiResponse<AttendanceResponseDTO>> getAttendanceById(@PathVariable Long id) {
        logger.debug("REST request to get attendance: {}", id);

        AttendanceResponseDTO attendance = attendanceService.getAttendanceById(id);

        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @GetMapping("/employee/{employeeId}/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get attendance by employee and date")
    public ResponseEntity<ApiResponse<AttendanceResponseDTO>> getAttendanceByEmployeeAndDate(
            @PathVariable Long employeeId,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        logger.debug("REST request to get attendance for employee {} on {}", employeeId, date);

        AttendanceResponseDTO attendance = attendanceService.getAttendanceByEmployeeAndDate(employeeId, date);

        return ResponseEntity.ok(ApiResponse.success(attendance));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get all attendance (paginated)", description = "ADMIN and HR can view all")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AttendanceResponseDTO>>> getAllAttendance(
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "attendanceDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        
        logger.debug("REST request to get all attendance");

        PagedResponseDTO<AttendanceResponseDTO> response = 
                attendanceService.getAllAttendance(page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get attendance by employee (paginated)")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AttendanceResponseDTO>>> getAttendanceByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "attendanceDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        
        logger.debug("REST request to get attendance for employee: {}", employeeId);

        PagedResponseDTO<AttendanceResponseDTO> response = 
                attendanceService.getAttendanceByEmployee(employeeId, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date/{date}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get attendance by date (paginated)")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AttendanceResponseDTO>>> getAttendanceByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "checkInTime") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc") String sortDir) {
        
        logger.debug("REST request to get attendance for date: {}", date);

        PagedResponseDTO<AttendanceResponseDTO> response = 
                attendanceService.getAttendanceByDate(date, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get attendance by date range (paginated)")
    public ResponseEntity<ApiResponse<PagedResponseDTO<AttendanceResponseDTO>>> getAttendanceByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "page", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(value = "sortBy", defaultValue = "attendanceDate") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {
        
        logger.debug("REST request to get attendance from {} to {}", startDate, endDate);

        PagedResponseDTO<AttendanceResponseDTO> response = 
                attendanceService.getAttendanceByDateRange(startDate, endDate, page, size, sortBy, sortDir);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/employee/{employeeId}/date-range")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get employee attendance by date range")
    public ResponseEntity<ApiResponse<List<AttendanceResponseDTO>>> getEmployeeAttendanceByDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("REST request to get attendance for employee {} from {} to {}", 
                     employeeId, startDate, endDate);

        List<AttendanceResponseDTO> attendanceList = 
                attendanceService.getEmployeeAttendanceByDateRange(employeeId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(attendanceList));
    }

    // ==========================================================================
    // DELETE
    // ==========================================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete attendance record", description = "Only ADMIN can delete")
    public ResponseEntity<ApiResponse<Void>> deleteAttendance(@PathVariable Long id) {
        logger.info("REST request to delete attendance: {}", id);

        attendanceService.deleteAttendance(id);

        return ResponseEntity.ok(ApiResponse.success("Attendance record deleted successfully", null));
    }

    // ==========================================================================
    // STATISTICS
    // ==========================================================================

    @GetMapping("/stats/present-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get present count for a date")
    public ResponseEntity<ApiResponse<Long>> getPresentCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        logger.debug("REST request to get present count for: {}", date);

        Long count = attendanceService.getPresentCount(date);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/stats/absent-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    @Operation(summary = "Get absent count for a date")
    public ResponseEntity<ApiResponse<Long>> getAbsentCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        logger.debug("REST request to get absent count for: {}", date);

        Long count = attendanceService.getAbsentCount(date);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    @GetMapping("/stats/employee/{employeeId}/working-hours")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get total working hours for employee in date range")
    public ResponseEntity<ApiResponse<Long>> getTotalWorkingMinutes(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("REST request to get working hours for employee {} from {} to {}", 
                     employeeId, startDate, endDate);

        Long minutes = attendanceService.getTotalWorkingMinutes(employeeId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(minutes));
    }

    @GetMapping("/stats/employee/{employeeId}/overtime")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get total overtime for employee in date range")
    public ResponseEntity<ApiResponse<Long>> getTotalOvertimeMinutes(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("REST request to get overtime for employee {} from {} to {}", 
                     employeeId, startDate, endDate);

        Long minutes = attendanceService.getTotalOvertimeMinutes(employeeId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(minutes));
    }

    @GetMapping("/stats/employee/{employeeId}/late-count")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'EMPLOYEE')")
    @Operation(summary = "Get late count for employee in date range")
    public ResponseEntity<ApiResponse<Long>> getLateCount(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        logger.debug("REST request to get late count for employee {} from {} to {}", 
                     employeeId, startDate, endDate);

        Long count = attendanceService.getLateCount(employeeId, startDate, endDate);

        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // ==========================================================================
    // HELPER METHODS
    // ==========================================================================

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}

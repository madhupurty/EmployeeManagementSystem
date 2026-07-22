package com.ems.service;

import com.ems.dto.LeaveActionDTO;
import com.ems.dto.LeaveRequestDTO;
import com.ems.dto.LeaveResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.entity.LeaveStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * =============================================================================
 * LEAVE SERVICE INTERFACE
 * =============================================================================
 */
public interface LeaveService {

    // CRUD Operations
    LeaveResponseDTO createLeaveRequest(LeaveRequestDTO requestDTO);

    LeaveResponseDTO updateLeaveRequest(Long id, LeaveRequestDTO requestDTO);

    LeaveResponseDTO getLeaveRequestById(Long id);

    void deleteLeaveRequest(Long id);

    // Approval Workflow
    LeaveResponseDTO approveLeave(Long id, LeaveActionDTO actionDTO, Long approverId);

    LeaveResponseDTO rejectLeave(Long id, LeaveActionDTO actionDTO, Long approverId);

    LeaveResponseDTO cancelLeave(Long id);

    // Query Operations
    PagedResponseDTO<LeaveResponseDTO> getAllLeaveRequests(int page, int size, String sortBy, String sortDir);

    PagedResponseDTO<LeaveResponseDTO> getLeaveRequestsByEmployee(Long employeeId, int page, int size, String sortBy, String sortDir);

    PagedResponseDTO<LeaveResponseDTO> getLeaveRequestsByStatus(LeaveStatus status, int page, int size, String sortBy, String sortDir);

    PagedResponseDTO<LeaveResponseDTO> getPendingLeaveRequests(int page, int size, String sortBy, String sortDir);

    List<LeaveResponseDTO> getLeaveRequestsByDateRange(LocalDate startDate, LocalDate endDate);

    List<LeaveResponseDTO> getEmployeeLeavesByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate);

    // Statistics
    Integer getUsedLeaveDays(Long employeeId, int year);

    Long getPendingLeaveCount();
}

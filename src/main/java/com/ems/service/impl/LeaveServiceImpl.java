package com.ems.service.impl;

import com.ems.dto.LeaveActionDTO;
import com.ems.dto.LeaveRequestDTO;
import com.ems.dto.LeaveResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.entity.Employee;
import com.ems.entity.LeaveRequest;
import com.ems.entity.LeaveStatus;
import com.ems.entity.LeaveType;
import com.ems.exception.BadRequestException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.LeaveRequestRepository;
import com.ems.service.LeaveService;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * LEAVE SERVICE IMPLEMENTATION
 * =============================================================================
 */
@Service
@RequiredArgsConstructor
@Transactional
public class LeaveServiceImpl implements LeaveService {

    private static final Logger logger = LoggerFactory.getLogger(LeaveServiceImpl.class);

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository employeeRepository;

    // ==========================================================================
    // CREATE
    // ==========================================================================

    @Override
    public LeaveResponseDTO createLeaveRequest(LeaveRequestDTO requestDTO) {
        logger.info("Creating leave request for employee ID: {}", requestDTO.getEmployeeId());

        // Validate employee exists
        Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", requestDTO.getEmployeeId()));

        // Validate dates
        validateDates(requestDTO.getStartDate(), requestDTO.getEndDate());

        // Check for overlapping leaves
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeaves(
                requestDTO.getEmployeeId(), requestDTO.getStartDate(), requestDTO.getEndDate());
        
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Leave request overlaps with existing leave from " +
                    overlapping.get(0).getStartDate() + " to " + overlapping.get(0).getEndDate());
        }

        // Create leave request
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(requestDTO.getLeaveType())
                .startDate(requestDTO.getStartDate())
                .endDate(requestDTO.getEndDate())
                .reason(requestDTO.getReason())
                .status(LeaveStatus.PENDING)
                .isHalfDay(requestDTO.getIsHalfDay())
                .halfDaySession(requestDTO.getHalfDaySession())
                .build();

        LeaveRequest savedRequest = leaveRequestRepository.save(leaveRequest);
        logger.info("Leave request created with ID: {}", savedRequest.getId());

        return mapToResponseDTO(savedRequest);
    }

    // ==========================================================================
    // UPDATE
    // ==========================================================================

    @Override
    public LeaveResponseDTO updateLeaveRequest(Long id, LeaveRequestDTO requestDTO) {
        logger.info("Updating leave request with ID: {}", id);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        // Can only update pending requests
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Cannot update leave request with status: " + leaveRequest.getStatus());
        }

        // Validate dates
        validateDates(requestDTO.getStartDate(), requestDTO.getEndDate());

        // Check for overlapping leaves (excluding current)
        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlappingLeavesExcluding(
                leaveRequest.getEmployee().getId(), requestDTO.getStartDate(), requestDTO.getEndDate(), id);
        
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Leave request overlaps with existing leave");
        }

        // Update fields
        leaveRequest.setLeaveType(requestDTO.getLeaveType());
        leaveRequest.setStartDate(requestDTO.getStartDate());
        leaveRequest.setEndDate(requestDTO.getEndDate());
        leaveRequest.setReason(requestDTO.getReason());
        leaveRequest.setIsHalfDay(requestDTO.getIsHalfDay());
        leaveRequest.setHalfDaySession(requestDTO.getHalfDaySession());

        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        logger.info("Leave request updated: {}", updatedRequest.getId());

        return mapToResponseDTO(updatedRequest);
    }

    // ==========================================================================
    // READ
    // ==========================================================================

    @Override
    @Transactional(readOnly = true)
    public LeaveResponseDTO getLeaveRequestById(Long id) {
        logger.debug("Fetching leave request by ID: {}", id);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        return mapToResponseDTO(leaveRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<LeaveResponseDTO> getAllLeaveRequests(int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching all leave requests - page: {}, size: {}", page, size);

        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<LeaveRequest> requestPage = leaveRequestRepository.findAll(pageable);

        return mapToPagedResponse(requestPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<LeaveResponseDTO> getLeaveRequestsByEmployee(Long employeeId, int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching leave requests for employee: {}", employeeId);

        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<LeaveRequest> requestPage = leaveRequestRepository.findByEmployeeId(employeeId, pageable);

        return mapToPagedResponse(requestPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<LeaveResponseDTO> getLeaveRequestsByStatus(LeaveStatus status, int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching leave requests by status: {}", status);

        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<LeaveRequest> requestPage = leaveRequestRepository.findByStatus(status, pageable);

        return mapToPagedResponse(requestPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<LeaveResponseDTO> getPendingLeaveRequests(int page, int size, String sortBy, String sortDir) {
        return getLeaveRequestsByStatus(LeaveStatus.PENDING, page, size, sortBy, sortDir);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getLeaveRequestsByDateRange(LocalDate startDate, LocalDate endDate) {
        logger.debug("Fetching leave requests from {} to {}", startDate, endDate);

        return leaveRequestRepository.findByDateRange(startDate, endDate)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponseDTO> getEmployeeLeavesByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Fetching leave requests for employee {} from {} to {}", employeeId, startDate, endDate);

        return leaveRequestRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==========================================================================
    // DELETE
    // ==========================================================================

    @Override
    public void deleteLeaveRequest(Long id) {
        logger.info("Deleting leave request with ID: {}", id);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        // Only pending requests can be deleted
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Cannot delete leave request with status: " + leaveRequest.getStatus());
        }

        leaveRequestRepository.delete(leaveRequest);
        logger.info("Leave request deleted: {}", id);
    }

    // ==========================================================================
    // APPROVAL WORKFLOW
    // ==========================================================================

    @Override
    public LeaveResponseDTO approveLeave(Long id, LeaveActionDTO actionDTO, Long approverId) {
        logger.info("Approving leave request {} by approver {}", id, approverId);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Cannot approve leave request with status: " + leaveRequest.getStatus());
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", approverId));

        leaveRequest.setStatus(LeaveStatus.APPROVED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setActionDate(LocalDate.now());
        leaveRequest.setApproverComments(actionDTO != null ? actionDTO.getComments() : null);

        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        logger.info("Leave request approved: {}", id);

        return mapToResponseDTO(updatedRequest);
    }

    @Override
    public LeaveResponseDTO rejectLeave(Long id, LeaveActionDTO actionDTO, Long approverId) {
        logger.info("Rejecting leave request {} by approver {}", id, approverId);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Cannot reject leave request with status: " + leaveRequest.getStatus());
        }

        Employee approver = employeeRepository.findById(approverId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", approverId));

        leaveRequest.setStatus(LeaveStatus.REJECTED);
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setActionDate(LocalDate.now());
        leaveRequest.setApproverComments(actionDTO != null ? actionDTO.getComments() : null);

        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        logger.info("Leave request rejected: {}", id);

        return mapToResponseDTO(updatedRequest);
    }

    @Override
    public LeaveResponseDTO cancelLeave(Long id) {
        logger.info("Cancelling leave request: {}", id);

        LeaveRequest leaveRequest = leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LeaveRequest", "id", id));

        if (!leaveRequest.isCancellable()) {
            throw new BadRequestException("Cannot cancel leave request with status: " + leaveRequest.getStatus());
        }

        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        leaveRequest.setActionDate(LocalDate.now());

        LeaveRequest updatedRequest = leaveRequestRepository.save(leaveRequest);
        logger.info("Leave request cancelled: {}", id);

        return mapToResponseDTO(updatedRequest);
    }

    // ==========================================================================
    // STATISTICS
    // ==========================================================================

    @Override
    @Transactional(readOnly = true)
    public Integer getUsedLeaveDays(Long employeeId, int year) {
        Integer totalDays = 0;
        for (LeaveType type : LeaveType.values()) {
            Integer days = leaveRequestRepository.sumApprovedLeaveDaysByTypeAndYear(employeeId, type, year);
            if (days != null) {
                totalDays += days;
            }
        }
        return totalDays;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getPendingLeaveCount() {
        return leaveRequestRepository.countByStatus(LeaveStatus.PENDING);
    }

    // ==========================================================================
    // HELPER METHODS
    // ==========================================================================

    private void validateDates(LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Start date cannot be after end date");
        }
        if (startDate.isBefore(LocalDate.now())) {
            throw new BadRequestException("Start date cannot be in the past");
        }
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    private LeaveResponseDTO mapToResponseDTO(LeaveRequest leaveRequest) {
        LeaveResponseDTO.LeaveResponseDTOBuilder builder = LeaveResponseDTO.builder()
                .id(leaveRequest.getId())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .totalDays(leaveRequest.getTotalDays())
                .reason(leaveRequest.getReason())
                .status(leaveRequest.getStatus())
                .isHalfDay(leaveRequest.getIsHalfDay())
                .halfDaySession(leaveRequest.getHalfDaySession())
                .actionDate(leaveRequest.getActionDate())
                .approverComments(leaveRequest.getApproverComments())
                .createdAt(leaveRequest.getCreatedAt())
                .updatedAt(leaveRequest.getUpdatedAt())
                .createdBy(leaveRequest.getCreatedBy())
                .updatedBy(leaveRequest.getUpdatedBy());

        // Add employee info
        if (leaveRequest.getEmployee() != null) {
            Employee emp = leaveRequest.getEmployee();
            builder.employeeId(emp.getId())
                   .employeeName(emp.getFullName())
                   .employeeCode(emp.getEmployeeCode())
                   .employeeDepartment(emp.getDepartment());
        }

        // Add approver info
        if (leaveRequest.getApprovedBy() != null) {
            builder.approvedById(leaveRequest.getApprovedBy().getId())
                   .approvedByName(leaveRequest.getApprovedBy().getFullName());
        }

        return builder.build();
    }

    private PagedResponseDTO<LeaveResponseDTO> mapToPagedResponse(Page<LeaveRequest> page) {
        List<LeaveResponseDTO> content = page.getContent()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PagedResponseDTO.<LeaveResponseDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}

package com.ems.service.impl;

import com.ems.dto.AttendanceRequestDTO;
import com.ems.dto.AttendanceResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.entity.Attendance;
import com.ems.entity.AttendanceStatus;
import com.ems.entity.Employee;
import com.ems.exception.BadRequestException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.AttendanceRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.service.AttendanceService;
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
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * ATTENDANCE SERVICE IMPLEMENTATION
 * =============================================================================
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceServiceImpl implements AttendanceService {

    private static final Logger logger = LoggerFactory.getLogger(AttendanceServiceImpl.class);
    
    // Standard office hours (configurable)
    private static final LocalTime STANDARD_CHECK_IN = LocalTime.of(9, 0);
    private static final LocalTime STANDARD_CHECK_OUT = LocalTime.of(18, 0);

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    // ==========================================================================
    // RECORD ATTENDANCE
    // ==========================================================================

    @Override
    public AttendanceResponseDTO recordAttendance(AttendanceRequestDTO requestDTO) {
        logger.info("Recording attendance for employee {} on {}", 
                    requestDTO.getEmployeeId(), requestDTO.getAttendanceDate());

        Employee employee = employeeRepository.findById(requestDTO.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", requestDTO.getEmployeeId()));

        // Check if attendance already exists for this date
        if (attendanceRepository.existsByEmployeeIdAndAttendanceDate(
                requestDTO.getEmployeeId(), requestDTO.getAttendanceDate())) {
            throw new BadRequestException("Attendance already recorded for " + 
                    employee.getFullName() + " on " + requestDTO.getAttendanceDate());
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .attendanceDate(requestDTO.getAttendanceDate())
                .checkInTime(requestDTO.getCheckInTime())
                .checkOutTime(requestDTO.getCheckOutTime())
                .status(requestDTO.getStatus() != null ? requestDTO.getStatus() : AttendanceStatus.PRESENT)
                .remarks(requestDTO.getRemarks())
                .checkInIp(requestDTO.getCheckInIp())
                .checkOutIp(requestDTO.getCheckOutIp())
                .checkInLocation(requestDTO.getCheckInLocation())
                .checkOutLocation(requestDTO.getCheckOutLocation())
                .isLate(requestDTO.getCheckInTime() != null && 
                        requestDTO.getCheckInTime().isAfter(STANDARD_CHECK_IN))
                .isEarlyCheckout(requestDTO.getCheckOutTime() != null && 
                        requestDTO.getCheckOutTime().isBefore(STANDARD_CHECK_OUT))
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        logger.info("Attendance recorded with ID: {}", savedAttendance.getId());

        return mapToResponseDTO(savedAttendance);
    }

    // ==========================================================================
    // UPDATE ATTENDANCE
    // ==========================================================================

    @Override
    public AttendanceResponseDTO updateAttendance(Long id, AttendanceRequestDTO requestDTO) {
        logger.info("Updating attendance with ID: {}", id);

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        if (requestDTO.getCheckInTime() != null) {
            attendance.setCheckInTime(requestDTO.getCheckInTime());
            attendance.setIsLate(requestDTO.getCheckInTime().isAfter(STANDARD_CHECK_IN));
        }
        if (requestDTO.getCheckOutTime() != null) {
            attendance.setCheckOutTime(requestDTO.getCheckOutTime());
            attendance.setIsEarlyCheckout(requestDTO.getCheckOutTime().isBefore(STANDARD_CHECK_OUT));
        }
        if (requestDTO.getStatus() != null) {
            attendance.setStatus(requestDTO.getStatus());
        }
        if (requestDTO.getRemarks() != null) {
            attendance.setRemarks(requestDTO.getRemarks());
        }

        Attendance updatedAttendance = attendanceRepository.save(attendance);
        logger.info("Attendance updated: {}", updatedAttendance.getId());

        return mapToResponseDTO(updatedAttendance);
    }

    // ==========================================================================
    // CHECK-IN / CHECK-OUT
    // ==========================================================================

    @Override
    public AttendanceResponseDTO checkIn(Long employeeId, String ip, String location) {
        logger.info("Employee {} checking in", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        // Check if already checked in today
        if (attendanceRepository.existsByEmployeeIdAndAttendanceDate(employeeId, today)) {
            throw new BadRequestException("Already checked in for today");
        }

        Attendance attendance = Attendance.builder()
                .employee(employee)
                .attendanceDate(today)
                .checkInTime(now)
                .status(AttendanceStatus.PRESENT)
                .checkInIp(ip)
                .checkInLocation(location)
                .isLate(now.isAfter(STANDARD_CHECK_IN))
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);
        logger.info("Check-in recorded for employee: {}", employee.getFullName());

        return mapToResponseDTO(savedAttendance);
    }

    @Override
    public AttendanceResponseDTO checkOut(Long employeeId, String ip, String location) {
        logger.info("Employee {} checking out", employeeId);

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new BadRequestException("No check-in record found for today"));

        if (attendance.hasCheckedOut()) {
            throw new BadRequestException("Already checked out for today");
        }

        attendance.setCheckOutTime(now);
        attendance.setCheckOutIp(ip);
        attendance.setCheckOutLocation(location);
        attendance.setIsEarlyCheckout(now.isBefore(STANDARD_CHECK_OUT));

        Attendance updatedAttendance = attendanceRepository.save(attendance);
        logger.info("Check-out recorded for employee: {}", attendance.getEmployeeName());

        return mapToResponseDTO(updatedAttendance);
    }

    // ==========================================================================
    // READ OPERATIONS
    // ==========================================================================

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponseDTO getAttendanceById(Long id) {
        logger.debug("Fetching attendance by ID: {}", id);

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        return mapToResponseDTO(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponseDTO getAttendanceByEmployeeAndDate(Long employeeId, LocalDate date) {
        logger.debug("Fetching attendance for employee {} on {}", employeeId, date);

        Attendance attendance = attendanceRepository.findByEmployeeIdAndAttendanceDate(employeeId, date)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "employee/date", 
                        employeeId + "/" + date));

        return mapToResponseDTO(attendance);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<AttendanceResponseDTO> getAllAttendance(int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching all attendance - page: {}, size: {}", page, size);

        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Attendance> attendancePage = attendanceRepository.findAll(pageable);

        return mapToPagedResponse(attendancePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<AttendanceResponseDTO> getAttendanceByEmployee(Long employeeId, int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching attendance for employee: {}", employeeId);

        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Attendance> attendancePage = attendanceRepository.findByEmployeeId(employeeId, pageable);

        return mapToPagedResponse(attendancePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<AttendanceResponseDTO> getAttendanceByDate(LocalDate date, int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching attendance for date: {}", date);

        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Attendance> attendancePage = attendanceRepository.findByAttendanceDate(date, pageable);

        return mapToPagedResponse(attendancePage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<AttendanceResponseDTO> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate, int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching attendance from {} to {}", startDate, endDate);

        Pageable pageable = createPageable(page, size, sortBy, sortDir);
        Page<Attendance> attendancePage = attendanceRepository.findByDateRange(startDate, endDate, pageable);

        return mapToPagedResponse(attendancePage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponseDTO> getEmployeeAttendanceByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate) {
        logger.debug("Fetching attendance for employee {} from {} to {}", employeeId, startDate, endDate);

        return attendanceRepository.findByEmployeeIdAndDateRange(employeeId, startDate, endDate)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==========================================================================
    // DELETE
    // ==========================================================================

    @Override
    public void deleteAttendance(Long id) {
        logger.info("Deleting attendance with ID: {}", id);

        Attendance attendance = attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));

        attendanceRepository.delete(attendance);
        logger.info("Attendance deleted: {}", id);
    }

    // ==========================================================================
    // STATISTICS
    // ==========================================================================

    @Override
    @Transactional(readOnly = true)
    public Long getPresentCount(LocalDate date) {
        return attendanceRepository.countByAttendanceDateAndStatus(date, AttendanceStatus.PRESENT);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getAbsentCount(LocalDate date) {
        return attendanceRepository.countByAttendanceDateAndStatus(date, AttendanceStatus.ABSENT);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalWorkingMinutes(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Long minutes = attendanceRepository.sumWorkingMinutesByEmployeeAndDateRange(employeeId, startDate, endDate);
        return minutes != null ? minutes : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getTotalOvertimeMinutes(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Long minutes = attendanceRepository.sumOvertimeMinutesByEmployeeAndDateRange(employeeId, startDate, endDate);
        return minutes != null ? minutes : 0L;
    }

    @Override
    @Transactional(readOnly = true)
    public Long getLateCount(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Long count = attendanceRepository.countByEmployeeIdAndIsLateTrueAndAttendanceDateBetween(
                employeeId, startDate, endDate);
        return count != null ? count : 0L;
    }

    // ==========================================================================
    // HELPER METHODS
    // ==========================================================================

    private Pageable createPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    private AttendanceResponseDTO mapToResponseDTO(Attendance attendance) {
        AttendanceResponseDTO.AttendanceResponseDTOBuilder builder = AttendanceResponseDTO.builder()
                .id(attendance.getId())
                .attendanceDate(attendance.getAttendanceDate())
                .checkInTime(attendance.getCheckInTime())
                .checkOutTime(attendance.getCheckOutTime())
                .status(attendance.getStatus())
                .workingHoursMinutes(attendance.getWorkingHoursMinutes())
                .formattedWorkingHours(attendance.getFormattedWorkingHours())
                .overtimeMinutes(attendance.getOvertimeMinutes())
                .formattedOvertime(attendance.getFormattedOvertime())
                .isLate(attendance.getIsLate())
                .isEarlyCheckout(attendance.getIsEarlyCheckout())
                .checkInIp(attendance.getCheckInIp())
                .checkOutIp(attendance.getCheckOutIp())
                .checkInLocation(attendance.getCheckInLocation())
                .checkOutLocation(attendance.getCheckOutLocation())
                .remarks(attendance.getRemarks())
                .createdAt(attendance.getCreatedAt())
                .updatedAt(attendance.getUpdatedAt())
                .createdBy(attendance.getCreatedBy())
                .updatedBy(attendance.getUpdatedBy());

        // Add employee info
        if (attendance.getEmployee() != null) {
            Employee emp = attendance.getEmployee();
            builder.employeeId(emp.getId())
                   .employeeName(emp.getFullName())
                   .employeeCode(emp.getEmployeeCode())
                   .employeeDepartment(emp.getDepartment());
        }

        return builder.build();
    }

    private PagedResponseDTO<AttendanceResponseDTO> mapToPagedResponse(Page<Attendance> page) {
        List<AttendanceResponseDTO> content = page.getContent()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PagedResponseDTO.<AttendanceResponseDTO>builder()
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

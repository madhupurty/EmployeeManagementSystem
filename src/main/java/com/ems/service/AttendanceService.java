package com.ems.service;

import com.ems.dto.AttendanceRequestDTO;
import com.ems.dto.AttendanceResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.entity.AttendanceStatus;

import java.time.LocalDate;
import java.util.List;

/**
 * =============================================================================
 * ATTENDANCE SERVICE INTERFACE
 * =============================================================================
 */
public interface AttendanceService {

    // CRUD Operations
    AttendanceResponseDTO recordAttendance(AttendanceRequestDTO requestDTO);

    AttendanceResponseDTO updateAttendance(Long id, AttendanceRequestDTO requestDTO);

    AttendanceResponseDTO getAttendanceById(Long id);

    void deleteAttendance(Long id);

    // Check-in / Check-out
    AttendanceResponseDTO checkIn(Long employeeId, String ip, String location);

    AttendanceResponseDTO checkOut(Long employeeId, String ip, String location);

    // Query Operations
    AttendanceResponseDTO getAttendanceByEmployeeAndDate(Long employeeId, LocalDate date);

    PagedResponseDTO<AttendanceResponseDTO> getAllAttendance(int page, int size, String sortBy, String sortDir);

    PagedResponseDTO<AttendanceResponseDTO> getAttendanceByEmployee(Long employeeId, int page, int size, String sortBy, String sortDir);

    PagedResponseDTO<AttendanceResponseDTO> getAttendanceByDate(LocalDate date, int page, int size, String sortBy, String sortDir);

    PagedResponseDTO<AttendanceResponseDTO> getAttendanceByDateRange(LocalDate startDate, LocalDate endDate, int page, int size, String sortBy, String sortDir);

    List<AttendanceResponseDTO> getEmployeeAttendanceByDateRange(Long employeeId, LocalDate startDate, LocalDate endDate);

    // Statistics
    Long getPresentCount(LocalDate date);

    Long getAbsentCount(LocalDate date);

    Long getTotalWorkingMinutes(Long employeeId, LocalDate startDate, LocalDate endDate);

    Long getTotalOvertimeMinutes(Long employeeId, LocalDate startDate, LocalDate endDate);

    Long getLateCount(Long employeeId, LocalDate startDate, LocalDate endDate);
}

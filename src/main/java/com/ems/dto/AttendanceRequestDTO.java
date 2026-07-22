package com.ems.dto;

import com.ems.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * =============================================================================
 * ATTENDANCE REQUEST DTO - Input for Recording Attendance
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRequestDTO {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Attendance date is required")
    private LocalDate attendanceDate;

    private LocalTime checkInTime;

    private LocalTime checkOutTime;

    private AttendanceStatus status;

    private String remarks;

    private String checkInIp;

    private String checkOutIp;

    private String checkInLocation;

    private String checkOutLocation;
}

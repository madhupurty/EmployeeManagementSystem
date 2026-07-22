package com.ems.dto;

import com.ems.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * =============================================================================
 * ATTENDANCE RESPONSE DTO - Output for Attendance Data
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponseDTO {

    private Long id;
    
    // Employee info
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String employeeDepartment;
    
    // Attendance details
    private LocalDate attendanceDate;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private AttendanceStatus status;
    
    // Working hours
    private Integer workingHoursMinutes;
    private String formattedWorkingHours;
    private Integer overtimeMinutes;
    private String formattedOvertime;
    
    // Flags
    private Boolean isLate;
    private Boolean isEarlyCheckout;
    
    // Location info
    private String checkInIp;
    private String checkOutIp;
    private String checkInLocation;
    private String checkOutLocation;
    
    private String remarks;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}

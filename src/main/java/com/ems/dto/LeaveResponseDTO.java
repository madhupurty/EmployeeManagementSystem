package com.ems.dto;

import com.ems.entity.LeaveStatus;
import com.ems.entity.LeaveType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * LEAVE RESPONSE DTO - Output for Leave Request Data
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveResponseDTO {

    private Long id;
    
    // Employee info
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private String employeeDepartment;
    
    // Leave details
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private LeaveStatus status;
    
    // Half day info
    private Boolean isHalfDay;
    private String halfDaySession;
    
    // Approval info
    private Long approvedById;
    private String approvedByName;
    private LocalDate actionDate;
    private String approverComments;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}

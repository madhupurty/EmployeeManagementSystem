package com.ems.dto;

import com.ems.entity.LeaveType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * =============================================================================
 * LEAVE REQUEST DTO - Input for Creating/Updating Leave Requests
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDTO {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotNull(message = "Start date is required")
    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @FutureOrPresent(message = "End date must be today or in the future")
    private LocalDate endDate;

    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    @Builder.Default
    private Boolean isHalfDay = false;

    private String halfDaySession; // FIRST_HALF or SECOND_HALF
}

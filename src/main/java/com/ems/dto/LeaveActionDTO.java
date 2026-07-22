package com.ems.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =============================================================================
 * LEAVE ACTION DTO - Input for Approving/Rejecting Leave Requests
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveActionDTO {

    @Size(max = 500, message = "Comments cannot exceed 500 characters")
    private String comments;
}

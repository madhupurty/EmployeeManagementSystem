package com.ems.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * =============================================================================
 * DEPARTMENT RESPONSE DTO - Output for Department Data
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponseDTO {

    private Long id;
    private String code;
    private String name;
    private String description;
    
    // Manager information (flattened for response)
    private Long managerId;
    private String managerName;
    private String managerEmail;
    
    private String location;
    private String contactEmail;
    private String contactPhone;
    private Double budget;
    private Boolean active;
    
    // Statistics
    private Integer employeeCount;
    
    // Audit fields
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
}

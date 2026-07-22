package com.ems.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =============================================================================
 * DEPARTMENT REQUEST DTO - Input for Creating/Updating Departments
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequestDTO {

    @NotBlank(message = "Department code is required")
    @Size(min = 2, max = 20, message = "Department code must be between 2 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Department code must be uppercase letters, numbers, or underscores")
    private String code;

    @NotBlank(message = "Department name is required")
    @Size(min = 2, max = 100, message = "Department name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    /**
     * Manager ID - optional, can be set later
     */
    private Long managerId;

    @Size(max = 200, message = "Location cannot exceed 200 characters")
    private String location;

    @Email(message = "Contact email must be a valid email address")
    @Size(max = 100, message = "Contact email cannot exceed 100 characters")
    private String contactEmail;

    @Size(max = 20, message = "Contact phone cannot exceed 20 characters")
    private String contactPhone;

    private Double budget;

    @Builder.Default
    private Boolean active = true;
}

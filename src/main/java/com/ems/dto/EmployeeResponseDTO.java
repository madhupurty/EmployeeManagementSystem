package com.ems.dto;

import com.ems.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * =============================================================================
 * EMPLOYEE RESPONSE DTO - Data Transfer Object for Output
 * =============================================================================
 * 
 * WHY SEPARATE REQUEST AND RESPONSE DTOs?
 * ----------------------------------------
 * 1. REQUEST DTO: Contains fields client CAN send (input validation)
 * 2. RESPONSE DTO: Contains fields client SHOULD receive (output format)
 * 
 * DIFFERENCES IN THIS PROJECT:
 * - Request: No id, createdAt, updatedAt (server-generated)
 * - Response: Includes id, createdAt, updatedAt, employeeCode
 * - Request: Has validation annotations
 * - Response: No validation needed (data already valid from DB)
 * 
 * INTERVIEW QUESTION: Why not use same DTO for request and response?
 * ANSWER:
 *   1. Different validation needs (request needs @NotBlank, response doesn't)
 *   2. Different fields (response has server-generated fields like id, timestamps)
 *   3. Clearer API documentation (OpenAPI/Swagger shows exact request/response)
 *   4. Flexibility to change input/output independently
 * 
 * =============================================================================
 * MAPPING STRATEGIES - Entity to DTO Conversion
 * =============================================================================
 * 
 * 1. MANUAL MAPPING (used in this phase)
 *    - Write conversion methods manually
 *    - Full control, no dependencies
 *    - More code to maintain
 * 
 * 2. MAPSTRUCT (recommended for large projects)
 *    - Generates mapping code at compile time
 *    - Zero runtime overhead
 *    - Reduces boilerplate
 * 
 * 3. MODELMAPPER (runtime reflection)
 *    - Maps by convention (matching field names)
 *    - Easy setup, but slower
 *    - Less type-safe
 * 
 * We'll use manual mapping in Phase 2 for learning, then introduce
 * MapStruct in Phase 4 for the Department module.
 * 
 * =============================================================================
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponseDTO {

    /*
     * Fields that are server-generated (not in request)
     */
    private Long id;
    private String employeeCode;

    /*
     * Basic employee information
     */
    private String firstName;
    private String lastName;
    private String fullName;  // Computed field: firstName + lastName
    private String email;
    private String phoneNumber;

    /*
     * Job-related fields
     */
    private String department;
    private String designation;
    private BigDecimal salary;

    /*
     * Dates
     */
    private LocalDate dateOfBirth;
    private LocalDate dateOfJoining;

    /*
     * Address fields
     */
    private String address;
    private String city;
    private String state;
    private String country;
    private String zipCode;

    /*
     * Status and relationships
     */
    private String status;
    private Long managerId;

    /*
     * Audit fields - important for tracking changes
     */
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // ==========================================================================
    // STATIC FACTORY METHOD - Converts Entity to DTO
    // ==========================================================================

    /*
     * WHY STATIC FACTORY METHOD?
     * --------------------------
     * 1. Encapsulates conversion logic in one place
     * 2. Can be called without creating DTO instance first
     * 3. Clear naming (fromEntity describes what it does)
     * 4. Easy to maintain when entity structure changes
     * 
     * ALTERNATIVE: Use a separate Mapper class or MapStruct
     * 
     * INTERVIEW QUESTION: Where should entity-to-DTO conversion happen?
     * ANSWER: In the Service layer, NOT in Controller or Repository.
     *         Service layer handles business logic including transformations.
     *         Controller should only receive/return DTOs.
     *         Repository should only work with Entities.
     */
    public static EmployeeResponseDTO fromEntity(Employee employee) {
        if (employee == null) {
            return null;
        }

        return EmployeeResponseDTO.builder()
                .id(employee.getId())
                .employeeCode(employee.getEmployeeCode())
                .firstName(employee.getFirstName())
                .lastName(employee.getLastName())
                .fullName(employee.getFullName())  // Using entity's computed method
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .department(employee.getDepartment())
                .designation(employee.getDesignation())
                .salary(employee.getSalary())
                .dateOfBirth(employee.getDateOfBirth())
                .dateOfJoining(employee.getDateOfJoining())
                .address(employee.getAddress())
                .city(employee.getCity())
                .state(employee.getState())
                .country(employee.getCountry())
                .zipCode(employee.getZipCode())
                .status(employee.getStatus() != null ? employee.getStatus().name() : null)
                .managerId(employee.getManagerId())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .createdBy(employee.getCreatedBy())
                .updatedBy(employee.getUpdatedBy())
                .build();
    }

    // ==========================================================================
    // STATIC FACTORY METHOD - Converts DTO to Entity
    // ==========================================================================

    /*
     * Converts EmployeeRequestDTO to Employee Entity
     * Used during CREATE operation
     * 
     * NOTE: id, employeeCode, createdAt, updatedAt are NOT set here
     * They are generated by the system (service layer or database)
     */
    public static Employee toEntity(EmployeeRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Employee.EmployeeStatus status = null;
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                status = Employee.EmployeeStatus.valueOf(dto.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status - will be handled by validation
                status = Employee.EmployeeStatus.ACTIVE; // Default
            }
        }

        return Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .department(dto.getDepartment())
                .designation(dto.getDesignation())
                .salary(dto.getSalary())
                .dateOfBirth(dto.getDateOfBirth())
                .dateOfJoining(dto.getDateOfJoining())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .zipCode(dto.getZipCode())
                .status(status != null ? status : Employee.EmployeeStatus.ACTIVE)
                .managerId(dto.getManagerId())
                .build();
    }

    /*
     * Updates existing entity from DTO
     * Used during UPDATE operation
     * 
     * WHY SEPARATE FROM toEntity?
     * 1. Update should preserve id, employeeCode, createdAt, createdBy
     * 2. Only modifiable fields should be updated
     * 3. Can implement partial update logic (only update non-null fields)
     */
    public static void updateEntity(Employee employee, EmployeeRequestDTO dto) {
        if (employee == null || dto == null) {
            return;
        }

        // Update only if value is provided (not null or blank)
        if (dto.getFirstName() != null && !dto.getFirstName().isBlank()) {
            employee.setFirstName(dto.getFirstName());
        }
        if (dto.getLastName() != null && !dto.getLastName().isBlank()) {
            employee.setLastName(dto.getLastName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            employee.setEmail(dto.getEmail());
        }
        if (dto.getPhoneNumber() != null) {
            employee.setPhoneNumber(dto.getPhoneNumber());
        }
        if (dto.getDepartment() != null) {
            employee.setDepartment(dto.getDepartment());
        }
        if (dto.getDesignation() != null) {
            employee.setDesignation(dto.getDesignation());
        }
        if (dto.getSalary() != null) {
            employee.setSalary(dto.getSalary());
        }
        if (dto.getDateOfBirth() != null) {
            employee.setDateOfBirth(dto.getDateOfBirth());
        }
        if (dto.getDateOfJoining() != null) {
            employee.setDateOfJoining(dto.getDateOfJoining());
        }
        if (dto.getAddress() != null) {
            employee.setAddress(dto.getAddress());
        }
        if (dto.getCity() != null) {
            employee.setCity(dto.getCity());
        }
        if (dto.getState() != null) {
            employee.setState(dto.getState());
        }
        if (dto.getCountry() != null) {
            employee.setCountry(dto.getCountry());
        }
        if (dto.getZipCode() != null) {
            employee.setZipCode(dto.getZipCode());
        }
        if (dto.getStatus() != null && !dto.getStatus().isBlank()) {
            try {
                employee.setStatus(Employee.EmployeeStatus.valueOf(dto.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                // Keep existing status if invalid value provided
            }
        }
        if (dto.getManagerId() != null) {
            employee.setManagerId(dto.getManagerId());
        }
    }
}

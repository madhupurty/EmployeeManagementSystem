package com.ems.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * =============================================================================
 * EMPLOYEE REQUEST DTO - Data Transfer Object for Input
 * =============================================================================
 * 
 * WHAT IS A DTO?
 * --------------
 * DTO (Data Transfer Object) is a design pattern used to transfer data between
 * layers of an application. It's a simple object that carries data.
 * 
 * WHY USE DTOs INSTEAD OF ENTITY DIRECTLY?
 * -----------------------------------------
 * 1. SECURITY: Entity may have sensitive fields (password, internal IDs)
 *    that should not be exposed to API consumers
 * 
 * 2. DECOUPLING: API contract is independent of database schema
 *    - You can change entity structure without breaking API
 *    - You can change API structure without modifying entity
 * 
 * 3. VALIDATION: Different validation rules for create vs update
 *    - Create: All fields required
 *    - Update: Only changed fields required
 * 
 * 4. PERFORMANCE: Transfer only needed fields (entity might have lazy-loaded
 *    relationships that cause N+1 queries if serialized directly)
 * 
 * 5. CLARITY: Clear separation between what client sends and what we store
 * 
 * INTERVIEW QUESTION: Why use DTO pattern in Spring Boot?
 * ANSWER: DTOs provide a clean separation between API layer and persistence
 *         layer. They improve security by controlling exposed data, enable
 *         different validation rules, prevent lazy loading issues, and make
 *         the API contract independent of database changes.
 * 
 * =============================================================================
 * VALIDATION ANNOTATIONS - Jakarta Bean Validation (formerly javax.validation)
 * =============================================================================
 * 
 * Spring Boot 3 uses Jakarta EE namespace (jakarta.validation.constraints.*)
 * Spring Boot 2 used javax.validation.constraints.*
 * 
 * VALIDATION FLOW:
 * 1. Request comes to Controller with @Valid or @Validated
 * 2. Spring triggers validation before method execution
 * 3. If validation fails, MethodArgumentNotValidException is thrown
 * 4. GlobalExceptionHandler catches and returns 400 Bad Request
 * 
 * =============================================================================
 */

/*
 * @Data - Lombok annotation that generates:
 *   - @Getter for all fields
 *   - @Setter for all non-final fields
 *   - @ToString
 *   - @EqualsAndHashCode
 *   - @RequiredArgsConstructor
 * 
 * NOTE: @Data is SAFE for DTOs (unlike Entities)
 * DTOs are simple data carriers, no JPA proxy issues.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeRequestDTO {

    /*
     * @NotBlank - Validates that field is:
     *   1. Not null
     *   2. Not empty ("")
     *   3. Not blank ("   ")
     * 
     * DIFFERENCE FROM @NotNull and @NotEmpty:
     * - @NotNull: Only checks null (allows "" and "   ")
     * - @NotEmpty: Checks null and empty (allows "   ")
     * - @NotBlank: Checks null, empty, AND blank - MOST STRICT
     * 
     * @Size - Validates string length
     *   min = 2, max = 50 - firstName must be between 2 and 50 characters
     * 
     * message - Custom error message returned when validation fails
     */
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    /*
     * @Email - Validates email format using regex
     * 
     * Default regex is basic. For stricter validation, use:
     * @Email(regexp = "^[A-Za-z0-9+_.-]+@(.+)$")
     * 
     * Combined with @NotBlank to ensure email is both present AND valid.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    /*
     * @Pattern - Validates against a regular expression
     * 
     * Phone number regex: ^[+]?[0-9]{10,15}$
     *   ^     - Start of string
     *   [+]?  - Optional + symbol
     *   [0-9] - Digits only
     *   {10,15} - Between 10 and 15 digits
     *   $     - End of string
     * 
     * NOTE: Not using @NotBlank because phone might be optional
     * The @Pattern will only validate if value is present
     */
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Please provide a valid phone number")
    private String phoneNumber;

    /*
     * Department field - optional in request
     * Will be validated against existing departments in Phase 4
     */
    @Size(max = 50, message = "Department must not exceed 50 characters")
    private String department;

    @Size(max = 100, message = "Designation must not exceed 100 characters")
    private String designation;

    /*
     * @DecimalMin / @DecimalMax - For BigDecimal validation
     * 
     * @Positive - Shorthand for "greater than 0"
     * @PositiveOrZero - Shorthand for "greater than or equal to 0"
     * 
     * @Digits - Controls precision and scale
     *   integer = 8 - Up to 8 digits before decimal
     *   fraction = 2 - Up to 2 decimal places
     * 
     * Example valid salaries: 50000.00, 150000.50, 99999999.99
     */
    @DecimalMin(value = "0.0", inclusive = false, message = "Salary must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Salary must have up to 8 digits and 2 decimal places")
    private BigDecimal salary;

    /*
     * @Past - Validates that date is in the past
     * @PastOrPresent - Allows today's date
     * @Future - Validates that date is in the future
     * @FutureOrPresent - Allows today's date
     * 
     * For date of birth, we need @Past (can't be born today or in future)
     */
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    /*
     * Date of joining can be past or present (joined today)
     */
    @PastOrPresent(message = "Date of joining cannot be in the future")
    private LocalDate dateOfJoining;

    /*
     * Address fields - all optional with size limits
     */
    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    @Size(max = 50, message = "City must not exceed 50 characters")
    private String city;

    @Size(max = 50, message = "State must not exceed 50 characters")
    private String state;

    @Size(max = 50, message = "Country must not exceed 50 characters")
    private String country;

    @Pattern(regexp = "^[0-9]{5,10}$", message = "Please provide a valid zip code")
    private String zipCode;

    /*
     * Status - optional on create (defaults to ACTIVE in Entity)
     * Must be a valid enum value if provided
     * 
     * VALIDATION NOTE: Enum validation happens automatically during
     * JSON deserialization. Invalid enum values cause HttpMessageNotReadableException
     * which we handle in GlobalExceptionHandler.
     */
    private String status;

    /*
     * Manager ID - optional reference to another employee
     * Will be validated to ensure manager exists in service layer
     */
    @Positive(message = "Manager ID must be a positive number")
    private Long managerId;
}

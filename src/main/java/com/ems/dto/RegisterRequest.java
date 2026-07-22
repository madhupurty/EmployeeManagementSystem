package com.ems.dto;

import com.ems.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =============================================================================
 * REGISTER REQUEST DTO - Data Transfer Object for User Registration
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * This DTO captures all data needed to register a new user.
 * It includes validation annotations to ensure data quality.
 * 
 * REGISTRATION FLOW:
 * ------------------
 * 1. Client sends this DTO
 * 2. Validation annotations check data quality
 * 3. Service checks if username/email already exists
 * 4. Password is hashed with BCrypt
 * 5. User is saved to database
 * 6. JWT token is generated and returned
 * 
 * REQUEST EXAMPLE:
 * ----------------
 * POST /api/auth/register
 * {
 *   "username": "john.doe",
 *   "email": "john.doe@company.com",
 *   "password": "SecurePass123!",
 *   "firstName": "John",
 *   "lastName": "Doe",
 *   "role": "EMPLOYEE"
 * }
 * 
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    /**
     * Username for login.
     * 
     * Constraints:
     * - Required (not blank)
     * - 3-50 characters
     * - Must be unique (checked in service)
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    /**
     * Email address.
     * 
     * Constraints:
     * - Required (not blank)
     * - Must be valid email format
     * - Must be unique (checked in service)
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    /**
     * Password (plain text - will be hashed before storing).
     * 
     * Constraints:
     * - Required (not blank)
     * - Minimum 6 characters
     * 
     * Note: In production, consider adding more complex password rules:
     *   - Must contain uppercase
     *   - Must contain number
     *   - Must contain special character
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /**
     * User's first name.
     */
    @NotBlank(message = "First name is required")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;

    /**
     * User's last name.
     */
    @NotBlank(message = "Last name is required")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;

    /**
     * User's role in the system.
     * 
     * Default: EMPLOYEE
     * 
     * Note: In a real system, you might want to restrict who can register
     * with ADMIN or HR roles. Only existing ADMINs should be able to
     * create new ADMIN/HR users.
     */
    @NotNull(message = "Role is required")
    private Role role;
}

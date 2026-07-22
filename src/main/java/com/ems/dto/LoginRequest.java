package com.ems.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =============================================================================
 * LOGIN REQUEST DTO - Data Transfer Object for Login
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * This DTO captures the data sent by clients when they want to log in.
 * It contains only the credentials needed for authentication.
 * 
 * WHY USE A DTO INSTEAD OF ENTITY?
 * ---------------------------------
 * 1. Security: Entity might have fields we don't want clients to set
 * 2. Validation: Can have different validation rules than entity
 * 3. Decoupling: API contract separate from database schema
 * 4. Flexibility: Can change API without changing database
 * 
 * REQUEST EXAMPLE:
 * ----------------
 * POST /api/auth/login
 * {
 *   "usernameOrEmail": "john.doe",
 *   "password": "myPassword123"
 * }
 * 
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * Username or email for login.
     * 
     * We accept both to make login more user-friendly.
     * Users can log in with either their username or email.
     */
    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;

    /**
     * User's password (plain text).
     * 
     * This will be compared against the BCrypt hashed password in database.
     * 
     * Note: Password is never stored or logged in plain text!
     */
    @NotBlank(message = "Password is required")
    private String password;
}

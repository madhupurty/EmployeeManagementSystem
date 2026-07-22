package com.ems.dto;

import com.ems.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * =============================================================================
 * AUTH RESPONSE DTO - Response After Authentication
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * This DTO is returned after successful login or registration.
 * It contains the JWT token and basic user information.
 * 
 * WHY RETURN USER INFO?
 * ---------------------
 * Frontend applications often need basic user info immediately after login:
 *   - Display username in navigation
 *   - Show role-specific UI elements
 *   - Store user info in local state
 * 
 * By returning this info with the token, we save an additional API call.
 * 
 * RESPONSE EXAMPLE:
 * -----------------
 * {
 *   "token": "eyJhbGciOiJIUzI1NiJ9...",
 *   "tokenType": "Bearer",
 *   "expiresIn": 86400000,
 *   "userId": 1,
 *   "username": "john.doe",
 *   "email": "john.doe@company.com",
 *   "fullName": "John Doe",
 *   "role": "EMPLOYEE"
 * }
 * 
 * CLIENT USAGE:
 * -------------
 * 1. Store token in localStorage or memory
 * 2. Include token in subsequent requests:
 *    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 * 3. Use user info for UI
 * 
 * =============================================================================
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)  // Don't include null fields in JSON
public class AuthResponse {

    /**
     * JWT access token.
     * 
     * Client must include this in Authorization header:
     *   Authorization: Bearer <token>
     */
    private String token;

    /**
     * Type of token - always "Bearer" for JWT.
     * 
     * OAuth2 standard - tells client how to use the token.
     */
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * Token expiration time in milliseconds.
     * 
     * Client can use this to:
     *   - Know when to refresh token
     *   - Auto-logout user when token expires
     */
    private Long expiresIn;

    /**
     * User's database ID.
     */
    private Long userId;

    /**
     * User's username.
     */
    private String username;

    /**
     * User's email.
     */
    private String email;

    /**
     * User's full name (firstName + lastName).
     */
    private String fullName;

    /**
     * User's role.
     * 
     * Client uses this for:
     *   - Showing/hiding UI elements based on permissions
     *   - Client-side route protection
     */
    private Role role;

    /**
     * Optional message (e.g., "Login successful").
     */
    private String message;
}

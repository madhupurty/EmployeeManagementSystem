package com.ems.service;

import com.ems.dto.AuthResponse;
import com.ems.dto.LoginRequest;
import com.ems.dto.RegisterRequest;

/**
 * =============================================================================
 * AUTHENTICATION SERVICE INTERFACE
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * Defines the contract for authentication operations.
 * Following the interface-implementation pattern for loose coupling.
 * 
 * OPERATIONS:
 * -----------
 * 1. Register: Create new user account
 * 2. Login: Authenticate user and return JWT
 * 
 * =============================================================================
 */
public interface AuthService {

    /**
     * Register a new user.
     * 
     * @param request Registration details (username, email, password, etc.)
     * @return AuthResponse with JWT token and user info
     * @throws BadRequestException if username or email already exists
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticate user and generate JWT token.
     * 
     * @param request Login credentials (username/email and password)
     * @return AuthResponse with JWT token and user info
     * @throws BadCredentialsException if credentials are invalid
     */
    AuthResponse login(LoginRequest request);
}

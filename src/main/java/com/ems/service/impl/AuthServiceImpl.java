package com.ems.service.impl;

import com.ems.dto.AuthResponse;
import com.ems.dto.LoginRequest;
import com.ems.dto.RegisterRequest;
import com.ems.entity.Role;
import com.ems.entity.User;
import com.ems.exception.BadRequestException;
import com.ems.repository.UserRepository;
import com.ems.security.JwtUtil;
import com.ems.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * =============================================================================
 * AUTHENTICATION SERVICE IMPLEMENTATION
 * =============================================================================
 * 
 * WHAT DOES THIS SERVICE DO?
 * --------------------------
 * Handles all authentication-related business logic:
 *   1. User registration (create account, hash password, generate token)
 *   2. User login (validate credentials, generate token)
 * 
 * AUTHENTICATION FLOW - LOGIN:
 * ----------------------------
 * 1. Client sends username/email and password
 * 2. AuthenticationManager validates credentials
 *    - Loads user via UserDetailsService
 *    - Compares password using PasswordEncoder
 * 3. If valid: Generate JWT token
 * 4. Return token and user info
 * 
 * AUTHENTICATION FLOW - REGISTER:
 * -------------------------------
 * 1. Client sends registration data
 * 2. Validate username/email uniqueness
 * 3. Hash password with BCrypt
 * 4. Save user to database
 * 5. Generate JWT token
 * 6. Return token and user info
 * 
 * INTERVIEW QUESTION: Why use AuthenticationManager instead of directly 
 * checking password?
 * ANSWER: AuthenticationManager:
 *   - Is Spring Security's standard authentication mechanism
 *   - Handles all authentication providers (DAO, LDAP, OAuth, etc.)
 *   - Fires authentication events (for logging, notifications)
 *   - Applies all security rules (account locked, disabled, etc.)
 * 
 * =============================================================================
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    // ==========================================================================
    // REGISTRATION
    // ==========================================================================

    /**
     * Register a new user.
     * 
     * FLOW:
     * 1. Check if username already exists
     * 2. Check if email already exists
     * 3. Create User entity with hashed password
     * 4. Save to database
     * 5. Generate JWT token
     * 6. Return AuthResponse
     * 
     * @param request Registration details
     * @return AuthResponse with JWT and user info
     */
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Processing registration request for username: {}", request.getUsername());

        // Validate username uniqueness
        if (userRepository.existsByUsername(request.getUsername())) {
            logger.warn("Registration failed - username already exists: {}", request.getUsername());
            throw new BadRequestException("Username '" + request.getUsername() + "' is already taken");
        }

        // Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("Registration failed - email already exists: {}", request.getEmail());
            throw new BadRequestException("Email '" + request.getEmail() + "' is already registered");
        }

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))  // Hash password!
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole() != null ? request.getRole() : Role.EMPLOYEE)  // Default role
                .enabled(true)
                .accountNonLocked(true)
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);
        logger.info("User registered successfully: {} with role: {}", 
                    savedUser.getUsername(), savedUser.getRole());

        // Generate JWT token
        String token = jwtUtil.generateToken(savedUser);

        // Build response
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .fullName(savedUser.getFullName())
                .role(savedUser.getRole())
                .message("Registration successful")
                .build();
    }

    // ==========================================================================
    // LOGIN
    // ==========================================================================

    /**
     * Authenticate user and generate JWT token.
     * 
     * FLOW:
     * 1. Create authentication token from credentials
     * 2. AuthenticationManager validates credentials
     *    - Calls UserDetailsService.loadUserByUsername()
     *    - Compares password using PasswordEncoder
     * 3. If successful: Get authenticated user
     * 4. Generate JWT token
     * 5. Return AuthResponse
     * 
     * @param request Login credentials
     * @return AuthResponse with JWT and user info
     */
    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        logger.info("Processing login request for: {}", request.getUsernameOrEmail());

        try {
            /*
             * Create authentication token with credentials.
             * This is NOT the JWT token - it's Spring Security's internal token
             * that holds the username and password for authentication.
             */
            UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    );

            /*
             * Authenticate using AuthenticationManager.
             * 
             * This will:
             * 1. Call UserDetailsService.loadUserByUsername()
             * 2. Compare submitted password with stored (hashed) password
             * 3. Check if account is enabled, not locked, etc.
             * 4. Throw AuthenticationException if anything fails
             * 5. Return Authentication object if successful
             */
            Authentication authentication = authenticationManager.authenticate(authToken);

            /*
             * Get the authenticated user.
             * The principal is the UserDetails object (our User entity).
             */
            User user = (User) authentication.getPrincipal();

            logger.info("User authenticated successfully: {}", user.getUsername());

            // Generate JWT token
            String token = jwtUtil.generateToken(user);

            // Build response
            return AuthResponse.builder()
                    .token(token)
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .fullName(user.getFullName())
                    .role(user.getRole())
                    .message("Login successful")
                    .build();

        } catch (AuthenticationException e) {
            /*
             * AuthenticationException is thrown when authentication fails.
             * Common causes:
             *   - BadCredentialsException: Wrong password
             *   - UsernameNotFoundException: User doesn't exist
             *   - DisabledException: Account disabled
             *   - LockedException: Account locked
             */
            logger.warn("Authentication failed for user: {} - Reason: {}", 
                        request.getUsernameOrEmail(), e.getMessage());
            
            throw new BadCredentialsException("Invalid username/email or password");
        }
    }
}

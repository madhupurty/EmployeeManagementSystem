package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.AuthResponse;
import com.ems.dto.LoginRequest;
import com.ems.dto.RegisterRequest;
import com.ems.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * =============================================================================
 * AUTHENTICATION CONTROLLER - Login and Registration Endpoints
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * Handles all authentication-related HTTP requests:
 *   - POST /api/auth/register - Create new user account
 *   - POST /api/auth/login - Authenticate and get JWT token
 * 
 * SECURITY NOTE:
 * --------------
 * These endpoints are PUBLIC (no authentication required).
 * This is configured in SecurityConfig with .requestMatchers("/api/auth/**").permitAll()
 * 
 * FLOW - REGISTRATION:
 * --------------------
 * 1. Client sends POST /api/auth/register with user details
 * 2. Server validates input
 * 3. Server creates user, hashes password
 * 4. Server generates JWT token
 * 5. Server returns token + user info
 * 
 * FLOW - LOGIN:
 * -------------
 * 1. Client sends POST /api/auth/login with credentials
 * 2. Server validates credentials
 * 3. Server generates JWT token
 * 4. Server returns token + user info
 * 5. Client stores token for future requests
 * 
 * CLIENT USAGE AFTER LOGIN:
 * -------------------------
 * For all subsequent requests, client must include:
 *   Header: Authorization: Bearer <token>
 * 
 * =============================================================================
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "User authentication and registration APIs")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    // ==========================================================================
    // REGISTER ENDPOINT
    // ==========================================================================

    /**
     * Register a new user.
     * 
     * Creates a new user account in the system.
     * Returns JWT token so user can immediately make authenticated requests.
     * 
     * @param request User registration details
     * @return AuthResponse with JWT token and user info
     * 
     * HTTP Response Codes:
     *   201 Created - User registered successfully
     *   400 Bad Request - Validation error or username/email exists
     */
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account and returns a JWT token for immediate authentication"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation error or username/email already exists"
        )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        logger.info("Received registration request for username: {}", request.getUsername());

        AuthResponse authResponse = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("User registered successfully")
                        .data(authResponse)
                        .build());
    }

    // ==========================================================================
    // LOGIN ENDPOINT
    // ==========================================================================

    /**
     * Authenticate user and return JWT token.
     * 
     * Validates user credentials and returns a JWT token for authentication.
     * User can provide either username or email for login.
     * 
     * @param request Login credentials (username/email and password)
     * @return AuthResponse with JWT token and user info
     * 
     * HTTP Response Codes:
     *   200 OK - Login successful
     *   401 Unauthorized - Invalid credentials
     *   400 Bad Request - Validation error
     */
    @Operation(
        summary = "Authenticate user",
        description = "Validates credentials and returns a JWT token. User can login with username or email."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Login successful",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "Invalid username/email or password"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Validation error"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        logger.info("Received login request for: {}", request.getUsernameOrEmail());

        AuthResponse authResponse = authService.login(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.<AuthResponse>builder()
                        .success(true)
                        .message("Login successful")
                        .data(authResponse)
                        .build());
    }

    // ==========================================================================
    // VALIDATE TOKEN ENDPOINT (Optional)
    // ==========================================================================

    /**
     * Validate current JWT token.
     * 
     * This endpoint allows clients to check if their token is still valid.
     * Useful for refreshing UI or determining if re-login is needed.
     * 
     * Note: This endpoint IS protected - requires valid JWT.
     * If request succeeds, token is valid. If 401, token is invalid/expired.
     */
    @Operation(
        summary = "Validate JWT token",
        description = "Check if the current JWT token is valid. Returns 200 if valid, 401 if invalid."
    )
    @GetMapping("/validate")
    public ResponseEntity<ApiResponse<String>> validateToken() {
        return ResponseEntity.ok(
                ApiResponse.<String>builder()
                        .success(true)
                        .message("Token is valid")
                        .build()
        );
    }
}

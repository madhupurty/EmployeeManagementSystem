package com.ems.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * =============================================================================
 * JWT AUTHENTICATION FILTER - Validates JWT on Every Request
 * =============================================================================
 * 
 * WHAT IS A FILTER?
 * -----------------
 * A Filter intercepts HTTP requests BEFORE they reach the controller.
 * It's part of the Servlet API (not Spring-specific).
 * 
 * Request Flow: Client -> Filter1 -> Filter2 -> ... -> Controller
 * 
 * WHY EXTEND OncePerRequestFilter?
 * ---------------------------------
 * OncePerRequestFilter guarantees the filter runs EXACTLY ONCE per request.
 * Regular filters might run multiple times during a single request
 * (e.g., due to internal forwards/includes).
 * 
 * PURPOSE OF THIS FILTER:
 * -----------------------
 * This filter intercepts EVERY request and:
 * 1. Checks if Authorization header contains a JWT token
 * 2. Validates the token (not expired, valid signature)
 * 3. If valid: Sets authentication in SecurityContext
 * 4. If invalid: Doesn't set authentication (request treated as anonymous)
 * 
 * JWT AUTHENTICATION FLOW:
 * ------------------------
 * 1. Client sends request with header: "Authorization: Bearer <token>"
 * 2. This filter extracts the token from header
 * 3. Extracts username from token
 * 4. Loads user from database
 * 5. Validates token against user
 * 6. If valid: Creates Authentication object and sets in SecurityContext
 * 7. Request continues to next filter/controller
 * 8. Controller can access authenticated user via SecurityContext
 * 
 * INTERVIEW QUESTION: Why do we need to set SecurityContext?
 * ANSWER: SecurityContext is Spring Security's way of knowing WHO is making
 *         the current request. All authorization checks (hasRole, etc.) 
 *         look at the Authentication object in SecurityContext.
 *         If it's null, the user is considered anonymous.
 * 
 * =============================================================================
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    /*
     * Authorization header format: "Bearer <token>"
     * "Bearer " is the prefix we need to remove to get the actual token
     */
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    /**
     * Main filter method - processes every HTTP request.
     * 
     * @param request  HTTP request
     * @param response HTTP response
     * @param filterChain Chain of filters to execute
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Step 1: Extract JWT from Authorization header
            String jwt = extractJwtFromRequest(request);

            if (jwt == null) {
                // No token found - continue filter chain (will be treated as anonymous)
                logger.trace("No JWT token found in request to: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }

            logger.debug("JWT token found in request to: {}", request.getRequestURI());

            // Step 2: Extract username from token
            String username = jwtUtil.extractUsername(jwt);

            if (username == null) {
                logger.warn("Could not extract username from JWT token");
                filterChain.doFilter(request, response);
                return;
            }

            // Step 3: Check if user is already authenticated
            // If SecurityContext already has authentication, skip (already authenticated)
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Step 4: Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // Step 5: Validate token against user
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    
                    // Step 6: Token is valid - Set up Spring Security context
                    logger.debug("JWT token is valid for user: {}", username);
                    setAuthenticationInContext(userDetails, request);
                    
                } else {
                    logger.warn("JWT token validation failed for user: {}", username);
                }
            }

        } catch (Exception e) {
            // Don't let exceptions break the filter chain
            // Just log and continue (request will be treated as unauthenticated)
            logger.error("Error processing JWT authentication: {}", e.getMessage());
        }

        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from the Authorization header.
     * 
     * Expected format: "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...."
     * 
     * @param request HTTP request
     * @return JWT token string, or null if not found
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        /*
         * Check if header exists and starts with "Bearer "
         * 
         * StringUtils.hasText() checks:
         *   - Not null
         *   - Not empty
         *   - Not just whitespace
         */
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            // Remove "Bearer " prefix to get actual token
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }

    /**
     * Set authentication in Spring Security context.
     * 
     * This method creates an Authentication object and stores it in
     * SecurityContextHolder. After this, Spring Security knows WHO
     * is making the request and WHAT roles they have.
     * 
     * @param userDetails The authenticated user's details
     * @param request     The HTTP request (for additional details)
     */
    private void setAuthenticationInContext(UserDetails userDetails, HttpServletRequest request) {
        /*
         * UsernamePasswordAuthenticationToken: Spring Security's default
         * implementation of Authentication interface.
         * 
         * Parameters:
         * 1. principal: The authenticated user (userDetails)
         * 2. credentials: Password (null because we already verified via JWT)
         * 3. authorities: User's roles/permissions
         */
        UsernamePasswordAuthenticationToken authToken = 
                new UsernamePasswordAuthenticationToken(
                        userDetails,           // Principal - who is this?
                        null,                  // Credentials - not needed (JWT verified)
                        userDetails.getAuthorities()  // Authorities - what can they do?
                );

        /*
         * Add additional details from the request (IP address, session ID, etc.)
         * This is optional but useful for audit logging.
         */
        authToken.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        /*
         * Store authentication in SecurityContext.
         * 
         * SecurityContextHolder is ThreadLocal - each request thread has
         * its own SecurityContext. This is safe for concurrent requests.
         * 
         * After this line, anywhere in this request's processing:
         *   - @PreAuthorize annotations work
         *   - hasRole() checks work
         *   - SecurityContextHolder.getContext().getAuthentication() returns this user
         */
        SecurityContextHolder.getContext().setAuthentication(authToken);
        
        logger.debug("Set authentication in SecurityContext for user: {}", 
                     userDetails.getUsername());
    }

    /**
     * Determine if this filter should NOT be applied to a request.
     * 
     * We skip the filter for certain paths that don't need authentication:
     * - Login endpoint
     * - Registration endpoint
     * - Public endpoints
     * 
     * Note: This is a performance optimization. These endpoints are also
     * configured as permitAll() in SecurityConfig, but skipping the filter
     * entirely is more efficient.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        
        // Skip filter for authentication endpoints
        return path.startsWith("/api/auth/") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/api-docs") ||
               path.startsWith("/v3/api-docs");
    }
}

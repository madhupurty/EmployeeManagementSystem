package com.ems.config;

import com.ems.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * =============================================================================
 * SECURITY CONFIGURATION - Spring Security Setup
 * =============================================================================
 * 
 * WHAT IS SPRING SECURITY?
 * ------------------------
 * Spring Security is a powerful framework that handles:
 *   1. Authentication: Verifying user identity (login)
 *   2. Authorization: Controlling access to resources (permissions)
 *   3. Protection: Against common attacks (CSRF, XSS, etc.)
 * 
 * HOW DOES SPRING SECURITY WORK?
 * ------------------------------
 * Spring Security uses a chain of Filters that process every HTTP request.
 * 
 * Request -> Filter1 -> Filter2 -> ... -> FilterN -> Controller
 * 
 * Key filters in order:
 *   1. CorsFilter - Handle CORS preflight requests
 *   2. CsrfFilter - CSRF protection
 *   3. AuthenticationFilter - Process login requests
 *   4. AuthorizationFilter - Check if user has required permissions
 * 
 * OUR CONFIGURATION:
 * ------------------
 * We're configuring JWT-based authentication:
 *   - Stateless sessions (no session cookies)
 *   - Token-based auth (JWT in Authorization header)
 *   - Custom JWT filter to validate tokens
 *   - Role-based access control
 * 
 * INTERVIEW QUESTION: What is the difference between @EnableWebSecurity 
 * and @EnableMethodSecurity?
 * ANSWER:
 *   @EnableWebSecurity: Enables Spring Security's web security support
 *                       Configures URL-level security (/api/employees/**)
 *   @EnableMethodSecurity: Enables method-level security annotations
 *                          (@PreAuthorize, @PostAuthorize, @Secured)
 * 
 * =============================================================================
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // Enables @PreAuthorize, @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    // ==========================================================================
    // SECURITY FILTER CHAIN
    // ==========================================================================

    /**
     * Main security configuration - defines security rules.
     * 
     * This bean configures:
     *   - Which endpoints are public vs protected
     *   - How authentication works
     *   - Session management
     *   - CORS and CSRF settings
     * 
     * CONFIGURATION BREAKDOWN:
     * ------------------------
     * 1. CORS: Allow cross-origin requests (for frontend apps)
     * 2. CSRF: Disable for REST APIs (we use JWT instead)
     * 3. Session: Stateless (no server-side session, use JWT)
     * 4. Authorization: Configure public and protected endpoints
     * 5. Filter: Add our JWT filter to the chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            /*
             * CSRF CONFIGURATION
             * ------------------
             * CSRF (Cross-Site Request Forgery) protection is disabled.
             * 
             * WHY DISABLE CSRF?
             * For traditional web apps with cookies, CSRF protection is essential.
             * For REST APIs with JWT:
             *   - We use Authorization header, not cookies
             *   - Attackers can't steal the header like they can cookies
             *   - JWT itself provides authentication proof
             * 
             * INTERVIEW QUESTION: When should you NOT disable CSRF?
             * ANSWER: Never disable CSRF for web apps that use:
             *   - Cookie-based authentication
             *   - Session IDs in cookies
             *   - Forms that submit to your server
             */
            .csrf(AbstractHttpConfigurer::disable)
            
            /*
             * AUTHORIZATION RULES
             * -------------------
             * Define which endpoints require authentication and which are public.
             * 
             * Order matters! More specific rules should come before general rules.
             * Rules are evaluated top to bottom, first match wins.
             */
            .authorizeHttpRequests(auth -> auth
                // ========== PUBLIC ENDPOINTS (No authentication required) ==========
                
                // Authentication endpoints - anyone can login/register
                .requestMatchers("/api/auth/**").permitAll()
                
                // Actuator health endpoints for Docker health checks
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                
                // Swagger/OpenAPI documentation
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()
                
                // Health check endpoint (useful for load balancers)
                .requestMatchers("/actuator/health").permitAll()
                
                // ========== PROTECTED ENDPOINTS (Authentication required) ==========
                
                // Employee endpoints - role-based access
                // GET: Anyone authenticated can view employees
                .requestMatchers(HttpMethod.GET, "/api/employees/**")
                    .hasAnyRole("ADMIN", "HR", "EMPLOYEE")
                
                // POST: Only ADMIN and HR can create employees
                .requestMatchers(HttpMethod.POST, "/api/employees/**")
                    .hasAnyRole("ADMIN", "HR")
                
                // PUT/PATCH: Only ADMIN and HR can update employees
                .requestMatchers(HttpMethod.PUT, "/api/employees/**")
                    .hasAnyRole("ADMIN", "HR")
                .requestMatchers(HttpMethod.PATCH, "/api/employees/**")
                    .hasAnyRole("ADMIN", "HR")
                
                // DELETE: Only ADMIN can delete employees
                .requestMatchers(HttpMethod.DELETE, "/api/employees/**")
                    .hasRole("ADMIN")
                
                // User management - ADMIN only
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            
            /*
             * SESSION MANAGEMENT
             * ------------------
             * We use STATELESS sessions because:
             *   - JWT contains all needed info (self-contained)
             *   - Server doesn't need to store session data
             *   - Better scalability (no session replication needed)
             *   - Works well with load balancers
             * 
             * STATELESS means:
             *   - No HttpSession created
             *   - No session cookie (JSESSIONID) sent
             *   - Every request must include JWT
             */
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            /*
             * AUTHENTICATION PROVIDER
             * -----------------------
             * Tells Spring Security how to authenticate users.
             * Our DaoAuthenticationProvider:
             *   - Uses UserDetailsService to load users
             *   - Uses PasswordEncoder to verify passwords
             */
            .authenticationProvider(authenticationProvider())
            
            /*
             * JWT FILTER
             * ----------
             * Add our custom JWT filter BEFORE UsernamePasswordAuthenticationFilter.
             * 
             * Why before? Because:
             *   - JWT validation should happen early
             *   - If JWT is valid, we set authentication in SecurityContext
             *   - Subsequent filters will see the user as authenticated
             */
            .addFilterBefore(
                jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    // ==========================================================================
    // AUTHENTICATION BEANS
    // ==========================================================================

    /**
     * Password Encoder - BCrypt implementation.
     * 
     * BCrypt is a password hashing function that:
     *   - Uses salt to protect against rainbow tables
     *   - Is intentionally slow (protects against brute force)
     *   - Has configurable strength (work factor)
     * 
     * INTERVIEW QUESTION: Why not use MD5 or SHA for passwords?
     * ANSWER: MD5/SHA are designed to be FAST (for file checksums, etc.)
     *         Fast = bad for passwords (brute force is easy)
     *         BCrypt is intentionally SLOW and includes salt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Provider - How to authenticate users.
     * 
     * DaoAuthenticationProvider:
     *   - DAO = Data Access Object
     *   - Loads user via UserDetailsService (from database)
     *   - Compares password using PasswordEncoder
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Authentication Manager - Used to authenticate users programmatically.
     * 
     * We inject this in our AuthController to authenticate login requests:
     *   authManager.authenticate(usernamePasswordToken)
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    // ==========================================================================
    // CORS CONFIGURATION
    // ==========================================================================

    /**
     * CORS Configuration Source - Cross-Origin Resource Sharing.
     * 
     * CORS is a browser security feature that:
     *   - Blocks requests from different origins by default
     *   - Requires server to explicitly allow cross-origin requests
     * 
     * Example:
     *   Frontend: http://localhost:3000 (React app)
     *   Backend: http://localhost:8080 (Spring Boot)
     *   Browser blocks requests from 3000 to 8080 unless CORS allows it.
     * 
     * CONFIGURATION:
     *   - allowedOrigins: Which domains can make requests
     *   - allowedMethods: Which HTTP methods are allowed
     *   - allowedHeaders: Which headers can be sent
     *   - allowCredentials: Allow cookies/auth headers
     * 
     * WARNING: In production, replace "*" with specific origins!
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow requests from any origin (restrict in production!)
        configuration.setAllowedOrigins(List.of("*"));
        
        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));
        
        // Allow common headers including Authorization (for JWT)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Access-Control-Request-Method",
            "Access-Control-Request-Headers"
        ));
        
        // Expose Authorization header in responses
        configuration.setExposedHeaders(List.of("Authorization"));
        
        // Allow credentials (cookies, authorization headers)
        // Note: Can't use allowCredentials with allowedOrigins("*")
        // configuration.setAllowCredentials(true);
        
        // How long browsers can cache CORS preflight response (1 hour)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Apply this configuration to all endpoints
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}

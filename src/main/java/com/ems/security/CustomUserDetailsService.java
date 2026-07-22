package com.ems.security;

import com.ems.entity.User;
import com.ems.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * =============================================================================
 * CUSTOM USER DETAILS SERVICE - Bridge between Spring Security and our User
 * =============================================================================
 * 
 * WHAT IS UserDetailsService?
 * ---------------------------
 * UserDetailsService is a core interface in Spring Security.
 * It has ONE method: loadUserByUsername(String username)
 * 
 * Spring Security calls this method during authentication to:
 *   1. Load user data from our database
 *   2. Get the user's credentials (password)
 *   3. Get the user's authorities (roles/permissions)
 * 
 * WHY DO WE NEED THIS?
 * --------------------
 * Spring Security doesn't know where our users are stored.
 * They could be in:
 *   - A database (our case)
 *   - LDAP directory
 *   - External service
 *   - In-memory (for testing)
 * 
 * By implementing UserDetailsService, we tell Spring Security:
 * "Here's how to get user information from OUR data source"
 * 
 * AUTHENTICATION FLOW:
 * --------------------
 * 1. User sends login request with username and password
 * 2. AuthenticationManager calls our loadUserByUsername()
 * 3. We load user from database and return UserDetails
 * 4. Spring Security compares submitted password with stored password
 * 5. If match: Authentication successful, create JWT
 * 6. If no match: Authentication failed, return 401
 * 
 * INTERVIEW QUESTION: What's the difference between UserDetails and User?
 * ANSWER:
 *   UserDetails: Spring Security interface - what Spring needs for auth
 *   User: Our entity - what our application needs for business logic
 *   
 *   Our User implements UserDetails, so it serves both purposes.
 *   Alternatively, you could have a UserDetailsAdapter class.
 * 
 * =============================================================================
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    /**
     * Load user by username for Spring Security authentication.
     * 
     * IMPORTANT: This method is called AUTOMATICALLY by Spring Security
     * during the authentication process. You don't call it directly.
     * 
     * FLOW:
     * 1. Search database for user with given username
     * 2. If found: Return the User (which implements UserDetails)
     * 3. If not found: Throw UsernameNotFoundException
     * 
     * @param username The username to search for
     * @return UserDetails object for Spring Security
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    @Transactional(readOnly = true)  // Read-only transaction for better performance
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempting to load user by username: {}", username);

        /*
         * Try to find user by username
         * 
         * We use Optional.orElseThrow() to handle the "not found" case elegantly.
         * If user doesn't exist, we throw UsernameNotFoundException which
         * Spring Security catches and converts to an authentication failure.
         */
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found with username: {}", username);
                    return new UsernameNotFoundException(
                            "User not found with username: " + username
                    );
                });

        logger.debug("Successfully loaded user: {} with role: {}", 
                     username, user.getRole());

        /*
         * Return the User object directly.
         * 
         * This works because our User class implements UserDetails.
         * Spring Security will use:
         *   - user.getPassword() to compare with submitted password
         *   - user.getAuthorities() for authorization
         *   - user.isEnabled(), isAccountNonLocked(), etc. for account status
         */
        return user;
    }

    /**
     * Load user by username or email.
     * 
     * Extended version that allows login with either username or email.
     * Not part of UserDetailsService interface, but useful for flexible login.
     * 
     * @param usernameOrEmail Username or email
     * @return UserDetails object
     * @throws UsernameNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsernameOrEmail(String usernameOrEmail) {
        logger.debug("Attempting to load user by username or email: {}", usernameOrEmail);

        User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                .orElseThrow(() -> {
                    logger.warn("User not found with username or email: {}", usernameOrEmail);
                    return new UsernameNotFoundException(
                            "User not found with username or email: " + usernameOrEmail
                    );
                });

        logger.debug("Successfully loaded user: {} with role: {}", 
                     user.getUsername(), user.getRole());

        return user;
    }
}

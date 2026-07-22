package com.ems.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * =============================================================================
 * JPA AUDIT CONFIGURATION - Automatic Audit Field Population
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * Configures Spring Data JPA auditing to automatically populate:
 *   - createdAt / updatedAt timestamps
 *   - createdBy / updatedBy user information
 * 
 * HOW IT WORKS:
 * -------------
 * 1. @EnableJpaAuditing enables auditing feature
 * 2. AuditorAware bean provides current user for @CreatedBy/@LastModifiedBy
 * 3. AuditingEntityListener on BaseEntity listens for persist/update events
 * 4. Before entity is saved, listener calls AuditorAware to get current user
 * 
 * INTERVIEW QUESTION: How does Spring know who the current user is?
 * ANSWER: The AuditorAware bean gets the current user from SecurityContextHolder.
 *         When a user is authenticated, their info is stored in SecurityContext.
 *         We extract the username from there for audit fields.
 * 
 * =============================================================================
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditConfig {

    /**
     * Provides the current auditor (user) for @CreatedBy and @LastModifiedBy.
     * 
     * This bean is called by Spring Data JPA's auditing infrastructure
     * whenever an entity is created or modified.
     * 
     * FLOW:
     * 1. Entity is about to be saved (new or update)
     * 2. AuditingEntityListener intercepts
     * 3. Calls auditorAware.getCurrentAuditor()
     * 4. Sets createdBy/updatedBy fields
     * 5. Entity is persisted
     */
    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            // Get current authentication from SecurityContext
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // If no authentication or anonymous, return "SYSTEM"
            if (authentication == null || !authentication.isAuthenticated() 
                    || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("SYSTEM");
            }
            
            // Return the username of the authenticated user
            return Optional.of(authentication.getName());
        };
    }
}

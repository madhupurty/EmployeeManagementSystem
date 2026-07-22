package com.ems.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * =============================================================================
 * USER ENTITY - Authentication and Authorization
 * =============================================================================
 * 
 * WHAT IS THIS CLASS?
 * -------------------
 * This entity represents a user in our system who can authenticate (login)
 * and be authorized (have permissions) to perform actions.
 * 
 * It implements Spring Security's UserDetails interface, which means
 * Spring Security can directly use this class for authentication.
 * 
 * WHY IMPLEMENT UserDetails?
 * --------------------------
 * Spring Security's authentication mechanism expects a UserDetails object.
 * By implementing this interface, we:
 *   1. Can use our User entity directly with Spring Security
 *   2. Don't need a separate adapter class
 *   3. Have full control over how credentials and authorities are provided
 * 
 * RELATIONSHIP WITH EMPLOYEE:
 * ---------------------------
 * - User: For authentication (login credentials)
 * - Employee: For employee data (name, department, salary, etc.)
 * 
 * A User MAY be linked to an Employee (if they are an employee),
 * or may be a system admin without an employee record.
 * 
 * INTERVIEW QUESTION: Why separate User and Employee entities?
 * ANSWER:
 *   1. Separation of concerns - Auth data vs Business data
 *   2. Not all users are employees (e.g., external auditors)
 *   3. An employee might not have system access (no User record)
 *   4. Different lifecycles - User can be disabled, Employee remains
 * 
 * =============================================================================
 */
@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username", name = "uk_user_username"),
        @UniqueConstraint(columnNames = "email", name = "uk_user_email")
    },
    indexes = {
        @Index(columnList = "username", name = "idx_user_username"),
        @Index(columnList = "email", name = "idx_user_email")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {

    /*
     * Primary key - Auto-generated ID
     * Using IDENTITY strategy for MySQL auto-increment
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * Username for login - must be unique
     * This will be the primary identifier for authentication
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /*
     * User's email address - must be unique
     * Can be used for password recovery, notifications
     */
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    /*
     * Encoded password - NEVER store plain text!
     * 
     * We use BCrypt encoding which:
     * - Is a one-way hash (cannot be reversed)
     * - Includes a salt (protects against rainbow tables)
     * - Is slow by design (protects against brute force)
     * 
     * INTERVIEW QUESTION: Why use BCrypt over MD5 or SHA?
     * ANSWER: BCrypt is specifically designed for passwords:
     *   - Built-in salt generation
     *   - Configurable work factor (can be made slower as hardware improves)
     *   - MD5/SHA are too fast, making brute force attacks easier
     */
    @Column(nullable = false)
    private String password;

    /*
     * User's first name - for display purposes
     */
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    /*
     * User's last name - for display purposes
     */
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    /*
     * User's role - determines what they can do
     * 
     * @Enumerated(EnumType.STRING): Store role name as string in DB
     * Better than ORDINAL because:
     *   - Human readable in database
     *   - Safe to reorder enum values
     *   - Safe to add new roles anywhere in enum
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /*
     * Is the account enabled?
     * Disabled accounts cannot login.
     * Use this for soft-delete or temporary suspension.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = true;

    /*
     * Is the account locked?
     * Locked accounts cannot login.
     * Can be used after too many failed login attempts.
     */
    @Column(name = "account_non_locked", nullable = false)
    @Builder.Default
    private boolean accountNonLocked = true;

    /*
     * Link to Employee record (optional)
     * 
     * @OneToOne: One User can have one Employee record
     * @JoinColumn: Creates employee_id foreign key column
     * 
     * This is optional - admin users might not be employees
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    private Employee employee;

    // ==========================================================================
    // AUDIT FIELDS
    // ==========================================================================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ==========================================================================
    // UserDetails INTERFACE IMPLEMENTATION
    // ==========================================================================
    
    /*
     * These methods are required by Spring Security's UserDetails interface.
     * Spring Security calls these during authentication and authorization.
     */

    /**
     * Returns the authorities (permissions) granted to the user.
     * 
     * In our case, we convert the Role enum to a GrantedAuthority.
     * We prefix with "ROLE_" which is Spring Security's convention.
     * 
     * Example: Role.ADMIN becomes GrantedAuthority("ROLE_ADMIN")
     * 
     * This is used by Spring Security for:
     *   - @PreAuthorize("hasRole('ADMIN')") - checks for ROLE_ADMIN
     *   - @Secured("ROLE_HR") - checks for ROLE_HR
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Return single authority based on role
        // If you need multiple roles per user, change Role to Set<Role>
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    /**
     * Returns the password used to authenticate the user.
     * Lombok @Getter already provides this, but we override for clarity.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the username used to authenticate the user.
     * Lombok @Getter already provides this, but we override for clarity.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Indicates whether the user's account has expired.
     * We don't implement account expiration, so always return true.
     * 
     * Return true = account is valid (not expired)
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is locked.
     * Can be used after multiple failed login attempts.
     * 
     * Return true = account is not locked (can login)
     */
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    /**
     * Indicates whether the user's credentials (password) has expired.
     * We don't implement credential expiration, so always return true.
     * 
     * Return true = credentials are valid (not expired)
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled or disabled.
     * Disabled users cannot authenticate.
     * 
     * Return true = user is enabled (can login)
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    // ==========================================================================
    // HELPER METHODS
    // ==========================================================================

    /**
     * Get the full name of the user.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Check if user has a specific role.
     */
    public boolean hasRole(Role checkRole) {
        return this.role == checkRole;
    }

    /**
     * Check if user is an admin.
     */
    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    /**
     * Check if user is HR.
     */
    public boolean isHR() {
        return this.role == Role.HR;
    }
}

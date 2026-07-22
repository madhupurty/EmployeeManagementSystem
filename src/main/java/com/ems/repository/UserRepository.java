package com.ems.repository;

import com.ems.entity.Role;
import com.ems.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * USER REPOSITORY - Database Operations for User Entity
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * This repository handles all database operations for the User entity.
 * It's used primarily for:
 *   1. Authentication - Finding user by username during login
 *   2. User management - CRUD operations on users
 *   3. Validation - Checking if username/email exists
 * 
 * SPRING DATA JPA MAGIC:
 * ----------------------
 * We just define method signatures, Spring generates the implementation!
 * 
 * Method naming convention -> SQL query:
 *   findByUsername -> SELECT * FROM users WHERE username = ?
 *   existsByEmail -> SELECT COUNT(*) > 0 FROM users WHERE email = ?
 * 
 * INTERVIEW QUESTION: How does Spring Data JPA create queries from method names?
 * ANSWER: Spring Data JPA parses the method name and creates a query:
 *   - findBy: SELECT query
 *   - existsBy: COUNT query returning boolean
 *   - countBy: COUNT query returning number
 *   - deleteBy: DELETE query
 *   After "By": Property names and operators (And, Or, LessThan, etc.)
 * 
 * =============================================================================
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // ==========================================================================
    // AUTHENTICATION METHODS
    // ==========================================================================

    /**
     * Find user by username.
     * 
     * This is the PRIMARY method used during authentication!
     * Spring Security's UserDetailsService calls this to load user by username.
     * 
     * Returns Optional because user might not exist.
     * 
     * Generated SQL:
     * SELECT * FROM users WHERE username = ?
     * 
     * @param username The username to search for
     * @return Optional containing user if found, empty otherwise
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email.
     * 
     * Useful for:
     * - Password reset functionality
     * - Login with email option
     * 
     * Generated SQL:
     * SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Find user by username or email.
     * 
     * Allows users to login with either username OR email.
     * 
     * Generated SQL:
     * SELECT * FROM users WHERE username = ? OR email = ?
     */
    Optional<User> findByUsernameOrEmail(String username, String email);

    // ==========================================================================
    // VALIDATION METHODS
    // ==========================================================================

    /**
     * Check if username already exists.
     * 
     * Used during registration to prevent duplicate usernames.
     * 
     * Generated SQL:
     * SELECT COUNT(*) > 0 FROM users WHERE username = ?
     */
    boolean existsByUsername(String username);

    /**
     * Check if email already exists.
     * 
     * Used during registration to prevent duplicate emails.
     * 
     * Generated SQL:
     * SELECT COUNT(*) > 0 FROM users WHERE email = ?
     */
    boolean existsByEmail(String email);

    // ==========================================================================
    // QUERY METHODS
    // ==========================================================================

    /**
     * Find all users with a specific role.
     * 
     * Useful for admin dashboard showing all HRs or all Employees.
     * 
     * Generated SQL:
     * SELECT * FROM users WHERE role = ?
     */
    List<User> findByRole(Role role);

    /**
     * Find all enabled users.
     * 
     * Generated SQL:
     * SELECT * FROM users WHERE enabled = true
     */
    List<User> findByEnabledTrue();

    /**
     * Find all disabled users (for admin review).
     * 
     * Generated SQL:
     * SELECT * FROM users WHERE enabled = false
     */
    List<User> findByEnabledFalse();

    /**
     * Count users by role.
     * 
     * Useful for dashboard statistics.
     * 
     * Generated SQL:
     * SELECT COUNT(*) FROM users WHERE role = ?
     */
    long countByRole(Role role);

    // ==========================================================================
    // CUSTOM QUERIES
    // ==========================================================================

    /**
     * Find active users by role using custom JPQL query.
     * 
     * Sometimes method naming convention isn't enough for complex queries.
     * We can write custom JPQL (Java Persistence Query Language).
     * 
     * JPQL vs SQL:
     * - JPQL uses entity names and field names (User, role, enabled)
     * - SQL uses table names and column names (users, role, enabled)
     * 
     * @param role The role to filter by
     * @return List of active users with the specified role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.enabled = true")
    List<User> findActiveUsersByRole(@Param("role") Role role);

    /**
     * Search users by name (first name or last name).
     * 
     * LIKE query for searching.
     * 
     * @param keyword Search keyword
     * @return List of matching users
     */
    @Query("SELECT u FROM User u WHERE LOWER(u.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByName(@Param("keyword") String keyword);

    /**
     * Check if a user exists by username and is enabled.
     * 
     * Useful for validation before allowing login.
     */
    boolean existsByUsernameAndEnabledTrue(String username);
}

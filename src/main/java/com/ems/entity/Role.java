package com.ems.entity;

/**
 * =============================================================================
 * ROLE ENUM - User Roles for Authorization
 * =============================================================================
 * 
 * WHAT IS ROLE-BASED ACCESS CONTROL (RBAC)?
 * ------------------------------------------
 * RBAC is a security model where permissions are assigned to roles,
 * and roles are assigned to users. This simplifies permission management.
 * 
 * Instead of: User -> [Permission1, Permission2, ...]
 * We have:    User -> Role -> [Permission1, Permission2, ...]
 * 
 * OUR ROLE HIERARCHY:
 * -------------------
 * ADMIN: Full system access
 *   - Can do everything HR can do
 *   - Can manage users (create, delete users)
 *   - Can assign roles
 *   - System configuration
 * 
 * HR: Human Resources staff
 *   - Can do everything EMPLOYEE can do
 *   - Can add/update/delete employees
 *   - Can view all employee data
 *   - Can manage leaves and attendance
 * 
 * EMPLOYEE: Regular employee
 *   - Can view own profile
 *   - Can update own profile (limited fields)
 *   - Can apply for leave
 *   - Can view own attendance
 * 
 * INTERVIEW QUESTION: Difference between Authentication and Authorization?
 * ANSWER:
 *   Authentication: WHO are you? (Login - verify identity)
 *   Authorization: WHAT can you do? (Permissions - verify access rights)
 * 
 * INTERVIEW QUESTION: Why use enum for roles?
 * ANSWER:
 *   1. Type safety - Compiler catches typos (ADMN vs ADMIN)
 *   2. Easy to maintain - All roles in one place
 *   3. IDE support - Autocomplete and refactoring
 *   4. Performance - Stored as ordinal in database (efficient)
 * 
 * =============================================================================
 */
public enum Role {
    
    /**
     * Administrator role - highest level of access.
     * Has all permissions in the system.
     */
    ADMIN,
    
    /**
     * Human Resources role - manages employee data.
     * Can create, read, update, delete employees.
     */
    HR,
    
    /**
     * Regular employee role - basic access.
     * Can only view and update own data.
     */
    EMPLOYEE
}

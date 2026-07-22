package com.ems.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * =============================================================================
 * BASE ENTITY - Audit Fields for All Entities
 * =============================================================================
 * 
 * WHAT IS THIS CLASS?
 * -------------------
 * BaseEntity is a mapped superclass that provides common audit fields
 * for all entities. Any entity extending this class will automatically
 * have these fields:
 *   - createdAt: When the record was created
 *   - updatedAt: When the record was last modified
 *   - createdBy: Who created the record
 *   - updatedBy: Who last modified the record
 * 
 * WHY USE AUDIT FIELDS?
 * ---------------------
 * 1. Compliance: Many regulations require audit trails
 * 2. Debugging: Track when and who made changes
 * 3. Security: Detect unauthorized modifications
 * 4. Business Logic: "Modified in last 24 hours" queries
 * 
 * HOW DOES IT WORK?
 * -----------------
 * Spring Data JPA's auditing feature automatically populates these fields:
 * 
 * @CreatedDate: Set when entity is first persisted
 * @LastModifiedDate: Updated on every save
 * @CreatedBy: Set from SecurityContext (logged-in user)
 * @LastModifiedBy: Updated from SecurityContext on every save
 * 
 * REQUIREMENTS:
 * 1. @EnableJpaAuditing on a @Configuration class
 * 2. @EntityListeners(AuditingEntityListener.class) on this class
 * 3. AuditorAware<String> bean to provide current user
 * 
 * INTERVIEW QUESTION: What is @MappedSuperclass?
 * ANSWER: @MappedSuperclass marks a class whose fields should be inherited
 *         by child entities, but the class itself is NOT an entity.
 *         No table is created for this class - its fields are added to
 *         child entity tables.
 * 
 * =============================================================================
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class BaseEntity {

    /**
     * Timestamp when the entity was created.
     * 
     * @CreatedDate: Automatically set by Spring Data JPA when entity is first saved
     * updatable = false: Prevents accidental updates to creation time
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when the entity was last modified.
     * 
     * @LastModifiedDate: Automatically updated by Spring Data JPA on every save
     */
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Username of the user who created this entity.
     * 
     * @CreatedBy: Automatically set from AuditorAware bean
     * updatable = false: Creator cannot be changed
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    /**
     * Username of the user who last modified this entity.
     * 
     * @LastModifiedBy: Automatically updated from AuditorAware bean
     */
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;
}

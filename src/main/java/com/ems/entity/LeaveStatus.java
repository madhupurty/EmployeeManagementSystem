package com.ems.entity;

/**
 * =============================================================================
 * LEAVE STATUS ENUM - Status of Leave Request
 * =============================================================================
 */
public enum LeaveStatus {
    
    /**
     * Leave request submitted, waiting for approval
     */
    PENDING,
    
    /**
     * Leave approved by manager/HR
     */
    APPROVED,
    
    /**
     * Leave rejected by manager/HR
     */
    REJECTED,
    
    /**
     * Leave cancelled by employee
     */
    CANCELLED
}

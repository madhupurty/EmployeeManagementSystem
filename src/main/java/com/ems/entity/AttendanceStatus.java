package com.ems.entity;

/**
 * =============================================================================
 * ATTENDANCE STATUS ENUM - Types of Attendance
 * =============================================================================
 */
public enum AttendanceStatus {
    
    /**
     * Employee was present
     */
    PRESENT,
    
    /**
     * Employee was absent (unplanned)
     */
    ABSENT,
    
    /**
     * Employee was on approved leave
     */
    ON_LEAVE,
    
    /**
     * Employee worked half day
     */
    HALF_DAY,
    
    /**
     * Weekend/Non-working day
     */
    WEEKEND,
    
    /**
     * Public holiday
     */
    HOLIDAY,
    
    /**
     * Working from home
     */
    WORK_FROM_HOME
}

package com.ems.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * =============================================================================
 * LEAVE REQUEST ENTITY - Employee Leave Management
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * Tracks employee leave requests including:
 *   - Type of leave (Annual, Sick, Casual, etc.)
 *   - Duration (start date to end date)
 *   - Status (Pending, Approved, Rejected, Cancelled)
 *   - Approval workflow (who approved/rejected)
 * 
 * WORKFLOW:
 * ---------
 * 1. Employee creates leave request (status = PENDING)
 * 2. Manager/HR reviews the request
 * 3. Manager/HR approves or rejects
 * 4. Employee can cancel if still pending
 * 
 * =============================================================================
 */
@Entity
@Table(
    name = "leave_requests",
    indexes = {
        @Index(columnList = "employee_id", name = "idx_leave_employee"),
        @Index(columnList = "status", name = "idx_leave_status"),
        @Index(columnList = "leave_type", name = "idx_leave_type"),
        @Index(columnList = "start_date", name = "idx_leave_start_date")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee who is requesting leave
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Type of leave being requested
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_type", nullable = false, length = 20)
    private LeaveType leaveType;

    /**
     * Start date of leave
     */
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    /**
     * End date of leave
     */
    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    /**
     * Total number of days (calculated)
     */
    @Column(name = "total_days", nullable = false)
    private Integer totalDays;

    /**
     * Reason for taking leave
     */
    @Column(name = "reason", length = 500)
    private String reason;

    /**
     * Current status of the leave request
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private LeaveStatus status = LeaveStatus.PENDING;

    /**
     * Who approved/rejected the request
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_id")
    private Employee approvedBy;

    /**
     * Date when the request was approved/rejected
     */
    @Column(name = "action_date")
    private LocalDate actionDate;

    /**
     * Comments from approver (reason for rejection, etc.)
     */
    @Column(name = "approver_comments", length = 500)
    private String approverComments;

    /**
     * Is this a half-day leave?
     */
    @Column(name = "is_half_day")
    @Builder.Default
    private Boolean isHalfDay = false;

    /**
     * If half-day, which session (FIRST_HALF or SECOND_HALF)
     */
    @Column(name = "half_day_session", length = 20)
    private String halfDaySession;

    // ==========================================================================
    // HELPER METHODS
    // ==========================================================================

    /**
     * Calculate total days of leave.
     * Should be called before saving.
     */
    @PrePersist
    @PreUpdate
    public void calculateTotalDays() {
        if (startDate != null && endDate != null) {
            // Add 1 because both start and end dates are inclusive
            long days = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
            this.totalDays = isHalfDay != null && isHalfDay ? 1 : (int) days;
            
            // Adjust for half day
            if (isHalfDay != null && isHalfDay && days == 1) {
                // Half day logic could be more complex
            }
        }
    }

    /**
     * Check if the leave request can be cancelled.
     */
    public boolean isCancellable() {
        return status == LeaveStatus.PENDING;
    }

    /**
     * Check if the leave is in the future.
     */
    public boolean isFutureLeave() {
        return startDate != null && startDate.isAfter(LocalDate.now());
    }

    /**
     * Get employee name.
     */
    public String getEmployeeName() {
        return employee != null ? employee.getFullName() : null;
    }

    /**
     * Get approver name.
     */
    public String getApproverName() {
        return approvedBy != null ? approvedBy.getFullName() : null;
    }
}

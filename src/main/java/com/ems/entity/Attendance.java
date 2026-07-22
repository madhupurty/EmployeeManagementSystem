package com.ems.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Duration;

/**
 * =============================================================================
 * ATTENDANCE ENTITY - Employee Attendance Tracking
 * =============================================================================
 * 
 * PURPOSE:
 * --------
 * Tracks daily attendance of employees including:
 *   - Check-in and check-out times
 *   - Total working hours
 *   - Attendance status (Present, Absent, Leave, etc.)
 *   - Overtime tracking
 * 
 * =============================================================================
 */
@Entity
@Table(
    name = "attendance",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"employee_id", "attendance_date"}, 
            name = "uk_attendance_employee_date"
        )
    },
    indexes = {
        @Index(columnList = "employee_id", name = "idx_attendance_employee"),
        @Index(columnList = "attendance_date", name = "idx_attendance_date"),
        @Index(columnList = "status", name = "idx_attendance_status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Employee whose attendance is being recorded
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Date of attendance
     */
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    /**
     * Time when employee checked in
     */
    @Column(name = "check_in_time")
    private LocalTime checkInTime;

    /**
     * Time when employee checked out
     */
    @Column(name = "check_out_time")
    private LocalTime checkOutTime;

    /**
     * Attendance status
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    /**
     * Total working hours in minutes
     */
    @Column(name = "working_hours_minutes")
    private Integer workingHoursMinutes;

    /**
     * Overtime hours in minutes (if any)
     */
    @Column(name = "overtime_minutes")
    @Builder.Default
    private Integer overtimeMinutes = 0;

    /**
     * Notes/remarks for the attendance
     */
    @Column(name = "remarks", length = 500)
    private String remarks;

    /**
     * IP address from which check-in was done
     */
    @Column(name = "check_in_ip", length = 50)
    private String checkInIp;

    /**
     * IP address from which check-out was done
     */
    @Column(name = "check_out_ip", length = 50)
    private String checkOutIp;

    /**
     * Location of check-in (optional)
     */
    @Column(name = "check_in_location", length = 200)
    private String checkInLocation;

    /**
     * Location of check-out (optional)
     */
    @Column(name = "check_out_location", length = 200)
    private String checkOutLocation;

    /**
     * Is this a late check-in?
     */
    @Column(name = "is_late")
    @Builder.Default
    private Boolean isLate = false;

    /**
     * Is this an early check-out?
     */
    @Column(name = "is_early_checkout")
    @Builder.Default
    private Boolean isEarlyCheckout = false;

    // ==========================================================================
    // HELPER METHODS
    // ==========================================================================

    /**
     * Calculate working hours from check-in and check-out times.
     */
    @PrePersist
    @PreUpdate
    public void calculateWorkingHours() {
        if (checkInTime != null && checkOutTime != null) {
            Duration duration = Duration.between(checkInTime, checkOutTime);
            this.workingHoursMinutes = (int) duration.toMinutes();
            
            // Standard working hours: 8 hours = 480 minutes
            int standardMinutes = 480;
            if (this.workingHoursMinutes > standardMinutes) {
                this.overtimeMinutes = this.workingHoursMinutes - standardMinutes;
            } else {
                this.overtimeMinutes = 0;
            }
        }
    }

    /**
     * Get working hours as formatted string (e.g., "8h 30m")
     */
    public String getFormattedWorkingHours() {
        if (workingHoursMinutes == null) return "N/A";
        int hours = workingHoursMinutes / 60;
        int minutes = workingHoursMinutes % 60;
        return hours + "h " + minutes + "m";
    }

    /**
     * Get overtime as formatted string
     */
    public String getFormattedOvertime() {
        if (overtimeMinutes == null || overtimeMinutes == 0) return "0h 0m";
        int hours = overtimeMinutes / 60;
        int minutes = overtimeMinutes % 60;
        return hours + "h " + minutes + "m";
    }

    /**
     * Check if employee has checked out
     */
    public boolean hasCheckedOut() {
        return checkOutTime != null;
    }

    /**
     * Get employee name
     */
    public String getEmployeeName() {
        return employee != null ? employee.getFullName() : null;
    }
}

package com.ems.repository;

import com.ems.entity.LeaveRequest;
import com.ems.entity.LeaveStatus;
import com.ems.entity.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * =============================================================================
 * LEAVE REQUEST REPOSITORY - Database Operations for Leave Requests
 * =============================================================================
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    // ==========================================================================
    // FIND BY EMPLOYEE
    // ==========================================================================

    List<LeaveRequest> findByEmployeeId(Long employeeId);

    Page<LeaveRequest> findByEmployeeId(Long employeeId, Pageable pageable);

    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    Page<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status, Pageable pageable);

    // ==========================================================================
    // FIND BY STATUS
    // ==========================================================================

    List<LeaveRequest> findByStatus(LeaveStatus status);

    Page<LeaveRequest> findByStatus(LeaveStatus status, Pageable pageable);

    // ==========================================================================
    // FIND BY DATE RANGE
    // ==========================================================================

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.startDate >= :startDate AND lr.endDate <= :endDate")
    List<LeaveRequest> findByDateRange(@Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.startDate >= :startDate AND lr.endDate <= :endDate")
    List<LeaveRequest> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                     @Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    // ==========================================================================
    // OVERLAP CHECKING
    // ==========================================================================

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.status != 'REJECTED' AND lr.status != 'CANCELLED' " +
           "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaves(@Param("employeeId") Long employeeId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.id != :excludeId " +
           "AND lr.status != 'REJECTED' AND lr.status != 'CANCELLED' " +
           "AND ((lr.startDate <= :endDate AND lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeavesExcluding(@Param("employeeId") Long employeeId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate,
                                                       @Param("excludeId") Long excludeId);

    // ==========================================================================
    // STATISTICS
    // ==========================================================================

    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.leaveType = :leaveType AND lr.status = 'APPROVED' " +
           "AND YEAR(lr.startDate) = :year")
    Long countApprovedLeavesByTypeAndYear(@Param("employeeId") Long employeeId,
                                           @Param("leaveType") LeaveType leaveType,
                                           @Param("year") int year);

    @Query("SELECT SUM(lr.totalDays) FROM LeaveRequest lr WHERE lr.employee.id = :employeeId " +
           "AND lr.leaveType = :leaveType AND lr.status = 'APPROVED' " +
           "AND YEAR(lr.startDate) = :year")
    Integer sumApprovedLeaveDaysByTypeAndYear(@Param("employeeId") Long employeeId,
                                               @Param("leaveType") LeaveType leaveType,
                                               @Param("year") int year);

    long countByStatus(LeaveStatus status);

    long countByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);

    // ==========================================================================
    // DEPARTMENT QUERIES
    // ==========================================================================

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.departmentEntity.id = :departmentId")
    Page<LeaveRequest> findByDepartmentId(@Param("departmentId") Long departmentId, Pageable pageable);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.departmentEntity.id = :departmentId " +
           "AND lr.status = :status")
    Page<LeaveRequest> findByDepartmentIdAndStatus(@Param("departmentId") Long departmentId,
                                                    @Param("status") LeaveStatus status,
                                                    Pageable pageable);
}

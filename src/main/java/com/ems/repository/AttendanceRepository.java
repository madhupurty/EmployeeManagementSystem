package com.ems.repository;

import com.ems.entity.Attendance;
import com.ems.entity.AttendanceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * ATTENDANCE REPOSITORY - Database Operations for Attendance Entity
 * =============================================================================
 */
@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // ==========================================================================
    // FIND BY EMPLOYEE AND DATE
    // ==========================================================================

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);

    boolean existsByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);

    // ==========================================================================
    // FIND BY EMPLOYEE
    // ==========================================================================

    List<Attendance> findByEmployeeId(Long employeeId);

    Page<Attendance> findByEmployeeId(Long employeeId, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate ORDER BY a.attendanceDate DESC")
    List<Attendance> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    // ==========================================================================
    // FIND BY DATE
    // ==========================================================================

    List<Attendance> findByAttendanceDate(LocalDate attendanceDate);

    Page<Attendance> findByAttendanceDate(LocalDate attendanceDate, Pageable pageable);

    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate BETWEEN :startDate AND :endDate")
    List<Attendance> findByDateRange(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    @Query("SELECT a FROM Attendance a WHERE a.attendanceDate BETWEEN :startDate AND :endDate")
    Page<Attendance> findByDateRange(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate,
                                      Pageable pageable);

    // ==========================================================================
    // FIND BY STATUS
    // ==========================================================================

    List<Attendance> findByStatus(AttendanceStatus status);

    Page<Attendance> findByStatus(AttendanceStatus status, Pageable pageable);

    List<Attendance> findByAttendanceDateAndStatus(LocalDate attendanceDate, AttendanceStatus status);

    // ==========================================================================
    // STATISTICS QUERIES
    // ==========================================================================

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.status = :status AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Long countByEmployeeIdAndStatusAndDateRange(@Param("employeeId") Long employeeId,
                                                 @Param("status") AttendanceStatus status,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(a.workingHoursMinutes) FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Long sumWorkingMinutesByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(a.overtimeMinutes) FROM Attendance a WHERE a.employee.id = :employeeId " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    Long sumOvertimeMinutesByEmployeeAndDateRange(@Param("employeeId") Long employeeId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    long countByAttendanceDateAndStatus(LocalDate date, AttendanceStatus status);

    // ==========================================================================
    // LATE AND EARLY CHECKOUT
    // ==========================================================================

    @Query("SELECT a FROM Attendance a WHERE a.employee.id = :employeeId AND a.isLate = true " +
           "AND a.attendanceDate BETWEEN :startDate AND :endDate")
    List<Attendance> findLateAttendances(@Param("employeeId") Long employeeId,
                                          @Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    Long countByEmployeeIdAndIsLateTrueAndAttendanceDateBetween(Long employeeId, 
                                                                  LocalDate startDate, 
                                                                  LocalDate endDate);

    // ==========================================================================
    // DEPARTMENT QUERIES
    // ==========================================================================

    @Query("SELECT a FROM Attendance a WHERE a.employee.departmentEntity.id = :departmentId " +
           "AND a.attendanceDate = :date")
    List<Attendance> findByDepartmentIdAndDate(@Param("departmentId") Long departmentId,
                                                @Param("date") LocalDate date);
}

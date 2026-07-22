package com.ems.repository;

import com.ems.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * =============================================================================
 * DEPARTMENT REPOSITORY - Database Operations for Department Entity
 * =============================================================================
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    // ==========================================================================
    // FIND METHODS
    // ==========================================================================

    Optional<Department> findByCode(String code);

    Optional<Department> findByName(String name);

    Optional<Department> findByCodeIgnoreCase(String code);

    Optional<Department> findByNameIgnoreCase(String name);

    // ==========================================================================
    // EXISTS METHODS
    // ==========================================================================

    boolean existsByCode(String code);

    boolean existsByName(String name);

    boolean existsByCodeAndIdNot(String code, Long id);

    boolean existsByNameAndIdNot(String name, Long id);

    // ==========================================================================
    // LIST METHODS
    // ==========================================================================

    List<Department> findByActiveTrue();

    List<Department> findByActiveFalse();

    Page<Department> findByActiveTrue(Pageable pageable);

    @Query("SELECT d FROM Department d WHERE d.manager.id = :managerId")
    List<Department> findByManagerId(@Param("managerId") Long managerId);

    // ==========================================================================
    // SEARCH METHODS
    // ==========================================================================

    @Query("SELECT d FROM Department d WHERE " +
           "LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Department> searchDepartments(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT d FROM Department d WHERE " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(d.code) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "d.active = :active")
    Page<Department> searchDepartmentsByKeywordAndStatus(
            @Param("keyword") String keyword,
            @Param("active") Boolean active,
            Pageable pageable);

    // ==========================================================================
    // STATISTICS METHODS
    // ==========================================================================

    long countByActiveTrue();

    long countByActiveFalse();

    @Query("SELECT d.name, COUNT(e) FROM Department d LEFT JOIN d.employees e GROUP BY d.id, d.name")
    List<Object[]> getDepartmentEmployeeCounts();
}

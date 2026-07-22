package com.ems.service;

import com.ems.dto.DepartmentRequestDTO;
import com.ems.dto.DepartmentResponseDTO;
import com.ems.dto.PagedResponseDTO;

import java.util.List;

/**
 * =============================================================================
 * DEPARTMENT SERVICE INTERFACE
 * =============================================================================
 */
public interface DepartmentService {

    // CRUD Operations
    DepartmentResponseDTO createDepartment(DepartmentRequestDTO requestDTO);

    DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO requestDTO);

    DepartmentResponseDTO getDepartmentById(Long id);

    DepartmentResponseDTO getDepartmentByCode(String code);

    void deleteDepartment(Long id);

    DepartmentResponseDTO deactivateDepartment(Long id);

    DepartmentResponseDTO activateDepartment(Long id);

    // Query Operations
    PagedResponseDTO<DepartmentResponseDTO> getAllDepartments(int page, int size, String sortBy, String sortDir);

    PagedResponseDTO<DepartmentResponseDTO> getActiveDepartments(int page, int size, String sortBy, String sortDir);

    PagedResponseDTO<DepartmentResponseDTO> searchDepartments(String keyword, int page, int size, String sortBy, String sortDir);

    List<DepartmentResponseDTO> getAllActiveDepartmentsList();

    // Manager Operations
    DepartmentResponseDTO assignManager(Long departmentId, Long employeeId);

    DepartmentResponseDTO removeManager(Long departmentId);

    // Validation
    boolean isDepartmentCodeExists(String code);

    boolean isDepartmentNameExists(String name);
}

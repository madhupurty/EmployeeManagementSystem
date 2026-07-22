package com.ems.service.impl;

import com.ems.dto.DepartmentRequestDTO;
import com.ems.dto.DepartmentResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.entity.Department;
import com.ems.entity.Employee;
import com.ems.exception.BadRequestException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * =============================================================================
 * DEPARTMENT SERVICE IMPLEMENTATION
 * =============================================================================
 */
@Service
@RequiredArgsConstructor
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    // ==========================================================================
    // CREATE
    // ==========================================================================

    @Override
    public DepartmentResponseDTO createDepartment(DepartmentRequestDTO requestDTO) {
        logger.info("Creating new department: {}", requestDTO.getCode());

        // Validate uniqueness
        if (departmentRepository.existsByCode(requestDTO.getCode())) {
            throw new BadRequestException("Department code '" + requestDTO.getCode() + "' already exists");
        }
        if (departmentRepository.existsByName(requestDTO.getName())) {
            throw new BadRequestException("Department name '" + requestDTO.getName() + "' already exists");
        }

        // Build department entity
        Department department = Department.builder()
                .code(requestDTO.getCode().toUpperCase())
                .name(requestDTO.getName())
                .description(requestDTO.getDescription())
                .location(requestDTO.getLocation())
                .contactEmail(requestDTO.getContactEmail())
                .contactPhone(requestDTO.getContactPhone())
                .budget(requestDTO.getBudget())
                .active(requestDTO.getActive() != null ? requestDTO.getActive() : true)
                .build();

        // Set manager if provided
        if (requestDTO.getManagerId() != null) {
            Employee manager = employeeRepository.findById(requestDTO.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee", "id", requestDTO.getManagerId()));
            department.setManager(manager);
        }

        Department savedDepartment = departmentRepository.save(department);
        logger.info("Department created successfully with ID: {}", savedDepartment.getId());

        return mapToResponseDTO(savedDepartment);
    }

    // ==========================================================================
    // UPDATE
    // ==========================================================================

    @Override
    public DepartmentResponseDTO updateDepartment(Long id, DepartmentRequestDTO requestDTO) {
        logger.info("Updating department with ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Validate uniqueness (excluding current department)
        if (departmentRepository.existsByCodeAndIdNot(requestDTO.getCode(), id)) {
            throw new BadRequestException("Department code '" + requestDTO.getCode() + "' already exists");
        }
        if (departmentRepository.existsByNameAndIdNot(requestDTO.getName(), id)) {
            throw new BadRequestException("Department name '" + requestDTO.getName() + "' already exists");
        }

        // Update fields
        department.setCode(requestDTO.getCode().toUpperCase());
        department.setName(requestDTO.getName());
        department.setDescription(requestDTO.getDescription());
        department.setLocation(requestDTO.getLocation());
        department.setContactEmail(requestDTO.getContactEmail());
        department.setContactPhone(requestDTO.getContactPhone());
        department.setBudget(requestDTO.getBudget());
        
        if (requestDTO.getActive() != null) {
            department.setActive(requestDTO.getActive());
        }

        // Update manager if provided
        if (requestDTO.getManagerId() != null) {
            Employee manager = employeeRepository.findById(requestDTO.getManagerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee", "id", requestDTO.getManagerId()));
            department.setManager(manager);
        }

        Department updatedDepartment = departmentRepository.save(department);
        logger.info("Department updated successfully: {}", updatedDepartment.getCode());

        return mapToResponseDTO(updatedDepartment);
    }

    // ==========================================================================
    // READ
    // ==========================================================================

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentById(Long id) {
        logger.debug("Fetching department by ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        return mapToResponseDTO(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponseDTO getDepartmentByCode(String code) {
        logger.debug("Fetching department by code: {}", code);

        Department department = departmentRepository.findByCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "code", code));

        return mapToResponseDTO(department);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<DepartmentResponseDTO> getAllDepartments(int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching all departments - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Department> departmentPage = departmentRepository.findAll(pageable);

        return mapToPagedResponse(departmentPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<DepartmentResponseDTO> getActiveDepartments(int page, int size, String sortBy, String sortDir) {
        logger.debug("Fetching active departments - page: {}, size: {}", page, size);

        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Department> departmentPage = departmentRepository.findByActiveTrue(pageable);

        return mapToPagedResponse(departmentPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<DepartmentResponseDTO> searchDepartments(String keyword, int page, int size, String sortBy, String sortDir) {
        logger.debug("Searching departments with keyword: {}", keyword);

        Sort sort = sortDir.equalsIgnoreCase("desc") 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Department> departmentPage = departmentRepository.searchDepartments(keyword, pageable);

        return mapToPagedResponse(departmentPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponseDTO> getAllActiveDepartmentsList() {
        logger.debug("Fetching all active departments as list");

        return departmentRepository.findByActiveTrue()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // ==========================================================================
    // DELETE / DEACTIVATE
    // ==========================================================================

    @Override
    public void deleteDepartment(Long id) {
        logger.info("Deleting department with ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        // Check if department has employees
        if (department.getEmployeeCount() > 0) {
            throw new BadRequestException(
                    "Cannot delete department '" + department.getName() + 
                    "' as it has " + department.getEmployeeCount() + " employees. " +
                    "Please reassign employees first or deactivate the department.");
        }

        departmentRepository.delete(department);
        logger.info("Department deleted successfully: {}", department.getCode());
    }

    @Override
    public DepartmentResponseDTO deactivateDepartment(Long id) {
        logger.info("Deactivating department with ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        department.setActive(false);
        Department savedDepartment = departmentRepository.save(department);

        logger.info("Department deactivated: {}", savedDepartment.getCode());
        return mapToResponseDTO(savedDepartment);
    }

    @Override
    public DepartmentResponseDTO activateDepartment(Long id) {
        logger.info("Activating department with ID: {}", id);

        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));

        department.setActive(true);
        Department savedDepartment = departmentRepository.save(department);

        logger.info("Department activated: {}", savedDepartment.getCode());
        return mapToResponseDTO(savedDepartment);
    }

    // ==========================================================================
    // MANAGER OPERATIONS
    // ==========================================================================

    @Override
    public DepartmentResponseDTO assignManager(Long departmentId, Long employeeId) {
        logger.info("Assigning manager {} to department {}", employeeId, departmentId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        Employee manager = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));

        department.setManager(manager);
        Department savedDepartment = departmentRepository.save(department);

        logger.info("Manager assigned successfully");
        return mapToResponseDTO(savedDepartment);
    }

    @Override
    public DepartmentResponseDTO removeManager(Long departmentId) {
        logger.info("Removing manager from department {}", departmentId);

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        department.setManager(null);
        Department savedDepartment = departmentRepository.save(department);

        logger.info("Manager removed successfully");
        return mapToResponseDTO(savedDepartment);
    }

    // ==========================================================================
    // VALIDATION
    // ==========================================================================

    @Override
    @Transactional(readOnly = true)
    public boolean isDepartmentCodeExists(String code) {
        return departmentRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isDepartmentNameExists(String name) {
        return departmentRepository.existsByName(name);
    }

    // ==========================================================================
    // MAPPING METHODS
    // ==========================================================================

    private DepartmentResponseDTO mapToResponseDTO(Department department) {
        DepartmentResponseDTO.DepartmentResponseDTOBuilder builder = DepartmentResponseDTO.builder()
                .id(department.getId())
                .code(department.getCode())
                .name(department.getName())
                .description(department.getDescription())
                .location(department.getLocation())
                .contactEmail(department.getContactEmail())
                .contactPhone(department.getContactPhone())
                .budget(department.getBudget())
                .active(department.getActive())
                .employeeCount(department.getEmployeeCount())
                .createdAt(department.getCreatedAt())
                .updatedAt(department.getUpdatedAt())
                .createdBy(department.getCreatedBy())
                .updatedBy(department.getUpdatedBy());

        // Add manager info if present
        if (department.getManager() != null) {
            builder.managerId(department.getManager().getId())
                   .managerName(department.getManager().getFullName())
                   .managerEmail(department.getManager().getEmail());
        }

        return builder.build();
    }

    private PagedResponseDTO<DepartmentResponseDTO> mapToPagedResponse(Page<Department> page) {
        List<DepartmentResponseDTO> content = page.getContent()
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());

        return PagedResponseDTO.<DepartmentResponseDTO>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }
}

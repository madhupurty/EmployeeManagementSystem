package com.ems.service.impl;

import com.ems.dto.EmployeeRequestDTO;
import com.ems.dto.EmployeeResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.entity.Employee;
import com.ems.exception.BadRequestException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EmployeeRepository;
import com.ems.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of EmployeeService providing business logic for employee management.
 * Handles CRUD operations, search, and pagination for employees.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public EmployeeResponseDTO createEmployee(EmployeeRequestDTO requestDTO) {
        logger.info("Creating new employee with email: {}", requestDTO.getEmail());

        if (employeeRepository.existsByEmail(requestDTO.getEmail())) {
            logger.warn("Employee creation failed: Email {} already exists", requestDTO.getEmail());
            throw new BadRequestException("Employee with email " + requestDTO.getEmail() + " already exists");
        }

        Employee employee = EmployeeResponseDTO.toEntity(requestDTO);
        employee.setEmployeeCode(generateEmployeeCode());

        if (employee.getStatus() == null) {
            employee.setStatus(Employee.EmployeeStatus.ACTIVE);
        }
        if (employee.getDateOfJoining() == null) {
            employee.setDateOfJoining(LocalDate.now());
        }

        Employee savedEmployee = employeeRepository.save(employee);
        logger.info("Employee created successfully with ID: {} and code: {}", 
                    savedEmployee.getId(), savedEmployee.getEmployeeCode());

        return EmployeeResponseDTO.fromEntity(savedEmployee);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO updateEmployee(Long id, EmployeeRequestDTO requestDTO) {
        logger.info("Updating employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Employee not found with ID: {}", id);
                    return new ResourceNotFoundException("Employee", "id", id);
                });

        if (requestDTO.getEmail() != null && 
            !requestDTO.getEmail().equals(employee.getEmail()) &&
            employeeRepository.existsByEmailAndIdNot(requestDTO.getEmail(), id)) {
            logger.warn("Email {} already exists for another employee", requestDTO.getEmail());
            throw new BadRequestException("Email " + requestDTO.getEmail() + " is already in use");
        }

        EmployeeResponseDTO.updateEntity(employee, requestDTO);
        Employee updatedEmployee = employeeRepository.save(employee);
        logger.info("Employee updated successfully with ID: {}", id);

        return EmployeeResponseDTO.fromEntity(updatedEmployee);
    }

    @Override
    public EmployeeResponseDTO getEmployeeById(Long id) {
        logger.debug("Fetching employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Employee not found with ID: {}", id);
                    return new ResourceNotFoundException("Employee", "id", id);
                });

        return EmployeeResponseDTO.fromEntity(employee);
    }

    @Override
    public EmployeeResponseDTO getEmployeeByCode(String employeeCode) {
        logger.debug("Fetching employee with code: {}", employeeCode);

        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> {
                    logger.warn("Employee not found with code: {}", employeeCode);
                    return new ResourceNotFoundException("Employee", "employeeCode", employeeCode);
                });

        return EmployeeResponseDTO.fromEntity(employee);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        logger.info("Deleting employee with ID: {}", id);

        if (!employeeRepository.existsById(id)) {
            logger.warn("Cannot delete: Employee not found with ID: {}", id);
            throw new ResourceNotFoundException("Employee", "id", id);
        }

        employeeRepository.deleteById(id);
        logger.info("Employee deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public EmployeeResponseDTO deactivateEmployee(Long id) {
        logger.info("Deactivating employee with ID: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Cannot deactivate: Employee not found with ID: {}", id);
                    return new ResourceNotFoundException("Employee", "id", id);
                });

        employee.setStatus(Employee.EmployeeStatus.INACTIVE);
        Employee deactivatedEmployee = employeeRepository.save(employee);
        
        logger.info("Employee deactivated successfully with ID: {}", id);
        return EmployeeResponseDTO.fromEntity(deactivatedEmployee);
    }

    @Override
    public PagedResponseDTO<EmployeeResponseDTO> getAllEmployees(int pageNo, int pageSize,
                                                                  String sortBy, String sortDir) {
        logger.debug("Fetching employees - page: {}, size: {}, sortBy: {}, sortDir: {}", 
                     pageNo, pageSize, sortBy, sortDir);

        Pageable pageable = createPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Employee> employeePage = employeeRepository.findAll(pageable);
        return convertToPagedResponse(employeePage);
    }

    @Override
    public PagedResponseDTO<EmployeeResponseDTO> getEmployeesByDepartment(String department,
                                                                           int pageNo, int pageSize,
                                                                           String sortBy, String sortDir) {
        logger.debug("Fetching employees by department: {}", department);

        Pageable pageable = createPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Employee> employeePage = employeeRepository.findByDepartment(department, pageable);
        return convertToPagedResponse(employeePage);
    }

    @Override
    public PagedResponseDTO<EmployeeResponseDTO> getEmployeesByStatus(String status,
                                                                       int pageNo, int pageSize,
                                                                       String sortBy, String sortDir) {
        logger.debug("Fetching employees by status: {}", status);

        Employee.EmployeeStatus employeeStatus;
        try {
            employeeStatus = Employee.EmployeeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status: " + status);
        }

        Pageable pageable = createPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Employee> employeePage = employeeRepository.findByStatus(employeeStatus, pageable);
        return convertToPagedResponse(employeePage);
    }

    @Override
    public PagedResponseDTO<EmployeeResponseDTO> searchEmployees(String keyword,
                                                                  int pageNo, int pageSize,
                                                                  String sortBy, String sortDir) {
        logger.debug("Searching employees with keyword: {}", keyword);

        Pageable pageable = createPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Employee> employeePage = employeeRepository.searchEmployees(keyword, pageable);
        return convertToPagedResponse(employeePage);
    }

    @Override
    public PagedResponseDTO<EmployeeResponseDTO> searchEmployeesWithFilters(String keyword,
                                                                             String department,
                                                                             String status,
                                                                             int pageNo, int pageSize,
                                                                             String sortBy, String sortDir) {
        logger.debug("Searching employees - keyword: {}, department: {}, status: {}", 
                     keyword, department, status);

        Employee.EmployeeStatus employeeStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                employeeStatus = Employee.EmployeeStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid status: " + status);
            }
        }

        Pageable pageable = createPageable(pageNo, pageSize, sortBy, sortDir);
        Page<Employee> employeePage = employeeRepository.searchEmployeesWithFilters(
                keyword, department, employeeStatus, pageable);
        return convertToPagedResponse(employeePage);
    }

    @Override
    public List<String> getAllDepartments() {
        return employeeRepository.findAllDepartments();
    }

    @Override
    public List<String> getAllDesignations() {
        return employeeRepository.findAllDesignations();
    }

    @Override
    public List<EmployeeResponseDTO> getEmployeesByManager(Long managerId) {
        logger.debug("Fetching employees reporting to manager ID: {}", managerId);

        return employeeRepository.findByManagerId(managerId)
                .stream()
                .map(EmployeeResponseDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isEmailExists(String email) {
        return employeeRepository.existsByEmail(email);
    }

    @Override
    public boolean isEmployeeCodeExists(String employeeCode) {
        return employeeRepository.existsByEmployeeCode(employeeCode);
    }

    private Pageable createPageable(int pageNo, int pageSize, String sortBy, String sortDir) {
        int zeroIndexedPage = pageNo > 0 ? pageNo - 1 : 0;
        Sort sort = sortDir.equalsIgnoreCase("desc") 
                    ? Sort.by(sortBy).descending() 
                    : Sort.by(sortBy).ascending();
        return PageRequest.of(zeroIndexedPage, pageSize, sort);
    }

    private PagedResponseDTO<EmployeeResponseDTO> convertToPagedResponse(Page<Employee> employeePage) {
        List<EmployeeResponseDTO> content = employeePage.getContent()
                .stream()
                .map(EmployeeResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return PagedResponseDTO.fromPage(employeePage, content);
    }

    private String generateEmployeeCode() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseCode = "EMP-" + datePrefix + "-";
        long count = employeeRepository.count();
        String sequence = String.format("%04d", count + 1);
        return baseCode + sequence;
    }
}
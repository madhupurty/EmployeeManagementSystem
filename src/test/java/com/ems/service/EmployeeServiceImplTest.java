package com.ems.service;

import com.ems.dto.EmployeeRequestDTO;
import com.ems.dto.EmployeeResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.entity.Employee;
import com.ems.exception.BadRequestException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.EmployeeRepository;
import com.ems.service.impl.EmployeeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * =============================================================================
 * EMPLOYEE SERVICE UNIT TESTS
 * =============================================================================
 * 
 * Unit tests for EmployeeServiceImpl using JUnit 5 and Mockito.
 * 
 * KEY CONCEPTS:
 * -------------
 * 1. @ExtendWith(MockitoExtension.class): Enables Mockito annotations
 * 2. @Mock: Creates mock objects
 * 3. @InjectMocks: Injects mocks into the class being tested
 * 4. BDD Style: given-when-then pattern for readability
 * 5. AssertJ: Fluent assertions for better readability
 * 
 * WHAT IS UNIT TESTING?
 * ---------------------
 * Unit tests test a single unit (class/method) in isolation.
 * Dependencies are mocked to focus only on the logic being tested.
 * 
 * INTERVIEW QUESTION: Difference between Unit and Integration tests?
 * ANSWER:
 *   - Unit Test: Tests single unit in isolation, mocks dependencies, fast
 *   - Integration Test: Tests multiple units together, uses real dependencies, slower
 * 
 * =============================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Employee Service Unit Tests")
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    // Test data
    private Employee employee;
    private EmployeeRequestDTO requestDTO;

    /**
     * Setup test data before each test.
     * 
     * @BeforeEach: Runs before each test method
     */
    @BeforeEach
    void setUp() {
        // Create test employee entity
        employee = Employee.builder()
                .id(1L)
                .employeeCode("EMP001")
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@company.com")
                .phoneNumber("1234567890")
                .department("IT")
                .designation("Software Engineer")
                .salary(new BigDecimal("75000.00"))
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .dateOfJoining(LocalDate.of(2020, 1, 10))
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();

        // Create test request DTO
        requestDTO = EmployeeRequestDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@company.com")
                .phoneNumber("1234567890")
                .department("IT")
                .designation("Software Engineer")
                .salary(new BigDecimal("75000.00"))
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .dateOfJoining(LocalDate.of(2020, 1, 10))
                .build();
    }

    // ==========================================================================
    // CREATE EMPLOYEE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Create Employee Tests")
    class CreateEmployeeTests {

        @Test
        @DisplayName("Should create employee successfully when email is unique")
        void createEmployee_WhenEmailIsUnique_ShouldReturnCreatedEmployee() {
            // Given (Arrange)
            given(employeeRepository.existsByEmail(anyString())).willReturn(false);
            given(employeeRepository.save(any(Employee.class))).willReturn(employee);

            // When (Act)
            EmployeeResponseDTO result = employeeService.createEmployee(requestDTO);

            // Then (Assert)
            assertThat(result).isNotNull();
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            assertThat(result.getEmail()).isEqualTo("john.doe@company.com");

            // Verify interactions
            verify(employeeRepository, times(1)).existsByEmail(anyString());
            verify(employeeRepository, times(1)).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when email already exists")
        void createEmployee_WhenEmailExists_ShouldThrowException() {
            // Given
            given(employeeRepository.existsByEmail(anyString())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> employeeService.createEmployee(requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("email");

            // Verify save was never called
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    // ==========================================================================
    // GET EMPLOYEE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Get Employee Tests")
    class GetEmployeeTests {

        @Test
        @DisplayName("Should return employee when found by ID")
        void getEmployeeById_WhenEmployeeExists_ShouldReturnEmployee() {
            // Given
            given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));

            // When
            EmployeeResponseDTO result = employeeService.getEmployeeById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getEmail()).isEqualTo("john.doe@company.com");

            verify(employeeRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when employee not found by ID")
        void getEmployeeById_WhenEmployeeNotExists_ShouldThrowException() {
            // Given
            given(employeeRepository.findById(anyLong())).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> employeeService.getEmployeeById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Employee");
        }

        @Test
        @DisplayName("Should return employee when found by employee code")
        void getEmployeeByCode_WhenEmployeeExists_ShouldReturnEmployee() {
            // Given
            given(employeeRepository.findByEmployeeCode("EMP001")).willReturn(Optional.of(employee));

            // When
            EmployeeResponseDTO result = employeeService.getEmployeeByCode("EMP001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmployeeCode()).isEqualTo("EMP001");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when employee not found by code")
        void getEmployeeByCode_WhenEmployeeNotExists_ShouldThrowException() {
            // Given
            given(employeeRepository.findByEmployeeCode(anyString())).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> employeeService.getEmployeeByCode("INVALID"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==========================================================================
    // UPDATE EMPLOYEE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Update Employee Tests")
    class UpdateEmployeeTests {

        @Test
        @DisplayName("Should update employee successfully")
        void updateEmployee_WhenEmployeeExists_ShouldReturnUpdatedEmployee() {
            // Given
            given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
            given(employeeRepository.existsByEmailAndIdNot(anyString(), anyLong())).willReturn(false);
            given(employeeRepository.save(any(Employee.class))).willReturn(employee);

            requestDTO.setFirstName("Jane");
            requestDTO.setLastName("Smith");

            // When
            EmployeeResponseDTO result = employeeService.updateEmployee(1L, requestDTO);

            // Then
            assertThat(result).isNotNull();
            verify(employeeRepository, times(1)).findById(1L);
            verify(employeeRepository, times(1)).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should throw BadRequestException when email already exists for another employee")
        void updateEmployee_WhenEmailExistsForAnother_ShouldThrowException() {
            // Given
            given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
            given(employeeRepository.existsByEmailAndIdNot(anyString(), anyLong())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> employeeService.updateEmployee(1L, requestDTO))
                    .isInstanceOf(BadRequestException.class);

            verify(employeeRepository, never()).save(any(Employee.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent employee")
        void updateEmployee_WhenEmployeeNotExists_ShouldThrowException() {
            // Given
            given(employeeRepository.findById(anyLong())).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> employeeService.updateEmployee(999L, requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // ==========================================================================
    // DELETE EMPLOYEE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Delete Employee Tests")
    class DeleteEmployeeTests {

        @Test
        @DisplayName("Should delete employee successfully")
        void deleteEmployee_WhenEmployeeExists_ShouldDeleteSuccessfully() {
            // Given
            given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
            doNothing().when(employeeRepository).delete(any(Employee.class));

            // When
            employeeService.deleteEmployee(1L);

            // Then
            verify(employeeRepository, times(1)).findById(1L);
            verify(employeeRepository, times(1)).delete(employee);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent employee")
        void deleteEmployee_WhenEmployeeNotExists_ShouldThrowException() {
            // Given
            given(employeeRepository.findById(anyLong())).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> employeeService.deleteEmployee(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(employeeRepository, never()).delete(any(Employee.class));
        }
    }

    // ==========================================================================
    // GET ALL EMPLOYEES TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Get All Employees Tests")
    class GetAllEmployeesTests {

        @Test
        @DisplayName("Should return paginated employees")
        void getAllEmployees_ShouldReturnPagedResponse() {
            // Given
            Employee employee2 = Employee.builder()
                    .id(2L)
                    .employeeCode("EMP002")
                    .firstName("Jane")
                    .lastName("Smith")
                    .email("jane.smith@company.com")
                    .status(Employee.EmployeeStatus.ACTIVE)
                    .build();

            List<Employee> employees = Arrays.asList(employee, employee2);
            Page<Employee> employeePage = new PageImpl<>(employees);

            given(employeeRepository.findAll(any(Pageable.class))).willReturn(employeePage);

            // When
            PagedResponseDTO<EmployeeResponseDTO> result = 
                    employeeService.getAllEmployees(0, 10, "firstName", "asc");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should return empty page when no employees exist")
        void getAllEmployees_WhenNoEmployees_ShouldReturnEmptyPage() {
            // Given
            Page<Employee> emptyPage = new PageImpl<>(List.of());
            given(employeeRepository.findAll(any(Pageable.class))).willReturn(emptyPage);

            // When
            PagedResponseDTO<EmployeeResponseDTO> result = 
                    employeeService.getAllEmployees(0, 10, "firstName", "asc");

            // Then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    // ==========================================================================
    // SEARCH EMPLOYEES TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Search Employees Tests")
    class SearchEmployeesTests {

        @Test
        @DisplayName("Should return employees matching search keyword")
        void searchEmployees_WithValidKeyword_ShouldReturnMatchingEmployees() {
            // Given
            List<Employee> employees = List.of(employee);
            Page<Employee> employeePage = new PageImpl<>(employees);

            given(employeeRepository.searchEmployees(anyString(), any(Pageable.class)))
                    .willReturn(employeePage);

            // When
            PagedResponseDTO<EmployeeResponseDTO> result = 
                    employeeService.searchEmployees("John", 0, 10, "firstName", "asc");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getFirstName()).isEqualTo("John");
        }
    }

    // ==========================================================================
    // DEACTIVATE EMPLOYEE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Deactivate Employee Tests")
    class DeactivateEmployeeTests {

        @Test
        @DisplayName("Should deactivate employee successfully")
        void deactivateEmployee_WhenEmployeeExists_ShouldSetStatusToInactive() {
            // Given
            given(employeeRepository.findById(1L)).willReturn(Optional.of(employee));
            given(employeeRepository.save(any(Employee.class))).willAnswer(invocation -> {
                Employee saved = invocation.getArgument(0);
                saved.setStatus(Employee.EmployeeStatus.INACTIVE);
                return saved;
            });

            // When
            EmployeeResponseDTO result = employeeService.deactivateEmployee(1L);

            // Then
            assertThat(result).isNotNull();
            verify(employeeRepository, times(1)).save(any(Employee.class));
        }
    }

    // ==========================================================================
    // VALIDATION TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should return true when email exists")
        void isEmailExists_WhenEmailExists_ShouldReturnTrue() {
            // Given
            given(employeeRepository.existsByEmail("john.doe@company.com")).willReturn(true);

            // When
            boolean result = employeeService.isEmailExists("john.doe@company.com");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when email does not exist")
        void isEmailExists_WhenEmailNotExists_ShouldReturnFalse() {
            // Given
            given(employeeRepository.existsByEmail("new@company.com")).willReturn(false);

            // When
            boolean result = employeeService.isEmailExists("new@company.com");

            // Then
            assertThat(result).isFalse();
        }
    }

    // ==========================================================================
    // GET BY DEPARTMENT TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Get By Department Tests")
    class GetByDepartmentTests {

        @Test
        @DisplayName("Should return employees by department")
        void getEmployeesByDepartment_ShouldReturnEmployeesInDepartment() {
            // Given
            List<Employee> itEmployees = List.of(employee);
            Page<Employee> employeePage = new PageImpl<>(itEmployees);

            given(employeeRepository.findByDepartment(anyString(), any(Pageable.class)))
                    .willReturn(employeePage);

            // When
            PagedResponseDTO<EmployeeResponseDTO> result = 
                    employeeService.getEmployeesByDepartment("IT", 0, 10, "firstName", "asc");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getDepartment()).isEqualTo("IT");
        }
    }

    // ==========================================================================
    // UTILITY METHOD TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should return all unique departments")
        void getAllDepartments_ShouldReturnUniqueDepartments() {
            // Given
            List<String> departments = Arrays.asList("IT", "HR", "Finance");
            given(employeeRepository.findAllDepartments()).willReturn(departments);

            // When
            List<String> result = employeeService.getAllDepartments();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).containsExactly("IT", "HR", "Finance");
        }

        @Test
        @DisplayName("Should return all unique designations")
        void getAllDesignations_ShouldReturnUniqueDesignations() {
            // Given
            List<String> designations = Arrays.asList("Software Engineer", "Manager", "Analyst");
            given(employeeRepository.findAllDesignations()).willReturn(designations);

            // When
            List<String> result = employeeService.getAllDesignations();

            // Then
            assertThat(result).hasSize(3);
            assertThat(result).contains("Software Engineer");
        }
    }
}

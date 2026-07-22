package com.ems.service;

import com.ems.dto.DepartmentRequestDTO;
import com.ems.dto.DepartmentResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.entity.Department;
import com.ems.entity.Employee;
import com.ems.exception.BadRequestException;
import com.ems.exception.ResourceNotFoundException;
import com.ems.repository.DepartmentRepository;
import com.ems.repository.EmployeeRepository;
import com.ems.service.impl.DepartmentServiceImpl;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * =============================================================================
 * DEPARTMENT SERVICE UNIT TESTS
 * =============================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Department Service Unit Tests")
class DepartmentServiceImplTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private DepartmentServiceImpl departmentService;

    private Department department;
    private DepartmentRequestDTO requestDTO;
    private Employee manager;

    @BeforeEach
    void setUp() {
        manager = Employee.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@company.com")
                .build();

        department = Department.builder()
                .id(1L)
                .code("IT")
                .name("Information Technology")
                .description("IT Department")
                .manager(manager)
                .location("Building A")
                .contactEmail("it@company.com")
                .budget(500000.0)
                .active(true)
                .employees(new ArrayList<>())
                .build();

        requestDTO = DepartmentRequestDTO.builder()
                .code("IT")
                .name("Information Technology")
                .description("IT Department")
                .managerId(1L)
                .location("Building A")
                .contactEmail("it@company.com")
                .budget(500000.0)
                .active(true)
                .build();
    }

    // ==========================================================================
    // CREATE DEPARTMENT TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Create Department Tests")
    class CreateDepartmentTests {

        @Test
        @DisplayName("Should create department successfully")
        void createDepartment_WhenValidData_ShouldReturnCreatedDepartment() {
            // Given
            given(departmentRepository.existsByCode(anyString())).willReturn(false);
            given(departmentRepository.existsByName(anyString())).willReturn(false);
            given(employeeRepository.findById(1L)).willReturn(Optional.of(manager));
            given(departmentRepository.save(any(Department.class))).willReturn(department);

            // When
            DepartmentResponseDTO result = departmentService.createDepartment(requestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("IT");
            assertThat(result.getName()).isEqualTo("Information Technology");
            verify(departmentRepository, times(1)).save(any(Department.class));
        }

        @Test
        @DisplayName("Should throw exception when department code already exists")
        void createDepartment_WhenCodeExists_ShouldThrowException() {
            // Given
            given(departmentRepository.existsByCode(anyString())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> departmentService.createDepartment(requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("code");

            verify(departmentRepository, never()).save(any(Department.class));
        }

        @Test
        @DisplayName("Should throw exception when department name already exists")
        void createDepartment_WhenNameExists_ShouldThrowException() {
            // Given
            given(departmentRepository.existsByCode(anyString())).willReturn(false);
            given(departmentRepository.existsByName(anyString())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> departmentService.createDepartment(requestDTO))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("name");

            verify(departmentRepository, never()).save(any(Department.class));
        }
    }

    // ==========================================================================
    // GET DEPARTMENT TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Get Department Tests")
    class GetDepartmentTests {

        @Test
        @DisplayName("Should return department when found by ID")
        void getDepartmentById_WhenExists_ShouldReturnDepartment() {
            // Given
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));

            // When
            DepartmentResponseDTO result = departmentService.getDepartmentById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getCode()).isEqualTo("IT");
        }

        @Test
        @DisplayName("Should throw exception when department not found by ID")
        void getDepartmentById_WhenNotExists_ShouldThrowException() {
            // Given
            given(departmentRepository.findById(anyLong())).willReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> departmentService.getDepartmentById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should return department when found by code")
        void getDepartmentByCode_WhenExists_ShouldReturnDepartment() {
            // Given
            given(departmentRepository.findByCodeIgnoreCase("IT")).willReturn(Optional.of(department));

            // When
            DepartmentResponseDTO result = departmentService.getDepartmentByCode("IT");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCode()).isEqualTo("IT");
        }
    }

    // ==========================================================================
    // UPDATE DEPARTMENT TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Update Department Tests")
    class UpdateDepartmentTests {

        @Test
        @DisplayName("Should update department successfully")
        void updateDepartment_WhenValidData_ShouldReturnUpdatedDepartment() {
            // Given
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
            given(departmentRepository.existsByCodeAndIdNot(anyString(), anyLong())).willReturn(false);
            given(departmentRepository.existsByNameAndIdNot(anyString(), anyLong())).willReturn(false);
            given(employeeRepository.findById(1L)).willReturn(Optional.of(manager));
            given(departmentRepository.save(any(Department.class))).willReturn(department);

            requestDTO.setName("IT Department Updated");

            // When
            DepartmentResponseDTO result = departmentService.updateDepartment(1L, requestDTO);

            // Then
            assertThat(result).isNotNull();
            verify(departmentRepository, times(1)).save(any(Department.class));
        }

        @Test
        @DisplayName("Should throw exception when updating to existing code")
        void updateDepartment_WhenCodeExistsForAnother_ShouldThrowException() {
            // Given
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
            given(departmentRepository.existsByCodeAndIdNot(anyString(), anyLong())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> departmentService.updateDepartment(1L, requestDTO))
                    .isInstanceOf(BadRequestException.class);
        }
    }

    // ==========================================================================
    // DELETE DEPARTMENT TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Delete Department Tests")
    class DeleteDepartmentTests {

        @Test
        @DisplayName("Should delete department successfully when no employees")
        void deleteDepartment_WhenNoEmployees_ShouldDeleteSuccessfully() {
            // Given
            department.setEmployees(new ArrayList<>());
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
            doNothing().when(departmentRepository).delete(any(Department.class));

            // When
            departmentService.deleteDepartment(1L);

            // Then
            verify(departmentRepository, times(1)).delete(department);
        }

        @Test
        @DisplayName("Should throw exception when department has employees")
        void deleteDepartment_WhenHasEmployees_ShouldThrowException() {
            // Given
            department.setEmployees(List.of(manager));
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));

            // When & Then
            assertThatThrownBy(() -> departmentService.deleteDepartment(1L))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("employees");

            verify(departmentRepository, never()).delete(any(Department.class));
        }
    }

    // ==========================================================================
    // DEACTIVATE/ACTIVATE DEPARTMENT TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Department Status Tests")
    class DepartmentStatusTests {

        @Test
        @DisplayName("Should deactivate department successfully")
        void deactivateDepartment_ShouldSetActiveToFalse() {
            // Given
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
            given(departmentRepository.save(any(Department.class))).willAnswer(invocation -> {
                Department d = invocation.getArgument(0);
                d.setActive(false);
                return d;
            });

            // When
            DepartmentResponseDTO result = departmentService.deactivateDepartment(1L);

            // Then
            assertThat(result).isNotNull();
            verify(departmentRepository, times(1)).save(any(Department.class));
        }

        @Test
        @DisplayName("Should activate department successfully")
        void activateDepartment_ShouldSetActiveToTrue() {
            // Given
            department.setActive(false);
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
            given(departmentRepository.save(any(Department.class))).willAnswer(invocation -> {
                Department d = invocation.getArgument(0);
                d.setActive(true);
                return d;
            });

            // When
            DepartmentResponseDTO result = departmentService.activateDepartment(1L);

            // Then
            assertThat(result).isNotNull();
            verify(departmentRepository, times(1)).save(any(Department.class));
        }
    }

    // ==========================================================================
    // MANAGER OPERATIONS TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Manager Operations Tests")
    class ManagerOperationsTests {

        @Test
        @DisplayName("Should assign manager successfully")
        void assignManager_ShouldSetManagerToDepartment() {
            // Given
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
            given(employeeRepository.findById(2L)).willReturn(Optional.of(manager));
            given(departmentRepository.save(any(Department.class))).willReturn(department);

            // When
            DepartmentResponseDTO result = departmentService.assignManager(1L, 2L);

            // Then
            assertThat(result).isNotNull();
            verify(departmentRepository, times(1)).save(any(Department.class));
        }

        @Test
        @DisplayName("Should remove manager successfully")
        void removeManager_ShouldSetManagerToNull() {
            // Given
            given(departmentRepository.findById(1L)).willReturn(Optional.of(department));
            given(departmentRepository.save(any(Department.class))).willReturn(department);

            // When
            DepartmentResponseDTO result = departmentService.removeManager(1L);

            // Then
            assertThat(result).isNotNull();
            verify(departmentRepository, times(1)).save(any(Department.class));
        }
    }

    // ==========================================================================
    // GET ALL AND SEARCH TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Get All and Search Tests")
    class GetAllAndSearchTests {

        @Test
        @DisplayName("Should return all departments paginated")
        void getAllDepartments_ShouldReturnPagedResponse() {
            // Given
            List<Department> departments = Arrays.asList(department);
            Page<Department> page = new PageImpl<>(departments);
            given(departmentRepository.findAll(any(Pageable.class))).willReturn(page);

            // When
            PagedResponseDTO<DepartmentResponseDTO> result = 
                    departmentService.getAllDepartments(0, 10, "name", "asc");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should return active departments as list")
        void getAllActiveDepartmentsList_ShouldReturnList() {
            // Given
            given(departmentRepository.findByActiveTrue()).willReturn(List.of(department));

            // When
            List<DepartmentResponseDTO> result = departmentService.getAllActiveDepartmentsList();

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getActive()).isTrue();
        }

        @Test
        @DisplayName("Should search departments by keyword")
        void searchDepartments_ShouldReturnMatchingDepartments() {
            // Given
            Page<Department> page = new PageImpl<>(List.of(department));
            given(departmentRepository.searchDepartments(anyString(), any(Pageable.class)))
                    .willReturn(page);

            // When
            PagedResponseDTO<DepartmentResponseDTO> result = 
                    departmentService.searchDepartments("IT", 0, 10, "name", "asc");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }
    }

    // ==========================================================================
    // VALIDATION TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @DisplayName("Should return true when code exists")
        void isDepartmentCodeExists_WhenExists_ShouldReturnTrue() {
            // Given
            given(departmentRepository.existsByCode("IT")).willReturn(true);

            // When
            boolean result = departmentService.isDepartmentCodeExists("IT");

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return true when name exists")
        void isDepartmentNameExists_WhenExists_ShouldReturnTrue() {
            // Given
            given(departmentRepository.existsByName("Information Technology")).willReturn(true);

            // When
            boolean result = departmentService.isDepartmentNameExists("Information Technology");

            // Then
            assertThat(result).isTrue();
        }
    }
}

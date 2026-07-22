package com.ems.controller;

import com.ems.dto.ApiResponse;
import com.ems.dto.EmployeeRequestDTO;
import com.ems.dto.EmployeeResponseDTO;
import com.ems.dto.PagedResponseDTO;
import com.ems.exception.ResourceNotFoundException;
import com.ems.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * =============================================================================
 * EMPLOYEE CONTROLLER UNIT TESTS
 * =============================================================================
 * 
 * Tests for EmployeeController using @WebMvcTest and MockMvc.
 * 
 * KEY CONCEPTS:
 * -------------
 * 1. @WebMvcTest: Only loads web layer, not full context
 * 2. @MockBean: Creates mock of service in Spring context
 * 3. MockMvc: Simulates HTTP requests without starting server
 * 4. @WithMockUser: Provides mock authentication
 * 
 * WHAT IS TESTED:
 * ---------------
 * - HTTP status codes
 * - Response structure
 * - Request validation
 * - Security (authentication/authorization)
 * 
 * =============================================================================
 */
@WebMvcTest(EmployeeController.class)
@DisplayName("Employee Controller Unit Tests")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeRequestDTO requestDTO;
    private EmployeeResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
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

        responseDTO = EmployeeResponseDTO.builder()
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
                .status("ACTIVE")
                .build();
    }

    // ==========================================================================
    // CREATE EMPLOYEE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Create Employee Tests")
    class CreateEmployeeTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should create employee when ADMIN")
        void createEmployee_WhenAdmin_ShouldReturn201() throws Exception {
            // Given
            given(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                    .willReturn(responseDTO);

            // When
            ResultActions result = mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)));

            // Then
            result.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", containsString("created")))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.firstName", is("John")))
                    .andExpect(jsonPath("$.data.email", is("john.doe@company.com")))
                    .andDo(print());

            verify(employeeService, times(1)).createEmployee(any(EmployeeRequestDTO.class));
        }

        @Test
        @WithMockUser(roles = "HR")
        @DisplayName("Should create employee when HR")
        void createEmployee_WhenHR_ShouldReturn201() throws Exception {
            // Given
            given(employeeService.createEmployee(any(EmployeeRequestDTO.class)))
                    .willReturn(responseDTO);

            // When & Then
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isCreated());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("Should return 403 when EMPLOYEE tries to create")
        void createEmployee_WhenEmployee_ShouldReturn403() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());

            verify(employeeService, never()).createEmployee(any());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void createEmployee_WhenNotAuthenticated_ShouldReturn401() throws Exception {
            // When & Then
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return 400 when validation fails")
        void createEmployee_WhenInvalidData_ShouldReturn400() throws Exception {
            // Given - Invalid request with missing required fields
            EmployeeRequestDTO invalidRequest = EmployeeRequestDTO.builder()
                    .firstName("")  // Invalid: blank
                    .lastName("")   // Invalid: blank
                    .email("invalid-email")  // Invalid format
                    .build();

            // When & Then
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andDo(print());
        }
    }

    // ==========================================================================
    // GET EMPLOYEE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Get Employee Tests")
    class GetEmployeeTests {

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("Should return employee by ID")
        void getEmployeeById_WhenExists_ShouldReturn200() throws Exception {
            // Given
            given(employeeService.getEmployeeById(1L)).willReturn(responseDTO);

            // When & Then
            mockMvc.perform(get("/api/employees/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.employeeCode", is("EMP001")))
                    .andDo(print());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("Should return 404 when employee not found")
        void getEmployeeById_WhenNotExists_ShouldReturn404() throws Exception {
            // Given
            given(employeeService.getEmployeeById(anyLong()))
                    .willThrow(new ResourceNotFoundException("Employee", "id", 999L));

            // When & Then
            mockMvc.perform(get("/api/employees/999"))
                    .andExpect(status().isNotFound())
                    .andDo(print());
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("Should return employee by code")
        void getEmployeeByCode_WhenExists_ShouldReturn200() throws Exception {
            // Given
            given(employeeService.getEmployeeByCode("EMP001")).willReturn(responseDTO);

            // When & Then
            mockMvc.perform(get("/api/employees/code/EMP001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.employeeCode", is("EMP001")));
        }
    }

    // ==========================================================================
    // GET ALL EMPLOYEES TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Get All Employees Tests")
    class GetAllEmployeesTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should return paginated employees")
        void getAllEmployees_ShouldReturnPagedResponse() throws Exception {
            // Given
            PagedResponseDTO<EmployeeResponseDTO> pagedResponse = PagedResponseDTO.<EmployeeResponseDTO>builder()
                    .content(List.of(responseDTO))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .last(true)
                    .first(true)
                    .build();

            given(employeeService.getAllEmployees(0, 10, "firstName", "asc"))
                    .willReturn(pagedResponse);

            // When & Then
            mockMvc.perform(get("/api/employees")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sortBy", "firstName")
                    .param("sortDir", "asc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.totalElements", is(1)))
                    .andExpect(jsonPath("$.data.pageNumber", is(0)))
                    .andDo(print());
        }
    }

    // ==========================================================================
    // UPDATE EMPLOYEE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Update Employee Tests")
    class UpdateEmployeeTests {

        @Test
        @WithMockUser(roles = "HR")
        @DisplayName("Should update employee successfully")
        void updateEmployee_WhenHR_ShouldReturn200() throws Exception {
            // Given
            given(employeeService.updateEmployee(anyLong(), any(EmployeeRequestDTO.class)))
                    .willReturn(responseDTO);

            // When & Then
            mockMvc.perform(put("/api/employees/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", containsString("updated")));
        }

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("Should return 403 when EMPLOYEE tries to update")
        void updateEmployee_WhenEmployee_ShouldReturn403() throws Exception {
            // When & Then
            mockMvc.perform(put("/api/employees/1")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDTO)))
                    .andExpect(status().isForbidden());
        }
    }

    // ==========================================================================
    // DELETE EMPLOYEE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Delete Employee Tests")
    class DeleteEmployeeTests {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Should delete employee when ADMIN")
        void deleteEmployee_WhenAdmin_ShouldReturn200() throws Exception {
            // Given
            doNothing().when(employeeService).deleteEmployee(1L);

            // When & Then
            mockMvc.perform(delete("/api/employees/1")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", containsString("deleted")));

            verify(employeeService, times(1)).deleteEmployee(1L);
        }

        @Test
        @WithMockUser(roles = "HR")
        @DisplayName("Should return 403 when HR tries to delete")
        void deleteEmployee_WhenHR_ShouldReturn403() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/employees/1")
                    .with(csrf()))
                    .andExpect(status().isForbidden());

            verify(employeeService, never()).deleteEmployee(anyLong());
        }
    }

    // ==========================================================================
    // SEARCH EMPLOYEES TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Search Employees Tests")
    class SearchEmployeesTests {

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("Should search employees by keyword")
        void searchEmployees_ShouldReturnMatchingEmployees() throws Exception {
            // Given
            PagedResponseDTO<EmployeeResponseDTO> pagedResponse = PagedResponseDTO.<EmployeeResponseDTO>builder()
                    .content(List.of(responseDTO))
                    .pageNumber(0)
                    .pageSize(10)
                    .totalElements(1)
                    .totalPages(1)
                    .build();

            given(employeeService.searchEmployees(anyString(), anyInt(), anyInt(), anyString(), anyString()))
                    .willReturn(pagedResponse);

            // When & Then
            mockMvc.perform(get("/api/employees/search")
                    .param("keyword", "John"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].firstName", is("John")));
        }
    }

    // ==========================================================================
    // DEACTIVATE EMPLOYEE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Deactivate Employee Tests")
    class DeactivateEmployeeTests {

        @Test
        @WithMockUser(roles = "HR")
        @DisplayName("Should deactivate employee successfully")
        void deactivateEmployee_WhenHR_ShouldReturn200() throws Exception {
            // Given
            responseDTO.setStatus("INACTIVE");
            given(employeeService.deactivateEmployee(1L)).willReturn(responseDTO);

            // When & Then
            mockMvc.perform(patch("/api/employees/1/deactivate")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.message", containsString("deactivated")));
        }
    }

    // ==========================================================================
    // UTILITY ENDPOINTS TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Utility Endpoints Tests")
    class UtilityEndpointsTests {

        @Test
        @WithMockUser(roles = "EMPLOYEE")
        @DisplayName("Should return all departments")
        void getAllDepartments_ShouldReturnList() throws Exception {
            // Given
            given(employeeService.getAllDepartments())
                    .willReturn(Arrays.asList("IT", "HR", "Finance"));

            // When & Then
            mockMvc.perform(get("/api/employees/departments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(3)))
                    .andExpect(jsonPath("$.data[0]", is("IT")));
        }

        @Test
        @WithMockUser(roles = "HR")
        @DisplayName("Should check if email exists")
        void checkEmailExists_ShouldReturnBoolean() throws Exception {
            // Given
            given(employeeService.isEmailExists("test@company.com")).willReturn(true);

            // When & Then
            mockMvc.perform(get("/api/employees/check-email")
                    .param("email", "test@company.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", is(true)));
        }
    }
}

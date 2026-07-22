package com.ems.integration;

import com.ems.dto.AuthResponse;
import com.ems.dto.EmployeeRequestDTO;
import com.ems.dto.LoginRequest;
import com.ems.dto.RegisterRequest;
import com.ems.entity.Role;
import com.ems.repository.EmployeeRepository;
import com.ems.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * =============================================================================
 * INTEGRATION TESTS
 * =============================================================================
 * 
 * Integration tests that test the full application stack including:
 * - Controllers
 * - Services
 * - Repositories
 * - Database (H2 in-memory)
 * - Security
 * 
 * KEY ANNOTATIONS:
 * ----------------
 * @SpringBootTest: Loads full application context
 * @AutoConfigureMockMvc: Configures MockMvc for testing
 * @ActiveProfiles("test"): Uses test profile (H2 database)
 * @TestMethodOrder: Orders test execution (important for dependent tests)
 * 
 * WHAT MAKES THIS DIFFERENT FROM UNIT TESTS?
 * ------------------------------------------
 * - Uses real database (H2)
 * - Uses real service implementations
 * - Tests actual HTTP flow
 * - Slower but more comprehensive
 * 
 * =============================================================================
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Integration Tests")
class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    private static String adminToken;
    private static String hrToken;
    private static String employeeToken;
    private static Long createdEmployeeId;

    // ==========================================================================
    // SETUP AND CLEANUP
    // ==========================================================================

    @BeforeAll
    static void beforeAll() {
        // Initialize static variables
        adminToken = null;
        hrToken = null;
        employeeToken = null;
        createdEmployeeId = null;
    }

    // ==========================================================================
    // AUTHENTICATION INTEGRATION TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Authentication Integration Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class AuthenticationIntegrationTests {

        @Test
        @Order(1)
        @DisplayName("Should register admin user successfully")
        void registerAdmin_ShouldSucceed() throws Exception {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("admin")
                    .email("admin@company.com")
                    .password("admin123")
                    .firstName("Admin")
                    .lastName("User")
                    .role(Role.ADMIN)
                    .build();

            // When
            MvcResult result = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.token").exists())
                    .andExpect(jsonPath("$.data.username", is("admin")))
                    .andExpect(jsonPath("$.data.role", is("ADMIN")))
                    .andDo(print())
                    .andReturn();

            // Store token for later tests
            String responseBody = result.getResponse().getContentAsString();
            AuthResponse authResponse = objectMapper.readValue(
                    objectMapper.readTree(responseBody).get("data").toString(),
                    AuthResponse.class
            );
            adminToken = authResponse.getToken();
        }

        @Test
        @Order(2)
        @DisplayName("Should register HR user successfully")
        void registerHR_ShouldSucceed() throws Exception {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("hruser")
                    .email("hr@company.com")
                    .password("hr123456")
                    .firstName("HR")
                    .lastName("Manager")
                    .role(Role.HR)
                    .build();

            // When
            MvcResult result = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.role", is("HR")))
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            AuthResponse authResponse = objectMapper.readValue(
                    objectMapper.readTree(responseBody).get("data").toString(),
                    AuthResponse.class
            );
            hrToken = authResponse.getToken();
        }

        @Test
        @Order(3)
        @DisplayName("Should register employee user successfully")
        void registerEmployee_ShouldSucceed() throws Exception {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("employee")
                    .email("employee@company.com")
                    .password("emp123456")
                    .firstName("John")
                    .lastName("Employee")
                    .role(Role.EMPLOYEE)
                    .build();

            // When
            MvcResult result = mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.role", is("EMPLOYEE")))
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            AuthResponse authResponse = objectMapper.readValue(
                    objectMapper.readTree(responseBody).get("data").toString(),
                    AuthResponse.class
            );
            employeeToken = authResponse.getToken();
        }

        @Test
        @Order(4)
        @DisplayName("Should fail registration with duplicate username")
        void registerDuplicateUsername_ShouldFail() throws Exception {
            // Given
            RegisterRequest request = RegisterRequest.builder()
                    .username("admin")  // Already exists
                    .email("another@company.com")
                    .password("password123")
                    .firstName("Another")
                    .lastName("User")
                    .role(Role.EMPLOYEE)
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("Username")));
        }

        @Test
        @Order(5)
        @DisplayName("Should login successfully")
        void login_ShouldSucceed() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .usernameOrEmail("admin")
                    .password("admin123")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.token").exists())
                    .andExpect(jsonPath("$.data.username", is("admin")));
        }

        @Test
        @Order(6)
        @DisplayName("Should fail login with wrong password")
        void loginWrongPassword_ShouldFail() throws Exception {
            // Given
            LoginRequest request = LoginRequest.builder()
                    .usernameOrEmail("admin")
                    .password("wrongpassword")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ==========================================================================
    // EMPLOYEE CRUD INTEGRATION TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Employee CRUD Integration Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class EmployeeCrudIntegrationTests {

        @Test
        @Order(1)
        @DisplayName("ADMIN should create employee successfully")
        void adminCreateEmployee_ShouldSucceed() throws Exception {
            // Given
            EmployeeRequestDTO request = EmployeeRequestDTO.builder()
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

            // When
            MvcResult result = mockMvc.perform(post("/api/employees")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success", is(true)))
                    .andExpect(jsonPath("$.data.firstName", is("John")))
                    .andExpect(jsonPath("$.data.email", is("john.doe@company.com")))
                    .andExpect(jsonPath("$.data.employeeCode").exists())
                    .andDo(print())
                    .andReturn();

            // Store created employee ID for later tests
            String responseBody = result.getResponse().getContentAsString();
            createdEmployeeId = objectMapper.readTree(responseBody)
                    .get("data").get("id").asLong();
        }

        @Test
        @Order(2)
        @DisplayName("Should get employee by ID")
        void getEmployeeById_ShouldSucceed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/employees/" + createdEmployeeId)
                    .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(createdEmployeeId.intValue())))
                    .andExpect(jsonPath("$.data.firstName", is("John")));
        }

        @Test
        @Order(3)
        @DisplayName("Should get all employees paginated")
        void getAllEmployees_ShouldSucceed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/employees")
                    .header("Authorization", "Bearer " + hrToken)
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements", greaterThan(0)));
        }

        @Test
        @Order(4)
        @DisplayName("HR should update employee successfully")
        void hrUpdateEmployee_ShouldSucceed() throws Exception {
            // Given
            EmployeeRequestDTO request = EmployeeRequestDTO.builder()
                    .firstName("John")
                    .lastName("Doe Updated")
                    .email("john.doe@company.com")
                    .phoneNumber("9876543210")
                    .department("IT")
                    .designation("Senior Software Engineer")
                    .salary(new BigDecimal("85000.00"))
                    .dateOfBirth(LocalDate.of(1990, 5, 15))
                    .dateOfJoining(LocalDate.of(2020, 1, 10))
                    .build();

            // When & Then
            mockMvc.perform(put("/api/employees/" + createdEmployeeId)
                    .header("Authorization", "Bearer " + hrToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.lastName", is("Doe Updated")))
                    .andExpect(jsonPath("$.data.designation", is("Senior Software Engineer")));
        }

        @Test
        @Order(5)
        @DisplayName("Should search employees by keyword")
        void searchEmployees_ShouldSucceed() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/employees/search")
                    .header("Authorization", "Bearer " + employeeToken)
                    .param("keyword", "John"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }

        @Test
        @Order(6)
        @DisplayName("EMPLOYEE should not be able to delete employee")
        void employeeDeleteEmployee_ShouldFail() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/employees/" + createdEmployeeId)
                    .header("Authorization", "Bearer " + employeeToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(7)
        @DisplayName("HR should not be able to delete employee")
        void hrDeleteEmployee_ShouldFail() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/employees/" + createdEmployeeId)
                    .header("Authorization", "Bearer " + hrToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @Order(8)
        @DisplayName("ADMIN should be able to delete employee")
        void adminDeleteEmployee_ShouldSucceed() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/employees/" + createdEmployeeId)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", containsString("deleted")));
        }

        @Test
        @Order(9)
        @DisplayName("Should return 404 for deleted employee")
        void getDeletedEmployee_ShouldReturn404() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/employees/" + createdEmployeeId)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNotFound());
        }
    }

    // ==========================================================================
    // SECURITY INTEGRATION TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Security Integration Tests")
    class SecurityIntegrationTests {

        @Test
        @DisplayName("Should return 401 for unauthenticated request")
        void unauthenticatedRequest_ShouldReturn401() throws Exception {
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 401 for invalid token")
        void invalidToken_ShouldReturn401() throws Exception {
            mockMvc.perform(get("/api/employees")
                    .header("Authorization", "Bearer invalid.token.here"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Public endpoints should be accessible without authentication")
        void publicEndpoints_ShouldBeAccessible() throws Exception {
            // Swagger UI should be accessible
            mockMvc.perform(get("/swagger-ui.html"))
                    .andExpect(status().is3xxRedirection());

            // API docs should be accessible
            mockMvc.perform(get("/v3/api-docs"))
                    .andExpect(status().isOk());
        }
    }
}

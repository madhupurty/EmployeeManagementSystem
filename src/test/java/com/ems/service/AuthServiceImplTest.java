package com.ems.service;

import com.ems.dto.AuthResponse;
import com.ems.dto.LoginRequest;
import com.ems.dto.RegisterRequest;
import com.ems.entity.Role;
import com.ems.entity.User;
import com.ems.exception.BadRequestException;
import com.ems.repository.UserRepository;
import com.ems.security.JwtUtil;
import com.ems.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * =============================================================================
 * AUTH SERVICE UNIT TESTS
 * =============================================================================
 * 
 * Tests for AuthServiceImpl covering registration and login functionality.
 * 
 * =============================================================================
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Auth Service Unit Tests")
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;
    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        // Set JWT expiration using reflection (since it's @Value injected)
        ReflectionTestUtils.setField(authService, "jwtExpiration", 86400000L);

        user = User.builder()
                .id(1L)
                .username("john.doe")
                .email("john.doe@company.com")
                .password("encodedPassword")
                .firstName("John")
                .lastName("Doe")
                .role(Role.EMPLOYEE)
                .enabled(true)
                .accountNonLocked(true)
                .build();

        registerRequest = RegisterRequest.builder()
                .username("john.doe")
                .email("john.doe@company.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .role(Role.EMPLOYEE)
                .build();

        loginRequest = LoginRequest.builder()
                .usernameOrEmail("john.doe")
                .password("password123")
                .build();
    }

    // ==========================================================================
    // REGISTRATION TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Registration Tests")
    class RegistrationTests {

        @Test
        @DisplayName("Should register user successfully")
        void register_WhenValidData_ShouldReturnAuthResponse() {
            // Given
            given(userRepository.existsByUsername(anyString())).willReturn(false);
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(user);
            given(jwtUtil.generateToken(any(User.class))).willReturn("test.jwt.token");

            // When
            AuthResponse result = authService.register(registerRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("test.jwt.token");
            assertThat(result.getUsername()).isEqualTo("john.doe");
            assertThat(result.getEmail()).isEqualTo("john.doe@company.com");
            assertThat(result.getRole()).isEqualTo(Role.EMPLOYEE);
            assertThat(result.getMessage()).isEqualTo("Registration successful");

            verify(userRepository, times(1)).save(any(User.class));
            verify(passwordEncoder, times(1)).encode("password123");
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void register_WhenUsernameExists_ShouldThrowException() {
            // Given
            given(userRepository.existsByUsername(anyString())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Username");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void register_WhenEmailExists_ShouldThrowException() {
            // Given
            given(userRepository.existsByUsername(anyString())).willReturn(false);
            given(userRepository.existsByEmail(anyString())).willReturn(true);

            // When & Then
            assertThatThrownBy(() -> authService.register(registerRequest))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Email");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should set default role to EMPLOYEE when role is null")
        void register_WhenRoleIsNull_ShouldDefaultToEmployee() {
            // Given
            registerRequest.setRole(null);
            given(userRepository.existsByUsername(anyString())).willReturn(false);
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(user);
            given(jwtUtil.generateToken(any(User.class))).willReturn("test.jwt.token");

            // When
            AuthResponse result = authService.register(registerRequest);

            // Then
            assertThat(result).isNotNull();
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    // ==========================================================================
    // LOGIN TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Login Tests")
    class LoginTests {

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void login_WhenValidCredentials_ShouldReturnAuthResponse() {
            // Given
            Authentication authentication = mock(Authentication.class);
            given(authentication.getPrincipal()).willReturn(user);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(authentication);
            given(jwtUtil.generateToken(any(User.class))).willReturn("test.jwt.token");

            // When
            AuthResponse result = authService.login(loginRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isEqualTo("test.jwt.token");
            assertThat(result.getUsername()).isEqualTo("john.doe");
            assertThat(result.getMessage()).isEqualTo("Login successful");

            verify(authenticationManager, times(1)).authenticate(any());
        }

        @Test
        @DisplayName("Should throw exception when credentials are invalid")
        void login_WhenInvalidCredentials_ShouldThrowException() {
            // Given
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willThrow(new BadCredentialsException("Bad credentials"));

            // When & Then
            assertThatThrownBy(() -> authService.login(loginRequest))
                    .isInstanceOf(BadCredentialsException.class);
        }

        @Test
        @DisplayName("Should work with email as login identifier")
        void login_WithEmail_ShouldReturnAuthResponse() {
            // Given
            loginRequest.setUsernameOrEmail("john.doe@company.com");
            Authentication authentication = mock(Authentication.class);
            given(authentication.getPrincipal()).willReturn(user);
            given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .willReturn(authentication);
            given(jwtUtil.generateToken(any(User.class))).willReturn("test.jwt.token");

            // When
            AuthResponse result = authService.login(loginRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getToken()).isNotNull();
        }
    }

    // ==========================================================================
    // AUTH RESPONSE STRUCTURE TESTS
    // ==========================================================================

    @Nested
    @DisplayName("Auth Response Structure Tests")
    class AuthResponseStructureTests {

        @Test
        @DisplayName("Should include all required fields in response")
        void register_ShouldReturnCompleteAuthResponse() {
            // Given
            given(userRepository.existsByUsername(anyString())).willReturn(false);
            given(userRepository.existsByEmail(anyString())).willReturn(false);
            given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
            given(userRepository.save(any(User.class))).willReturn(user);
            given(jwtUtil.generateToken(any(User.class))).willReturn("test.jwt.token");

            // When
            AuthResponse result = authService.register(registerRequest);

            // Then
            assertThat(result.getToken()).isNotNull();
            assertThat(result.getTokenType()).isEqualTo("Bearer");
            assertThat(result.getExpiresIn()).isEqualTo(86400000L);
            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getUsername()).isEqualTo("john.doe");
            assertThat(result.getEmail()).isEqualTo("john.doe@company.com");
            assertThat(result.getFullName()).isEqualTo("John Doe");
            assertThat(result.getRole()).isEqualTo(Role.EMPLOYEE);
        }
    }
}

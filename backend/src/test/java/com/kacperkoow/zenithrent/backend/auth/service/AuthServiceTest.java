package com.kacperkoow.zenithrent.backend.auth.service;

import com.kacperkoow.zenithrent.backend.auth.dto.LoginRequest;
import com.kacperkoow.zenithrent.backend.auth.dto.LoginResponse;
import com.kacperkoow.zenithrent.backend.auth.dto.RegisterRequest;
import com.kacperkoow.zenithrent.backend.auth.dto.RegisterResponse;
import com.kacperkoow.zenithrent.backend.user.model.Role;
import com.kacperkoow.zenithrent.backend.user.model.User;
import com.kacperkoow.zenithrent.backend.user.repository.UserRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .password("encoded_password")
                .role(Role.CUSTOMER)
                .build();
    }

    @Nested
    @DisplayName("Tests for user registration")
    class RegisterTests {

        @Test
        @DisplayName("Should throw Exception when email is already in use")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            RegisterRequest request = new RegisterRequest("John", "Doe","john.doe@example.com", "password123");

            when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> authService.register(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User with this email already exists");

            verify(userRepository, never()).save(any(User.class));
            verify(jwtService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should encode password, save user and return RegisterResponse with JWT")
        void shouldRegisterUserSuccessfully() {
            RegisterRequest request = new RegisterRequest( "Alice", "Smith","new.user@example.com", "rawPassword");

            when(userRepository.findByEmail("new.user@example.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("rawPassword")).thenReturn("hashedPassword");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User saved = invocation.getArgument(0);
                saved.setId(2L);
                return saved;
            });
            when(jwtService.generateToken(any(User.class))).thenReturn("mocked_jwt_token");

            RegisterResponse response = authService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("mocked_jwt_token");
            assertThat(response.id()).isEqualTo(2L);
            assertThat(response.email()).isEqualTo("new.user@example.com");
            assertThat(response.role()).isEqualTo("CUSTOMER");

            verify(passwordEncoder, times(1)).encode("rawPassword");
            verify(userRepository, times(1)).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Tests for user login")
    class LoginTests {

        @Test
        @DisplayName("Should throw Exception when authentication fails")
        void shouldThrowExceptionWhenAuthenticationFails() {
            LoginRequest request = new LoginRequest("john.doe@example.com", "wrongPassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Bad credentials");

            verify(userRepository, never()).findByEmail(anyString());
        }

        @Test
        @DisplayName("Should throw Exception when authenticated user is not found in database")
        void shouldThrowExceptionWhenUserNotFoundAfterAuth() {
            LoginRequest request = new LoginRequest("unknown@example.com", "password123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("Should authenticate and return LoginResponse with JWT")
        void shouldLoginSuccessfully() {
            LoginRequest request = new LoginRequest("john.doe@example.com", "password123");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenReturn(null);
            when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(sampleUser));
            when(jwtService.generateToken(sampleUser)).thenReturn("mocked_jwt_token");

            LoginResponse response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("mocked_jwt_token");
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("john.doe@example.com");
            assertThat(response.role()).isEqualTo("CUSTOMER");

            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }
}
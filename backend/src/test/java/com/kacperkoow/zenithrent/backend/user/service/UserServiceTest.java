package com.kacperkoow.zenithrent.backend.user.service;

import com.kacperkoow.zenithrent.backend.user.dto.UserAdminUpdateRequest;
import com.kacperkoow.zenithrent.backend.user.dto.UserCreateRequest;
import com.kacperkoow.zenithrent.backend.user.dto.UserResponse;
import com.kacperkoow.zenithrent.backend.user.dto.UserSelfUpdateRequest;
import com.kacperkoow.zenithrent.backend.user.model.Role;
import com.kacperkoow.zenithrent.backend.user.model.User;
import com.kacperkoow.zenithrent.backend.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

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

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockSecurityContext(String email) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(email, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Nested
    @DisplayName("Tests for retrieving users")
    class GetUserTests {

        @Test
        @DisplayName("Should return all users mapped to responses")
        void shouldReturnAllUsers() {
            when(userRepository.findAll()).thenReturn(List.of(sampleUser));

            List<UserResponse> result = userService.getAllUsers();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).email()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should return user response when user exists by ID")
        void shouldReturnUserById() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

            UserResponse result = userService.getUserById(1L);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.email()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should throw Exception when user ID is not found")
        void shouldThrowExceptionWhenUserNotFound() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("Should return current user details based on security context session")
        void shouldReturnCurrentUser() {
            mockSecurityContext("john.doe@example.com");
            when(userRepository.findByEmail("john.doe@example.com")).thenReturn(Optional.of(sampleUser));

            UserResponse result = userService.getCurrentUser();

            assertThat(result).isNotNull();
            assertThat(result.email()).isEqualTo("john.doe@example.com");
        }

        @Test
        @DisplayName("Should throw Exception when current user session is not in database")
        void shouldThrowExceptionWhenCurrentSessionNotFound() {
            mockSecurityContext("unknown@example.com");
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getCurrentUser())
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Current user session not found");
        }
    }

    @Nested
    @DisplayName("Tests for creating users")
    class CreateUserTests {

        @Test
        @DisplayName("Should throw Exception when email is already taken upon creation")
        void shouldThrowExceptionWhenEmailExists() {
            UserCreateRequest request = new UserCreateRequest(
                    "John",
                    "Doe",
                    "john.doe@example.com",
                    "password123",
                    Role.CUSTOMER
            );

            when(userRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User with email john.doe@example.com already exists");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should encode password and save user successfully")
        void shouldCreateUserSuccessfully() {
            UserCreateRequest request = new UserCreateRequest(
                    "Alice",
                    "Smith",
                    "new.user@example.com",
                    "raw_password",
                    Role.CUSTOMER
            );

            when(userRepository.existsByEmail("new.user@example.com")).thenReturn(false);
            when(passwordEncoder.encode("raw_password")).thenReturn("encoded_raw_password");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User u = invocation.getArgument(0);
                u.setId(2L);
                return u;
            });

            UserResponse result = userService.createUser(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(2L);
            assertThat(result.email()).isEqualTo("new.user@example.com");
            verify(passwordEncoder, times(1)).encode("raw_password");
        }
    }

    @Nested
    @DisplayName("Tests for updating users")
    class UpdateUserTests {

        @Test
        @DisplayName("Should throw AccessDeniedException when trying to update another user profile")
        void shouldThrowAccessDeniedWhenUpdatingAnotherUser() {
            mockSecurityContext("hacker@example.com");
            UserSelfUpdateRequest request = new UserSelfUpdateRequest("john.doe@example.com", "John", "Doe");

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));

            assertThatThrownBy(() -> userService.updateSelf(1L, request))
                    .isInstanceOf(AccessDeniedException.class)
                    .hasMessage("You can only update your own profile");
        }

        @Test
        @DisplayName("Should throw Exception when changing to an already taken email in self update")
        void shouldThrowExceptionWhenSelfUpdateEmailTaken() {
            mockSecurityContext("john.doe@example.com");
            UserSelfUpdateRequest request = new UserSelfUpdateRequest( "John", "Doe","taken@example.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.updateSelf(1L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email taken@example.com is already taken");
        }

        @Test
        @DisplayName("Should update self profile successfully")
        void shouldUpdateSelfSuccessfully() {
            mockSecurityContext("john.doe@example.com");
            UserSelfUpdateRequest request = new UserSelfUpdateRequest( "Johnathan", "Doe-Updated","john.doe@example.com");

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponse result = userService.updateSelf(1L, request);

            assertThat(result).isNotNull();
            assertThat(result.firstName()).isEqualTo("Johnathan");
            assertThat(result.lastName()).isEqualTo("Doe-Updated");
        }

        @Test
        @DisplayName("Should update user role and details by Admin")
        void shouldUpdateByAdminSuccessfully() {
            UserAdminUpdateRequest request = new UserAdminUpdateRequest("john.doe@example.com", "John", "Doe", Role.EMPLOYEE);

            when(userRepository.findById(1L)).thenReturn(Optional.of(sampleUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserResponse result = userService.updateByAdmin(1L, request);

            assertThat(result).isNotNull();
            assertThat(result.role()).isEqualTo(Role.EMPLOYEE);
        }
    }

    @Nested
    @DisplayName("Tests for deleting users")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user when exists")
        void shouldDeleteUserSuccessfully() {
            when(userRepository.existsById(1L)).thenReturn(true);
            doNothing().when(userRepository).deleteById(1L);

            userService.deleteUser(1L);

            verify(userRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw Exception when deleting non-existent user")
        void shouldThrowExceptionWhenDeletingNonExistentUser() {
            when(userRepository.existsById(99L)).thenReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User with ID 99 does not exist");

            verify(userRepository, never()).deleteById(anyLong());
        }
    }
}
package com.kacperkoow.zenithrent.backend.user.service;

import com.kacperkoow.zenithrent.backend.booking.repository.BookingRepository;
import com.kacperkoow.zenithrent.backend.user.dto.UserAdminUpdateRequest;
import com.kacperkoow.zenithrent.backend.user.dto.UserCreateRequest;
import com.kacperkoow.zenithrent.backend.user.dto.UserResponse;
import com.kacperkoow.zenithrent.backend.user.dto.UserSelfUpdateRequest;
import com.kacperkoow.zenithrent.backend.user.model.User;
import com.kacperkoow.zenithrent.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return mapToUserResponse(user);
    }

    public UserResponse getCurrentUser() {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("Current user session not found"));

        return mapToUserResponse(user);
    }

    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("User with email " + request.email() + " already exists");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        User savedUser = userRepository.save(user);
        return mapToUserResponse(savedUser);
    }

    public UserResponse updateSelf(Long id, UserSelfUpdateRequest request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getEmail().equals(currentUserEmail)) {
            throw new AccessDeniedException("You can only update your own profile");
        }

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email " + request.email() + " is already taken");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public UserResponse updateByAdmin(Long id, UserAdminUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email " + request.email() + " is already taken");
        }

        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setRole(request.role());
        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User with ID " + id + " does not exist");
        }
        userRepository.deleteById(id);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole()
        );
    }
}
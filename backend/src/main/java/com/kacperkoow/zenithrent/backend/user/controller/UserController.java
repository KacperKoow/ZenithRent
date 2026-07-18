package com.kacperkoow.zenithrent.backend.user.controller;

import com.kacperkoow.zenithrent.backend.user.dto.UserAdminUpdateRequest;
import com.kacperkoow.zenithrent.backend.user.dto.UserCreateRequest;
import com.kacperkoow.zenithrent.backend.user.dto.UserResponse;
import com.kacperkoow.zenithrent.backend.user.dto.UserSelfUpdateRequest;
import com.kacperkoow.zenithrent.backend.user.model.User;
import com.kacperkoow.zenithrent.backend.user.repository.UserRepository;
import com.kacperkoow.zenithrent.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse userResponse = userService.getCurrentUser();
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateSelf(@RequestBody UserSelfUpdateRequest request) {
        String currentUserEmail = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return ResponseEntity.ok(userService.updateSelf(user.getId(), request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateByAdmin(
            @PathVariable Long id,
            @RequestBody UserAdminUpdateRequest request
    ) {
        return ResponseEntity.ok(userService.updateByAdmin(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
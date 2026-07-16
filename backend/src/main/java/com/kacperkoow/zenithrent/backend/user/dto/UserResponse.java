package com.kacperkoow.zenithrent.backend.user.dto;

import com.kacperkoow.zenithrent.backend.user.model.Role;

public record UserResponse(
        Long id,
        String email,
        String firstName,
        String lastName,
        Role role
) {}

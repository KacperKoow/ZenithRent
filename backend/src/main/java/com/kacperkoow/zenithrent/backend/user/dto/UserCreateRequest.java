package com.kacperkoow.zenithrent.backend.user.dto;

import com.kacperkoow.zenithrent.backend.user.model.Role;

public record UserCreateRequest(
        String firstName,
        String lastName,
        String email,
        String password,
        Role role
) {}
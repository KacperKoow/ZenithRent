package com.kacperkoow.zenithrent.backend.user.dto;

import com.kacperkoow.zenithrent.backend.user.model.Role;

public record UserAdminUpdateRequest(
        String firstName,
        String lastName,
        String email,
        Role role
) {}
package com.kacperkoow.zenithrent.backend.user.dto;

import com.kacperkoow.zenithrent.backend.user.model.Role;

public record UserSelfUpdateRequest(
        String firstName,
        String lastName,
        String email
) {}
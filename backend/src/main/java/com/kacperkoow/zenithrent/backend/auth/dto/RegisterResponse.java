package com.kacperkoow.zenithrent.backend.auth.dto;

public record RegisterResponse(
        String token,
        Long id,
        String email,
        String firstName,
        String lastName,
        String role
) {}
package com.kacperkoow.zenithrent.backend.auth.dto;

public record LoginResponse(
        String token,
        Long id,
        String email,
        String firstName,
        String lastName,
        String role
) {}
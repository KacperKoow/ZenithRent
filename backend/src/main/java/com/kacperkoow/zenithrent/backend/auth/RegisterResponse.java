package com.kacperkoow.zenithrent.backend.auth;

public record RegisterResponse(
        String token,
        Long id,
        String email,
        String firstName,
        String lastName,
        String role
) {}
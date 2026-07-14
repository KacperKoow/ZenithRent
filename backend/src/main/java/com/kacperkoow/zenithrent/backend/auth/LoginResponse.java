package com.kacperkoow.zenithrent.backend.auth;

public record LoginResponse(
        String token,
        Long id,
        String email,
        String firstName,
        String lastName,
        String role
) {}
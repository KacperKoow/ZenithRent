package com.kacperkoow.zenithrent.backend.auth.dto;

public record RegisterRequest(
        String firstName,
        String lastName,
        String email,
        String password
) {}
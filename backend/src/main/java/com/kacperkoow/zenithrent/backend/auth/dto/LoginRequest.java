package com.kacperkoow.zenithrent.backend.auth.dto;

public record LoginRequest(
        String email,
        String password
) {}
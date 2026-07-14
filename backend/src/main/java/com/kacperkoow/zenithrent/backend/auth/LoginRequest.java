package com.kacperkoow.zenithrent.backend.auth;

public record LoginRequest(
        String email,
        String password
) {}
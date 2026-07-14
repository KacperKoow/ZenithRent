package com.kacperkoow.zenithrent.backend.user;

public record RegisterRequest(
        String email,
        String password,
        String firstName,
        String lastName
) {}
package com.kacperkoow.zenithrent.backend.auth.controller;

import com.kacperkoow.zenithrent.backend.auth.dto.LoginRequest;
import com.kacperkoow.zenithrent.backend.auth.dto.LoginResponse;
import com.kacperkoow.zenithrent.backend.auth.dto.RegisterRequest;
import com.kacperkoow.zenithrent.backend.auth.dto.RegisterResponse;
import com.kacperkoow.zenithrent.backend.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
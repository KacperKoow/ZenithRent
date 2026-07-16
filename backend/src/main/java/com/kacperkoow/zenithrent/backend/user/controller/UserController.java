package com.kacperkoow.zenithrent.backend.user.controller;

import com.kacperkoow.zenithrent.backend.user.dto.RegisterRequest;
import com.kacperkoow.zenithrent.backend.user.dto.UserResponse;
import com.kacperkoow.zenithrent.backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
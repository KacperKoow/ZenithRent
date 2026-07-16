package com.kacperkoow.zenithrent.backend.booking.controller;

import com.kacperkoow.zenithrent.backend.booking.dto.BookingRequest;
import com.kacperkoow.zenithrent.backend.booking.dto.BookingResponse;
import com.kacperkoow.zenithrent.backend.booking.service.BookingService;
import com.kacperkoow.zenithrent.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request, @AuthenticationPrincipal User currentUser) {
        BookingResponse response = bookingService.createBooking(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(bookingService.getMyBookings(currentUser.getId()));
    }
}
package com.kacperkoow.zenithrent.backend.booking.controller;

import com.kacperkoow.zenithrent.backend.booking.dto.BookingRequest;
import com.kacperkoow.zenithrent.backend.booking.dto.BookingResponse;
import com.kacperkoow.zenithrent.backend.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
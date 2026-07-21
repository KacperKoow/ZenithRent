package com.kacperkoow.zenithrent.backend.booking.controller;

import com.kacperkoow.zenithrent.backend.booking.dto.BookingUpdateRequest;
import com.kacperkoow.zenithrent.backend.booking.dto.EmployeeBookingRequest;
import com.kacperkoow.zenithrent.backend.booking.dto.BookingResponse;
import com.kacperkoow.zenithrent.backend.booking.dto.UserBookingRequest;
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
    public ResponseEntity<BookingResponse> createBookingByUser(
            @RequestBody UserBookingRequest request,
            @AuthenticationPrincipal User currentUser
    ) {
        BookingResponse response = bookingService.createBookingByUser(request, currentUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/manage")
    public ResponseEntity<BookingResponse> createBookingByEmployee(
            @RequestBody EmployeeBookingRequest request
    ) {
        BookingResponse response = bookingService.createBookingByEmployee(request);
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

    @PutMapping("/{id}/manage")
    public ResponseEntity<BookingResponse> updateBookingByStaff(
            @PathVariable Long id,
            @RequestBody BookingUpdateRequest request
    ) {
        BookingResponse response = bookingService.updateBookingByStaff(id, request);
        return ResponseEntity.ok(response);
    }
}
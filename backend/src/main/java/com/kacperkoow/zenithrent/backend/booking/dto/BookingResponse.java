package com.kacperkoow.zenithrent.backend.booking.dto;

import com.kacperkoow.zenithrent.backend.booking.model.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BookingResponse(
        Long id,
        Long userId,
        Long carId,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal totalPrice,
        BookingStatus status
) {}
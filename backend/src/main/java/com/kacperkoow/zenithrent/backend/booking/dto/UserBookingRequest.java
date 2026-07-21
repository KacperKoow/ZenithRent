package com.kacperkoow.zenithrent.backend.booking.dto;

import java.time.LocalDate;

public record UserBookingRequest(
        Long carId,
        LocalDate startDate,
        LocalDate endDate
) {}
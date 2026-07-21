package com.kacperkoow.zenithrent.backend.booking.dto;

import java.time.LocalDate;

public record EmployeeBookingRequest(
        Long userId,
        Long carId,
        LocalDate startDate,
        LocalDate endDate
) {}
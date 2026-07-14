package com.kacperkoow.zenithrent.backend.booking;

import java.time.LocalDate;

public record BookingRequest(
        Long userId,
        Long carId,
        LocalDate startDate,
        LocalDate endDate
) {}
package com.kacperkoow.zenithrent.backend.booking.dto;

import com.kacperkoow.zenithrent.backend.booking.model.BookingStatus;
import java.time.LocalDate;

public record BookingUpdateRequest(
        Long carId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status
) {}
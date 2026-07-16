package com.kacperkoow.zenithrent.backend.booking.service;

import com.kacperkoow.zenithrent.backend.booking.dto.BookingRequest;
import com.kacperkoow.zenithrent.backend.booking.dto.BookingResponse;
import com.kacperkoow.zenithrent.backend.booking.model.BookingStatus;
import com.kacperkoow.zenithrent.backend.booking.model.Booking;
import com.kacperkoow.zenithrent.backend.booking.repository.BookingRepository;
import com.kacperkoow.zenithrent.backend.car.model.Car;
import com.kacperkoow.zenithrent.backend.car.repository.CarRepository;
import com.kacperkoow.zenithrent.backend.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request, User currentUser) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found"));

        if (!car.getIsAvailable()) {
            throw new IllegalStateException("Car is currently not available for rent");
        }

        boolean isBooked = bookingRepository.existsOverlappingBooking(
                car.getId(),
                request.startDate(),
                request.endDate()
        );

        if (isBooked) {
            throw new IllegalStateException("Car is already booked for the selected dates");
        }

        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;
        BigDecimal totalPrice = car.getPricePerDay().multiply(BigDecimal.valueOf(days));

        Booking booking = Booking.builder()
                .user(currentUser)
                .car(car)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getMyBookings(Long userId) {
        return bookingRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private BookingResponse mapToResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getCar().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getTotalPrice(),
                booking.getStatus()
        );
    }
}
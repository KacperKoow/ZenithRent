package com.kacperkoow.zenithrent.backend.booking;

import com.kacperkoow.zenithrent.backend.car.Car;
import com.kacperkoow.zenithrent.backend.car.CarRepository;
import com.kacperkoow.zenithrent.backend.user.User;
import com.kacperkoow.zenithrent.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final CarRepository carRepository;

    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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
                .user(user)
                .car(car)
                .startDate(request.startDate())
                .endDate(request.endDate())
                .totalPrice(totalPrice)
                .status(BookingStatus.CONFIRMED)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        return mapToResponse(savedBooking);
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
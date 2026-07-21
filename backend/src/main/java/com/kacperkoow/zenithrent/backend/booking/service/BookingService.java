package com.kacperkoow.zenithrent.backend.booking.service;

import com.kacperkoow.zenithrent.backend.booking.dto.BookingUpdateRequest;
import com.kacperkoow.zenithrent.backend.booking.dto.EmployeeBookingRequest;
import com.kacperkoow.zenithrent.backend.booking.dto.BookingResponse;
import com.kacperkoow.zenithrent.backend.booking.dto.UserBookingRequest;
import com.kacperkoow.zenithrent.backend.booking.model.BookingStatus;
import com.kacperkoow.zenithrent.backend.booking.model.Booking;
import com.kacperkoow.zenithrent.backend.booking.repository.BookingRepository;
import com.kacperkoow.zenithrent.backend.car.model.Car;
import com.kacperkoow.zenithrent.backend.car.repository.CarRepository;
import com.kacperkoow.zenithrent.backend.user.model.User;
import com.kacperkoow.zenithrent.backend.user.repository.UserRepository;
import com.kacperkoow.zenithrent.backend.user.service.UserService;
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
    private final UserRepository userRepository;


    @Transactional
    public BookingResponse createBookingByUser(UserBookingRequest request, User currentUser) {
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

    @Transactional
    public BookingResponse createBookingByEmployee(EmployeeBookingRequest request) {
        if (request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found"));

        User targetUser = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

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
                .user(targetUser)
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

    @Transactional
    public BookingResponse updateBookingByStaff(Long bookingId, BookingUpdateRequest request) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        Car car = carRepository.findById(request.carId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found"));

        if (!car.getIsAvailable() && !booking.getCar().getId().equals(car.getId())) {
            throw new IllegalStateException("New car is currently not available");
        }

        boolean isBooked = bookingRepository.existsOverlappingBookingExcludingCurrent(
                car.getId(),
                bookingId,
                request.startDate(),
                request.endDate()
        );

        if (isBooked) {
            throw new IllegalStateException("Car is already booked for the selected dates");
        }

        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate()) + 1;
        BigDecimal newTotalPrice = car.getPricePerDay().multiply(BigDecimal.valueOf(days));

        booking.setCar(car);
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setTotalPrice(newTotalPrice);
        booking.setStatus(request.status());

        return mapToResponse(bookingRepository.save(booking));
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
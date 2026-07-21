package com.kacperkoow.zenithrent.backend.booking.service;

import com.kacperkoow.zenithrent.backend.booking.dto.BookingResponse;
import com.kacperkoow.zenithrent.backend.booking.dto.BookingUpdateRequest;
import com.kacperkoow.zenithrent.backend.booking.dto.EmployeeBookingRequest;
import com.kacperkoow.zenithrent.backend.booking.dto.UserBookingRequest;
import com.kacperkoow.zenithrent.backend.booking.model.Booking;
import com.kacperkoow.zenithrent.backend.booking.model.BookingStatus;
import com.kacperkoow.zenithrent.backend.booking.repository.BookingRepository;
import com.kacperkoow.zenithrent.backend.car.model.Car;
import com.kacperkoow.zenithrent.backend.car.repository.CarRepository;
import com.kacperkoow.zenithrent.backend.user.model.User;
import com.kacperkoow.zenithrent.backend.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BookingService bookingService;

    private User sampleUser;
    private Car sampleCar;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(10L);
        sampleUser.setEmail("user@example.com");

        sampleCar = new Car();
        sampleCar.setId(1L);
        sampleCar.setIsAvailable(true);
        sampleCar.setPricePerDay(new BigDecimal("100.00"));
    }

    @Nested
    @DisplayName("Tests for createBookingByUser")
    class CreateBookingByUserTests {

        @Test
        @DisplayName("Should throw Exception when booking start date is after end date")
        void shouldThrowExceptionWhenStartDateIsAfterEndDate() {
            UserBookingRequest request = new UserBookingRequest(
                    1L,
                    LocalDate.of(2026, 8, 10),
                    LocalDate.of(2026, 8, 5)
            );

            assertThatThrownBy(() -> bookingService.createBookingByUser(request, sampleUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Start date cannot be after end date");
        }

        @Test
        @DisplayName("Should throw Exception when car is not found in database")
        void shouldThrowExceptionWhenCarNotFound() {
            UserBookingRequest request = new UserBookingRequest(
                    99L,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            when(carRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.createBookingByUser(request, sampleUser))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Car not found");
        }

        @Test
        @DisplayName("Should reject booking creation when selected car is unavailable")
        void shouldThrowExceptionWhenCarIsNotAvailable() {
            UserBookingRequest request = new UserBookingRequest(
                    1L,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );
            sampleCar.setIsAvailable(false);

            when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));

            assertThatThrownBy(() -> bookingService.createBookingByUser(request, sampleUser))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Car is currently not available for rent");
        }

        @Test
        @DisplayName("Should throw Exception when booking dates overlap with an existing booking")
        void shouldThrowExceptionWhenDatesOverlap() {
            LocalDate start = LocalDate.of(2026, 8, 1);
            LocalDate end = LocalDate.of(2026, 8, 5);
            UserBookingRequest request = new UserBookingRequest(1L, start, end);

            when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));
            when(bookingRepository.existsOverlappingBooking(1L, start, end)).thenReturn(true);

            assertThatThrownBy(() -> bookingService.createBookingByUser(request, sampleUser))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Car is already booked for the selected dates");
        }

        @Test
        @DisplayName("Should calculate total price as daily rate multiplied by rental days")
        void shouldCreateBookingAndCalculatePriceCorrectly() {
            LocalDate start = LocalDate.of(2026, 8, 1);
            LocalDate end = LocalDate.of(2026, 8, 3);
            UserBookingRequest request = new UserBookingRequest(1L, start, end);

            when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));
            when(bookingRepository.existsOverlappingBooking(1L, start, end)).thenReturn(false);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BookingResponse response = bookingService.createBookingByUser(request, sampleUser);

            assertThat(response).isNotNull();
            assertThat(response.totalPrice()).isEqualByComparingTo(new BigDecimal("300.00"));
        }
    }

    @Nested
    @DisplayName("Tests for createBookingByEmployee")
    class CreateBookingByEmployeeTests {

        @Test
        @DisplayName("Should throw Exception when target user is not found")
        void shouldThrowExceptionWhenTargetUserNotFound() {
            EmployeeBookingRequest request = new EmployeeBookingRequest(
                    99L,
                    1L,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3)
            );

            when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.createBookingByEmployee(request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("User not found");
        }

        @Test
        @DisplayName("Should create booking for specified user successfully")
        void shouldCreateBookingForSpecifiedUser() {
            LocalDate start = LocalDate.of(2026, 8, 10);
            LocalDate end = LocalDate.of(2026, 8, 11);
            EmployeeBookingRequest request = new EmployeeBookingRequest(10L, 1L, start, end);

            when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));
            when(userRepository.findById(10L)).thenReturn(Optional.of(sampleUser));
            when(bookingRepository.existsOverlappingBooking(1L, start, end)).thenReturn(false);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BookingResponse response = bookingService.createBookingByEmployee(request);

            assertThat(response).isNotNull();
            assertThat(response.totalPrice()).isEqualByComparingTo(new BigDecimal("200.00"));
        }
    }

    @Nested
    @DisplayName("Tests for updateBookingByStaff")
    class UpdateBookingByStaffTests {

        @Test
        @DisplayName("Should throw Exception when booking ID to update does not exist")
        void shouldThrowExceptionWhenBookingNotFound() {
            BookingUpdateRequest request = new BookingUpdateRequest(
                    1L,
                    LocalDate.now().plusDays(1),
                    LocalDate.now().plusDays(3),
                    BookingStatus.CONFIRMED
            );

            when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.updateBookingByStaff(99L, request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Booking not found");
        }

        @Test
        @DisplayName("Should update booking car and recalculate price automatically")
        void shouldUpdateBookingCarAndRecalculatePrice() {
            Booking existingBooking = Booking.builder()
                    .id(5L)
                    .car(sampleCar)
                    .user(sampleUser)
                    .startDate(LocalDate.of(2026, 8, 1))
                    .endDate(LocalDate.of(2026, 8, 2)) // 2 days * 100 = 200
                    .totalPrice(new BigDecimal("200.00"))
                    .status(BookingStatus.CONFIRMED)
                    .build();

            Car newExpensiveCar = new Car();
            newExpensiveCar.setId(2L);
            newExpensiveCar.setIsAvailable(true);
            newExpensiveCar.setPricePerDay(new BigDecimal("250.00"));

            LocalDate newStart = LocalDate.of(2026, 8, 1);
            LocalDate newEnd = LocalDate.of(2026, 8, 4); // 4 days * 250 = 1000

            BookingUpdateRequest updateRequest = new BookingUpdateRequest(
                    2L,
                    newStart,
                    newEnd,
                    BookingStatus.CONFIRMED
            );

            when(bookingRepository.findById(5L)).thenReturn(Optional.of(existingBooking));
            when(carRepository.findById(2L)).thenReturn(Optional.of(newExpensiveCar));
            when(bookingRepository.existsOverlappingBookingExcludingCurrent(2L, 5L, newStart, newEnd)).thenReturn(false);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

            BookingResponse response = bookingService.updateBookingByStaff(5L, updateRequest);

            assertThat(response).isNotNull();
            assertThat(response.totalPrice()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }
    }
}
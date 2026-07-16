package com.kacperkoow.zenithrent.backend.config;

import com.kacperkoow.zenithrent.backend.booking.model.Booking;
import com.kacperkoow.zenithrent.backend.booking.model.BookingStatus;
import com.kacperkoow.zenithrent.backend.booking.repository.BookingRepository;
import com.kacperkoow.zenithrent.backend.car.model.Car;
import com.kacperkoow.zenithrent.backend.car.model.FuelType;
import com.kacperkoow.zenithrent.backend.car.repository.CarRepository;
import com.kacperkoow.zenithrent.backend.user.model.Role;
import com.kacperkoow.zenithrent.backend.user.model.User;
import com.kacperkoow.zenithrent.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CarRepository carRepository;
    private final BookingRepository bookingRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initializeUsers();
        initializeCars();
        initializeBookings();
    }

    private void initializeUsers() {
        if (userRepository.count() == 0) {
            User admin = User.builder()
                    .firstName("System")
                    .lastName("Administrator")
                    .email("admin@zenithrent.com")
                    .password(passwordEncoder.encode("Admin123!"))
                    .role(Role.ADMIN)
                    .build();

            User employee1 = User.builder()
                    .firstName("Adam")
                    .lastName("Nowak")
                    .email("employee1@zenithrent.com")
                    .password(passwordEncoder.encode("Employee123!"))
                    .role(Role.EMPLOYEE)
                    .build();

            User employee2 = User.builder()
                    .firstName("Jan")
                    .lastName("Kowalski")
                    .email("employee2@zenithrent.com")
                    .password(passwordEncoder.encode("Employee123!"))
                    .role(Role.EMPLOYEE)
                    .build();

            User customer1 = User.builder()
                    .firstName("Anna")
                    .lastName("Wiśniewska")
                    .email("customer1@zenithrent.com")
                    .password(passwordEncoder.encode("Customer123!"))
                    .role(Role.CUSTOMER)
                    .build();

            User customer2 = User.builder()
                    .firstName("Piotr")
                    .lastName("Zieliński")
                    .email("customer2@zenithrent.com")
                    .password(passwordEncoder.encode("Customer123!"))
                    .role(Role.CUSTOMER)
                    .build();

            User customer3 = User.builder()
                    .firstName("Marta")
                    .lastName("Wójcik")
                    .email("customer3@zenithrent.com")
                    .password(passwordEncoder.encode("Customer123!"))
                    .role(Role.CUSTOMER)
                    .build();

            userRepository.saveAll(List.of(admin, employee1, employee2, customer1, customer2, customer3));
            System.out.println(">>> [DataInitializer] Users initialized");
        }
    }

    private void initializeCars() {
        if (carRepository.count() == 0) {
            Car car1 = Car.builder()
                    .brand("Toyota")
                    .model("Corolla")
                    .productionYear(2023)
                    .vin("JTDKR32U001234567")
                    .fuelType(FuelType.HYBRID)
                    .pricePerDay(BigDecimal.valueOf(150.00))
                    .isAvailable(true)
                    .build();

            Car car2 = Car.builder()
                    .brand("BMW")
                    .model("3 Series")
                    .productionYear(2022)
                    .vin("WBA31AK0023456789")
                    .fuelType(FuelType.PETROL)
                    .pricePerDay(BigDecimal.valueOf(300.00))
                    .isAvailable(true)
                    .build();

            Car car3 = Car.builder()
                    .brand("Tesla")
                    .model("Model 3")
                    .productionYear(2023)
                    .vin("5YJ3E1EA034567890")
                    .fuelType(FuelType.ELECTRIC)
                    .pricePerDay(BigDecimal.valueOf(350.00))
                    .isAvailable(true)
                    .build();

            carRepository.saveAll(List.of(car1, car2, car3));
            System.out.println(">>> [DataInitializer] Cars initialized");
        }
    }

    private void initializeBookings() {
        if (bookingRepository.count() == 0) {
            User customer1 = userRepository.findByEmail("customer1@zenithrent.com")
                    .orElseThrow(() -> new IllegalStateException("Customer1 not found during seeding"));

            User customer3 = userRepository.findByEmail("customer3@zenithrent.com")
                    .orElseThrow(() -> new IllegalStateException("Customer3 not found during seeding"));

            List<Car> cars = carRepository.findAll();

            if (cars.size() >= 3) {
                Car car2 = cars.get(1);
                Car car3 = cars.get(2);

                Booking booking1 = Booking.builder()
                        .user(customer1)
                        .car(car3)
                        .startDate(LocalDate.now().plusDays(2))
                        .endDate(LocalDate.now().plusDays(6))
                        .totalPrice(car3.getPricePerDay().multiply(BigDecimal.valueOf(5))) // 5 dni
                        .status(BookingStatus.CONFIRMED)
                        .build();

                Booking booking2 = Booking.builder()
                        .user(customer3)
                        .car(car2)
                        .startDate(LocalDate.now().plusDays(10))
                        .endDate(LocalDate.now().plusDays(12))
                        .totalPrice(car2.getPricePerDay().multiply(BigDecimal.valueOf(3))) // 3 dni
                        .status(BookingStatus.CONFIRMED)
                        .build();

                bookingRepository.saveAll(List.of(booking1, booking2));
                System.out.println(">>> [DataInitializer] Bookings initialized");
            }
        }
    }
}
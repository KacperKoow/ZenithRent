package com.kacperkoow.zenithrent.backend.car.service;

import com.kacperkoow.zenithrent.backend.car.dto.CarCreateRequest;
import com.kacperkoow.zenithrent.backend.car.dto.CarResponse;
import com.kacperkoow.zenithrent.backend.car.model.Car;
import com.kacperkoow.zenithrent.backend.car.model.FuelType;
import com.kacperkoow.zenithrent.backend.car.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository carRepository;

    @InjectMocks
    private CarService carService;

    private Car sampleCar;

    @BeforeEach
    void setUp() {
        sampleCar = Car.builder()
                .id(1L)
                .brand("Audi")
                .model("RS6")
                .productionYear(2023)
                .vin("WAUZZZ4K1MN123456")
                .fuelType(FuelType.PETROL)
                .pricePerDay(new BigDecimal("500.00"))
                .isAvailable(true)
                .build();
    }

    @Nested
    @DisplayName("Tests for fetching cars")
    class FetchCarTests {

        @Test
        @DisplayName("Should return all cars mapped to responses")
        void shouldReturnAllCars() {
            when(carRepository.findAll()).thenReturn(List.of(sampleCar));

            List<CarResponse> result = carService.getAllCars();

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().brand()).isEqualTo("Audi");
            assertThat(result.getFirst().model()).isEqualTo("RS6");
        }

        @Test
        @DisplayName("Should return car response when car exists by ID")
        void shouldReturnCarById() {
            when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));

            CarResponse result = carService.getCarById(1L);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.pricePerDay()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("Should throw Exception when car is not found by ID")
        void shouldThrowExceptionWhenCarNotFound() {
            when(carRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carService.getCarById(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Car with ID 99 not found");
        }
    }

    @Nested
    @DisplayName("Tests for creating, updating and deleting cars")
    class ManageCarTests {

        @Test
        @DisplayName("Should save and return created car")
        void shouldCreateCarSuccessfully() {
            CarCreateRequest request = new CarCreateRequest(
                    "Audi",
                    "RS6",
                    2023,
                    "WAUZZZ4K1MN123456",
                    FuelType.PETROL,
                    new BigDecimal("500.00")
            );

            when(carRepository.save(any(Car.class))).thenAnswer(invocation -> {
                Car savedCar = invocation.getArgument(0);
                savedCar.setId(1L);
                return savedCar;
            });

            CarResponse result = carService.createCar(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.brand()).isEqualTo("Audi");
            verify(carRepository, times(1)).save(any(Car.class));
        }

        @Test
        @DisplayName("Should update existing car details")
        void shouldUpdateCarSuccessfully() {
            CarCreateRequest updateRequest = new CarCreateRequest(
                    "BMW",
                    "M5",
                    2024,
                    "WBA12345678901234",
                    FuelType.PETROL,
                    new BigDecimal("600.00")
            );

            when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));
            when(carRepository.save(any(Car.class))).thenAnswer(invocation -> invocation.getArgument(0));

            CarResponse result = carService.updateCar(1L, updateRequest);

            assertThat(result).isNotNull();
            assertThat(result.brand()).isEqualTo("BMW");
            assertThat(result.model()).isEqualTo("M5");
            assertThat(result.pricePerDay()).isEqualByComparingTo(new BigDecimal("600.00"));
        }

        @Test
        @DisplayName("Should delete car when exists")
        void shouldDeleteCarSuccessfully() {
            when(carRepository.findById(1L)).thenReturn(Optional.of(sampleCar));
            doNothing().when(carRepository).delete(sampleCar);

            carService.deleteCar(1L);

            verify(carRepository, times(1)).delete(sampleCar);
        }

        @Test
        @DisplayName("Should throw Exception when deleting non-existent car")
        void shouldThrowExceptionWhenDeletingNonExistentCar() {
            when(carRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> carService.deleteCar(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Car with ID 99 not found");

            verify(carRepository, never()).delete(any(Car.class));
        }
    }
}
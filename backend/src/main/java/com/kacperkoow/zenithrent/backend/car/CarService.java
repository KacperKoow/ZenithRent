package com.kacperkoow.zenithrent.backend.car;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public List<CarResponse> getAllCars() {
        return carRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CarResponse createCar(CarCreateRequest request) {
        Car car = Car.builder()
                .brand(request.brand())
                .model(request.model())
                .year(request.year())
                .vin(request.vin())
                .fuelType(request.fuelType())
                .pricePerDay(request.pricePerDay())
                .isAvailable(true) // Nowe auto jest domyślnie dostępne
                .build();

        Car savedCar = carRepository.save(car);
        return mapToResponse(savedCar);
    }

    private CarResponse mapToResponse(Car car) {
        return new CarResponse(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getYear(),
                car.getFuelType(),
                car.getPricePerDay(),
                car.getIsAvailable()
        );
    }
}
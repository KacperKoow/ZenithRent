package com.kacperkoow.zenithrent.backend.car.service;

import com.kacperkoow.zenithrent.backend.car.model.Car;
import com.kacperkoow.zenithrent.backend.car.dto.CarCreateRequest;
import com.kacperkoow.zenithrent.backend.car.repository.CarRepository;
import com.kacperkoow.zenithrent.backend.car.dto.CarResponse;
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

    @Transactional(readOnly = true)
    public CarResponse getCarById(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Car with ID " + id + " not found"));
        return mapToResponse(car);
    }

    @Transactional
    public CarResponse createCar(CarCreateRequest request) {
        Car car = Car.builder()
                .brand(request.brand())
                .model(request.model())
                .productionYear(request.productionYear())
                .vin(request.vin())
                .fuelType(request.fuelType())
                .pricePerDay(request.pricePerDay())
                .isAvailable(true)
                .build();

        Car savedCar = carRepository.save(car);
        return mapToResponse(savedCar);
    }

    @Transactional
    public CarResponse updateCar(Long id, CarCreateRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Car with ID " + id + " not found"));

        car.setBrand(request.brand());
        car.setModel(request.model());
        car.setProductionYear(request.productionYear());
        car.setFuelType(request.fuelType());
        car.setPricePerDay(request.pricePerDay());

        Car updatedCar = carRepository.save(car);
        return mapToResponse(updatedCar);
    }

    @Transactional
    public void deleteCar(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Car with ID " + id + " not found"));
        carRepository.delete(car);
    }

    private CarResponse mapToResponse(Car car) {
        return new CarResponse(
                car.getId(),
                car.getBrand(),
                car.getModel(),
                car.getProductionYear(),
                car.getFuelType(),
                car.getPricePerDay(),
                car.getIsAvailable()
        );
    }
}
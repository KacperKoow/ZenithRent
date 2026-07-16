package com.kacperkoow.zenithrent.backend.car.controller;

import com.kacperkoow.zenithrent.backend.car.dto.CarCreateRequest;
import com.kacperkoow.zenithrent.backend.car.dto.CarResponse;
import com.kacperkoow.zenithrent.backend.car.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cars")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @GetMapping
    public ResponseEntity<List<CarResponse>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Long id) {
        return ResponseEntity.ok(carService.getCarById(id));
    }

    @PostMapping
    public ResponseEntity<CarResponse> createCar(@RequestBody CarCreateRequest request) {
        CarResponse createdCar = carService.createCar(request);
        return new ResponseEntity<>(createdCar, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarResponse> updateCar(@PathVariable Long id, @RequestBody CarCreateRequest request) {
        return ResponseEntity.ok(carService.updateCar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.noContent().build();
    }
}
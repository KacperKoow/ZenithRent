package com.kacperkoow.zenithrent.backend.car.dto;

import com.kacperkoow.zenithrent.backend.car.model.FuelType;

import java.math.BigDecimal;

public record CarCreateRequest(
        String brand,
        String model,
        Integer year,
        String vin,
        FuelType fuelType,
        BigDecimal pricePerDay
) {}
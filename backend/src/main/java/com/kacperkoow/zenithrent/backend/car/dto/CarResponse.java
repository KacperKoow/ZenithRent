package com.kacperkoow.zenithrent.backend.car.dto;

import com.kacperkoow.zenithrent.backend.car.model.FuelType;

import java.math.BigDecimal;

public record CarResponse(
        Long id,
        String brand,
        String model,
        Integer year,
        FuelType fuelType,
        BigDecimal pricePerDay,
        Boolean isAvailable
) {}
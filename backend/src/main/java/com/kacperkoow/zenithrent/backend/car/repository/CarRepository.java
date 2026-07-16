package com.kacperkoow.zenithrent.backend.car.repository;

import com.kacperkoow.zenithrent.backend.car.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
}
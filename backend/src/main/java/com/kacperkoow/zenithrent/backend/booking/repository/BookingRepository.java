package com.kacperkoow.zenithrent.backend.booking.repository;

import com.kacperkoow.zenithrent.backend.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT COUNT(b) > 0 FROM Booking b " +
            "WHERE b.car.id = :carId " +
            "AND b.status != 'CANCELLED' " +
            "AND b.startDate <= :endDate " +
            "AND b.endDate >= :startDate")
    boolean existsOverlappingBooking(
            @Param("carId") Long carId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
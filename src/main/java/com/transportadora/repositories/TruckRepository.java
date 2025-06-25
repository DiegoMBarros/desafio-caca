package com.transportadora.repositories;

import com.transportadora.models.Truck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface TruckRepository extends JpaRepository<Truck, Long> {
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.truck.id = :truckId AND d.deliveryDateTime BETWEEN :startDate AND :endDate")
    int countDeliveriesInMonth(@Param("truckId") Long truckId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
} 
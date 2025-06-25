package com.transportadora.repositories;

import com.transportadora.models.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;

public interface DriverRepository extends JpaRepository<Driver, Long> {
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.driver.id = :driverId AND d.deliveryDateTime BETWEEN :startDate AND :endDate")
    int countDeliveriesInMonth(@Param("driverId") Long driverId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.driver.id = :driverId AND d.destination = 'NORDESTE'")
    int countDeliveriesToNordeste(@Param("driverId") Long driverId);
} 
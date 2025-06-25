package com.transportadora.repositories;

import com.transportadora.models.Delivery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Page<Delivery> findByDeliveryDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    List<Delivery> findByDeliveryDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query(value = "SELECT COALESCE(SUM(value), 0) FROM deliveries WHERE DATE(delivery_date_time) = CURRENT_DATE", nativeQuery = true)
    BigDecimal getTotalValueForToday();
} 
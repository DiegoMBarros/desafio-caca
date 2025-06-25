package com.transportadora.services;

import com.transportadora.models.Delivery;
import com.transportadora.models.Driver;
import com.transportadora.models.Truck;
import com.transportadora.repositories.DeliveryRepository;
import com.transportadora.repositories.DriverRepository;
import com.transportadora.repositories.TruckRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class DeliveryService {

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private TruckRepository truckRepository;

    @Autowired
    private DriverRepository driverRepository;

    @CachePut(value = "deliveries", key = "#result.id")
    @Transactional
    public Delivery createDelivery(Delivery delivery) {
        validateDelivery(delivery);
        applyRegionalTaxes(delivery);
        return deliveryRepository.save(delivery);
    }

    @Cacheable(value = "deliveries", key = "'page:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<Delivery> findAll(Pageable pageable) {
        return deliveryRepository.findAll(pageable);
    }

    @Cacheable(value = "deliveries", key = "#id")
    public Optional<Delivery> findById(Long id) {
        return deliveryRepository.findById(id);
    }

    @Cacheable(value = "deliveries", key = "'period:' + #startDate + ':' + #endDate + ':page:' + #pageable.pageNumber")
    public Page<Delivery> findByPeriod(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return deliveryRepository.findByDeliveryDateTimeBetween(startDate, endDate, pageable);
    }

    @Cacheable(value = "deliveries", key = "'total:' + T(java.time.LocalDate).now()")
    public BigDecimal getTotalValueForToday() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        return deliveryRepository.findByDeliveryDateTimeBetween(startOfDay, endOfDay)
                .stream()
                .map(Delivery::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateDelivery(Delivery delivery) {
        validateTruckAvailability(delivery.getTruck());
        validateDriverAvailability(delivery.getDriver());
        validateDriverNordesteLimit(delivery);
    }

    private void validateTruckAvailability(Truck truck) {
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        
        int deliveriesThisMonth = truckRepository.countDeliveriesInMonth(
            truck.getId(), startOfMonth, endOfMonth);
            
        if (deliveriesThisMonth >= 4) {
            throw new IllegalStateException("Caminhão já atingiu o limite de 4 entregas no mês");
        }
    }

    private void validateDriverAvailability(Driver driver) {
        LocalDateTime startOfMonth = YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
        
        int deliveriesThisMonth = driverRepository.countDeliveriesInMonth(
            driver.getId(), startOfMonth, endOfMonth);
            
        if (deliveriesThisMonth >= 2) {
            throw new IllegalStateException("Motorista já atingiu o limite de 2 entregas no mês");
        }
    }

    private void validateDriverNordesteLimit(Delivery delivery) {
        if ("NORDESTE".equalsIgnoreCase(delivery.getDestination())) {
            int nordesteDeliveries = driverRepository.countDeliveriesToNordeste(
                delivery.getDriver().getId());
                
            if (nordesteDeliveries >= 1) {
                throw new IllegalStateException("Motorista já realizou uma entrega para o Nordeste");
            }
        }
    }

    private void applyRegionalTaxes(Delivery delivery) {
        BigDecimal originalValue = delivery.getValue();
        BigDecimal taxedValue = originalValue;

        switch (delivery.getDestination().toUpperCase()) {
            case "NORDESTE":
                taxedValue = originalValue.multiply(new BigDecimal("1.20")); // +20%
                break;
            case "ARGENTINA":
                taxedValue = originalValue.multiply(new BigDecimal("1.40")); // +40%
                break;
            case "AMAZONIA":
                taxedValue = originalValue.multiply(new BigDecimal("1.30")); // +30%
                break;
        }

        delivery.setValue(taxedValue);
    }
} 
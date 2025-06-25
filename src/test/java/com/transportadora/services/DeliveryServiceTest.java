package com.transportadora.services;

import com.transportadora.models.*;
import com.transportadora.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private TruckRepository truckRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    private Delivery delivery;
    private Driver driver;
    private Truck truck;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        now = LocalDateTime.now();
        
        driver = new Driver();
        driver.setId(1L);
        driver.setName("João da Silva");

        truck = new Truck();
        truck.setId(1L);
        truck.setModel("Volvo FH");
        truck.setYear(2023);
        truck.setPlate("ABC1D23");

        delivery = new Delivery();
        delivery.setId(1L);
        delivery.setDriver(driver);
        delivery.setTruck(truck);
        delivery.setDeliveryDateTime(now.plusDays(1));
        delivery.setValue(BigDecimal.valueOf(1000));
        delivery.setDescription("Test delivery");
    }

    @Test
    void shouldApplyTaxForNordeste() {
        // Arrange
        delivery.setDestination("NORDESTE");
        delivery.setValue(new BigDecimal("1000.00"));
        delivery.setDriver(new Driver());
        delivery.setTruck(new Truck());
        
        when(deliveryRepository.save(any())).thenReturn(delivery);
        when(driverRepository.countDeliveriesToNordeste(any())).thenReturn(0);
        when(driverRepository.countDeliveriesInMonth(any(), any(), any())).thenReturn(0);
        when(truckRepository.countDeliveriesInMonth(any(), any(), any())).thenReturn(0);

        // Act
        Delivery result = deliveryService.createDelivery(delivery);

        // Assert
        assertEquals(new BigDecimal("1200.00"), result.getValue());
    }

    @Test
    void shouldThrowExceptionWhenDriverExceedsNordesteLimit() {
        // Arrange
        delivery.setDestination("NORDESTE");
        delivery.setDriver(new Driver());
        
        when(driverRepository.countDeliveriesToNordeste(any())).thenReturn(1);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            deliveryService.createDelivery(delivery);
        });
    }

    @Test
    void createDelivery_ShouldSaveAndReturnDelivery() {
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(delivery);

        Delivery result = deliveryService.createDelivery(delivery);

        assertNotNull(result);
        assertEquals(delivery.getDriver(), result.getDriver());
        assertEquals(delivery.getTruck(), result.getTruck());
        assertEquals(delivery.getValue(), result.getValue());
        verify(deliveryRepository).save(delivery);
    }

    @Test
    void findAll_ShouldReturnPageOfDeliveries() {
        Page<Delivery> page = new PageImpl<>(List.of(delivery));
        Pageable pageable = PageRequest.of(0, 10);
        when(deliveryRepository.findAll(pageable)).thenReturn(page);

        Page<Delivery> result = deliveryService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(delivery, result.getContent().get(0));
        verify(deliveryRepository).findAll(pageable);
    }

    @Test
    void findById_WhenDeliveryExists_ShouldReturnDelivery() {
        when(deliveryRepository.findById(1L)).thenReturn(Optional.of(delivery));

        Optional<Delivery> result = deliveryService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(delivery, result.get());
        verify(deliveryRepository).findById(1L);
    }

    @Test
    void findById_WhenDeliveryDoesNotExist_ShouldReturnEmpty() {
        when(deliveryRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Delivery> result = deliveryService.findById(1L);

        assertTrue(result.isEmpty());
        verify(deliveryRepository).findById(1L);
    }

    @Test
    void findByPeriod_ShouldReturnDeliveriesInPeriod() {
        LocalDateTime startDate = now;
        LocalDateTime endDate = now.plusDays(2);
        Page<Delivery> page = new PageImpl<>(List.of(delivery));
        Pageable pageable = PageRequest.of(0, 10);
        
        when(deliveryRepository.findByDeliveryDateTimeBetween(startDate, endDate, pageable))
            .thenReturn(page);

        Page<Delivery> result = deliveryService.findByPeriod(startDate, endDate, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(delivery, result.getContent().get(0));
        verify(deliveryRepository).findByDeliveryDateTimeBetween(startDate, endDate, pageable);
    }

    @Test
    void getTotalValueForToday_ShouldCalculateTotal() {
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59);
        
        List<Delivery> todayDeliveries = List.of(
            createDeliveryWithValue(BigDecimal.valueOf(1000)),
            createDeliveryWithValue(BigDecimal.valueOf(2000)),
            createDeliveryWithValue(BigDecimal.valueOf(3000))
        );

        when(deliveryRepository.findByDeliveryDateTimeBetween(
            any(LocalDateTime.class), 
            any(LocalDateTime.class)
        )).thenReturn(todayDeliveries);

        BigDecimal total = deliveryService.getTotalValueForToday();

        assertEquals(BigDecimal.valueOf(6000), total);
        verify(deliveryRepository).findByDeliveryDateTimeBetween(
            any(LocalDateTime.class), 
            any(LocalDateTime.class)
        );
    }

    private Delivery createDeliveryWithValue(BigDecimal value) {
        Delivery d = new Delivery();
        d.setDriver(driver);
        d.setTruck(truck);
        d.setDeliveryDateTime(now);
        d.setValue(value);
        d.setDescription("Test delivery");
        return d;
    }
} 
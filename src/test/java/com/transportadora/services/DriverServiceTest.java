package com.transportadora.services;

import com.transportadora.models.Driver;
import com.transportadora.repositories.DriverRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = new Driver();
        driver.setId(1L);
        driver.setName("João da Silva");
        driver.setCnh("12345678901");
    }

    @Test
    void createDriver_ShouldSaveAndReturnDriver() {
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        Driver result = driverService.createDriver(driver);

        assertNotNull(result);
        assertEquals(driver.getName(), result.getName());
        assertEquals(driver.getCnh(), result.getCnh());
        verify(driverRepository).save(driver);
    }

    @Test
    void findAll_ShouldReturnPageOfDrivers() {
        Page<Driver> page = new PageImpl<>(List.of(driver));
        Pageable pageable = PageRequest.of(0, 10);
        when(driverRepository.findAll(pageable)).thenReturn(page);

        Page<Driver> result = driverService.findAll(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(driver, result.getContent().get(0));
        verify(driverRepository).findAll(pageable);
    }

    @Test
    void findById_WhenDriverExists_ShouldReturnDriver() {
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));

        Optional<Driver> result = driverService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(driver, result.get());
        verify(driverRepository).findById(1L);
    }

    @Test
    void findById_WhenDriverDoesNotExist_ShouldReturnEmpty() {
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Driver> result = driverService.findById(1L);

        assertTrue(result.isEmpty());
        verify(driverRepository).findById(1L);
    }

    @Test
    void updateDriver_WhenDriverExists_ShouldUpdateAndReturnDriver() {
        when(driverRepository.existsById(1L)).thenReturn(true);
        when(driverRepository.save(any(Driver.class))).thenReturn(driver);

        Driver updatedDriver = driverService.updateDriver(1L, driver);

        assertNotNull(updatedDriver);
        assertEquals(driver.getName(), updatedDriver.getName());
        assertEquals(driver.getCnh(), updatedDriver.getCnh());
        verify(driverRepository).existsById(1L);
        verify(driverRepository).save(driver);
    }

    @Test
    void updateDriver_WhenDriverDoesNotExist_ShouldThrowException() {
        when(driverRepository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
            driverService.updateDriver(1L, driver)
        );
        verify(driverRepository).existsById(1L);
        verify(driverRepository, never()).save(any());
    }

    @Test
    void deleteDriver_WhenDriverExists_ShouldDelete() {
        when(driverRepository.existsById(1L)).thenReturn(true);
        doNothing().when(driverRepository).deleteById(1L);

        driverService.deleteDriver(1L);

        verify(driverRepository).existsById(1L);
        verify(driverRepository).deleteById(1L);
    }

    @Test
    void deleteDriver_WhenDriverDoesNotExist_ShouldThrowException() {
        when(driverRepository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> 
            driverService.deleteDriver(1L)
        );
        verify(driverRepository).existsById(1L);
        verify(driverRepository, never()).deleteById(any());
    }
} 
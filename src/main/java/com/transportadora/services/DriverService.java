package com.transportadora.services;

import com.transportadora.models.Driver;
import com.transportadora.repositories.DriverRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class DriverService {

    @Autowired
    private DriverRepository driverRepository;

    @Transactional
    @CachePut(value = "drivers", key = "#result.id")
    public Driver createDriver(Driver driver) {
        return driverRepository.save(driver);
    }

    @Cacheable(value = "drivers", key = "'page:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<Driver> findAll(Pageable pageable) {
        return driverRepository.findAll(pageable);
    }

    @Cacheable(value = "drivers", key = "#id")
    public Optional<Driver> findById(Long id) {
        return driverRepository.findById(id);
    }

    @Transactional
    @CachePut(value = "drivers", key = "#id")
    public Driver updateDriver(Long id, Driver driver) {
        if (!driverRepository.existsById(id)) {
            throw new IllegalArgumentException("Motorista não encontrado com o ID: " + id);
        }
        driver.setId(id);
        return driverRepository.save(driver);
    }

    @Transactional
    @CacheEvict(value = "drivers", key = "#id")
    public void deleteDriver(Long id) {
        if (!driverRepository.existsById(id)) {
            throw new IllegalArgumentException("Motorista não encontrado com o ID: " + id);
        }
        driverRepository.deleteById(id);
    }
} 
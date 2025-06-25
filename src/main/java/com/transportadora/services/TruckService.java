package com.transportadora.services;

import com.transportadora.models.Truck;
import com.transportadora.repositories.TruckRepository;
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
public class TruckService {

    @Autowired
    private TruckRepository truckRepository;

    @Transactional
    @CachePut(value = "trucks", key = "#result.id")
    public Truck createTruck(Truck truck) {
        return truckRepository.save(truck);
    }

    @Cacheable(value = "trucks", key = "'page:' + #pageable.pageNumber + ':' + #pageable.pageSize")
    public Page<Truck> findAll(Pageable pageable) {
        return truckRepository.findAll(pageable);
    }

    @Cacheable(value = "trucks", key = "#id")
    public Optional<Truck> findById(Long id) {
        return truckRepository.findById(id);
    }

    @Transactional
    @CachePut(value = "trucks", key = "#id")
    public Truck updateTruck(Long id, Truck truck) {
        if (!truckRepository.existsById(id)) {
            throw new IllegalArgumentException("Caminhão não encontrado com o ID: " + id);
        }
        truck.setId(id);
        return truckRepository.save(truck);
    }

    @Transactional
    @CacheEvict(value = "trucks", key = "#id")
    public void deleteTruck(Long id) {
        if (!truckRepository.existsById(id)) {
            throw new IllegalArgumentException("Caminhão não encontrado com o ID: " + id);
        }
        truckRepository.deleteById(id);
    }
} 
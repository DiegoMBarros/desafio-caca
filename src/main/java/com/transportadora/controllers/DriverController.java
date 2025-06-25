package com.transportadora.controllers;

import com.transportadora.models.Driver;
import com.transportadora.services.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/drivers")
@Tag(name = "Motoristas", description = "API para gerenciamento de motoristas")
public class DriverController {

    @Autowired
    private DriverService driverService;

    @PostMapping
    @Operation(summary = "Criar um novo motorista", 
              description = "Cria um novo motorista com todas as validações necessárias")
    public ResponseEntity<Driver> createDriver(@Valid @RequestBody Driver driver) {
        return ResponseEntity.ok(driverService.createDriver(driver));
    }

    @GetMapping
    @Operation(summary = "Listar todos os motoristas", 
              description = "Retorna a lista paginada de todos os motoristas cadastrados")
    public ResponseEntity<Page<Driver>> getAllDrivers(
            @Parameter(description = "Número da página (começa em 0)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da página")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Campo para ordenação")
            @RequestParam(defaultValue = "id") String sort,
            @Parameter(description = "Direção da ordenação (ASC ou DESC)")
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        return ResponseEntity.ok(driverService.findAll(pageRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar motorista por ID", 
              description = "Retorna um motorista específico pelo seu ID")
    public ResponseEntity<Driver> getDriverById(@PathVariable Long id) {
        return driverService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um motorista", 
              description = "Atualiza os dados de um motorista existente")
    public ResponseEntity<Driver> updateDriver(@PathVariable Long id, @Valid @RequestBody Driver driver) {
        return ResponseEntity.ok(driverService.updateDriver(id, driver));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um motorista", 
              description = "Remove um motorista do sistema")
    public ResponseEntity<Void> deleteDriver(@PathVariable Long id) {
        driverService.deleteDriver(id);
        return ResponseEntity.noContent().build();
    }
} 
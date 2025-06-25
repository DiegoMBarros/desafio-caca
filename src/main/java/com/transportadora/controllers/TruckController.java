package com.transportadora.controllers;

import com.transportadora.models.Truck;
import com.transportadora.services.TruckService;
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
@RequestMapping("/api/trucks")
@Tag(name = "Caminhões", description = "API para gerenciamento de caminhões")
public class TruckController {

    @Autowired
    private TruckService truckService;

    @PostMapping
    @Operation(summary = "Criar um novo caminhão", 
              description = "Cria um novo caminhão com todas as validações necessárias")
    public ResponseEntity<Truck> createTruck(@Valid @RequestBody Truck truck) {
        return ResponseEntity.ok(truckService.createTruck(truck));
    }

    @GetMapping
    @Operation(summary = "Listar todos os caminhões", 
              description = "Retorna a lista paginada de todos os caminhões cadastrados")
    public ResponseEntity<Page<Truck>> getAllTrucks(
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
        
        return ResponseEntity.ok(truckService.findAll(pageRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar caminhão por ID", 
              description = "Retorna um caminhão específico pelo seu ID")
    public ResponseEntity<Truck> getTruckById(@PathVariable Long id) {
        return truckService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar um caminhão", 
              description = "Atualiza os dados de um caminhão existente")
    public ResponseEntity<Truck> updateTruck(@PathVariable Long id, @Valid @RequestBody Truck truck) {
        return ResponseEntity.ok(truckService.updateTruck(id, truck));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir um caminhão", 
              description = "Remove um caminhão do sistema")
    public ResponseEntity<Void> deleteTruck(@PathVariable Long id) {
        truckService.deleteTruck(id);
        return ResponseEntity.noContent().build();
    }
} 
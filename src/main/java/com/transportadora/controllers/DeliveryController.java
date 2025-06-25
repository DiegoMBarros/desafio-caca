package com.transportadora.controllers;

import com.transportadora.models.Delivery;
import com.transportadora.services.DeliveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@Tag(name = "Entregas", description = "API para gerenciamento de entregas")
public class DeliveryController {

    @Autowired
    private DeliveryService deliveryService;

    @PostMapping
    @Operation(summary = "Criar uma nova entrega", 
              description = "Cria uma nova entrega aplicando todas as regras de negócio")
    public ResponseEntity<Delivery> createDelivery(@Valid @RequestBody Delivery delivery) {
        return ResponseEntity.ok(deliveryService.createDelivery(delivery));
    }

    @GetMapping
    @Operation(summary = "Listar todas as entregas", 
              description = "Retorna a lista paginada de todas as entregas cadastradas")
    public ResponseEntity<Page<Delivery>> getAllDeliveries(
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
        
        return ResponseEntity.ok(deliveryService.findAll(pageRequest));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar entrega por ID", 
              description = "Retorna uma entrega específica pelo seu ID")
    public ResponseEntity<Delivery> getDeliveryById(@PathVariable Long id) {
        return deliveryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/period")
    @Operation(summary = "Buscar entregas por período", 
              description = "Retorna todas as entregas entre duas datas")
    public ResponseEntity<Page<Delivery>> getDeliveriesByPeriod(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "deliveryDateTime") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
        
        Sort.Direction sortDirection = Sort.Direction.fromString(direction.toUpperCase());
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        return ResponseEntity.ok(deliveryService.findByPeriod(startDate, endDate, pageRequest));
    }

    @GetMapping("/today/total")
    @Operation(summary = "Total de entregas do dia", 
              description = "Retorna o valor total das entregas do dia atual")
    public ResponseEntity<BigDecimal> getTotalValueForToday() {
        return ResponseEntity.ok(deliveryService.getTotalValueForToday());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
} 
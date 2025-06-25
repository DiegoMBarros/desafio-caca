package com.transportadora.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "deliveries")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O destino é obrigatório")
    @Size(min = 2, max = 100, message = "O destino deve ter entre 2 e 100 caracteres")
    @Column(nullable = false)
    private String destination;

    @NotNull(message = "A data/hora da entrega é obrigatória")
    @Future(message = "A data da entrega deve ser no futuro")
    @Column(nullable = false)
    private LocalDateTime deliveryDateTime;

    @NotNull(message = "O tipo de carga é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CargoType cargoType;

    @NotNull(message = "O valor é obrigatório")
    @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
    @Column(nullable = false)
    private BigDecimal value;

    @Column(nullable = false)
    private boolean isValuable;

    @Column(nullable = false)
    private boolean hasInsurance;

    @Column(nullable = false)
    private boolean isDangerous;

    @NotNull(message = "O caminhão é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "truck_id")
    private Truck truck;

    @NotNull(message = "O motorista é obrigatório")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @PrePersist
    @PreUpdate
    private void calculateFlags() {
        this.isValuable = value.compareTo(new BigDecimal("30000")) > 0;
        this.isDangerous = CargoType.COMBUSTIVEL.equals(cargoType);
        this.hasInsurance = CargoType.ELETRONICOS.equals(cargoType);
    }
} 
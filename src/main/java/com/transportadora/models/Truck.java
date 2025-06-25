package com.transportadora.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;
import java.time.Year;

@Data
@Entity
@Table(name = "trucks")
public class Truck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A placa é obrigatória")
    @Pattern(regexp = "^[A-Z]{3}[0-9][0-9A-Z][0-9]{2}$", message = "Placa deve estar no formato Mercosul (AAA0A00)")
    @Column(nullable = false)
    private String plate;

    @NotBlank(message = "O modelo é obrigatório")
    @Size(min = 2, max = 50, message = "O modelo deve ter entre 2 e 50 caracteres")
    @Column(nullable = false)
    private String model;

    @Min(value = 1990, message = "Ano de fabricação deve ser maior que 1990")
    @Max(value = 2025, message = "Ano de fabricação não pode ser maior que 2025")
    @Column(name = "manufacturing_year")
    private Integer manufacturingYear;

    @OneToMany(mappedBy = "truck", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Delivery> deliveries = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;
} 
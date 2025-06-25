package com.transportadora.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transportadora.config.IntegrationTestConfig;
import com.transportadora.models.Delivery;
import com.transportadora.models.Driver;
import com.transportadora.models.Truck;
import com.transportadora.repositories.DeliveryRepository;
import com.transportadora.repositories.DriverRepository;
import com.transportadora.repositories.TruckRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
class DeliveryControllerTest extends IntegrationTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private TruckRepository truckRepository;

    private Driver driver;
    private Truck truck;

    @BeforeEach
    void setup() {
        // Cria um motorista e um caminhão para usar nos testes
        driver = new Driver();
        driver.setName("Test Driver");
        driver.setCnh("12345678901");
        driver = driverRepository.save(driver);

        truck = new Truck();
        truck.setModel("Test Truck");
        truck.setYear(2023);
        truck.setPlate("ABC1D23");
        truck = truckRepository.save(truck);
    }

    @AfterEach
    void cleanup() {
        deliveryRepository.deleteAll();
        driverRepository.deleteAll();
        truckRepository.deleteAll();
    }

    @Test
    void shouldCreateDelivery() throws Exception {
        Delivery delivery = new Delivery();
        delivery.setDriver(driver);
        delivery.setTruck(truck);
        delivery.setDeliveryDateTime(LocalDateTime.now().plusDays(1));
        delivery.setValue(BigDecimal.valueOf(1000));
        delivery.setDescription("Test delivery");

        mockMvc.perform(post("/api/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(delivery)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.driver.id").value(driver.getId()))
                .andExpect(jsonPath("$.truck.id").value(truck.getId()))
                .andExpect(jsonPath("$.value").value(1000));
    }

    @Test
    void shouldGetDeliveryById() throws Exception {
        // Cria uma entrega para teste
        Delivery delivery = new Delivery();
        delivery.setDriver(driver);
        delivery.setTruck(truck);
        delivery.setDeliveryDateTime(LocalDateTime.now().plusDays(1));
        delivery.setValue(BigDecimal.valueOf(1000));
        delivery.setDescription("Test delivery");
        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Primeira chamada - deve vir do banco
        mockMvc.perform(get("/api/deliveries/" + savedDelivery.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedDelivery.getId()))
                .andExpect(jsonPath("$.driver.id").value(driver.getId()))
                .andExpect(jsonPath("$.truck.id").value(truck.getId()));

        // Segunda chamada - deve vir do cache
        mockMvc.perform(get("/api/deliveries/" + savedDelivery.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedDelivery.getId()));
    }

    @Test
    void shouldListDeliveriesPaginated() throws Exception {
        // Cria algumas entregas para teste
        for (int i = 1; i <= 15; i++) {
            Delivery delivery = new Delivery();
            delivery.setDriver(driver);
            delivery.setTruck(truck);
            delivery.setDeliveryDateTime(LocalDateTime.now().plusDays(i));
            delivery.setValue(BigDecimal.valueOf(1000 * i));
            delivery.setDescription("Delivery " + i);
            deliveryRepository.save(delivery);
        }

        // Testa primeira página
        mockMvc.perform(get("/api/deliveries")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));

        // Testa segunda página
        mockMvc.perform(get("/api/deliveries")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldGetDeliveriesByPeriod() throws Exception {
        // Cria algumas entregas em diferentes datas
        LocalDateTime now = LocalDateTime.now();
        
        // Entrega passada
        Delivery pastDelivery = new Delivery();
        pastDelivery.setDriver(driver);
        pastDelivery.setTruck(truck);
        pastDelivery.setDeliveryDateTime(now.minusDays(1));
        pastDelivery.setValue(BigDecimal.valueOf(1000));
        pastDelivery.setDescription("Past delivery");
        deliveryRepository.save(pastDelivery);

        // Entrega futura
        Delivery futureDelivery = new Delivery();
        futureDelivery.setDriver(driver);
        futureDelivery.setTruck(truck);
        futureDelivery.setDeliveryDateTime(now.plusDays(1));
        futureDelivery.setValue(BigDecimal.valueOf(2000));
        futureDelivery.setDescription("Future delivery");
        deliveryRepository.save(futureDelivery);

        // Busca entregas no período
        mockMvc.perform(get("/api/deliveries/period")
                .param("startDate", now.minusDays(2).toString())
                .param("endDate", now.plusDays(2).toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void shouldValidateDeliveryData() throws Exception {
        Delivery invalidDelivery = new Delivery();
        // Sem motorista, caminhão e com valor negativo
        invalidDelivery.setValue(BigDecimal.valueOf(-1000));
        invalidDelivery.setDeliveryDateTime(LocalDateTime.now().minusDays(1)); // Data no passado

        mockMvc.perform(post("/api/deliveries")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDelivery)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldCalculateTotalValueForToday() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        
        // Cria algumas entregas para hoje
        for (int i = 1; i <= 3; i++) {
            Delivery delivery = new Delivery();
            delivery.setDriver(driver);
            delivery.setTruck(truck);
            delivery.setDeliveryDateTime(now);
            delivery.setValue(BigDecimal.valueOf(1000));
            delivery.setDescription("Today's delivery " + i);
            deliveryRepository.save(delivery);
        }

        // Cria uma entrega para amanhã
        Delivery tomorrowDelivery = new Delivery();
        tomorrowDelivery.setDriver(driver);
        tomorrowDelivery.setTruck(truck);
        tomorrowDelivery.setDeliveryDateTime(now.plusDays(1));
        tomorrowDelivery.setValue(BigDecimal.valueOf(1000));
        tomorrowDelivery.setDescription("Tomorrow's delivery");
        deliveryRepository.save(tomorrowDelivery);

        // Verifica o total de hoje
        mockMvc.perform(get("/api/deliveries/today/total"))
                .andExpect(status().isOk())
                .andExpect(content().string("3000.00")); // 3 entregas de 1000 cada
    }
} 
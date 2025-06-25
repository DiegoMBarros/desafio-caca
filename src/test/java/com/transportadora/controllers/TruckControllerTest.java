package com.transportadora.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transportadora.config.IntegrationTestConfig;
import com.transportadora.models.Truck;
import com.transportadora.repositories.TruckRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
class TruckControllerTest extends IntegrationTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TruckRepository truckRepository;

    @AfterEach
    void cleanup() {
        truckRepository.deleteAll();
    }

    @Test
    void shouldCreateTruck() throws Exception {
        Truck truck = new Truck();
        truck.setModel("Volvo FH");
        truck.setYear(2023);
        truck.setPlate("ABC1D23");

        MvcResult result = mockMvc.perform(post("/api/trucks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(truck)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.model").value("Volvo FH"))
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.plate").value("ABC1D23"))
                .andReturn();

        // Verifica se o caminhão foi salvo no banco
        Truck savedTruck = objectMapper.readValue(
            result.getResponse().getContentAsString(), 
            Truck.class
        );
        assertTrue(truckRepository.findById(savedTruck.getId()).isPresent());
    }

    @Test
    void shouldGetTruckById() throws Exception {
        // Cria um caminhão para teste
        Truck truck = new Truck();
        truck.setModel("Volvo FH");
        truck.setYear(2023);
        truck.setPlate("ABC1D23");
        Truck savedTruck = truckRepository.save(truck);

        // Primeira chamada - deve vir do banco
        mockMvc.perform(get("/api/trucks/" + savedTruck.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTruck.getId()))
                .andExpect(jsonPath("$.model").value("Volvo FH"))
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.plate").value("ABC1D23"));

        // Segunda chamada - deve vir do cache
        mockMvc.perform(get("/api/trucks/" + savedTruck.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTruck.getId()))
                .andExpect(jsonPath("$.model").value("Volvo FH"))
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.plate").value("ABC1D23"));
    }

    @Test
    void shouldUpdateTruck() throws Exception {
        // Cria um caminhão para teste
        Truck truck = new Truck();
        truck.setModel("Volvo FH");
        truck.setYear(2023);
        truck.setPlate("ABC1D23");
        Truck savedTruck = truckRepository.save(truck);

        // Atualiza o caminhão
        savedTruck.setModel("Volvo FM");
        
        mockMvc.perform(put("/api/trucks/" + savedTruck.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedTruck)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedTruck.getId()))
                .andExpect(jsonPath("$.model").value("Volvo FM"))
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.plate").value("ABC1D23"));

        // Verifica se foi atualizado no banco
        Truck updatedTruck = truckRepository.findById(savedTruck.getId()).orElseThrow();
        assertEquals("Volvo FM", updatedTruck.getModel());
    }

    @Test
    void shouldDeleteTruck() throws Exception {
        // Cria um caminhão para teste
        Truck truck = new Truck();
        truck.setModel("Volvo FH");
        truck.setYear(2023);
        truck.setPlate("ABC1D23");
        Truck savedTruck = truckRepository.save(truck);

        // Deleta o caminhão
        mockMvc.perform(delete("/api/trucks/" + savedTruck.getId()))
                .andExpect(status().isNoContent());

        // Verifica se foi removido do banco
        assertFalse(truckRepository.findById(savedTruck.getId()).isPresent());
    }

    @Test
    void shouldListTrucksPaginated() throws Exception {
        // Cria alguns caminhões para teste
        for (int i = 1; i <= 15; i++) {
            Truck truck = new Truck();
            truck.setModel("Truck " + i);
            truck.setYear(2023);
            truck.setPlate(String.format("ABC%dD%d%d", i%10, i%10, i%10));
            truckRepository.save(truck);
        }

        // Testa primeira página
        mockMvc.perform(get("/api/trucks")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));

        // Testa segunda página
        mockMvc.perform(get("/api/trucks")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldValidateTruckData() throws Exception {
        Truck invalidTruck = new Truck();
        // Modelo vazio, ano inválido e placa inválida
        invalidTruck.setModel("");
        invalidTruck.setYear(1899); // Ano muito antigo
        invalidTruck.setPlate("INVALID"); // Formato inválido

        mockMvc.perform(post("/api/trucks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidTruck)))
                .andExpect(status().isBadRequest());
    }
} 
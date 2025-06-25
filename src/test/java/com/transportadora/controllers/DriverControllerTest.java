package com.transportadora.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.transportadora.config.IntegrationTestConfig;
import com.transportadora.models.Driver;
import com.transportadora.repositories.DriverRepository;
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
class DriverControllerTest extends IntegrationTestConfig {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DriverRepository driverRepository;

    @AfterEach
    void cleanup() {
        driverRepository.deleteAll();
    }

    @Test
    void shouldCreateDriver() throws Exception {
        Driver driver = new Driver();
        driver.setName("João da Silva");
        driver.setCnh("12345678901");

        MvcResult result = mockMvc.perform(post("/api/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(driver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("João da Silva"))
                .andExpect(jsonPath("$.cnh").value("12345678901"))
                .andReturn();

        // Verifica se o driver foi salvo no banco
        Driver savedDriver = objectMapper.readValue(
            result.getResponse().getContentAsString(), 
            Driver.class
        );
        assertTrue(driverRepository.findById(savedDriver.getId()).isPresent());
    }

    @Test
    void shouldGetDriverById() throws Exception {
        // Cria um driver para teste
        Driver driver = new Driver();
        driver.setName("João da Silva");
        driver.setCnh("12345678901");
        Driver savedDriver = driverRepository.save(driver);

        // Primeira chamada - deve vir do banco
        mockMvc.perform(get("/api/drivers/" + savedDriver.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedDriver.getId()))
                .andExpect(jsonPath("$.name").value("João da Silva"))
                .andExpect(jsonPath("$.cnh").value("12345678901"));

        // Segunda chamada - deve vir do cache
        mockMvc.perform(get("/api/drivers/" + savedDriver.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedDriver.getId()))
                .andExpect(jsonPath("$.name").value("João da Silva"))
                .andExpect(jsonPath("$.cnh").value("12345678901"));
    }

    @Test
    void shouldUpdateDriver() throws Exception {
        // Cria um driver para teste
        Driver driver = new Driver();
        driver.setName("João da Silva");
        driver.setCnh("12345678901");
        Driver savedDriver = driverRepository.save(driver);

        // Atualiza o driver
        savedDriver.setName("João Silva");
        
        mockMvc.perform(put("/api/drivers/" + savedDriver.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(savedDriver)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedDriver.getId()))
                .andExpect(jsonPath("$.name").value("João Silva"))
                .andExpect(jsonPath("$.cnh").value("12345678901"));

        // Verifica se foi atualizado no banco
        Driver updatedDriver = driverRepository.findById(savedDriver.getId()).orElseThrow();
        assertEquals("João Silva", updatedDriver.getName());
    }

    @Test
    void shouldDeleteDriver() throws Exception {
        // Cria um driver para teste
        Driver driver = new Driver();
        driver.setName("João da Silva");
        driver.setCnh("12345678901");
        Driver savedDriver = driverRepository.save(driver);

        // Deleta o driver
        mockMvc.perform(delete("/api/drivers/" + savedDriver.getId()))
                .andExpect(status().isNoContent());

        // Verifica se foi removido do banco
        assertFalse(driverRepository.findById(savedDriver.getId()).isPresent());
    }

    @Test
    void shouldListDriversPaginated() throws Exception {
        // Cria alguns drivers para teste
        for (int i = 1; i <= 15; i++) {
            Driver driver = new Driver();
            driver.setName("Driver " + i);
            driver.setCnh(String.format("%011d", i));
            driverRepository.save(driver);
        }

        // Testa primeira página
        mockMvc.perform(get("/api/drivers")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(10))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));

        // Testa segunda página
        mockMvc.perform(get("/api/drivers")
                .param("page", "1")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldValidateDriverData() throws Exception {
        Driver invalidDriver = new Driver();
        // Nome vazio e CNH inválida
        invalidDriver.setName("");
        invalidDriver.setCnh("123"); // CNH deve ter 11 dígitos

        mockMvc.perform(post("/api/drivers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDriver)))
                .andExpect(status().isBadRequest());
    }
} 
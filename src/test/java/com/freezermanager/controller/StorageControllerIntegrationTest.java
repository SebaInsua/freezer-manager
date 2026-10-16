package com.freezermanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.hamcrest.Matchers.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.repository.StorageRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StorageControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired StorageRepository storageRepository;

        @Test
    public void testIntegrationWhenStoragesItsEmpty() throws Exception{
        mockMvc.perform(get("/api/v1/storages"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

        @Test
    public void testIntegrationWhenExistsStorages() throws Exception{
        Storage storage = new Storage();
        Storage storage2 = new Storage();
        
        storage.setCapacidadLitros(BigDecimal.valueOf(150));
        storage2.setCapacidadLitros(BigDecimal.valueOf(250));

        storageRepository.save(storage);
        storageRepository.save(storage2);


        mockMvc.perform(get("/api/v1/storages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].capacidadLitros", comparesEqualTo(150)))
                .andExpect(jsonPath("$[1].capacidadLitros", comparesEqualTo(250)));
    }

        @Test
    public void testIntegrationStorageById() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setMarca("Philco");

        Storage storageGuardado = storageRepository.save(storage);

        mockMvc.perform(get("/api/v1/storages/{id}",storageGuardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(storageGuardado.getId()))
                .andExpect(jsonPath("$.marca").value("Philco"));
    }

        @Test
    public void testIntegrationWhenStoragesDoesntExists() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));

        Storage storageGuardado = storageRepository.save(storage);
        Long idInexistente = storageGuardado.getId()+1;
        while(storageRepository.existsById(idInexistente)){
            idInexistente += 1;
        }

        String messageExpected = "No existe un Freezer con el ID: " + idInexistente;

        mockMvc.perform(get("/api/v1/storages/{id}",idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(messageExpected))
                .andExpect(jsonPath("$.status").value(404));
    }
}

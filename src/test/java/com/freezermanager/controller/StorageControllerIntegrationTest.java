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
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.empty").value(true))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
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
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].capacidadLitros",
                        comparesEqualTo(150)))
                .andExpect(jsonPath("$.content[1].capacidadLitros",
                        comparesEqualTo(250)))
                .andExpect(jsonPath("$.totalElements").value(2));
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


        @Test
    public void testIntegrationStoragesWithPagination() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Storage storage = new Storage();
            storage.setMarca("Marca" + i);
            storage.setCapacidadLitros(BigDecimal.valueOf(i * 100));
            storageRepository.save(storage);
        }

        mockMvc.perform(get("/api/v1/storages")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].marca").value("Marca1"))
                .andExpect(jsonPath("$.content[1].marca").value("Marca2"))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

        @Test
    public void testIntegrationStoragesWithSecondPage() throws Exception {
        for (int i = 1; i <= 5; i++) {
            Storage storage = new Storage();
            storage.setMarca("Marca" + i);
            storage.setCapacidadLitros(BigDecimal.valueOf(i * 100));
            storageRepository.save(storage);
        }

        mockMvc.perform(get("/api/v1/storages")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].marca").value("Marca3"))
                .andExpect(jsonPath("$.content[1].marca").value("Marca4"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

        @Test
    public void testIntegrationStoragesWithDescendingSort() throws Exception {
        Storage storage1 = new Storage();
        storage1.setMarca("Philco");
        storage1.setCapacidadLitros(BigDecimal.valueOf(150));

        Storage storage2 = new Storage();
        storage2.setMarca("Samsung");
        storage2.setCapacidadLitros(BigDecimal.valueOf(250));

        Storage storage3 = new Storage();
        storage3.setMarca("Gafa");
        storage3.setCapacidadLitros(BigDecimal.valueOf(200));

        storageRepository.save(storage1);
        storageRepository.save(storage2);
        storageRepository.save(storage3);

        mockMvc.perform(get("/api/v1/storages")
                        .param("sort", "marca,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].marca").value("Samsung"))
                .andExpect(jsonPath("$.content[1].marca").value("Philco"))
                .andExpect(jsonPath("$.content[2].marca").value("Gafa"));
    }

}

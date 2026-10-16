package com.freezermanager.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StorageEntryControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageEntryRepository storageEntryRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Test
    public void testIntegrationWhenStoragesEntriesItsEmpty() throws Exception{
        mockMvc.perform(get("/api/v1/storagesentry"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

        @Test
    public void testIntegrationWhenExistsStorages() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        Storage storage2 = new Storage();
        storage2.setCapacidadLitros(BigDecimal.valueOf(220));
        storageRepository.save(storage2);

        StorageEntry entry = new StorageEntry();
        entry.setMovementDateTime(LocalDateTime.of(2026,10,16,13,15));
        entry.setRecordedAt(LocalDateTime.now());
        entry.setStorage(storage);

        StorageEntry entry2 = new StorageEntry();
        entry2.setMovementDateTime(LocalDateTime.of(2026,10,17,13,15));
        entry2.setRecordedAt(LocalDateTime.now());
        entry2.setStorage(storage2);

        storageEntryRepository.save(entry);
        storageEntryRepository.save(entry2);

        mockMvc.perform(get("/api/v1/storagesentry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].storage.capacidadLitros", comparesEqualTo(120)))
                .andExpect(jsonPath("$[1].storage.capacidadLitros", comparesEqualTo(220)));
    }

        @Test
    public void testIntegrationStorageEntryById() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        StorageEntry entry = new StorageEntry();
        entry.setMovementDateTime(LocalDateTime.of(2026,10,16,13,15));
        entry.setRecordedAt(LocalDateTime.now());
        entry.setStorage(storage);

        StorageEntry entryGuardado = storageEntryRepository.save(entry);

        mockMvc.perform(get("/api/v1/storagesentry/{id}",entryGuardado.getStorageEntryId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageEntryId").value(entryGuardado.getStorageEntryId()))
                .andExpect(jsonPath("$.storage.capacidadLitros", comparesEqualTo(120)));
    }

        @Test
    public void testIntegrationWhenStoragesEntriesDoesntExists() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        StorageEntry entry = new StorageEntry();
        entry.setMovementDateTime(LocalDateTime.of(2026,10,16,13,15));
        entry.setRecordedAt(LocalDateTime.now());
        entry.setStorage(storage);

        StorageEntry entryGuardado = storageEntryRepository.save(entry);
        Long idInexistente = entryGuardado.getStorageEntryId()+1;
        while(storageEntryRepository.existsById(idInexistente)){
            idInexistente += 1;
        }

        String messageExpected = "No existe un registro de ingreso con el ID: " + idInexistente;

        mockMvc.perform(get("/api/v1/storagesentry/{id}",idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(messageExpected))
                .andExpect(jsonPath("$.status").value(404));
    }
}

package com.freezermanager.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageEntryDetailRepository;
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
    private StorageEntryDetailRepository storageEntryDetailRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private FoodRepository foodRepository;

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

        createEntryWithDetail(storage, "Tomate");
        createEntryWithDetail(storage2, "Pollo");

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

        StorageEntry entryGuardado = createEntryWithDetail(storage, "Carne");

        mockMvc.perform(get("/api/v1/storagesentry/{id}",entryGuardado.getStorageEntryId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageEntryId").value(entryGuardado.getStorageEntryId()))
                .andExpect(jsonPath("$.storage.capacidadLitros", comparesEqualTo(120)));
    }

        @Test
    public void testIntegrationWhenStoragesEntriesDoesntExists() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        StorageEntry entryGuardado = createEntryWithDetail(storage, "Pescado");

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

        @Test
    public void testIntegrationWhenStorageEntryHasDetails() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        StorageEntry entryGuardado = createEntryWithDetail(storage, "Tomate");

        mockMvc.perform(
                get("/api/v1/storagesentry/{id}/details",
                        entryGuardado.getStorageEntryId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cantidad",
                        comparesEqualTo(2)))
                .andExpect(jsonPath("$[0].selectedUnit")
                        .value("KG"));
    }

        private StorageEntry createEntryWithDetail(Storage storage, String foodName) {
        Food food = new Food();
        food.setName(foodName);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

        StorageEntryDetail detail = new StorageEntryDetail();
        detail.setFood(savedFood);
        detail.setSelectedUnit(UnitOfMeasure.KG);
        detail.setCantidad(BigDecimal.valueOf(2));

        StorageEntry entry = new StorageEntry();
        entry.setMovementDateTime(LocalDateTime.of(2026, 10, 16, 13, 15));
        entry.setRecordedAt(LocalDateTime.now());
        entry.setStorage(storage);
        entry.setDetails(new java.util.ArrayList<>(Set.of(detail)));

        detail.setStorageEntry(entry);

        return storageEntryRepository.save(entry);
    }

}

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
import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.repository.StorageExitRepository;
import com.freezermanager.repository.StorageRepository;

import java.util.Set;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageExitDetailRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StorageExitControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageExitRepository storageExitRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private StorageExitDetailRepository storageExitDetailRepository;

    @Autowired
    private FoodRepository foodRepository;

        @Test
    public void testIntegrationWhenStoragesExitsItsEmpty() throws Exception{
        mockMvc.perform(get("/api/v1/storagesexit"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

        @Test
    public void testIntegrationWhenExistsStoragesExits() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        Storage storage2 = new Storage();
        storage2.setCapacidadLitros(BigDecimal.valueOf(220));
        storageRepository.save(storage2);

        StorageExit exit = new StorageExit();
        exit.setMovementDateTime(LocalDateTime.of(2026,10,16,13,15));
        exit.setRecordedAt(LocalDateTime.now());
        exit.setStorage(storage);

        StorageExit exit2 = new StorageExit();
        exit2.setMovementDateTime(LocalDateTime.of(2026,10,17,13,15));
        exit2.setRecordedAt(LocalDateTime.now());
        exit2.setStorage(storage2);

        storageExitRepository.save(exit);
        storageExitRepository.save(exit2);

        mockMvc.perform(get("/api/v1/storagesexit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].storage.capacidadLitros", comparesEqualTo(120)))
                .andExpect(jsonPath("$[1].storage.capacidadLitros", comparesEqualTo(220)));
    }

        @Test
    public void testIntegrationStorageExitById() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        StorageExit exit = new StorageExit();
        exit.setMovementDateTime(LocalDateTime.of(2026,10,16,13,15));
        exit.setRecordedAt(LocalDateTime.now());
        exit.setStorage(storage);

        StorageExit exitGuardado = storageExitRepository.save(exit);

        mockMvc.perform(get("/api/v1/storagesexit/{id}",exitGuardado.getStorageExitId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageExitId").value(exitGuardado.getStorageExitId()))
                .andExpect(jsonPath("$.storage.capacidadLitros", comparesEqualTo(120)));
    }

        @Test
    public void testIntegrationWhenStoragesExitsDoesntExists() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        StorageExit exit = new StorageExit();
        exit.setMovementDateTime(LocalDateTime.of(2026,10,16,13,15));
        exit.setRecordedAt(LocalDateTime.now());
        exit.setStorage(storage);

        StorageExit exitGuardado = storageExitRepository.save(exit);
        Long idInexistente = exitGuardado.getStorageExitId()+1;
        while(storageExitRepository.existsById(idInexistente)){
            idInexistente += 1;
        }

        String messageExpected = "No existe un registro de egreso con el ID: " + idInexistente;

        mockMvc.perform(get("/api/v1/storagesexit/{id}",idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(messageExpected))
                .andExpect(jsonPath("$.status").value(404));
    }

        @Test
    public void testIntegrationWhenExitHasDetails() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        StorageExit exit = new StorageExit();
        exit.setMovementDateTime(LocalDateTime.of(2026, 10, 16, 13, 15));
        exit.setRecordedAt(LocalDateTime.now());
        exit.setStorage(storage);
        StorageExit exitGuardado = storageExitRepository.save(exit);

        Food food = new Food();
        food.setName("Pollo");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.values()[0]));
        Food foodGuardado = foodRepository.save(food);

        StorageExitDetail detail = new StorageExitDetail();
        detail.setFood(foodGuardado);
        detail.setSelectedUnit(UnitOfMeasure.values()[0]);
        detail.setCantidad(BigDecimal.valueOf(2));
        detail.setStorageExit(exitGuardado);
        storageExitDetailRepository.save(detail);

        mockMvc.perform(get("/api/v1/storagesexit/{id}/details",
                        exitGuardado.getStorageExitId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cantidad", comparesEqualTo(2)))
                .andExpect(jsonPath("$[0].selectedUnit").exists())
                .andExpect(jsonPath("$[0].storageExit").doesNotExist());
    }

}

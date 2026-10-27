
package com.freezermanager.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
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
import com.freezermanager.domain.storage.ExpirationDateType;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.StorageEntryDetailRepository;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StorageBatchControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private StorageEntryRepository storageEntryRepository;

    @Autowired
    private StorageEntryDetailRepository storageEntryDetailRepository;

    @Autowired
    private StorageBatchRepository storageBatchRepository;

    private StorageBatch createBatch(
            String foodName,
            LocalDate expirationDate,
            ExpirationDateType expirationDateType) {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        Set<UnitOfMeasure> units = new LinkedHashSet<>();
        units.add(UnitOfMeasure.KG);

        Food food = new Food();
        food.setName(foodName);
        food.setUnitOfMeasures(units);
        foodRepository.save(food);

        StorageEntry entry = new StorageEntry();
        entry.setMovementDateTime(LocalDateTime.of(2026, 10, 19, 12, 0));
        entry.setStorage(storage);

        StorageEntryDetail detail = new StorageEntryDetail();
        detail.setFood(food);
        detail.setSelectedUnit(UnitOfMeasure.KG);
        detail.setCantidad(BigDecimal.valueOf(3));
        detail.setStorageEntry(entry);

        entry.setDetails(new java.util.ArrayList<>());
        entry.getDetails().add(detail);

        entry.setRecordedAt(LocalDateTime.now());

        storageEntryRepository.save(entry);
        storageEntryDetailRepository.save(detail);

        detail.setFood(food);
        detail.setSelectedUnit(UnitOfMeasure.KG);
        detail.setCantidad(BigDecimal.valueOf(3));
        detail.setStorageEntry(entry);
        storageEntryDetailRepository.save(detail);

        StorageBatch batch = new StorageBatch();
        batch.setStorageEntryDetail(detail);
        batch.setInitialQuantity(BigDecimal.valueOf(3));
        batch.setAvailableQuantity(BigDecimal.valueOf(2));
        batch.setExpirationDate(expirationDate);
        batch.setExpirationDateType(expirationDateType);

        return storageBatchRepository.save(batch);
    }

    @Test
    void shouldReturnBatchesWithActualExpirationDate() throws Exception {
        StorageBatch batch = createBatch(
                "Pollo",
                LocalDate.of(2027, 2, 10),
                ExpirationDateType.ACTUAL);

        mockMvc.perform(get("/api/v1/storagebatches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].batchId").value(batch.getId()))
                .andExpect(jsonPath("$[0].foodName").value("Pollo"))
                .andExpect(jsonPath("$[0].initialQuantity", comparesEqualTo(3)))
                .andExpect(jsonPath("$[0].availableQuantity", comparesEqualTo(2)))
                .andExpect(jsonPath("$[0].expirationDate").value("2027-02-10"))
                .andExpect(jsonPath("$[0].expirationDateType").value("ACTUAL"));
    }

    @Test
    void shouldReturnBatchesWithUndefinedExpirationDate() throws Exception {
        createBatch("Pescado", null, ExpirationDateType.UNDEFINED);

        mockMvc.perform(get("/api/v1/storagebatches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].foodName").value("Pescado"))
                .andExpect(jsonPath("$[0].expirationDate").doesNotExist())
                .andExpect(jsonPath("$[0].expirationDateType").value("UNDEFINED"));
    }

    @Test
    void shouldReturnEmptyListWhenNoBatchesExist() throws Exception {
        mockMvc.perform(get("/api/v1/storagebatches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void shouldReturnBatchesWithEstimatedExpirationDate() throws Exception {
        StorageBatch batch = createBatch(
                "Carne",
                LocalDate.of(2027, 1, 17),
                ExpirationDateType.ESTIMATED);

        mockMvc.perform(get("/api/v1/storagebatches"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].batchId").value(batch.getId()))
                .andExpect(jsonPath("$[0].foodName").value("Carne"))
                .andExpect(jsonPath("$[0].expirationDate").value("2027-01-17"))
                .andExpect(jsonPath("$[0].expirationDateType").value("ESTIMATED"));
    }
}

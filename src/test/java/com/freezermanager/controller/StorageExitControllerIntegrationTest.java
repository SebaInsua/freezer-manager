package com.freezermanager.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageExitDetailRepository;
import com.freezermanager.repository.StorageExitRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;

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

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private StorageBatchRepository storageBatchRepository;

    @Autowired
    private StorageEntryRepository storageEntryRepository;

    private final Map<Long, List<Long>> createdBatchIdsByStorage =
            new HashMap<>();

    private final Map<Long, List<Long>> createdEntryIdsByStorage =
            new HashMap<>();

    private StorageBatch createBatch(
            Storage storage,
            Food food,
            BigDecimal quantity) {

        StorageEntry entry = new StorageEntry();
        entry.setStorage(storage);
        entry.setMovementDateTime(LocalDateTime.now());
        entry.setRecordedAt(LocalDateTime.now());

        UnitOfMeasure selectedUnit =
                food.getUnitOfMeasures().contains(UnitOfMeasure.G)
                        ? UnitOfMeasure.G
                        : food.getUnitOfMeasures().iterator().next();

        StorageEntryDetail entryDetail = new StorageEntryDetail();
        entryDetail.setFood(food);
        entryDetail.setSelectedUnit(selectedUnit);
        entryDetail.setCantidad(quantity);
        entryDetail.setStorageEntry(entry);

        entry.setDetails(new ArrayList<>(List.of(entryDetail)));

        StorageEntry savedEntry =
                storageEntryRepository.saveAndFlush(entry);

        StorageEntryDetail savedDetail = savedEntry.getDetails().get(0);

        StorageBatch batch = new StorageBatch();
        batch.setStorageEntryDetail(savedDetail);
        batch.setInitialQuantity(quantity);
        batch.setAvailableQuantity(quantity);
        batch.setActive(true);

        StorageBatch savedBatch =
                storageBatchRepository.saveAndFlush(batch);

        createdEntryIdsByStorage
                .computeIfAbsent(
                        storage.getId(),
                        ignored -> new ArrayList<>())
                .add(savedEntry.getStorageEntryId());

        createdBatchIdsByStorage
                .computeIfAbsent(
                        storage.getId(),
                        ignored -> new ArrayList<>())
                .add(savedBatch.getId());

        return savedBatch;
    }

    private void cleanupBatchesAndEntries(Long storageId) {
        List<Long> batchIds =
                createdBatchIdsByStorage.remove(storageId);

        if (batchIds != null) {
            batchIds.forEach(storageBatchRepository::deleteById);
        }

        List<Long> entryIds =
                createdEntryIdsByStorage.remove(storageId);

        if (entryIds != null) {
            entryIds.forEach(storageEntryRepository::deleteById);
        }
    }

    @Test
    public void testIntegrationWhenStoragesExitsItsEmpty()
            throws Exception {

        mockMvc.perform(get("/api/v1/storagesexit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.empty").value(true));
    }

    @Test
    public void testIntegrationWhenExistsStoragesExits()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        Storage storage2 = new Storage();
        storage2.setCapacidadLitros(BigDecimal.valueOf(220));
        storageRepository.save(storage2);

        StorageExit exit = new StorageExit();
        exit.setMovementDateTime(
                LocalDateTime.of(2026, 10, 16, 13, 15));
        exit.setRecordedAt(LocalDateTime.now());
        exit.setStorage(storage);

        StorageExit exit2 = new StorageExit();
        exit2.setMovementDateTime(
                LocalDateTime.of(2026, 10, 17, 13, 15));
        exit2.setRecordedAt(LocalDateTime.now());
        exit2.setStorage(storage2);

        storageExitRepository.save(exit);
        storageExitRepository.save(exit2);

        mockMvc.perform(get("/api/v1/storagesexit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath(
                        "$.content[0].storage.capacidadLitros",
                        comparesEqualTo(120)))
                .andExpect(jsonPath(
                        "$.content[1].storage.capacidadLitros",
                        comparesEqualTo(220)));
    }

    @Test
    public void testIntegrationStorageExitById() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        StorageExit exit = new StorageExit();
        exit.setMovementDateTime(
                LocalDateTime.of(2026, 10, 16, 13, 15));
        exit.setRecordedAt(LocalDateTime.now());
        exit.setStorage(storage);

        StorageExit exitGuardado = storageExitRepository.save(exit);

        mockMvc.perform(get(
                        "/api/v1/storagesexit/{id}",
                        exitGuardado.getStorageExitId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageExitId")
                        .value(exitGuardado.getStorageExitId()))
                .andExpect(jsonPath(
                        "$.storage.capacidadLitros",
                        comparesEqualTo(120)));
    }

    @Test
    public void testIntegrationWhenStoragesExitsDoesntExists()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        StorageExit exit = new StorageExit();
        exit.setMovementDateTime(
                LocalDateTime.of(2026, 10, 16, 13, 15));
        exit.setRecordedAt(LocalDateTime.now());
        exit.setStorage(storage);

        StorageExit exitGuardado = storageExitRepository.save(exit);

        Long idInexistente = exitGuardado.getStorageExitId() + 1;

        while (storageExitRepository.existsById(idInexistente)) {
            idInexistente += 1;
        }

        String messageExpected =
                "No existe un registro de egreso con el ID: "
                        + idInexistente;

        mockMvc.perform(get(
                        "/api/v1/storagesexit/{id}",
                        idInexistente))
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
        exit.setMovementDateTime(
                LocalDateTime.of(2026, 10, 16, 13, 15));
        exit.setRecordedAt(LocalDateTime.now());
        exit.setStorage(storage);

        StorageExit exitGuardado = storageExitRepository.save(exit);

        Food food = new Food();
        food.setName("Pollo");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.values()[0]));

        Food foodGuardado = foodRepository.save(food);

        StorageBatch batch = createBatch(
                storage,
                foodGuardado,
                BigDecimal.valueOf(2));

        StorageExitDetail detail = new StorageExitDetail();
        detail.setFood(foodGuardado);
        detail.setSelectedUnit(UnitOfMeasure.values()[0]);
        detail.setCantidad(BigDecimal.valueOf(2));
        detail.setStorageExit(exitGuardado);
        detail.setStorageBatch(batch);

        storageExitDetailRepository.save(detail);

        mockMvc.perform(get(
                        "/api/v1/storagesexit/{id}/details",
                        exitGuardado.getStorageExitId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath(
                        "$[0].cantidad",
                        comparesEqualTo(2)))
                .andExpect(jsonPath("$[0].selectedUnit").exists())
                .andExpect(jsonPath("$[0].storageExit").doesNotExist());
    }

    @Test
    public void testIntegrationCreateStorageExitAndSubtractStock()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo para egreso");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));
        food.setActive(true);

        Food savedFood = foodRepository.save(food);

        StorageItem item = new StorageItem();
        item.setStorage(savedStorage);
        item.setFood(savedFood);
        item.setCantidad(BigDecimal.valueOf(5));
        storageItemRepository.save(item);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(5));

        String requestBody = """
            {
                "movementDateTime": "2026-10-22T10:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "batchId": %d,
                        "selectedUnit": "UNIT",
                        "cantidad": 2
                    }
                ]
            }
            """.formatted(
                savedStorage.getId(),
                savedFood.getFoodId(),
                batch.getId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.storageExitId").exists())
            .andExpect(jsonPath("$.details", hasSize(1)))
            .andExpect(jsonPath(
                    "$.details[0].cantidad",
                    comparesEqualTo(2)));

        StorageItem updatedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(3).compareTo(updatedItem.getCantidad()));

        StorageBatch updatedBatch = storageBatchRepository
            .findById(batch.getId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(3)
                .compareTo(updatedBatch.getAvailableQuantity()));
    }

    @Test
    public void testIntegrationExitWithInsufficientStock()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo stock insuficiente");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));
        food.setActive(true);

        Food savedFood = foodRepository.save(food);

        StorageItem item = new StorageItem();
        item.setStorage(savedStorage);
        item.setFood(savedFood);
        item.setCantidad(BigDecimal.valueOf(5));
        storageItemRepository.save(item);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(5));

        String requestBody = """
            {
                "movementDateTime": "2026-10-22T10:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "batchId": %d,
                        "selectedUnit": "UNIT",
                        "cantidad": 6
                    }
                ]
            }
            """.formatted(
                savedStorage.getId(),
                savedFood.getFoodId(),
                batch.getId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        StorageItem unchangedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(5).compareTo(unchangedItem.getCantidad()));

        StorageBatch unchangedBatch = storageBatchRepository
            .findById(batch.getId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(5)
                .compareTo(unchangedBatch.getAvailableQuantity()));

        assertEquals(0, storageExitRepository.count());
    }

        @Test
    public void testIntegrationExitInGramsAndNormalizeInventory()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo en gramos");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
        food.setActive(true);

        Food savedFood = foodRepository.save(food);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(2000));

        StorageItem item = new StorageItem();
        item.setStorage(savedStorage);
        item.setFood(savedFood);
        item.setCantidad(BigDecimal.valueOf(2000));
        storageItemRepository.save(item);

        String requestBody = """
            {
                "movementDateTime": "2026-10-22T10:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "batchId": %d,
                        "selectedUnit": "G",
                        "cantidad": 500
                    }
                ]
            }
            """.formatted(
                savedStorage.getId(),
                savedFood.getFoodId(),
                batch.getId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath(
                    "$.details[0].cantidad",
                    comparesEqualTo(500)))
            .andExpect(jsonPath(
                    "$.details[0].selectedUnit").value("G"));

        StorageItem updatedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(1500).compareTo(updatedItem.getCantidad()));
    }

    @Test
    public void testIntegrationExitInKilogramsAndNormalizeInventory()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo en kilogramos");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
        food.setActive(true);

        Food savedFood = foodRepository.save(food);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(2000));

        StorageItem item = new StorageItem();
        item.setStorage(savedStorage);
        item.setFood(savedFood);
        item.setCantidad(BigDecimal.valueOf(2000));
        storageItemRepository.save(item);

        String requestBody = """
            {
                "movementDateTime": "2026-10-22T10:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "batchId": %d,
                        "selectedUnit": "KG",
                        "cantidad": 0.5
                    }
                ]
            }
            """.formatted(
                savedStorage.getId(),
                savedFood.getFoodId(),
                batch.getId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath(
                    "$.details[0].cantidad",
                    comparesEqualTo(0.5)))
            .andExpect(jsonPath(
                    "$.details[0].selectedUnit").value("KG"));

        StorageItem updatedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(1500).compareTo(updatedItem.getCantidad()));
    }

    @Test
    public void testIntegrationExitEntireStockAndDeleteStorageItem()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo sin stock");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
        food.setActive(true);

        Food savedFood = foodRepository.save(food);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(500));

        StorageItem item = new StorageItem();
        item.setStorage(savedStorage);
        item.setFood(savedFood);
        item.setCantidad(BigDecimal.valueOf(500));
        storageItemRepository.save(item);

        String requestBody = """
            {
                "movementDateTime": "2026-10-22T10:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "batchId": %d,
                        "selectedUnit": "G",
                        "cantidad": 500
                    }
                ]
            }
            """.formatted(
                savedStorage.getId(),
                savedFood.getFoodId(),
                batch.getId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath(
                    "$.details[0].cantidad",
                    comparesEqualTo(500)))
            .andExpect(jsonPath(
                    "$.details[0].selectedUnit").value("G"));

        assertTrue(
            storageItemRepository
                .findByStorageIdAndFoodId(
                    savedStorage.getId(),
                    savedFood.getFoodId())
                .isEmpty());

        StorageBatch updatedBatch = storageBatchRepository
            .findById(batch.getId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.ZERO.compareTo(updatedBatch.getAvailableQuantity()));

        assertFalse(updatedBatch.isActive());
    }

    @Test
    public void testIntegrationExitWithInsufficientWeightStock()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo con peso insuficiente");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
        food.setActive(true);

        Food savedFood = foodRepository.save(food);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(500));

        StorageItem item = new StorageItem();
        item.setStorage(savedStorage);
        item.setFood(savedFood);
        item.setCantidad(BigDecimal.valueOf(500));
        storageItemRepository.save(item);

        String requestBody = """
            {
                "movementDateTime": "2026-10-22T10:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "batchId": %d,
                        "selectedUnit": "KG",
                        "cantidad": 1
                    }
                ]
            }
            """.formatted(
                savedStorage.getId(),
                savedFood.getFoodId(),
                batch.getId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        StorageItem unchangedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(500).compareTo(unchangedItem.getCantidad()));

        StorageBatch unchangedBatch = storageBatchRepository
            .findById(batch.getId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(500)
                .compareTo(unchangedBatch.getAvailableQuantity()));

        assertEquals(0, storageExitRepository.count());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    public void testIntegrationExitWithMultipleDetailsAndInsufficientStockRollsBack()
            throws Exception {

        Storage savedStorage = null;
        Food savedChicken = null;
        Food savedFish = null;
        StorageItem chickenItem = null;
        StorageItem fishItem = null;

        try {
            Storage storage = new Storage();
            storage.setCapacidadLitros(BigDecimal.valueOf(120));
            savedStorage = storageRepository.save(storage);

            final Long storageId = savedStorage.getId();

            Food chicken = new Food();
            chicken.setName("Pollo rollback");
            chicken.setCategory(FoodCategory.values()[0]);
            chicken.setUnitOfMeasures(
                    Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
            chicken.setActive(true);

            savedChicken = foodRepository.save(chicken);

            StorageBatch chickenBatch = createBatch(
                    savedStorage,
                    savedChicken,
                    BigDecimal.valueOf(2000));

            Food fish = new Food();
            fish.setName("Pescado rollback");
            fish.setCategory(FoodCategory.values()[0]);
            fish.setUnitOfMeasures(
                    Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
            fish.setActive(true);

            savedFish = foodRepository.save(fish);

            StorageBatch fishBatch = createBatch(
                    savedStorage,
                    savedFish,
                    BigDecimal.valueOf(300));

            chickenItem = new StorageItem();
            chickenItem.setStorage(savedStorage);
            chickenItem.setFood(savedChicken);
            chickenItem.setCantidad(BigDecimal.valueOf(2000));
            chickenItem = storageItemRepository.save(chickenItem);

            fishItem = new StorageItem();
            fishItem.setStorage(savedStorage);
            fishItem.setFood(savedFish);
            fishItem.setCantidad(BigDecimal.valueOf(300));
            fishItem = storageItemRepository.save(fishItem);

            String requestBody = """
                {
                    "movementDateTime": "2026-10-22T10:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "batchId": %d,
                            "selectedUnit": "G",
                            "cantidad": 500
                        },
                        {
                            "foodId": %d,
                            "batchId": %d,
                            "selectedUnit": "G",
                            "cantidad": 500
                        }
                    ]
                }
                """.formatted(
                    storageId,
                    savedChicken.getFoodId(),
                    chickenBatch.getId(),
                    savedFish.getFoodId(),
                    fishBatch.getId());

            mockMvc.perform(post("/api/v1/storagesexit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest());

            StorageItem unchangedChicken = storageItemRepository
                .findByStorageIdAndFoodId(
                    storageId,
                    savedChicken.getFoodId())
                .orElseThrow();

            assertEquals(
                0,
                BigDecimal.valueOf(2000)
                    .compareTo(unchangedChicken.getCantidad()));

            StorageItem unchangedFish = storageItemRepository
                .findByStorageIdAndFoodId(
                    storageId,
                    savedFish.getFoodId())
                .orElseThrow();

            assertEquals(
                0,
                BigDecimal.valueOf(300)
                    .compareTo(unchangedFish.getCantidad()));

            StorageBatch unchangedChickenBatch = storageBatchRepository
                .findById(chickenBatch.getId())
                .orElseThrow();

            assertEquals(
                0,
                BigDecimal.valueOf(2000)
                    .compareTo(unchangedChickenBatch.getAvailableQuantity()));

            StorageBatch unchangedFishBatch = storageBatchRepository
                .findById(fishBatch.getId())
                .orElseThrow();

            assertEquals(
                0,
                BigDecimal.valueOf(300)
                    .compareTo(unchangedFishBatch.getAvailableQuantity()));

            assertEquals(
                0,
                storageExitRepository.findAll().stream()
                    .filter(exit ->
                        storageId.equals(exit.getStorage().getId()))
                    .count());

        } finally {
            if (savedStorage != null) {
                final Long cleanupStorageId = savedStorage.getId();

                var exitsToDelete = storageExitRepository.findAll().stream()
                    .filter(exit ->
                        cleanupStorageId.equals(exit.getStorage().getId()))
                    .toList();

                for (StorageExit exit : exitsToDelete) {
                    var detailsToDelete =
                        storageExitDetailRepository.findAll().stream()
                            .filter(detail ->
                                detail.getStorageExit() != null
                                && exit.getStorageExitId().equals(
                                    detail.getStorageExit()
                                        .getStorageExitId()))
                            .toList();

                    storageExitDetailRepository.deleteAll(detailsToDelete);
                }

                storageExitRepository.deleteAll(exitsToDelete);
            }

            if (chickenItem != null) {
                storageItemRepository.delete(chickenItem);
            }

            if (fishItem != null) {
                storageItemRepository.delete(fishItem);
            }

            if (savedStorage != null) {
                cleanupBatchesAndEntries(savedStorage.getId());
            }

            if (savedChicken != null) {
                foodRepository.delete(savedChicken);
            }

            if (savedFish != null) {
                foodRepository.delete(savedFish);
            }

            if (savedStorage != null) {
                storageRepository.delete(savedStorage);
            }
        }
    }

        @Test
    public void testIntegrationExitWithZeroQuantityIsRejected()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Alimento egreso cantidad cero");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));

        Food savedFood = foodRepository.save(food);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(2000));

        StorageItem storageItem = new StorageItem();
        storageItem.setStorage(savedStorage);
        storageItem.setFood(savedFood);
        storageItem.setCantidad(BigDecimal.valueOf(2000));
        storageItemRepository.save(storageItem);

        String requestBody = """
            {
                "movementDateTime": "2026-10-23T12:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "batchId": %d,
                        "selectedUnit": "KG",
                        "cantidad": 0
                    }
                ]
            }
            """.formatted(
                savedStorage.getId(),
                savedFood.getFoodId(),
                batch.getId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        assertEquals(0, storageExitRepository.count());

        StorageItem unchangedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            unchangedItem.getCantidad().compareTo(BigDecimal.valueOf(2000)));
    }

    @Test
    public void testIntegrationExitWithNegativeQuantityIsRejected()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Alimento egreso cantidad negativa");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));

        Food savedFood = foodRepository.save(food);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(2000));

        StorageItem storageItem = new StorageItem();
        storageItem.setStorage(savedStorage);
        storageItem.setFood(savedFood);
        storageItem.setCantidad(BigDecimal.valueOf(2000));
        storageItemRepository.save(storageItem);

        String requestBody = """
            {
                "movementDateTime": "2026-10-23T12:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "batchId": %d,
                        "selectedUnit": "KG",
                        "cantidad": -1
                    }
                ]
            }
            """.formatted(
                savedStorage.getId(),
                savedFood.getFoodId(),
                batch.getId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        assertEquals(0, storageExitRepository.count());

        StorageItem unchangedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            unchangedItem.getCantidad().compareTo(BigDecimal.valueOf(2000)));
    }

    @Test
    public void testIntegrationExitWithNullQuantityIsRejected()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Alimento egreso cantidad nula");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));

        Food savedFood = foodRepository.save(food);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(2000));

        StorageItem storageItem = new StorageItem();
        storageItem.setStorage(savedStorage);
        storageItem.setFood(savedFood);
        storageItem.setCantidad(BigDecimal.valueOf(2000));
        storageItemRepository.save(storageItem);

        String requestBody = """
            {
                "movementDateTime": "2026-10-23T12:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "batchId": %d,
                        "selectedUnit": "KG",
                        "cantidad": null
                    }
                ]
            }
            """.formatted(
                savedStorage.getId(),
                savedFood.getFoodId(),
                batch.getId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        assertEquals(0, storageExitRepository.count());

        StorageItem unchangedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            unchangedItem.getCantidad().compareTo(BigDecimal.valueOf(2000)));
    }

    @Test
    public void testIntegrationInvalidUnitDoesNotUpdateInventory()
            throws Exception {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Carne unidad incompatible");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        food.setActive(true);

        Food savedFood = foodRepository.save(food);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(2000));

        StorageItem item = new StorageItem();
        item.setStorage(savedStorage);
        item.setFood(savedFood);
        item.setCantidad(BigDecimal.valueOf(2000));
        storageItemRepository.save(item);

        String requestBody = """
            {
                "movementDateTime": "2026-10-23T12:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "batchId": %d,
                        "selectedUnit": "UNIT",
                        "cantidad": 1
                    }
                ]
            }
            """.formatted(
                savedStorage.getId(),
                savedFood.getFoodId(),
                batch.getId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        assertEquals(0, storageExitRepository.count());

        StorageItem unchangedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(2000).compareTo(unchangedItem.getCantidad()));
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    public void testIntegrationExitWithInvalidUnitRollsBackEntireExit()
            throws Exception {

        Storage savedStorage = null;
        Food savedChicken = null;
        Food savedFish = null;
        StorageItem chickenItem = null;
        StorageItem fishItem = null;

        try {
            Storage storage = new Storage();
            storage.setCapacidadLitros(BigDecimal.valueOf(120));
            savedStorage = storageRepository.save(storage);

            Food chicken = new Food();
            chicken.setName("Pollo rollback unidad");
            chicken.setCategory(FoodCategory.values()[0]);
            chicken.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
            chicken.setActive(true);

            savedChicken = foodRepository.save(chicken);

            StorageBatch chickenBatch = createBatch(
                    savedStorage,
                    savedChicken,
                    BigDecimal.valueOf(2000));

            Food fish = new Food();
            fish.setName("Pescado rollback unidad");
            fish.setCategory(FoodCategory.values()[0]);
            fish.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
            fish.setActive(true);

            savedFish = foodRepository.save(fish);

            StorageBatch fishBatch = createBatch(
                    savedStorage,
                    savedFish,
                    BigDecimal.valueOf(2000));

            chickenItem = new StorageItem();
            chickenItem.setStorage(savedStorage);
            chickenItem.setFood(savedChicken);
            chickenItem.setCantidad(BigDecimal.valueOf(2000));
            chickenItem = storageItemRepository.save(chickenItem);

            fishItem = new StorageItem();
            fishItem.setStorage(savedStorage);
            fishItem.setFood(savedFish);
            fishItem.setCantidad(BigDecimal.valueOf(2000));
            fishItem = storageItemRepository.save(fishItem);

            String requestBody = """
                {
                    "movementDateTime": "2026-10-23T12:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "batchId": %d,
                            "selectedUnit": "KG",
                            "cantidad": 0.5
                        },
                        {
                            "foodId": %d,
                            "batchId": %d,
                            "selectedUnit": "UNIT",
                            "cantidad": 1
                        }
                    ]
                }
                """.formatted(
                    savedStorage.getId(),
                    savedChicken.getFoodId(),
                    chickenBatch.getId(),
                    savedFish.getFoodId(),
                    fishBatch.getId());

            mockMvc.perform(post("/api/v1/storagesexit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isBadRequest());

            StorageItem unchangedChicken = storageItemRepository
                .findByStorageIdAndFoodId(
                    savedStorage.getId(),
                    savedChicken.getFoodId())
                .orElseThrow();

            StorageItem unchangedFish = storageItemRepository
                .findByStorageIdAndFoodId(
                    savedStorage.getId(),
                    savedFish.getFoodId())
                .orElseThrow();

            assertEquals(
                0,
                BigDecimal.valueOf(2000)
                    .compareTo(unchangedChicken.getCantidad()));

            assertEquals(
                0,
                BigDecimal.valueOf(2000)
                    .compareTo(unchangedFish.getCantidad()));

            final Long storageIdForAssertion = savedStorage.getId();

            assertEquals(
                0,
                storageExitRepository.findAll().stream()
                    .filter(exit ->
                        storageIdForAssertion.equals(
                            exit.getStorage().getId()))
                    .count());

        } finally {
            if (savedStorage != null) {
                final Long storageId = savedStorage.getId();

                var exitsToDelete = storageExitRepository.findAll().stream()
                    .filter(exit ->
                        storageId.equals(exit.getStorage().getId()))
                    .toList();

                for (StorageExit exit : exitsToDelete) {
                    var detailsToDelete =
                        storageExitDetailRepository.findAll().stream()
                            .filter(detail ->
                                detail.getStorageExit() != null
                                && exit.getStorageExitId().equals(
                                    detail.getStorageExit()
                                        .getStorageExitId()))
                            .toList();

                    storageExitDetailRepository.deleteAll(detailsToDelete);
                }

                storageExitRepository.deleteAll(exitsToDelete);
            }

            if (chickenItem != null) {
                storageItemRepository.delete(chickenItem);
            }

            if (fishItem != null) {
                storageItemRepository.delete(fishItem);
            }

            if (savedStorage != null) {
                cleanupBatchesAndEntries(savedStorage.getId());
            }

            if (savedChicken != null) {
                foodRepository.delete(savedChicken);
            }

            if (savedFish != null) {
                foodRepository.delete(savedFish);
            }

            if (savedStorage != null) {
                storageRepository.delete(savedStorage);
            }
        }
    }

    @Test
    public void testIntegrationStorageExitsPagination() throws Exception {
        Storage storage1 = new Storage();
        storage1.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage1);

        Storage storage2 = new Storage();
        storage2.setCapacidadLitros(BigDecimal.valueOf(220));
        storageRepository.save(storage2);

        Storage storage3 = new Storage();
        storage3.setCapacidadLitros(BigDecimal.valueOf(320));
        storageRepository.save(storage3);

        StorageExit exit1 = new StorageExit();
        exit1.setMovementDateTime(
                LocalDateTime.of(2026, 10, 16, 13, 15));
        exit1.setRecordedAt(LocalDateTime.now());
        exit1.setStorage(storage1);
        storageExitRepository.save(exit1);

        StorageExit exit2 = new StorageExit();
        exit2.setMovementDateTime(
                LocalDateTime.of(2026, 10, 17, 13, 15));
        exit2.setRecordedAt(LocalDateTime.now());
        exit2.setStorage(storage2);
        storageExitRepository.save(exit2);

        StorageExit exit3 = new StorageExit();
        exit3.setMovementDateTime(
                LocalDateTime.of(2026, 10, 18, 13, 15));
        exit3.setRecordedAt(LocalDateTime.now());
        exit3.setStorage(storage3);
        storageExitRepository.save(exit3);

        mockMvc.perform(get("/api/v1/storagesexit")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "storageExitId,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].storageExitId")
                        .value(exit1.getStorageExitId()))
                .andExpect(jsonPath("$.content[1].storageExitId")
                        .value(exit2.getStorageExitId()));

        mockMvc.perform(get("/api/v1/storagesexit")
                        .param("page", "1")
                        .param("size", "2")
                        .param("sort", "storageExitId,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.content[0].storageExitId")
                        .value(exit3.getStorageExitId()));
    }

    @Test
    public void testIntegrationStorageExitsDescendingOrder()
            throws Exception {

        Storage storage1 = new Storage();
        storage1.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage1);

        Storage storage2 = new Storage();
        storage2.setCapacidadLitros(BigDecimal.valueOf(220));
        storageRepository.save(storage2);

        StorageExit exit1 = new StorageExit();
        exit1.setMovementDateTime(
                LocalDateTime.of(2026, 10, 16, 13, 15));
        exit1.setRecordedAt(LocalDateTime.now());
        exit1.setStorage(storage1);
        storageExitRepository.save(exit1);

        StorageExit exit2 = new StorageExit();
        exit2.setMovementDateTime(
                LocalDateTime.of(2026, 10, 17, 13, 15));
        exit2.setRecordedAt(LocalDateTime.now());
        exit2.setStorage(storage2);
        storageExitRepository.save(exit2);

        mockMvc.perform(get("/api/v1/storagesexit")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "storageExitId,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].storageExitId")
                        .value(exit2.getStorageExitId()))
                .andExpect(jsonPath("$.content[1].storageExitId")
                        .value(exit1.getStorageExitId()));
    }
}
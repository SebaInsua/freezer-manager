package com.freezermanager.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.ExpirationDateType;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.domain.user.User;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.UserRepository;
import com.freezermanager.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StorageEntryControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String authorizationHeader;

    @BeforeEach
    void setUpAuthenticatedUser() {
        User user = new User();
        user.setEmail("storage-entry-test-" + UUID.randomUUID() + "@example.com");
        user.setPassword("test-password-hash");
        testUser = userRepository.save(user);
        authorizationHeader = "Bearer " + jwtService.generateToken(testUser.getEmail());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
            authenticatedGet(String url, Object... uriVariables) {
        return get(url, uriVariables).header("Authorization", authorizationHeader);
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder
            authenticatedPost(String url, Object... uriVariables) {
        return post(url, uriVariables).header("Authorization", authorizationHeader);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageEntryRepository storageEntryRepository;

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private StorageBatchRepository storageBatchRepository;

        @Test
    public void testIntegrationWhenStoragesEntriesItsEmpty() throws Exception {
        mockMvc.perform(authenticatedGet("/api/v1/storagesentry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.empty").value(true))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

        @Test
    public void testIntegrationWhenExistsStorages() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        storageRepository.save(storage);

        Storage storage2 = new Storage();
        storage2.setCapacidadLitros(BigDecimal.valueOf(220));
        storage2.setOwner(testUser);
        storageRepository.save(storage2);

        createEntryWithDetail(storage, "Tomate");
        createEntryWithDetail(storage2, "Pollo");

        mockMvc.perform(authenticatedGet("/api/v1/storagesentry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].storage.capacidadLitros",
                        comparesEqualTo(120)))
                .andExpect(jsonPath("$.content[1].storage.capacidadLitros",
                        comparesEqualTo(220)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

        @Test
    public void testIntegrationStorageEntryById() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        storageRepository.save(storage);

        StorageEntry entryGuardado = createEntryWithDetail(storage, "Carne");

        mockMvc.perform(authenticatedGet("/api/v1/storagesentry/{id}",entryGuardado.getStorageEntryId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storageEntryId").value(entryGuardado.getStorageEntryId()))
                .andExpect(jsonPath("$.storage.capacidadLitros", comparesEqualTo(120)));
    }

        @Test
    public void testIntegrationWhenStoragesEntriesDoesntExists() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        storageRepository.save(storage);

        StorageEntry entryGuardado = createEntryWithDetail(storage, "Pescado");

        Long idInexistente = entryGuardado.getStorageEntryId()+1;
        while(storageEntryRepository.existsById(idInexistente)){
            idInexistente += 1;
        }

        String messageExpected = "No existe un registro de ingreso con el ID: " + idInexistente;

        mockMvc.perform(authenticatedGet("/api/v1/storagesentry/{id}",idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(messageExpected))
                .andExpect(jsonPath("$.status").value(404));
    }

        @Test
    public void testIntegrationWhenStorageEntryHasDetails() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        storageRepository.save(storage);

        StorageEntry entryGuardado = createEntryWithDetail(storage, "Tomate");

        mockMvc.perform(
                authenticatedGet("/api/v1/storagesentry/{id}/details",
                        entryGuardado.getStorageEntryId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].cantidad",
                        comparesEqualTo(2)))
                .andExpect(jsonPath("$[0].selectedUnit")
                        .value("KG"));
    }

        @Test
    public void testIntegrationCreateStorageEntryAndCreateStorageItem() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

        String requestBody = """
                {
                    "movementDateTime": "2026-10-19T12:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "selectedUnit": "KG",
                            "cantidad": 3
                        }
                    ]
                }
                """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.storageEntryId").isNumber())
                .andExpect(jsonPath("$.movementDateTime")
                        .value("2026-10-19T12:00:00"))
                .andExpect(jsonPath("$.details", hasSize(1)))
                .andExpect(jsonPath("$.details[0].food.foodId")
                        .value(savedFood.getFoodId()))
                .andExpect(jsonPath("$.details[0].selectedUnit").value("KG"))
                .andExpect(jsonPath("$.details[0].cantidad", comparesEqualTo(3)));

        StorageItem savedItem = storageItemRepository
                .findByStorageIdAndFoodId(savedStorage.getId(), savedFood.getFoodId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                savedItem.getCantidad().compareTo(BigDecimal.valueOf(3000)));
        List<StorageBatch> batches = storageBatchRepository.findAll();
        assertEquals(1, batches.size());

        StorageBatch savedBatch = batches.get(0);

        assertEquals(
            0,
            savedBatch.getInitialQuantity().compareTo(BigDecimal.valueOf(3000))
        );

        assertEquals(
            0,
            savedBatch.getAvailableQuantity().compareTo(BigDecimal.valueOf(3000))
        );

        assertEquals(
            savedBatch.getStorageEntryDetail().getEntryDetailId(),
            storageEntryRepository.findAll().get(0)
                .getDetails().get(0).getEntryDetailId()
        );
    }

        @Test
    public void testIntegrationCreateStorageEntryWithActualExpirationDate() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo con vencimiento real");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        food.setDefaultShelfLifeDays(90);
        Food savedFood = foodRepository.save(food);

        String requestBody = """
                {
                    "movementDateTime": "2026-10-19T12:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "selectedUnit": "KG",
                            "cantidad": 3,
                            "expirationDate": "2027-02-10"
                        }
                    ]
                }
                """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        List<StorageBatch> batches = storageBatchRepository.findAll();
        assertEquals(1, batches.size());

        StorageBatch batch = batches.get(0);
        assertEquals(LocalDate.of(2027, 2, 10), batch.getExpirationDate());
        assertEquals(ExpirationDateType.ACTUAL, batch.getExpirationDateType());
    }

        @Test
    public void testIntegrationCreateStorageEntryWithEstimatedExpirationDate() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo con vencimiento estimado");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        food.setDefaultShelfLifeDays(90);
        Food savedFood = foodRepository.save(food);

        String requestBody = """
                {
                    "movementDateTime": "2026-10-19T12:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "selectedUnit": "KG",
                            "cantidad": 3
                        }
                    ]
                }
                """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated());

        List<StorageBatch> batches = storageBatchRepository.findAll();
        assertEquals(1, batches.size());

        StorageBatch batch = batches.get(0);
        assertEquals(LocalDate.of(2027, 1, 17), batch.getExpirationDate());
        assertEquals(ExpirationDateType.ESTIMATED, batch.getExpirationDateType());
    }

        @Test
    public void testIntegrationCreateStorageEntryAndIncreaseExistingStorageItem() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Carne");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

        StorageItem existingItem = new StorageItem();
        existingItem.setStorage(savedStorage);
        existingItem.setFood(savedFood);
        existingItem.setCantidad(BigDecimal.valueOf(2000));
        storageItemRepository.save(existingItem);

        String requestBody = """
                {
                    "movementDateTime": "2026-10-19T12:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "selectedUnit": "KG",
                            "cantidad": 3
                        }
                    ]
                }
                """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.details", hasSize(1)))
                .andExpect(jsonPath("$.details[0].cantidad", comparesEqualTo(3)));

        StorageItem updatedItem = storageItemRepository
                .findByStorageIdAndFoodId(savedStorage.getId(), savedFood.getFoodId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                updatedItem.getCantidad().compareTo(BigDecimal.valueOf(5000)));
    }

        @Test
    public void testIntegrationInvalidUnitDoesNotUpdateInventory() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Carne");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

        StorageItem existingItem = new StorageItem();
        existingItem.setStorage(savedStorage);
        existingItem.setFood(savedFood);
        existingItem.setCantidad(BigDecimal.valueOf(2));
        storageItemRepository.save(existingItem);

        String requestBody = """
                {
                    "movementDateTime": "2026-10-19T12:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "selectedUnit": "UNIT",
                            "cantidad": 3
                        }
                    ]
                }
                """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        StorageItem unchangedItem = storageItemRepository
                .findByStorageIdAndFoodId(
                        savedStorage.getId(),
                        savedFood.getFoodId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                unchangedItem.getCantidad().compareTo(BigDecimal.valueOf(2)));

        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                storageEntryRepository.count());
    }

        @Test
    public void testIntegrationInactiveFoodDoesNotCreateStorageEntryOrUpdateInventory() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        food.setActive(false);
        Food savedFood = foodRepository.save(food);

        String requestBody = """
                {
                    "movementDateTime": "2026-10-19T12:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "selectedUnit": "KG",
                            "cantidad": 3
                        }
                    ]
                }
                """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        org.junit.jupiter.api.Assertions.assertEquals(
                0, storageEntryRepository.count());

        org.junit.jupiter.api.Assertions.assertTrue(
                storageItemRepository
                        .findByStorageIdAndFoodId(
                                savedStorage.getId(), savedFood.getFoodId())
                        .isEmpty());
    }
    
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    public void testIntegrationInactiveFoodRollsBackEntireStorageEntry() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food activeFood = new Food();
        activeFood.setName("Pollo rollback transaccional");
        activeFood.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        activeFood.setActive(true);
        Food savedActiveFood = foodRepository.save(activeFood);

        Food inactiveFood = new Food();
        inactiveFood.setName("Carne inactiva rollback transaccional");
        inactiveFood.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        inactiveFood.setActive(false);
        Food savedInactiveFood = foodRepository.save(inactiveFood);

        String requestBody = """
                {
                    "movementDateTime": "2026-10-19T12:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "selectedUnit": "KG",
                            "cantidad": 3
                        },
                        {
                            "foodId": %d,
                            "selectedUnit": "KG",
                            "cantidad": 2
                        }
                    ]
                }
                """.formatted(
                        savedStorage.getId(),
                        savedActiveFood.getFoodId(),
                        savedInactiveFood.getFoodId());

        long batchesBefore = storageBatchRepository.count();

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                        .contentType("application/json")
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        org.junit.jupiter.api.Assertions.assertEquals(
                0, storageEntryRepository.count());

        assertEquals(
            batchesBefore,
            storageBatchRepository.count(),
            "No deben persistirse lotes si la entrada completa se revierte"
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                storageItemRepository.findByStorageIdAndFoodId(
                        savedStorage.getId(), savedActiveFood.getFoodId())
                        .isEmpty());

        org.junit.jupiter.api.Assertions.assertTrue(
        storageItemRepository.findByStorageIdAndFoodId(
                savedStorage.getId(), savedInactiveFood.getFoodId()).isEmpty());
        storageItemRepository.findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedActiveFood.getFoodId())
        .ifPresent(storageItemRepository::delete);

        storageItemRepository.findByStorageIdAndFoodId(
                        savedStorage.getId(),
                        savedInactiveFood.getFoodId())
                .ifPresent(storageItemRepository::delete);
        foodRepository.delete(savedActiveFood);
        foodRepository.delete(savedInactiveFood);
        storageRepository.delete(savedStorage);
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


    @Test
    public void testIntegrationCreateStorageEntryWithDifferentWeightUnits() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Carne prueba KG y G");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
        Food savedFood = foodRepository.save(food);

        String firstRequestBody = """
                {
                    "movementDateTime": "2026-10-22T12:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "selectedUnit": "KG",
                            "cantidad": 2
                        }
                    ]
                }
                """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                        .contentType("application/json")
                        .content(firstRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.details[0].cantidad", comparesEqualTo(2)))
                .andExpect(jsonPath("$.details[0].selectedUnit").value("KG"));

        String secondRequestBody = """
                {
                    "movementDateTime": "2026-10-22T13:00:00",
                    "storageId": %d,
                    "details": [
                        {
                            "foodId": %d,
                            "selectedUnit": "G",
                            "cantidad": 500
                        }
                    ]
                }
                """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                        .contentType("application/json")
                        .content(secondRequestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.details[0].cantidad", comparesEqualTo(500)))
                .andExpect(jsonPath("$.details[0].selectedUnit").value("G"));

        StorageItem savedItem = storageItemRepository
                .findByStorageIdAndFoodId(savedStorage.getId(), savedFood.getFoodId())
                .orElseThrow();

        org.junit.jupiter.api.Assertions.assertEquals(
                0,
                savedItem.getCantidad().compareTo(BigDecimal.valueOf(2500)));
    }

        @Test
    public void testIntegrationEntryWithZeroQuantityIsRejected() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Alimento cantidad cero");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

        String requestBody = """
            {
                "movementDateTime": "2026-10-23T12:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "selectedUnit": "KG",
                        "cantidad": 0
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isBadRequest());

        assertEquals(0, storageEntryRepository.count());

        assertTrue(storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .isEmpty());
    }

        @Test
    public void testIntegrationEntryWithNegativeQuantityIsRejected() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Alimento cantidad cero");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

        String requestBody = """
            {
                "movementDateTime": "2026-10-23T12:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "selectedUnit": "KG",
                        "cantidad": -1
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isBadRequest());

        assertEquals(0, storageEntryRepository.count());

        assertTrue(storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .isEmpty());
    }

        @Test
    public void testIntegrationEntryWithNullQuantityIsRejected() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Alimento cantidad cero");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

        String requestBody = """
            {
                "movementDateTime": "2026-10-23T12:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "selectedUnit": "KG",
                        "cantidad": null
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(authenticatedPost("/api/v1/storagesentry")
                .contentType("application/json")
                .content(requestBody))
            .andExpect(status().isBadRequest());

        assertEquals(0, storageEntryRepository.count());

        assertTrue(storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId())
            .isEmpty());
    }


        @Test
    public void testIntegrationStorageEntriesWithPagination() throws Exception {
        Storage storage1 = new Storage();
        storage1.setCapacidadLitros(BigDecimal.valueOf(120));
        storage1.setOwner(testUser);
        storageRepository.save(storage1);

        Storage storage2 = new Storage();
        storage2.setCapacidadLitros(BigDecimal.valueOf(220));
        storage2.setOwner(testUser);
        storageRepository.save(storage2);

        Storage storage3 = new Storage();
        storage3.setCapacidadLitros(BigDecimal.valueOf(320));
        storage3.setOwner(testUser);
        storageRepository.save(storage3);

        createEntryWithDetail(storage1, "Tomate");
        createEntryWithDetail(storage2, "Pollo");
        createEntryWithDetail(storage3, "Carne");

        mockMvc.perform(authenticatedGet("/api/v1/storagesentry")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

        @Test
    public void testIntegrationStorageEntriesWithSecondPage() throws Exception {
        Storage storage1 = new Storage();
        storage1.setCapacidadLitros(BigDecimal.valueOf(120));
        storage1.setOwner(testUser);
        storageRepository.save(storage1);

        Storage storage2 = new Storage();
        storage2.setCapacidadLitros(BigDecimal.valueOf(220));
        storage2.setOwner(testUser);
        storageRepository.save(storage2);

        Storage storage3 = new Storage();
        storage3.setCapacidadLitros(BigDecimal.valueOf(320));
        storage3.setOwner(testUser);
        storageRepository.save(storage3);

        createEntryWithDetail(storage1, "Tomate");
        createEntryWithDetail(storage2, "Pollo");
        createEntryWithDetail(storage3, "Carne");

        mockMvc.perform(authenticatedGet("/api/v1/storagesentry")
                .param("page", "1")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

        @Test
    public void testIntegrationStorageEntriesWithDescendingSort() throws Exception {
        Storage storage1 = new Storage();
        storage1.setCapacidadLitros(BigDecimal.valueOf(120));
        storage1.setOwner(testUser);
        storageRepository.save(storage1);

        Storage storage2 = new Storage();
        storage2.setCapacidadLitros(BigDecimal.valueOf(220));
        storage2.setOwner(testUser);
        storageRepository.save(storage2);

        createEntryWithDetail(storage1, "Tomate");
        createEntryWithDetail(storage2, "Pollo");

        mockMvc.perform(authenticatedGet("/api/v1/storagesentry")
                .param("sort", "storageEntryId,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].storageEntryId")
                        .value(greaterThan(
                                (Integer) 0)));
    }

}

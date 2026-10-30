package com.freezermanager.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.UUID;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.user.User;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.UserRepository;
import com.freezermanager.service.JwtService;
import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.StorageExitDetailRepository;
import com.freezermanager.repository.StorageExitRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MovementControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private StorageEntryRepository storageEntryRepository;

    @Autowired
    private StorageBatchRepository storageBatchRepository;

    @Autowired
    private StorageExitRepository storageExitRepository;

    @Autowired
    private StorageExitDetailRepository storageExitDetailRepository;

    private String authorizationHeader;
    private User testUser;

    @BeforeEach
    void setUpAuthenticatedUser() {
        User user = new User();
        user.setEmail("movement-test-" + UUID.randomUUID() + "@example.com");
        user.setPassword("test-password-hash");

        testUser = userRepository.save(user);

        authorizationHeader = "Bearer " + jwtService.generateToken(testUser.getEmail());
    }

    @Test
    void shouldReturnEmptyPageWhenUserHasNoMovements() throws Exception {
        mockMvc.perform(get("/api/v1/movements")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.empty").value(true));
    }

    @Test
    void shouldReturnEntryMovementWithDetails() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        StorageEntry entry = createEntryWithDetail(savedStorage, "Tomate");

        mockMvc.perform(get("/api/v1/movements")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].movementId")
                        .value(entry.getStorageEntryId()))
                .andExpect(jsonPath("$.content[0].type").value("ENTRY"))
                .andExpect(jsonPath("$.content[0].movementDateTime")
                        .value("2026-10-16T13:15:00"))
                .andExpect(jsonPath("$.content[0].details", hasSize(1)))
                .andExpect(jsonPath("$.content[0].details[0].food.name")
                        .value("Tomate"))
                .andExpect(jsonPath("$.content[0].details[0].selectedUnit")
                        .value("KG"))
                .andExpect(jsonPath("$.content[0].details[0].cantidad")
                        .value(2))
                .andExpect(jsonPath("$.content[0].details[0].batchId").doesNotExist())
                .andExpect(jsonPath("$.content[0].storage.capacidadLitros")
                        .value(120));
    }

    @Test
    void shouldReturnExitMovementWithDetailsAndBatchId() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        food.setActive(true);
        Food savedFood = foodRepository.save(food);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(5));

        StorageExit exit = new StorageExit();
        exit.setMovementDateTime(LocalDateTime.of(2026, 10, 20, 15, 30));
        exit.setRecordedAt(LocalDateTime.now());
        exit.setStorage(savedStorage);

        StorageExit savedExit = storageExitRepository.save(exit);

        StorageExitDetail detail = new StorageExitDetail();
        detail.setFood(savedFood);
        detail.setSelectedUnit(UnitOfMeasure.KG);
        detail.setCantidad(BigDecimal.valueOf(2));
        detail.setStorageExit(savedExit);
        detail.setStorageBatch(batch);

        storageExitDetailRepository.save(detail);

        mockMvc.perform(get("/api/v1/movements")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].movementId")
                        .value(savedExit.getStorageExitId()))
                .andExpect(jsonPath("$.content[0].type").value("EXIT"))
                .andExpect(jsonPath("$.content[0].movementDateTime")
                        .value("2026-10-20T15:30:00"))
                .andExpect(jsonPath("$.content[0].details", hasSize(1)))
                .andExpect(jsonPath("$.content[0].details[0].food.name")
                        .value("Pollo"))
                .andExpect(jsonPath("$.content[0].details[0].selectedUnit")
                        .value("KG"))
                .andExpect(jsonPath("$.content[0].details[0].cantidad")
                        .value(2))
                .andExpect(jsonPath("$.content[0].details[0].batchId")
                        .value(batch.getId()))
                .andExpect(jsonPath("$.content[0].storage.capacidadLitros")
                        .value(120));
    }

    @Test
    void shouldReturnOnlyMovementsOwnedByAuthenticatedUser() throws Exception {
        // Almacenamiento del usuario autenticado
        Storage ownStorage = new Storage();
        ownStorage.setCapacidadLitros(BigDecimal.valueOf(120));
        ownStorage.setOwner(testUser);
        Storage savedOwnStorage = storageRepository.save(ownStorage);

        createEntryWithDetail(savedOwnStorage, "Pollo propio");

        // Creamos un segundo usuario
        User anotherUser = new User();
        anotherUser.setEmail("another-user-" + UUID.randomUUID() + "@example.com");
        anotherUser.setPassword("test-password-hash");
        User savedAnotherUser = userRepository.save(anotherUser);

        // Almacenamiento del segundo usuario
        Storage anotherStorage = new Storage();
        anotherStorage.setCapacidadLitros(BigDecimal.valueOf(220));
        anotherStorage.setOwner(savedAnotherUser);
        Storage savedAnotherStorage = storageRepository.save(anotherStorage);

        createEntryWithDetail(savedAnotherStorage, "Pescado ajeno");

        // Consultamos autenticados como el primer usuario
        mockMvc.perform(get("/api/v1/movements")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].type").value("ENTRY"))
                .andExpect(jsonPath("$.content[0].details[0].food.name")
                        .value("Pollo propio"))
                .andExpect(jsonPath("$.content[0].details[0].food.name")
                        .value(not("Pescado ajeno")));
    }

    @Test
    void shouldFilterMovementsByType() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo filtro");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        food.setActive(true);
        Food savedFood = foodRepository.save(food);

        StorageBatch batch = createBatch(
                savedStorage,
                savedFood,
                BigDecimal.valueOf(5));

        StorageExit exit = new StorageExit();
        exit.setMovementDateTime(LocalDateTime.of(2026, 10, 20, 15, 30));
        exit.setRecordedAt(LocalDateTime.now());
        exit.setStorage(savedStorage);

        StorageExit savedExit = storageExitRepository.save(exit);

        StorageExitDetail detail = new StorageExitDetail();
        detail.setFood(savedFood);
        detail.setSelectedUnit(UnitOfMeasure.KG);
        detail.setCantidad(BigDecimal.valueOf(2));
        detail.setStorageExit(savedExit);
        detail.setStorageBatch(batch);

        storageExitDetailRepository.save(detail);

        mockMvc.perform(get("/api/v1/movements")
                .param("type", "EXIT")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].type").value("EXIT"))
                .andExpect(jsonPath("$.content[0].movementId")
                        .value(savedExit.getStorageExitId()));
    }

    @Test
    void shouldFilterMovementsByStorageId() throws Exception {
        Storage firstStorage = new Storage();
        firstStorage.setCapacidadLitros(BigDecimal.valueOf(120));
        firstStorage.setOwner(testUser);
        Storage savedFirstStorage = storageRepository.save(firstStorage);

        Storage secondStorage = new Storage();
        secondStorage.setCapacidadLitros(BigDecimal.valueOf(220));
        secondStorage.setOwner(testUser);
        storageRepository.save(secondStorage);

        StorageEntry firstEntry = createEntryWithDetail(savedFirstStorage, "Pollo");

        createEntryWithDetail(secondStorage, "Pescado");

        mockMvc.perform(get("/api/v1/movements")
                .param("storageId", savedFirstStorage.getId().toString())
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].movementId")
                        .value(firstEntry.getStorageEntryId()))
                .andExpect(jsonPath("$.content[0].details[0].food.name")
                        .value("Pollo"));
    }

    @Test
    void shouldFilterMovementsByFoodId() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food chicken = new Food();
        chicken.setName("Pollo");
        chicken.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedChicken = foodRepository.save(chicken);

        Food fish = new Food();
        fish.setName("Pescado");
        fish.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFish = foodRepository.save(fish);

        StorageEntry chickenEntry = createEntryWithDetail(savedStorage, savedChicken);

        createEntryWithDetail(savedStorage, savedFish);

        mockMvc.perform(get("/api/v1/movements")
                .param("foodId", savedChicken.getFoodId().toString())
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].movementId")
                        .value(chickenEntry.getStorageEntryId()))
                .andExpect(jsonPath("$.content[0].details[0].food.name")
                        .value("Pollo"));
    }

    @Test
    void shouldFilterMovementsByDateRange() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo fechas");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

        createEntryWithDetail(
                savedStorage,
                savedFood,
                LocalDateTime.of(2026, 10, 15, 10, 0));

        StorageEntry expectedEntry = createEntryWithDetail(
                savedStorage,
                savedFood,
                LocalDateTime.of(2026, 10, 20, 15, 30));

        createEntryWithDetail(
                savedStorage,
                savedFood,
                LocalDateTime.of(2026, 10, 25, 18, 0));

        mockMvc.perform(get("/api/v1/movements")
                .param("fromDate", "2026-10-18T00:00:00")
                .param("toDate", "2026-10-22T23:59:59")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].movementId")
                        .value(expectedEntry.getStorageEntryId()))
                .andExpect(jsonPath("$.content[0].movementDateTime")
                        .value("2026-10-20T15:30:00"));
    }

    @Test
    void shouldPaginateMovements() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food chicken = new Food();
        chicken.setName("Pollo");
        chicken.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedChicken = foodRepository.save(chicken);

        Food fish = new Food();
        fish.setName("Pescado");
        fish.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFish = foodRepository.save(fish);

        Food meat = new Food();
        meat.setName("Carne");
        meat.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedMeat = foodRepository.save(meat);

        createEntryWithDetail(
                savedStorage,
                savedChicken,
                LocalDateTime.of(2026, 10, 15, 10, 0));

        createEntryWithDetail(
                savedStorage,
                savedFish,
                LocalDateTime.of(2026, 10, 16, 10, 0));

        createEntryWithDetail(
                savedStorage,
                savedMeat,
                LocalDateTime.of(2026, 10, 17, 10, 0));

        mockMvc.perform(get("/api/v1/movements")
                .param("page", "0")
                .param("size", "2")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    void shouldReturnBadRequestWhenFromDateIsAfterToDate() throws Exception {
        mockMvc.perform(get("/api/v1/movements")
                .param("fromDate", "2026-10-25T00:00:00")
                .param("toDate", "2026-10-15T00:00:00")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnEmptyPageWhenFilteringByAnotherUsersStorage() throws Exception {
        User anotherUser = new User();
        anotherUser.setEmail("another-storage-" + UUID.randomUUID() + "@example.com");
        anotherUser.setPassword("test-password-hash");
        User savedAnotherUser = userRepository.save(anotherUser);

        Storage anotherStorage = new Storage();
        anotherStorage.setCapacidadLitros(BigDecimal.valueOf(220));
        anotherStorage.setOwner(savedAnotherUser);
        Storage savedAnotherStorage = storageRepository.save(anotherStorage);

        createEntryWithDetail(savedAnotherStorage, "Pescado ajeno");

        mockMvc.perform(get("/api/v1/movements")
                .param("storageId", savedAnotherStorage.getId().toString())
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void shouldReturnUnauthorizedWhenUserIsNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/movements"))
                .andExpect(status().isUnauthorized());
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
    void shouldIgnoreDynamicSortingAndKeepChronologicalOrder() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo orden");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

        createEntryWithDetail(
                savedStorage,
                savedFood,
                LocalDateTime.of(2026, 10, 15, 10, 0));

        StorageEntry latestEntry = createEntryWithDetail(
                savedStorage,
                savedFood,
                LocalDateTime.of(2026, 10, 20, 10, 0));

        mockMvc.perform(get("/api/v1/movements")
                .param("sort", "movementDateTime,asc")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].movementId")
                        .value(latestEntry.getStorageEntryId()))
                .andExpect(jsonPath("$.content[0].movementDateTime")
                        .value("2026-10-20T10:00:00"));
    }

    private StorageEntry createEntryWithDetail(Storage storage, Food food) {
        return createEntryWithDetail(
                storage,
                food,
                LocalDateTime.of(2026, 10, 16, 13, 15));
    }

    private StorageEntry createEntryWithDetail(
            Storage storage,
            Food food,
            LocalDateTime movementDateTime) {

        StorageEntryDetail detail = new StorageEntryDetail();
        detail.setFood(food);
        detail.setSelectedUnit(UnitOfMeasure.KG);
        detail.setCantidad(BigDecimal.valueOf(2));

        StorageEntry entry = new StorageEntry();
        entry.setMovementDateTime(movementDateTime);
        entry.setRecordedAt(LocalDateTime.now());
        entry.setStorage(storage);
        entry.setDetails(new ArrayList<>(List.of(detail)));

        detail.setStorageEntry(entry);

        return storageEntryRepository.save(entry);
    }

    private StorageBatch createBatch(
            Storage storage,
            Food food,
            BigDecimal quantity) {

        StorageEntry entry = new StorageEntry();
        entry.setStorage(storage);
        entry.setMovementDateTime(
                LocalDateTime.of(2026, 10, 16, 13, 15));
        entry.setRecordedAt(LocalDateTime.now());

        StorageEntryDetail entryDetail = new StorageEntryDetail();
        entryDetail.setFood(food);
        entryDetail.setSelectedUnit(UnitOfMeasure.KG);
        entryDetail.setCantidad(quantity);
        entryDetail.setStorageEntry(entry);

        entry.setDetails(new ArrayList<>(List.of(entryDetail)));

        StorageEntry savedEntry = storageEntryRepository.saveAndFlush(entry);
        StorageEntryDetail savedDetail = savedEntry.getDetails().get(0);

        StorageBatch batch = new StorageBatch();
        batch.setStorageEntryDetail(savedDetail);
        batch.setInitialQuantity(quantity);
        batch.setAvailableQuantity(quantity);
        batch.setActive(true);

        return storageBatchRepository.saveAndFlush(batch);
    }
}
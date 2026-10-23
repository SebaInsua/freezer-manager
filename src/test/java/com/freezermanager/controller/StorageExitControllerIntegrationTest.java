package com.freezermanager.controller;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.repository.StorageExitRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;

import java.util.Set;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageExitDetailRepository;

import org.springframework.http.MediaType;

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

    @Test
    public void testIntegrationCreateStorageExitAndSubtractStock() throws Exception {
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

        String requestBody = """
            {
                "movementDateTime": "2026-10-22T10:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "selectedUnit": "UNIT",
                        "cantidad": 2
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.storageExitId").exists())
            .andExpect(jsonPath("$.details", hasSize(1)))
            .andExpect(jsonPath("$.details[0].cantidad", comparesEqualTo(2)));

        StorageItem updatedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId()
            )
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(3).compareTo(updatedItem.getCantidad())
        );
    }

    @Test
    public void testIntegrationExitWithInsufficientStock() throws Exception {
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

        String requestBody = """
            {
                "movementDateTime": "2026-10-22T10:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "selectedUnit": "UNIT",
                        "cantidad": 6
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        StorageItem unchangedItem = storageItemRepository
            .findByStorageIdAndFoodId(
                savedStorage.getId(),
                savedFood.getFoodId()
            )
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(5).compareTo(unchangedItem.getCantidad())
        );

        assertEquals(0, storageExitRepository.count());
    }

        @Test
    public void testIntegrationExitInGramsAndNormalizeInventory() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo en gramos");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
        food.setActive(true);
        Food savedFood = foodRepository.save(food);

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
                        "selectedUnit": "G",
                        "cantidad": 500
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.details[0].cantidad", comparesEqualTo(500)))
            .andExpect(jsonPath("$.details[0].selectedUnit").value("G"));

        StorageItem updatedItem = storageItemRepository
            .findByStorageIdAndFoodId(savedStorage.getId(), savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(1500).compareTo(updatedItem.getCantidad())
        );
    }

        @Test
    public void testIntegrationExitInKilogramsAndNormalizeInventory() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo en kilogramos");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
        food.setActive(true);
        Food savedFood = foodRepository.save(food);

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
                        "selectedUnit": "KG",
                        "cantidad": 0.5
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.details[0].cantidad", comparesEqualTo(0.5)))
            .andExpect(jsonPath("$.details[0].selectedUnit").value("KG"));

        StorageItem updatedItem = storageItemRepository
            .findByStorageIdAndFoodId(savedStorage.getId(), savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(1500).compareTo(updatedItem.getCantidad())
        );
    }

        @Test
    public void testIntegrationExitEntireStockAndDeleteStorageItem() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo sin stock");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
        food.setActive(true);
        Food savedFood = foodRepository.save(food);

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
                        "selectedUnit": "G",
                        "cantidad": 500
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.details[0].cantidad", comparesEqualTo(500)))
            .andExpect(jsonPath("$.details[0].selectedUnit").value("G"));

        assertTrue(
            storageItemRepository
                .findByStorageIdAndFoodId(savedStorage.getId(), savedFood.getFoodId())
                .isEmpty()
        );
    }

        @Test
    public void testIntegrationExitWithInsufficientWeightStock() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Pollo con peso insuficiente");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG, UnitOfMeasure.G));
        food.setActive(true);
        Food savedFood = foodRepository.save(food);

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
                        "selectedUnit": "KG",
                        "cantidad": 1
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        StorageItem unchangedItem = storageItemRepository
            .findByStorageIdAndFoodId(savedStorage.getId(), savedFood.getFoodId())
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(500).compareTo(unchangedItem.getCantidad())
        );

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
        // Arrange: crear almacenamiento
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        savedStorage = storageRepository.save(storage);

        // Variable final para usar en las lambdas
        final Long storageId = savedStorage.getId();

        // Crear alimento: pollo
        Food chicken = new Food();
        chicken.setName("Pollo rollback");
        chicken.setCategory(FoodCategory.values()[0]);
        chicken.setUnitOfMeasures(
            Set.of(UnitOfMeasure.KG, UnitOfMeasure.G)
        );
        chicken.setActive(true);
        savedChicken = foodRepository.save(chicken);

        // Crear alimento: pescado
        Food fish = new Food();
        fish.setName("Pescado rollback");
        fish.setCategory(FoodCategory.values()[0]);
        fish.setUnitOfMeasures(
            Set.of(UnitOfMeasure.KG, UnitOfMeasure.G)
        );
        fish.setActive(true);
        savedFish = foodRepository.save(fish);

        // Cargar stock inicial
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

        // Intentar retirar 500 g de pollo y 500 g de pescado.
        // El pollo alcanza, pero el pescado no tiene stock suficiente.
        String requestBody = """
            {
                "movementDateTime": "2026-10-22T10:00:00",
                "storageId": %d,
                "details": [
                    {
                        "foodId": %d,
                        "selectedUnit": "G",
                        "cantidad": 500
                    },
                    {
                        "foodId": %d,
                        "selectedUnit": "G",
                        "cantidad": 500
                    }
                ]
            }
            """.formatted(
                storageId,
                savedChicken.getFoodId(),
                savedFish.getFoodId()
            );

        // Act: la operación debe rechazarse
        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest());

        // Assert: el stock de pollo debe seguir siendo 2000 g
        StorageItem unchangedChicken = storageItemRepository
            .findByStorageIdAndFoodId(
                storageId,
                savedChicken.getFoodId()
            )
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(2000)
                .compareTo(unchangedChicken.getCantidad())
        );

        // Assert: el stock de pescado debe seguir siendo 300 g
        StorageItem unchangedFish = storageItemRepository
            .findByStorageIdAndFoodId(
                storageId,
                savedFish.getFoodId()
            )
            .orElseThrow();

        assertEquals(
            0,
            BigDecimal.valueOf(300)
                .compareTo(unchangedFish.getCantidad())
        );

        // Assert: no debe haberse registrado un egreso
        // para este almacenamiento
        assertEquals(
            0,
            storageExitRepository.findAll().stream()
                .filter(exit ->
                    storageId.equals(exit.getStorage().getId())
                )
                .count()
        );

    } finally {
        // Limpiar los datos creados por este test
        if (savedStorage != null) {
            // Variable final para el filtro de limpieza
            final Long cleanupStorageId = savedStorage.getId();

            // Buscar los egresos asociados al almacenamiento
            var exitsToDelete = storageExitRepository.findAll().stream()
                .filter(exit ->
                    cleanupStorageId.equals(exit.getStorage().getId())
                )
                .toList();

            // Eliminar primero los detalles de cada egreso
            for (StorageExit exit : exitsToDelete) {
                var detailsToDelete =
                    storageExitDetailRepository.findAll().stream()
                        .filter(detail ->
                            detail.getStorageExit() != null
                            && exit.getStorageExitId().equals(
                                detail.getStorageExit().getStorageExitId()
                            )
                        )
                        .toList();

                storageExitDetailRepository.deleteAll(detailsToDelete);
            }

            // Eliminar los egresos asociados
            storageExitRepository.deleteAll(exitsToDelete);
        }

        // Eliminar artículos de stock antes que sus referencias
        if (chickenItem != null) {
            storageItemRepository.delete(chickenItem);
        }

        if (fishItem != null) {
            storageItemRepository.delete(fishItem);
        }

        // Eliminar alimentos
        if (savedChicken != null) {
            foodRepository.delete(savedChicken);
        }

        if (savedFish != null) {
            foodRepository.delete(savedFish);
        }

        // Eliminar almacenamiento
        if (savedStorage != null) {
            storageRepository.delete(savedStorage);
        }
    }
}

        @Test
    public void testIntegrationExitWithZeroQuantityIsRejected() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Alimento egreso cantidad cero");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

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
                        "selectedUnit": "KG",
                        "cantidad": 0
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType("application/json")
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
            unchangedItem.getCantidad().compareTo(BigDecimal.valueOf(2000))
        );
    }

        @Test
    public void testIntegrationExitWithNegativeQuantityIsRejected() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Alimento egreso cantidad negativa");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

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
                        "selectedUnit": "KG",
                        "cantidad": -1
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType("application/json")
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
            unchangedItem.getCantidad().compareTo(BigDecimal.valueOf(2000))
        );
    }

        @Test
    public void testIntegrationExitWithNullQuantityIsRejected() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Alimento egreso cantidad nula");
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

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
                        "selectedUnit": "KG",
                        "cantidad": null
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

        mockMvc.perform(post("/api/v1/storagesexit")
                .contentType("application/json")
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
            unchangedItem.getCantidad().compareTo(BigDecimal.valueOf(2000))
        );
    }

        @Test
    public void testIntegrationInvalidUnitDoesNotUpdateInventory() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.save(storage);

        Food food = new Food();
        food.setName("Carne unidad incompatible");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        food.setActive(true);
        Food savedFood = foodRepository.save(food);

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
                        "selectedUnit": "UNIT",
                        "cantidad": 1
                    }
                ]
            }
            """.formatted(savedStorage.getId(), savedFood.getFoodId());

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
            BigDecimal.valueOf(2000).compareTo(unchangedItem.getCantidad())
        );
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

            Food fish = new Food();
            fish.setName("Pescado rollback unidad");
            fish.setCategory(FoodCategory.values()[0]);
            fish.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
            fish.setActive(true);
            savedFish = foodRepository.save(fish);

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
                            "selectedUnit": "KG",
                            "cantidad": 0.5
                        },
                        {
                            "foodId": %d,
                            "selectedUnit": "UNIT",
                            "cantidad": 1
                        }
                    ]
                }
                """.formatted(
                    savedStorage.getId(),
                    savedChicken.getFoodId(),
                    savedFish.getFoodId()
                );

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
                    .compareTo(unchangedChicken.getCantidad())
            );

            assertEquals(
                0,
                BigDecimal.valueOf(2000)
                    .compareTo(unchangedFish.getCantidad())
            );

            final Long storageIdForAssertion = savedStorage.getId();

            assertEquals(
                0,
                storageExitRepository.findAll().stream()
                    .filter(exit ->
                        storageIdForAssertion.equals(exit.getStorage().getId()))
                    .count()
            );

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
                                    detail.getStorageExit().getStorageExitId()))
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
}

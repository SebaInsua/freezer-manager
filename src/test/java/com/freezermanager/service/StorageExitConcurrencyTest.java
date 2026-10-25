package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageExitRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;

import java.time.LocalDateTime;

import java.util.Set;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.freezermanager.dto.StorageExitDetailRequest;
import com.freezermanager.dto.StorageExitRequest;

@SpringBootTest
@ActiveProfiles("postgres-test")
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class StorageExitConcurrencyTest {
    @Autowired
    private StorageExitService storageExitService;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private StorageExitRepository storageExitRepository;

    private StorageItem createInventoryWithStock(BigDecimal quantity) {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        Storage savedStorage = storageRepository.saveAndFlush(storage);

        Food food = new Food();
        food.setName("Pollo concurrencia");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.G, UnitOfMeasure.KG));
        food.setActive(true);
        Food savedFood = foodRepository.saveAndFlush(food);

        StorageItem item = new StorageItem();
        item.setStorage(savedStorage);
        item.setFood(savedFood);
        item.setCantidad(quantity);

        return storageItemRepository.saveAndFlush(item);
    }

    private void cleanUp(StorageItem item) {
        storageExitRepository.findAllByStorageId(item.getStorage().getId())
            .forEach(storageExitRepository::delete);

        storageItemRepository.delete(item);
        foodRepository.delete(item.getFood());
        storageRepository.delete(item.getStorage());
    }

    @Test
    void shouldCreateInventoryWithInitialStock() {
        StorageItem item = createInventoryWithStock(new BigDecimal("1000"));

        try {
            assertEquals(0, new BigDecimal("1000").compareTo(item.getCantidad()));
            assertNotNull(item.getId());
        } finally {
            cleanUp(item);
        }
    }

    @Test
    void shouldPreventConcurrentExitsFromConsumingMoreThanAvailableStock() throws Exception {
        StorageItem item = createInventoryWithStock(new BigDecimal("1000"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        try {
            StorageExitDetailRequest detail = new StorageExitDetailRequest(
                item.getFood().getFoodId(),
                UnitOfMeasure.G,
                new BigDecimal("700")
            );

            StorageExitRequest request = new StorageExitRequest(
                LocalDateTime.now(),
                item.getStorage().getId(),
                List.of(detail)
            );

            Future<Boolean> firstExit = executor.submit(() -> {
                startSignal.await();
                try {
                    storageExitService.createStorageExit(request);
                    return true;
                } catch (IllegalArgumentException e) {
                    if ("No hay stock suficiente para realizar el retiro".equals(e.getMessage())) {
                        return false;
                    }
                    throw e;
                }
            });

            Future<Boolean> secondExit = executor.submit(() -> {
                startSignal.await();
                try {
                    storageExitService.createStorageExit(request);
                    return true;
                } catch (IllegalArgumentException e) {
                    if ("No hay stock suficiente para realizar el retiro".equals(e.getMessage())) {
                        return false;
                    }
                    throw e;
                }
            });

            startSignal.countDown();

            boolean firstSucceeded = firstExit.get(10, TimeUnit.SECONDS);
            boolean secondSucceeded = secondExit.get(10, TimeUnit.SECONDS);

            assertTrue(firstSucceeded ^ secondSucceeded,
                "Exactamente uno de los dos retiros debe completarse");

            StorageItem remainingItem = storageItemRepository
                .findByStorageIdAndFoodId(
                    item.getStorage().getId(),
                    item.getFood().getFoodId()
                )
                .orElseThrow();

            assertEquals(
                0,
                new BigDecimal("300").compareTo(remainingItem.getCantidad()),
                "El stock final debe ser de 300 g"
            );

            assertEquals(
                1,
                storageExitRepository.findAll().stream()
                    .filter(exit -> exit.getStorage().getId()
                        .equals(item.getStorage().getId()))
                    .count(),
                "Debe registrarse exactamente un egreso"
            );
        } finally {
            startSignal.countDown();
            executor.shutdown();

            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();

                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException(
                        "Los hilos de la prueba no finalizaron a tiempo"
                    );
                }
            }

            cleanUp(item);
        }
    }
}

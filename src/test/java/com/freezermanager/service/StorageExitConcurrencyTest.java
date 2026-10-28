
package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.domain.user.User;
import com.freezermanager.dto.StorageExitDetailRequest;
import com.freezermanager.dto.StorageExitRequest;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.StorageEntryDetailRepository;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageExitDetailRepository;
import com.freezermanager.repository.StorageExitRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.UserRepository;

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

    @Autowired
    private StorageExitDetailRepository storageExitDetailRepository;

    @Autowired
    private StorageEntryRepository storageEntryRepository;

    @Autowired
    private StorageEntryDetailRepository storageEntryDetailRepository;

    @Autowired
    private StorageBatchRepository storageBatchRepository;

    @Autowired
    private UserRepository userRepository;

    private Long batchId;

    private String ownerEmail;

    private User createOwner() {
        User user = new User();
        user.setEmail("concurrency-" + UUID.randomUUID() + "@example.com");
        user.setPassword("test-password-hash");
        return userRepository.saveAndFlush(user);
    }

    private void authenticateAs(String email) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(
            new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of()
            )
        );
        SecurityContextHolder.setContext(context);
    }

    private StorageItem createInventoryWithStock(BigDecimal quantity) {
        User owner = createOwner();
        ownerEmail = owner.getEmail();

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(owner);
        Storage savedStorage = storageRepository.saveAndFlush(storage);

        Food food = new Food();
        food.setName("Pollo concurrencia " + UUID.randomUUID());
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.G, UnitOfMeasure.KG));
        food.setActive(true);
        Food savedFood = foodRepository.saveAndFlush(food);

        StorageEntry entry = new StorageEntry();
        entry.setStorage(savedStorage);
        entry.setMovementDateTime(LocalDateTime.now());
        entry.setRecordedAt(LocalDateTime.now());

        StorageEntryDetail entryDetail = new StorageEntryDetail();
        entryDetail.setFood(savedFood);
        entryDetail.setSelectedUnit(UnitOfMeasure.G);
        entryDetail.setCantidad(quantity);
        entryDetail.setStorageEntry(entry);

        entry.setDetails(List.of(entryDetail));

        StorageEntry savedEntry = storageEntryRepository.saveAndFlush(entry);

        StorageEntryDetail savedDetail =
            storageEntryDetailRepository
                .findDetailsByStorageEntryId(savedEntry.getStorageEntryId())
                .get(0);

        StorageBatch batch = new StorageBatch();
        batch.setStorageEntryDetail(savedDetail);
        batch.setInitialQuantity(quantity);
        batch.setAvailableQuantity(quantity);
        batch.setActive(true);

        StorageBatch savedBatch = storageBatchRepository.saveAndFlush(batch);
        batchId = savedBatch.getId();

        StorageItem item = new StorageItem();
        item.setStorage(savedStorage);
        item.setFood(savedFood);
        item.setCantidad(quantity);

        return storageItemRepository.saveAndFlush(item);
    }

    private void cleanUp(StorageItem item) {
        Long storageId = item.getStorage().getId();
        Long foodId = item.getFood().getFoodId();

        storageExitRepository.findAllByStorageId(storageId)
            .forEach(exit -> {
                storageExitDetailRepository
                    .findDetailsByStorageExitId(exit.getStorageExitId())
                    .forEach(storageExitDetailRepository::delete);

                storageExitRepository.delete(exit);
            });

        storageItemRepository.findByStorageIdAndFoodId(storageId, foodId)
            .ifPresent(storageItemRepository::delete);

        if (batchId != null) {
            storageBatchRepository.findById(batchId).ifPresent(batch -> {
                storageBatchRepository.delete(batch);
                storageBatchRepository.flush();
            });
        }

        storageEntryRepository.findAll().stream()
            .filter(entry -> entry.getStorage().getId().equals(storageId))
            .forEach(entry -> {
                storageEntryDetailRepository
                    .findDetailsByStorageEntryId(entry.getStorageEntryId())
                    .forEach(storageEntryDetailRepository::delete);

                storageEntryRepository.delete(entry);
            });

        foodRepository.delete(item.getFood());
        storageRepository.delete(item.getStorage());

        if (ownerEmail != null) {
            userRepository.findByEmailIgnoreCase(ownerEmail)
                .ifPresent(userRepository::delete);
        }

        batchId = null;
        ownerEmail = null;
    }

    @Test
    void shouldCreateInventoryWithInitialStock() {
        StorageItem item = createInventoryWithStock(new BigDecimal("1000"));

        try {
            assertEquals(
                0,
                new BigDecimal("1000").compareTo(item.getCantidad())
            );
            assertNotNull(item.getId());
            assertNotNull(batchId);
        } finally {
            cleanUp(item);
        }
    }

    @Test
    void shouldPreventConcurrentExitsFromConsumingMoreThanAvailableStock()
            throws Exception {

        StorageItem item = createInventoryWithStock(new BigDecimal("1000"));

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch startSignal = new CountDownLatch(1);

        try {
            StorageExitDetailRequest detail = new StorageExitDetailRequest(
                item.getFood().getFoodId(),
                batchId,
                UnitOfMeasure.G,
                new BigDecimal("700")
            );

            StorageExitRequest request = new StorageExitRequest(
                LocalDateTime.now(),
                item.getStorage().getId(),
                List.of(detail)
            );

            Future<Boolean> firstExit = executor.submit(() -> {
                authenticateAs(ownerEmail);
                try {
                    startSignal.await();
                    storageExitService.createStorageExit(request);
                    return true;
                } catch (IllegalArgumentException e) {
                    if ("No hay stock suficiente para realizar el retiro"
                            .equals(e.getMessage())
                        || "No hay stock suficiente en el lote seleccionado"
                            .equals(e.getMessage())) {
                        return false;
                    }
                    throw e;
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });

            Future<Boolean> secondExit = executor.submit(() -> {
                authenticateAs(ownerEmail);
                try {
                    startSignal.await();
                    storageExitService.createStorageExit(request);
                    return true;
                } catch (IllegalArgumentException e) {
                    if ("No hay stock suficiente para realizar el retiro"
                            .equals(e.getMessage())
                        || "No hay stock suficiente en el lote seleccionado"
                            .equals(e.getMessage())) {
                        return false;
                    }
                    throw e;
                } finally {
                    SecurityContextHolder.clearContext();
                }
            });

            startSignal.countDown();

            boolean firstSucceeded = firstExit.get(10, TimeUnit.SECONDS);
            boolean secondSucceeded = secondExit.get(10, TimeUnit.SECONDS);

            assertTrue(
                firstSucceeded ^ secondSucceeded,
                "Exactamente uno de los dos retiros debe completarse"
            );

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

            SecurityContextHolder.clearContext();
            cleanUp(item);
        }
    }
}

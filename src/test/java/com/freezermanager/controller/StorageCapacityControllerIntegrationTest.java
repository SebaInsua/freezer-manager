
package com.freezermanager.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.domain.user.User;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.UserRepository;
import com.freezermanager.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StorageCapacityControllerIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private StorageRepository storageRepository;

        @Autowired
        private FoodRepository foodRepository;

        @Autowired
        private StorageEntryRepository storageEntryRepository;

        @Autowired
        private StorageBatchRepository storageBatchRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private JwtService jwtService;

        private User createUser() {
                User user = new User();
                user.setEmail("capacity-test-" + System.nanoTime() + "@example.com");
                user.setPassword("password-hash-for-tests");
                return userRepository.save(user);
        }

        private Storage createStorage(BigDecimal capacityLiters) {
                Storage storage = new Storage();
                storage.setCapacidadLitros(capacityLiters);
                storage.setOwner(createUser());
                return storageRepository.save(storage);
        }

        private RequestPostProcessor authenticatedAs(Storage storage) {
        String token = jwtService.generateToken(storage.getOwner().getEmail());

        return request -> {
                request.addHeader(
                HttpHeaders.AUTHORIZATION,
                "Bearer " + token
                );
                return request;
        };
        }

        private void createBatch(
                Storage storage,
                String foodName,
                UnitOfMeasure unit,
                BigDecimal availableQuantity,
                BigDecimal estimatedWeightPerUnitKg) {

                Food food = new Food();
                food.setName(foodName);
                food.setUnitOfMeasures(Set.of(unit));
                food.setEstimatedWeightPerUnitKg(estimatedWeightPerUnitKg);
                foodRepository.save(food);

                StorageEntry entry = new StorageEntry();
                entry.setMovementDateTime(LocalDateTime.of(2026, 10, 28, 12, 0));
                entry.setRecordedAt(LocalDateTime.of(2026, 10, 28, 12, 0));
                entry.setStorage(storage);
                entry.setDetails(new ArrayList<>());

                StorageEntryDetail detail = new StorageEntryDetail();
                detail.setFood(food);
                detail.setSelectedUnit(unit);
                detail.setCantidad(availableQuantity);
                detail.setStorageEntry(entry);
                entry.getDetails().add(detail);

                storageEntryRepository.save(entry);

                StorageBatch batch = new StorageBatch();
                batch.setStorageEntryDetail(detail);
                batch.setInitialQuantity(availableQuantity);
                batch.setAvailableQuantity(availableQuantity);

                storageBatchRepository.save(batch);
        }

        @Test
        void shouldReturnCapacityForEmptyStorage() throws Exception {
                Storage storage = createStorage(new BigDecimal("100"));

                mockMvc.perform(
                        get("/api/v1/storages/{id}/capacity", storage.getId())
                                .with(authenticatedAs(storage)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.storageId").value(storage.getId()))
                        .andExpect(jsonPath("$.capacidadNominalLitros").value(100.0))
                        .andExpect(jsonPath("$.porcentajeMaximoRecomendado").value(80.0))
                        .andExpect(jsonPath("$.capacidadRecomendadaLitros").value(80.0))
                        .andExpect(jsonPath("$.cargaActualKgEstimada").value(0.0))
                        .andExpect(jsonPath("$.ocupacionEquivalenteLitros").value(0.0))
                        .andExpect(jsonPath("$.capacidadRestanteRecomendadaLitros").value(80.0))
                        .andExpect(jsonPath("$.estimacionCompleta").value(true))
                        .andExpect(jsonPath("$.estado").value("DENTRO_DEL_LIMITE"));
        }

        @Test
        void shouldCalculateCapacityFromAvailableKilogramsStoredAsGrams()
                throws Exception {
                Storage storage = createStorage(new BigDecimal("100"));

                createBatch(
                        storage,
                        "Pollo",
                        UnitOfMeasure.KG,
                        new BigDecimal("10000"),
                        null);

                mockMvc.perform(
                        get("/api/v1/storages/{id}/capacity", storage.getId())
                                .with(authenticatedAs(storage)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.cargaActualKgEstimada").value(10.0))
                        .andExpect(jsonPath("$.ocupacionEquivalenteLitros").value(35.0))
                        .andExpect(jsonPath("$.porcentajeOcupacionRecomendado").value(43.75))
                        .andExpect(jsonPath("$.capacidadRestanteRecomendadaLitros").value(45.0))
                        .andExpect(jsonPath("$.estimacionCompleta").value(true))
                        .andExpect(jsonPath("$.estado").value("DENTRO_DEL_LIMITE"));
        }

        @Test
        void shouldCalculateCapacityUsingEstimatedWeightForUnits()
                        throws Exception {
                Storage storage = createStorage(new BigDecimal("100"));

                createBatch(
                        storage,
                        "Hamburguesas",
                        UnitOfMeasure.UNIT,
                        new BigDecimal("20"),
                        new BigDecimal("0.150"));

                mockMvc.perform(
                        get("/api/v1/storages/{id}/capacity", storage.getId())
                                .with(authenticatedAs(storage)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.cargaActualKgEstimada").value(3.0))
                        .andExpect(jsonPath("$.ocupacionEquivalenteLitros").value(10.5))
                        .andExpect(jsonPath("$.estimacionCompleta").value(true))
                        .andExpect(jsonPath("$.estado").value("DENTRO_DEL_LIMITE"));
        }

        @Test
        void shouldReturnPartialEstimateWhenUnitWeightIsMissing()
                throws Exception {
                Storage storage = createStorage(new BigDecimal("100"));

                createBatch(
                        storage,
                        "Empanadas",
                        UnitOfMeasure.UNIT,
                        new BigDecimal("12"),
                        null);

                mockMvc.perform(
                        get("/api/v1/storages/{id}/capacity", storage.getId())
                                .with(authenticatedAs(storage)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.estimacionCompleta").value(false))
                        .andExpect(jsonPath("$.estado").value("ESTIMACION_PARCIAL"));
        }

        @Test
        void shouldReturnNotFoundWhenStorageDoesNotExist() throws Exception {
                User user = createUser();
                String token = jwtService.generateToken(user.getEmail());

                mockMvc.perform(
                        get("/api/v1/storages/{id}/capacity", 999999L)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                        .andExpect(status().isNotFound());
        }

        @Test
        void shouldReturnExceededLimitWhenEstimatedOccupancyIsAboveRecommendedCapacity()
                throws Exception {
                Storage storage = createStorage(new BigDecimal("100"));

                // 25 kg equivalen a 87.5 litros, por encima del límite recomendado de 80.
                createBatch(
                        storage,
                        "Carne",
                        UnitOfMeasure.KG,
                        new BigDecimal("25000"),
                        null);

                mockMvc.perform(
                        get("/api/v1/storages/{id}/capacity", storage.getId())
                                .with(authenticatedAs(storage)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.cargaActualKgEstimada").value(25.0))
                        .andExpect(jsonPath("$.ocupacionEquivalenteLitros").value(87.5))
                        .andExpect(jsonPath("$.capacidadRestanteRecomendadaLitros").value(0.0))
                        .andExpect(jsonPath("$.estado").value("LIMITE_SUPERADO"));
        }

        @Test
        void shouldIgnoreBatchWithZeroAvailableQuantity() throws Exception {
                Storage storage = createStorage(new BigDecimal("100"));

                createBatch(
                        storage,
                        "Pollo",
                        UnitOfMeasure.KG,
                        new BigDecimal("10000"),
                        null);

                StorageBatch batch = storageBatchRepository.findAll().get(0);
                batch.setAvailableQuantity(BigDecimal.ZERO);
                storageBatchRepository.save(batch);

                mockMvc.perform(
                        get("/api/v1/storages/{id}/capacity", storage.getId())
                                .with(authenticatedAs(storage)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.cargaActualKgEstimada").value(0.0))
                        .andExpect(jsonPath("$.ocupacionEquivalenteLitros").value(0.0))
                        .andExpect(jsonPath("$.estimacionCompleta").value(true))
                        .andExpect(jsonPath("$.estado").value("DENTRO_DEL_LIMITE"));
        }

        @Test
        void shouldIgnoreInactiveBatch() throws Exception {
                Storage storage = createStorage(new BigDecimal("100"));

                createBatch(
                        storage,
                        "Pescado",
                        UnitOfMeasure.KG,
                        new BigDecimal("10000"),
                        null);

                StorageBatch batch = storageBatchRepository.findAll().get(0);
                batch.setActive(false);
                storageBatchRepository.save(batch);

                mockMvc.perform(
                        get("/api/v1/storages/{id}/capacity", storage.getId())
                                .with(authenticatedAs(storage)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.cargaActualKgEstimada").value(0.0))
                        .andExpect(jsonPath("$.ocupacionEquivalenteLitros").value(0.0))
                        .andExpect(jsonPath("$.estimacionCompleta").value(true))
                        .andExpect(jsonPath("$.estado").value("DENTRO_DEL_LIMITE"));
        }

        @Test
        void shouldCalculateCapacityFromAvailableGrams() throws Exception {
                Storage storage = createStorage(new BigDecimal("100"));

                createBatch(
                        storage,
                        "Verduras",
                        UnitOfMeasure.G,
                        new BigDecimal("5000"),
                        null);

                mockMvc.perform(
                        get("/api/v1/storages/{id}/capacity", storage.getId())
                                .with(authenticatedAs(storage)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.cargaActualKgEstimada").value(5.0))
                        .andExpect(jsonPath("$.ocupacionEquivalenteLitros").value(17.5))
                        .andExpect(jsonPath("$.estimacionCompleta").value(true))
                        .andExpect(jsonPath("$.estado").value("DENTRO_DEL_LIMITE"));
        }

        @Test
        void shouldCalculateKnownWeightAndReturnPartialEstimateWhenUnitWeightIsMissing()
                throws Exception {
                Storage storage = createStorage(new BigDecimal("100"));

                createBatch(
                        storage,
                        "Pollo",
                        UnitOfMeasure.KG,
                        new BigDecimal("10000"),
                        null);

                createBatch(
                        storage,
                        "Empanadas",
                        UnitOfMeasure.UNIT,
                        new BigDecimal("12"),
                        null);

                mockMvc.perform(
                        get("/api/v1/storages/{id}/capacity", storage.getId())
                                .with(authenticatedAs(storage)))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.cargaActualKgEstimada").value(10.0))
                        .andExpect(jsonPath("$.ocupacionEquivalenteLitros").value(35.0))
                        .andExpect(jsonPath("$.estimacionCompleta").value(false))
                        .andExpect(jsonPath("$.estado").value("ESTIMACION_PARCIAL"));
        }
}

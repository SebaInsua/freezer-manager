
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
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
import com.freezermanager.domain.user.User;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.StorageEntryDetailRepository;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.UserRepository;
import com.freezermanager.service.JwtService;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User createUser() {
        User user = new User();
        user.setEmail("storage-batch-test@example.com");
        user.setPassword("test-password-hash");
        return userRepository.save(user);
    }

    private String authorizationHeader(User user) {
        return "Bearer " + jwtService.generateToken(user.getEmail());
    }

    private StorageBatch createBatch(
            User owner,
            String foodName,
            LocalDate expirationDate,
            ExpirationDateType expirationDateType) {

        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(owner);
        storageRepository.save(storage);

        Set<UnitOfMeasure> units = new LinkedHashSet<>();
        units.add(UnitOfMeasure.KG);

        Food food = new Food();
        food.setName(foodName);
        food.setUnitOfMeasures(units);
        foodRepository.save(food);

        StorageEntry entry = new StorageEntry();
        entry.setMovementDateTime(
                LocalDateTime.of(2026, 10, 19, 12, 0));
        entry.setStorage(storage);
        entry.setRecordedAt(LocalDateTime.now());
        entry.setDetails(new java.util.ArrayList<>());

        StorageEntryDetail detail = new StorageEntryDetail();
        detail.setFood(food);
        detail.setSelectedUnit(UnitOfMeasure.KG);
        detail.setCantidad(BigDecimal.valueOf(3));
        detail.setStorageEntry(entry);

        entry.getDetails().add(detail);

        storageEntryRepository.save(entry);
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
        User owner = createUser();

        StorageBatch batch = createBatch(
                owner,
                "Pollo",
                LocalDate.of(2027, 2, 10),
                ExpirationDateType.ACTUAL);

        mockMvc.perform(get("/api/v1/storagebatches")
                .header("Authorization", authorizationHeader(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].batchId").value(batch.getId()))
                .andExpect(jsonPath("$.content[0].foodName").value("Pollo"))
                .andExpect(jsonPath("$.content[0].initialQuantity", comparesEqualTo(3)))
                .andExpect(jsonPath("$.content[0].availableQuantity", comparesEqualTo(2)))
                .andExpect(jsonPath("$.content[0].expirationDate").value("2027-02-10"))
                .andExpect(jsonPath("$.content[0].expirationDateType").value("ACTUAL"));
    }

    @Test
    void shouldReturnBatchesWithUndefinedExpirationDate() throws Exception {
        User owner = createUser();

        createBatch(
                owner,
                "Pescado",
                null,
                ExpirationDateType.UNDEFINED);

        mockMvc.perform(get("/api/v1/storagebatches")
                .header("Authorization", authorizationHeader(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].foodName").value("Pescado"))
                .andExpect(jsonPath("$.content[0].expirationDate").doesNotExist())
                .andExpect(jsonPath("$.content[0].expirationDateType").value("UNDEFINED"));
    }

    @Test
    void shouldReturnEmptyListWhenNoBatchesExist() throws Exception {
        User owner = createUser();

        mockMvc.perform(get("/api/v1/storagebatches")
                .header("Authorization", authorizationHeader(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void shouldReturnBatchesWithEstimatedExpirationDate() throws Exception {
        User owner = createUser();

        StorageBatch batch = createBatch(
                owner,
                "Carne",
                LocalDate.of(2027, 1, 17),
                ExpirationDateType.ESTIMATED);

        mockMvc.perform(get("/api/v1/storagebatches")
                .header("Authorization", authorizationHeader(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].batchId").value(batch.getId()))
                .andExpect(jsonPath("$.content[0].foodName").value("Carne"))
                .andExpect(jsonPath("$.content[0].expirationDate").value("2027-01-17"))
                .andExpect(jsonPath("$.content[0].expirationDateType").value("ESTIMATED"));
    }

    @Test
    void shouldReturnBatchesAccordingToRequestedPageSize() throws Exception {
        User owner = createUser();

        createBatch(owner, "Pollo", LocalDate.of(2027, 2, 10), ExpirationDateType.ACTUAL);
        createBatch(owner, "Carne", LocalDate.of(2027, 3, 10), ExpirationDateType.ACTUAL);
        createBatch(owner, "Pescado", LocalDate.of(2027, 4, 10), ExpirationDateType.ACTUAL);

        mockMvc.perform(get("/api/v1/storagebatches")
                .param("page", "0")
                .param("size", "2")
                .header("Authorization", authorizationHeader(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(2));
    }

    @Test
    void shouldReturnDifferentBatchesWhenRequestingNextPage() throws Exception {
        User owner = createUser();

        createBatch(owner, "Pollo", LocalDate.of(2027, 2, 10), ExpirationDateType.ACTUAL);
        createBatch(owner, "Carne", LocalDate.of(2027, 3, 10), ExpirationDateType.ACTUAL);
        createBatch(owner, "Pescado", LocalDate.of(2027, 4, 10), ExpirationDateType.ACTUAL);

        mockMvc.perform(get("/api/v1/storagebatches")
                .param("page", "0")
                .param("size", "2")
                .header("Authorization", authorizationHeader(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].foodName").value("Pollo"))
                .andExpect(jsonPath("$.content[1].foodName").value("Carne"));

        mockMvc.perform(get("/api/v1/storagebatches")
                .param("page", "1")
                .param("size", "2")
                .header("Authorization", authorizationHeader(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].foodName").value("Pescado"))
                .andExpect(jsonPath("$.number").value(1));
    }

    @Test
    void shouldReturnOnlyBatchesBelongingToAuthenticatedUser() throws Exception {
        User owner = createUser();

        User anotherUser = new User();
        anotherUser.setEmail("another-user@example.com");
        anotherUser.setPassword("test-password-hash");
        userRepository.save(anotherUser);

        createBatch(owner, "Pollo", LocalDate.of(2027, 2, 10), ExpirationDateType.ACTUAL);
        createBatch(anotherUser, "Carne", LocalDate.of(2027, 3, 10), ExpirationDateType.ACTUAL);

        mockMvc.perform(get("/api/v1/storagebatches")
                .header("Authorization", authorizationHeader(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].foodName").value("Pollo"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}


package com.freezermanager.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.domain.user.User;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageEntryDetailRepository;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.UserRepository;
import com.freezermanager.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StorageEntryDetailControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageEntryDetailRepository storageEntryDetailRepository;

    @Autowired
    private StorageEntryRepository storageEntryRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private String authorizationHeader;

    @BeforeEach
    void setUpAuthenticatedUser() {
        User user = new User();
        user.setEmail("entry-detail-test-" + UUID.randomUUID() + "@example.com");
        user.setPassword("test-password-hash");

        testUser = userRepository.save(user);
        authorizationHeader = "Bearer " + jwtService.generateToken(testUser.getEmail());
    }

    @Test
    void shouldReturnEmptyPageWhenNoDetailsExist() throws Exception {
        mockMvc.perform(get("/api/v1/storage-entrydetail")
                .header("Authorization", authorizationHeader))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.empty").value(true));
    }

    @Test
    void shouldReturnPagedStorageEntryDetails() throws Exception {
        createEntryWithDetail("Tomate", BigDecimal.valueOf(2));
        createEntryWithDetail("Pollo", BigDecimal.valueOf(3));

        mockMvc.perform(get("/api/v1/storage-entrydetail")
                .header("Authorization", authorizationHeader)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void shouldReturnSecondPageOfStorageEntryDetails() throws Exception {
        createEntryWithDetail("Tomate", BigDecimal.valueOf(2));
        createEntryWithDetail("Pollo", BigDecimal.valueOf(3));
        createEntryWithDetail("Carne", BigDecimal.valueOf(4));

        mockMvc.perform(get("/api/v1/storage-entrydetail")
                .header("Authorization", authorizationHeader)
                .param("page", "1")
                .param("size", "2")
                .param("sort", "entryDetailId,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void shouldSortStorageEntryDetailsDescending() throws Exception {
        createEntryWithDetail("Tomate", BigDecimal.valueOf(2));
        createEntryWithDetail("Pollo", BigDecimal.valueOf(3));

        var details = storageEntryDetailRepository.findAll(
                Sort.by(Sort.Direction.ASC, "entryDetailId"));

        Long firstId = details.get(0).getEntryDetailId();
        Long secondId = details.get(1).getEntryDetailId();

        mockMvc.perform(get("/api/v1/storage-entrydetail")
                .header("Authorization", authorizationHeader)
                .param("page", "0")
                .param("size", "10")
                .param("sort", "entryDetailId,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].entryDetailId").value(secondId))
                .andExpect(jsonPath("$.content[1].entryDetailId").value(firstId));
    }

    private StorageEntry createEntryWithDetail(String foodName, BigDecimal quantity) {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage.setOwner(testUser);
        storageRepository.save(storage);

        Food food = new Food();
        food.setName(foodName);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        Food savedFood = foodRepository.save(food);

        StorageEntryDetail detail = new StorageEntryDetail();
        detail.setFood(savedFood);
        detail.setSelectedUnit(UnitOfMeasure.KG);
        detail.setCantidad(quantity);

        StorageEntry entry = new StorageEntry();
        entry.setMovementDateTime(LocalDateTime.of(2026, 10, 16, 13, 15));
        entry.setRecordedAt(LocalDateTime.now());
        entry.setStorage(storage);
        entry.setDetails(new java.util.ArrayList<>(Set.of(detail)));

        detail.setStorageEntry(entry);

        return storageEntryRepository.save(entry);
    }
}


package com.freezermanager.controller;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

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
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.domain.user.User;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.UserRepository;
import com.freezermanager.service.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StorageItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private FoodRepository foodRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String uniqueEmail() {
        return "storage-item-" + UUID.randomUUID() + "@example.com";
    }

    private User createUser() {
        User user = new User();
        user.setEmail(uniqueEmail());
        user.setPassword("test-password-hash");
        return userRepository.save(user);
    }

    private Storage createStorage(User owner, BigDecimal capacity) {
        Storage storage = new Storage();
        storage.setCapacidadLitros(capacity);
        storage.setOwner(owner);
        return storageRepository.save(storage);
    }

    private RequestPostProcessor authenticatedAs(User user) {
        String token = jwtService.generateToken(user.getEmail());

        return request -> {
            request.addHeader(
                    HttpHeaders.AUTHORIZATION,
                    "Bearer " + token);
            return request;
        };
    }

    private Set<UnitOfMeasure> supportedUnits() {
        Set<UnitOfMeasure> units = new LinkedHashSet<>();
        units.add(UnitOfMeasure.KG);
        units.add(UnitOfMeasure.G);
        return units;
    }

    private Food createFood(String name) {
        Food food = new Food();
        food.setName(name + "-" + UUID.randomUUID());
        food.setUnitOfMeasures(supportedUnits());
        return foodRepository.save(food);
    }

    private StorageItem createStorageItem(
            Storage storage,
            Food food,
            BigDecimal quantity) {
        StorageItem item = new StorageItem();
        item.setStorage(storage);
        item.setFood(food);
        item.setCantidad(quantity);
        return storageItemRepository.save(item);
    }

    @Test
    void testIntegrationWhenStorageItemItsEmpty() throws Exception {
        User user = createUser();

        mockMvc.perform(get("/api/v1/storageitems")
                .with(authenticatedAs(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.empty").value(true))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void testIntegrationWhenExistsStoragesItems() throws Exception {
        User owner = createUser();

        Storage storage = createStorage(owner, BigDecimal.valueOf(120));
        Storage storage2 = createStorage(owner, BigDecimal.valueOf(200));

        Food food = createFood("Tomate");
        Food food2 = createFood("Pescado");

        createStorageItem(storage, food, BigDecimal.valueOf(20));
        createStorageItem(storage2, food2, BigDecimal.valueOf(10));

        mockMvc.perform(get("/api/v1/storageitems")
                .with(authenticatedAs(owner))
                .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].storage.capacidadLitros",
                        comparesEqualTo(120)))
                .andExpect(jsonPath("$.content[0].food.name",
                        org.hamcrest.Matchers.startsWith("Tomate")))
                .andExpect(jsonPath("$.content[0].cantidad", comparesEqualTo(20)))
                .andExpect(jsonPath("$.content[1].storage.capacidadLitros",
                        comparesEqualTo(200)))
                .andExpect(jsonPath("$.content[1].food.name",
                        org.hamcrest.Matchers.startsWith("Pescado")))
                .andExpect(jsonPath("$.content[1].cantidad", comparesEqualTo(10)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void testIntegrationStorageItemById() throws Exception {
        User owner = createUser();
        Storage storage = createStorage(owner, BigDecimal.valueOf(120));
        Food food = createFood("Tomate");

        StorageItem item = createStorageItem(
                storage,
                food,
                BigDecimal.valueOf(20));

        mockMvc.perform(get("/api/v1/storageitems/{id}", item.getId())
                .with(authenticatedAs(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(item.getId()))
                .andExpect(jsonPath("$.storage.capacidadLitros",
                        comparesEqualTo(120)))
                .andExpect(jsonPath("$.food.name",
                        org.hamcrest.Matchers.startsWith("Tomate")))
                .andExpect(jsonPath("$.cantidad", comparesEqualTo(20)));
    }

    @Test
    void testIntegrationWhenStoragesItemsDoesntExists() throws Exception {
        User owner = createUser();
        Storage storage = createStorage(owner, BigDecimal.valueOf(120));
        Food food = createFood("Tomate");

        StorageItem item = createStorageItem(
                storage,
                food,
                BigDecimal.valueOf(20));

        Long nonexistentId = item.getId() + 1;

        while (storageItemRepository.existsById(nonexistentId)) {
            nonexistentId++;
        }

        String expectedMessage = "No existe un elemento en el inventario con el ID: " + nonexistentId;

        mockMvc.perform(get("/api/v1/storageitems/{id}", nonexistentId)
                .with(authenticatedAs(owner)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void testIntegrationStorageItemsWithPagination() throws Exception {
        User owner = createUser();

        for (int i = 1; i <= 3; i++) {
            Storage storage = createStorage(
                    owner,
                    BigDecimal.valueOf(100 + i));

            Food food = createFood("Alimento " + i);

            createStorageItem(
                    storage,
                    food,
                    BigDecimal.valueOf(i));
        }

        mockMvc.perform(get("/api/v1/storageitems")
                .with(authenticatedAs(owner))
                .param("page", "0")
                .param("size", "1")
                .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].cantidad", comparesEqualTo(1)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void testIntegrationStorageItemsWithSecondPage() throws Exception {
        User owner = createUser();

        for (int i = 1; i <= 3; i++) {
            Storage storage = createStorage(
                    owner,
                    BigDecimal.valueOf(100 + i));

            Food food = createFood("Alimento " + i);

            createStorageItem(
                    storage,
                    food,
                    BigDecimal.valueOf(i));
        }

        mockMvc.perform(get("/api/v1/storageitems")
                .with(authenticatedAs(owner))
                .param("page", "1")
                .param("size", "1")
                .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].cantidad", comparesEqualTo(2)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    @Test
    void testIntegrationStorageItemsWithDescendingSort() throws Exception {
        User owner = createUser();

        for (int i = 1; i <= 3; i++) {
            Storage storage = createStorage(
                    owner,
                    BigDecimal.valueOf(100 + i));

            Food food = createFood("Alimento " + i);

            createStorageItem(
                    storage,
                    food,
                    BigDecimal.valueOf(i));
        }

        mockMvc.perform(get("/api/v1/storageitems")
                .with(authenticatedAs(owner))
                .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].cantidad", comparesEqualTo(3)))
                .andExpect(jsonPath("$.content[1].cantidad", comparesEqualTo(2)))
                .andExpect(jsonPath("$.content[2].cantidad", comparesEqualTo(1)));
    }

    @Test
    void testIntegrationStorageItemsCannotBeAccessedByAnotherUser() throws Exception {
        User owner = createUser();
        User otherUser = createUser();

        Storage storage = createStorage(owner, BigDecimal.valueOf(120));
        Food food = createFood("Tomate");

        StorageItem item = createStorageItem(
                storage,
                food,
                BigDecimal.valueOf(20));

        mockMvc.perform(get("/api/v1/storageitems/{id}", item.getId())
                .with(authenticatedAs(otherUser)))
                .andExpect(status().isNotFound());
    }
}

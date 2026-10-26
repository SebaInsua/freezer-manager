package com.freezermanager.controller;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StorageItemIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StorageItemRepository storageItemRepository;

    @Autowired
    private StorageRepository storageRepository;

    @Autowired
    private FoodRepository foodRepository;


        @Test
    public void testIntegrationWhenStorageItemItsEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/storageitems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.empty").value(true))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }


        @Test
    public void testIntegrationWhenExistsStoragesItems() throws Exception{
        Storage storage = new Storage();
        Storage storage2 = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storage2.setCapacidadLitros(BigDecimal.valueOf(200));
        storageRepository.save(storage);
        storageRepository.save(storage2);

        Set<UnitOfMeasure> unitOfMeasures = new LinkedHashSet<>();
        unitOfMeasures.add(UnitOfMeasure.KG);
        unitOfMeasures.add(UnitOfMeasure.G);
        Food food = new Food();
        Food food2 = new Food();
        food.setName("Tomate");
        food.setUnitOfMeasures(unitOfMeasures);
        food2.setName("Pescado");
        food2.setUnitOfMeasures(unitOfMeasures);
        foodRepository.save(food);
        foodRepository.save(food2);

        StorageItem item = new StorageItem();
        StorageItem item2 = new StorageItem();
        item.setStorage(storage);
        item.setFood(food);
        item.setCantidad(BigDecimal.valueOf(20));
        item2.setStorage(storage2);
        item2.setFood(food2);
        item2.setCantidad(BigDecimal.valueOf(10));
        storageItemRepository.save(item);
        storageItemRepository.save(item2);

        mockMvc.perform(get("/api/v1/storageitems"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].storage.capacidadLitros", comparesEqualTo(120)))
                .andExpect(jsonPath("$.content[0].food.name").value("Tomate"))
                .andExpect(jsonPath("$.content[0].cantidad", comparesEqualTo(20)))
                .andExpect(jsonPath("$.content[1].storage.capacidadLitros", comparesEqualTo(200)))
                .andExpect(jsonPath("$.content[1].food.name").value("Pescado"))
                .andExpect(jsonPath("$.content[1].cantidad", comparesEqualTo(10)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

        @Test
    public void testIntegrationStorageItemById() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        Set<UnitOfMeasure> unitOfMeasures = new LinkedHashSet<>();
        unitOfMeasures.add(UnitOfMeasure.KG);
        unitOfMeasures.add(UnitOfMeasure.G);
        Food food = new Food();
        food.setName("Tomate");
        food.setUnitOfMeasures(unitOfMeasures);
        foodRepository.save(food);

        StorageItem item = new StorageItem();
        item.setStorage(storage);
        item.setFood(food);
        item.setCantidad(BigDecimal.valueOf(20));
        StorageItem storageItemGuardado = storageItemRepository.save(item);
        
        mockMvc.perform(get("/api/v1/storageitems/{id}",storageItemGuardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(storageItemGuardado.getId()))
                .andExpect(jsonPath("$.storage.capacidadLitros",comparesEqualTo(120)))
                .andExpect(jsonPath("$.food.name").value("Tomate"))
                .andExpect(jsonPath("$.cantidad", comparesEqualTo(20)));
    }

        @Test
    public void testIntegrationWhenStoragesItemsDoesntExists() throws Exception{
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        Set<UnitOfMeasure> unitOfMeasures = new LinkedHashSet<>();
        unitOfMeasures.add(UnitOfMeasure.KG);
        unitOfMeasures.add(UnitOfMeasure.G);
        Food food = new Food();
        food.setName("Tomate");
        food.setUnitOfMeasures(unitOfMeasures);
        foodRepository.save(food);
        
        StorageItem item = new StorageItem();
        item.setStorage(storage);
        item.setFood(food);
        item.setCantidad(BigDecimal.valueOf(20));

        StorageItem storageItemGuardado = storageItemRepository.save(item);
        Long idInexistente = storageItemGuardado.getId()+1;
        while (storageItemRepository.existsById(idInexistente)) {
            idInexistente += 1;
        }
        String messageExpected = "No existe un elemento en el inventario con el ID: " + idInexistente;

        mockMvc.perform(get("/api/v1/storageitems/{id}",idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(messageExpected))
                .andExpect(jsonPath("$.status").value(404));
    }


        @Test
    public void testIntegrationStorageItemsWithPagination() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        Set<UnitOfMeasure> units = new LinkedHashSet<>();
        units.add(UnitOfMeasure.KG);
        units.add(UnitOfMeasure.G);

        Food food = new Food();
        food.setName("Tomate");
        food.setUnitOfMeasures(units);
        foodRepository.save(food);

        for (int i = 1; i <= 3; i++) {
            StorageItem item = new StorageItem();

            Storage storageItem = new Storage();
            storageItem.setCapacidadLitros(BigDecimal.valueOf(100 + i));
            storageRepository.save(storageItem);

            Food foodItem = new Food();
            foodItem.setName("Alimento " + i);
            foodItem.setUnitOfMeasures(units);
            foodRepository.save(foodItem);

            item.setStorage(storageItem);
            item.setFood(foodItem);
            item.setCantidad(BigDecimal.valueOf(i));

            storageItemRepository.save(item);
        }

        mockMvc.perform(get("/api/v1/storageitems")
                .param("page", "0")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].cantidad", comparesEqualTo(1)))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

        @Test
    public void testIntegrationStorageItemsWithSecondPage() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        Set<UnitOfMeasure> units = new LinkedHashSet<>();
        units.add(UnitOfMeasure.KG);
        units.add(UnitOfMeasure.G);

        Food food = new Food();
        food.setName("Tomate");
        food.setUnitOfMeasures(units);
        foodRepository.save(food);

        for (int i = 1; i <= 3; i++) {
            StorageItem item = new StorageItem();

            Storage storageItem = new Storage();
            storageItem.setCapacidadLitros(BigDecimal.valueOf(100 + i));
            storageRepository.save(storageItem);

            Food foodItem = new Food();
            foodItem.setName("Alimento " + i);
            foodItem.setUnitOfMeasures(units);
            foodRepository.save(foodItem);

            item.setStorage(storageItem);
            item.setFood(foodItem);
            item.setCantidad(BigDecimal.valueOf(i));

            storageItemRepository.save(item);
        }

        mockMvc.perform(get("/api/v1/storageitems")
                .param("page", "1")
                .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].cantidad", comparesEqualTo(2)))
                .andExpect(jsonPath("$.totalElements").value(3));
    }

        @Test
    public void testIntegrationStorageItemsWithDescendingSort() throws Exception {
        Storage storage = new Storage();
        storage.setCapacidadLitros(BigDecimal.valueOf(120));
        storageRepository.save(storage);

        Set<UnitOfMeasure> units = new LinkedHashSet<>();
        units.add(UnitOfMeasure.KG);
        units.add(UnitOfMeasure.G);

        Food food = new Food();
        food.setName("Tomate");
        food.setUnitOfMeasures(units);
        foodRepository.save(food);

        for (int i = 1; i <= 3; i++) {
            StorageItem item = new StorageItem();

            Storage storageItem = new Storage();
            storageItem.setCapacidadLitros(BigDecimal.valueOf(100 + i));
            storageRepository.save(storageItem);

            Food foodItem = new Food();
            foodItem.setName("Alimento " + i);
            foodItem.setUnitOfMeasures(units);
            foodRepository.save(foodItem);

            item.setStorage(storageItem);
            item.setFood(foodItem);
            item.setCantidad(BigDecimal.valueOf(i));

            storageItemRepository.save(item);
        }

        mockMvc.perform(get("/api/v1/storageitems")
                .param("sort", "id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(3)))
                .andExpect(jsonPath("$.content[0].cantidad", comparesEqualTo(3)))
                .andExpect(jsonPath("$.content[1].cantidad", comparesEqualTo(2)))
                .andExpect(jsonPath("$.content[2].cantidad", comparesEqualTo(1)));
    }

}

package com.freezermanager.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.repository.FoodRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class FoodControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FoodRepository foodRepository;

    @Test
    public void testIntegrationWhenFoodsItsEmpty() throws Exception{
        mockMvc.perform(get("/api/v1/foods"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void testIntegrationWhenFoodsHaveAliments() throws Exception{
        Food foodItem = new Food();
        Food foodItem2 = new Food();
        Set<UnitOfMeasure> setUnitOfMeasures = new LinkedHashSet<>();
        setUnitOfMeasures.add(UnitOfMeasure.KG);
        setUnitOfMeasures.add(UnitOfMeasure.G);

        foodItem.setName("Tomate");
        foodItem.setCategory(FoodCategory.VEGETABLE);
        foodItem.setUnitOfMeasures(setUnitOfMeasures);
        foodItem2.setName("Matambre");
        foodItem2.setCategory(FoodCategory.MEAT);
        foodItem2.setUnitOfMeasures(setUnitOfMeasures);
        foodRepository.save(foodItem);
        foodRepository.save(foodItem2);

        mockMvc.perform(get("/api/v1/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Tomate"))
                .andExpect(jsonPath("$[0].category").value("VEGETABLE"))
                .andExpect(jsonPath("$[0].unitOfMeasures",containsInAnyOrder("KG","G")))
                .andExpect(jsonPath("$[1].name").value("Matambre"))
                .andExpect(jsonPath("$[1].category").value("MEAT"))
                .andExpect(jsonPath("$[1].unitOfMeasures",containsInAnyOrder("KG","G")));
    }

    @Test
    public void testIntegrationFoodsById() throws Exception{
        Food foodItem = new Food();
        Set<UnitOfMeasure> setUnitOfMeasures = new LinkedHashSet<>();
        setUnitOfMeasures.add(UnitOfMeasure.KG);
        setUnitOfMeasures.add(UnitOfMeasure.G);

        foodItem.setName("Tomate");
        foodItem.setCategory(FoodCategory.VEGETABLE);
        foodItem.setUnitOfMeasures(setUnitOfMeasures);

        Food itemGuardado = foodRepository.save(foodItem);

        mockMvc.perform(get("/api/v1/foods/{id}",itemGuardado.getFoodId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.foodId").value(foodItem.getFoodId()))
                .andExpect(jsonPath("$.name").value("Tomate"))
                .andExpect(jsonPath("$.category").value("VEGETABLE"))
                .andExpect(jsonPath("$.unitOfMeasures",containsInAnyOrder("KG","G")));
    }

    @Test
    public void testIntegrationFoodWhenIdDoesntExist() throws Exception{
        Food foodItem = new Food();
        Set<UnitOfMeasure> setUnitOfMeasures = new LinkedHashSet<>();
        setUnitOfMeasures.add(UnitOfMeasure.KG);
        setUnitOfMeasures.add(UnitOfMeasure.G);

        foodItem.setName("Tomate");
        foodItem.setCategory(FoodCategory.VEGETABLE);
        foodItem.setUnitOfMeasures(setUnitOfMeasures);

        Food itemGuardado = foodRepository.save(foodItem);
        Long idInexistente = itemGuardado.getFoodId()+1;
        while(foodRepository.existsById(idInexistente)){
            idInexistente += 1;
        }

        String messageExpected = "No existe un alimento con el ID: " + idInexistente;

        mockMvc.perform(get("/api/v1/foods/{id}",idInexistente))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(messageExpected))
                .andExpect(jsonPath("$.status").value(404));
    }
}

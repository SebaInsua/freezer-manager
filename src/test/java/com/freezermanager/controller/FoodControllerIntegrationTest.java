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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.springframework.test.web.servlet.MvcResult;
import com.jayway.jsonpath.JsonPath;

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

                @Test
        public void testIntegrationCreateFood() throws Exception {
                String requestBody = """
                        {
                        "name": "Pollo",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(requestBody))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.foodId").isNumber())
                        .andExpect(jsonPath("$.name").value("Pollo"))
                        .andExpect(jsonPath("$.active").value(true))
                        .andExpect(jsonPath("$.unitOfMeasures[0]").value("KG"));
        }

        @Test
        public void testIntegrationCreateFoodWithDuplicateNameIgnoringCase() throws Exception {
                String firstRequest = """
                        {
                        "name": "Pollo",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                String duplicateRequest = """
                        {
                        "name": "POLLO",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(firstRequest))
                        .andExpect(status().isCreated());

                mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(duplicateRequest))
                        .andExpect(status().isConflict())
                        .andExpect(jsonPath("$.status").value(409));
        }

        @Test
        public void testIntegrationCreateFoodWithBlankName() throws Exception {
                String requestBody = """
                        {
                        "name": "   ",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(requestBody))
                        .andExpect(status().isBadRequest());
        }

        @Test
        public void testIntegrationCreateFoodWithoutUnits() throws Exception {
                String requestBody = """
                        {
                        "name": "Pollo",
                        "category": "MEAT",
                        "unitOfMeasures": []
                        }
                        """;

                mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(requestBody))
                        .andExpect(status().isBadRequest());
        }

                @Test
        public void testIntegrationCreateFoodWithoutCategory() throws Exception {
                String requestBody = """
                        {
                        "name": "Pollo",
                        "category": null,
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(requestBody))
                        .andExpect(status().isBadRequest());
        }

    // ACTUALIZACION EXITOSA

        @Test
        public void testIntegrationUpdateFood() throws Exception {
                String createBody = """
                        {
                        "name": "Pollo",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                MvcResult result = mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(createBody))
                        .andExpect(status().isCreated())
                        .andReturn();

                String response = result.getResponse().getContentAsString();
                Long foodId = ((Number) JsonPath.read(response, "$.foodId")).longValue();

                String updateBody = """
                        {
                        "name": "Pollo orgánico",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG", "G"]
                        }
                        """;

                mockMvc.perform(put("/api/v1/foods/" + foodId)
                                .contentType("application/json")
                                .content(updateBody))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.name").value("Pollo orgánico"))
                        .andExpect(jsonPath("$.category").value("MEAT"))
                        .andExpect(jsonPath("$.active").value(true))
                        .andExpect(jsonPath("$.unitOfMeasures", containsInAnyOrder("KG", "G")));
        }

    // ID INEXISTENTE AL ACTUALIZAR

        @Test
        public void testIntegrationUpdateFoodNotFound() throws Exception {
                String requestBody = """
                        {
                        "name": "Pollo",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                mockMvc.perform(put("/api/v1/foods/9999")
                                .contentType("application/json")
                                .content(requestBody))
                        .andExpect(status().isNotFound());
        }

    // NOMBRE DUPLICADO IGNORANDO MAYUSCULAS AL ACTUALIZAR

        @Test
        public void testIntegrationUpdateFoodWithDuplicateName() throws Exception {
                String firstFood = """
                        {
                        "name": "Pollo",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                String secondFood = """
                        {
                        "name": "Pescado",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(firstFood))
                        .andExpect(status().isCreated());

                MvcResult result = mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(secondFood))
                        .andExpect(status().isCreated())
                        .andReturn();

                Long foodId = ((Number) JsonPath.read(
                result.getResponse().getContentAsString(), "$.foodId")).longValue();

                String updateBody = """
                        {
                        "name": "POLLO",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                mockMvc.perform(put("/api/v1/foods/" + foodId)
                                .contentType("application/json")
                                .content(updateBody))
                        .andExpect(status().isConflict());
        }

    // NOMBRE VACIO AL ACTUALIZAR

        @Test
        public void testIntegrationUpdateFoodWithBlankName() throws Exception {
                String createBody = """
                        {
                        "name": "Pollo",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                MvcResult result = mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(createBody))
                        .andExpect(status().isCreated())
                        .andReturn();

                Long foodId = ((Number) JsonPath.read(
                        result.getResponse().getContentAsString(), "$.foodId")).longValue();

                String updateBody = """
                        {
                        "name": " ",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                mockMvc.perform(put("/api/v1/foods/" + foodId)
                                .contentType("application/json")
                                .content(updateBody))
                        .andExpect(status().isBadRequest());
        }

    // UNIDADES DE MEDIDA VACIAS AL ACTUALIZAR

        @Test
        public void testIntegrationUpdateFoodWithoutUnits() throws Exception {
                String createBody = """
                        {
                        "name": "Pollo",
                        "category": "MEAT",
                        "unitOfMeasures": ["KG"]
                        }
                        """;

                MvcResult result = mockMvc.perform(post("/api/v1/foods")
                                .contentType("application/json")
                                .content(createBody))
                        .andExpect(status().isCreated())
                        .andReturn();

                Long foodId = ((Number) JsonPath.read(
                result.getResponse().getContentAsString(), "$.foodId")).longValue();

                String updateBody = """
                        {
                        "name": "Pollo",
                        "category": "MEAT",
                        "unitOfMeasures": []
                        }
                        """;

                mockMvc.perform(put("/api/v1/foods/" + foodId)
                                .contentType("application/json")
                                .content(updateBody))
                        .andExpect(status().isBadRequest());
        }

        @Test
        public void testIntegrationGetFoodsReturnsOnlyActiveFoods() throws Exception {
        foodRepository.save(createFood("Pollo", true));
        foodRepository.save(createFood("Pescado", false));

        mockMvc.perform(get("/api/v1/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Pollo"))
                .andExpect(jsonPath("$[0].active").value(true));
        }

                @Test
        public void testIntegrationGetFoodsWithActiveTrue() throws Exception {
        foodRepository.save(createFood("Pollo", true));
        foodRepository.save(createFood("Pescado", false));
        foodRepository.save(createFood("Carne", true));

        mockMvc.perform(get("/api/v1/foods")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].active",
                        everyItem(equalTo(true))));
        }

                @Test
        public void testIntegrationGetFoodsWithActiveFalse() throws Exception {
        foodRepository.save(createFood("Pollo", true));
        foodRepository.save(createFood("Pescado", false));
        foodRepository.save(createFood("Carne", false));

        mockMvc.perform(get("/api/v1/foods")
                        .param("active", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].active",
                        everyItem(equalTo(false))));
        }

        private Food createFood(String name, boolean active) {
        Food food = new Food();
        food.setName(name);
        food.setCategory(FoodCategory.MEAT);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.KG));
        food.setActive(active);
        return food;
        }


        @Test
        public void testIntegrationUpdateFoodWithIncompatibleUnits() throws Exception {
        String createBody = """
                {
                "name": "Harina",
                "category": "MEAT",
                "unitOfMeasures": ["KG"]
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/v1/foods")
                        .contentType("application/json")
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        Long foodId = ((Number) JsonPath.read(
                result.getResponse().getContentAsString(), "$.foodId")).longValue();

        String updateBody = """
                {
                "name": "Harina",
                "category": "MEAT",
                "unitOfMeasures": ["KG", "UNIT"]
                }
                """;

        mockMvc.perform(put("/api/v1/foods/" + foodId)
                        .contentType("application/json")
                        .content(updateBody))
                .andExpect(status().isBadRequest());

        Food foodAfterUpdate = foodRepository.findById(foodId).orElseThrow();
        assertEquals(Set.of(UnitOfMeasure.KG), foodAfterUpdate.getUnitOfMeasures());
        }

}

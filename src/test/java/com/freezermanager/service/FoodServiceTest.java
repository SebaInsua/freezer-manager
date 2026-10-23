package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.dto.FoodRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.FoodRepository;

@ExtendWith(MockitoExtension.class)
public class FoodServiceTest {
    
    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodService foodService;

    @Test
    public void shouldFindAllFoods() {
        List<Food> expectedFoods = new ArrayList<>();
        Food item = new Food();
        item.setFoodId(1L);
        item.setName("Pizza");
        item.setActive(true);
        expectedFoods.add(item);

        when(foodRepository.findAllByActive(true)).thenReturn(expectedFoods);

        List<Food> result = foodService.getFoods(null);

        assertEquals(expectedFoods, result);

        verify(foodRepository).findAllByActive(true);
    }

    @Test
    public void shouldFindFoodById(){
        Food expectedFood = new Food();
        expectedFood.setFoodId(1L);
        expectedFood.setName("Pizza");
        when(foodRepository.findById(expectedFood.getFoodId())).thenReturn(Optional.of(expectedFood));
        Food result = foodService.getFoodById(expectedFood.getFoodId());
        assertEquals(expectedFood, result);
        verify(foodRepository).findById(expectedFood.getFoodId());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenFoodDoesNotExist(){
        when(foodRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> foodService.getFoodById(99L));
        verify(foodRepository).findById(99L);
    }


    @Test
    public void shouldCreateFoodWithCompatibleWeightUnits() {
        FoodRequest request = new FoodRequest(
            "Harina",
            FoodCategory.values()[0],
            Set.of(UnitOfMeasure.KG, UnitOfMeasure.G)
        );

        when(foodRepository.existsByNameIgnoreCase("Harina")).thenReturn(false);
        when(foodRepository.save(any(Food.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Food result = foodService.createFood(request);

        assertEquals(Set.of(UnitOfMeasure.KG, UnitOfMeasure.G), result.getUnitOfMeasures());
        verify(foodRepository).save(any(Food.class));
    }

    @Test
    public void shouldRejectFoodWithWeightAndUnitMeasures() {
        FoodRequest request = new FoodRequest(
            "Harina",
            FoodCategory.values()[0],
            Set.of(UnitOfMeasure.KG, UnitOfMeasure.UNIT)
        );

        when(foodRepository.existsByNameIgnoreCase("Harina")).thenReturn(false);

        assertThrows(
            IllegalArgumentException.class,
            () -> foodService.createFood(request)
        );

        verify(foodRepository, never()).save(any(Food.class));
    }

}

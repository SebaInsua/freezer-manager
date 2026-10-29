
package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.user.User;
import com.freezermanager.dto.FoodRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class FoodServiceTest {

    private static final Long USER_ID = 10L;
    private static final String USER_EMAIL = "test@example.com";

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FoodService foodService;

    private User authenticatedUser;

    @BeforeEach
    void setUp() {
        authenticatedUser = new User();
        authenticatedUser.setId(USER_ID);
        authenticatedUser.setEmail(USER_EMAIL);
        authenticatedUser.setPassword("encoded-password");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        USER_EMAIL,
                        "credentials",
                        List.of()
                )
        );

        when(userRepository.findByEmailIgnoreCase(USER_EMAIL))
                .thenReturn(Optional.of(authenticatedUser));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldFindAllFoods() {
        List<Food> expectedFoods = new ArrayList<>();

        Food item = new Food();
        item.setFoodId(1L);
        item.setName("Pizza");
        item.setActive(true);
        expectedFoods.add(item);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Food> expectedPage = new PageImpl<>(expectedFoods, pageable, 1);

        when(foodRepository.findVisibleFoods(true, USER_ID, pageable))
                .thenReturn(expectedPage);

        Page<Food> result = foodService.getFoods(null, pageable);

        assertEquals(expectedPage, result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Pizza", result.getContent().get(0).getName());

        verify(foodRepository).findVisibleFoods(true, USER_ID, pageable);
    }

    @Test
    void shouldFindFoodById() {
        Food expectedFood = new Food();
        expectedFood.setFoodId(1L);
        expectedFood.setName("Pizza");

        when(foodRepository.findVisibleById(1L, USER_ID))
                .thenReturn(Optional.of(expectedFood));

        Food result = foodService.getFoodById(1L);

        assertEquals(expectedFood, result);
        verify(foodRepository).findVisibleById(1L, USER_ID);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFoodDoesNotExist() {
        when(foodRepository.findVisibleById(99L, USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> foodService.getFoodById(99L)
        );

        verify(foodRepository).findVisibleById(99L, USER_ID);
    }

    @Test
    void shouldCreateFoodWithCompatibleWeightUnits() {
        FoodRequest request = new FoodRequest(
                "Harina",
                FoodCategory.values()[0],
                Set.of(UnitOfMeasure.KG, UnitOfMeasure.G),
                null,
                null
        );

        when(foodRepository.existsByNameIgnoreCaseAndOwner_Id(
                "Harina", USER_ID))
                .thenReturn(false);

        when(foodRepository.save(any(Food.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Food result = foodService.createFood(request);

        assertEquals(
                Set.of(UnitOfMeasure.KG, UnitOfMeasure.G),
                result.getUnitOfMeasures()
        );
        assertEquals(authenticatedUser, result.getOwner());
        assertTrue(result.isActive());

        verify(foodRepository).save(any(Food.class));
    }

    @Test
    void shouldRejectFoodWithWeightAndUnitMeasures() {
        FoodRequest request = new FoodRequest(
                "Harina",
                FoodCategory.values()[0],
                Set.of(UnitOfMeasure.KG, UnitOfMeasure.UNIT),
                null,
                null
        );

        when(foodRepository.existsByNameIgnoreCaseAndOwner_Id(
                "Harina", USER_ID))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> foodService.createFood(request)
        );

        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void shouldCreateFoodWithEstimatedWeightPerUnit() {
        BigDecimal estimatedWeight = new BigDecimal("0.150");

        FoodRequest request = new FoodRequest(
                "Hamburguesa",
                FoodCategory.values()[0],
                Set.of(UnitOfMeasure.UNIT),
                null,
                estimatedWeight
        );

        when(foodRepository.existsByNameIgnoreCaseAndOwner_Id(
                "Hamburguesa", USER_ID))
                .thenReturn(false);

        when(foodRepository.save(any(Food.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Food result = foodService.createFood(request);

        assertEquals(estimatedWeight, result.getEstimatedWeightPerUnitKg());
        assertEquals(authenticatedUser, result.getOwner());

        verify(foodRepository).save(any(Food.class));
    }

    @Test
    void shouldUpdateFoodWithEstimatedWeightPerUnit() {
        Food food = new Food();
        food.setFoodId(1L);
        food.setName("Hamburguesa");
        food.setCategory(FoodCategory.values()[0]);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));
        food.setOwner(authenticatedUser);

        when(foodRepository.findVisibleById(1L, USER_ID))
                .thenReturn(Optional.of(food));

        when(foodRepository
                .existsByNameIgnoreCaseAndOwner_IdAndFoodIdNot(
                        "Hamburguesa", USER_ID, 1L))
                .thenReturn(false);

        BigDecimal estimatedWeight = new BigDecimal("0.150");

        FoodRequest request = new FoodRequest(
                "Hamburguesa",
                FoodCategory.values()[0],
                Set.of(UnitOfMeasure.UNIT),
                null,
                estimatedWeight
        );

        Food result = foodService.updateFoods(request, 1L);

        assertEquals(estimatedWeight, result.getEstimatedWeightPerUnitKg());
        assertEquals(authenticatedUser, result.getOwner());

        verify(foodRepository).findVisibleById(1L, USER_ID);
    }
}

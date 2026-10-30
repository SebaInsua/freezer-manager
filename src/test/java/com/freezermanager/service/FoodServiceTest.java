
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
import com.freezermanager.dto.response.FoodResponse;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.mapper.FoodMapper;
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

    @Mock
    private FoodMapper foodMapper;

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
                        List.of()));

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

        FoodResponse expectedResponse = mock(FoodResponse.class);

        when(foodRepository.findVisibleFoods(true, USER_ID, pageable))
                .thenReturn(expectedPage);

        when(foodMapper.toResponse(item))
                .thenReturn(expectedResponse);

        Page<FoodResponse> result = foodService.getFoods(null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(expectedResponse, result.getContent().get(0));

        verify(foodRepository).findVisibleFoods(true, USER_ID, pageable);
        verify(foodMapper).toResponse(item);
    }

    @Test
    void shouldFindFoodById() {
        Food expectedFood = new Food();
        expectedFood.setFoodId(1L);
        expectedFood.setName("Pizza");

        FoodResponse expectedResponse = mock(FoodResponse.class);

        when(foodRepository.findVisibleById(1L, USER_ID))
                .thenReturn(Optional.of(expectedFood));

        when(foodMapper.toResponse(expectedFood))
                .thenReturn(expectedResponse);

        FoodResponse result = foodService.getFoodById(1L);

        assertEquals(expectedResponse, result);

        verify(foodRepository).findVisibleById(1L, USER_ID);
        verify(foodMapper).toResponse(expectedFood);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenFoodDoesNotExist() {
        when(foodRepository.findVisibleById(99L, USER_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> foodService.getFoodById(99L));

        verify(foodRepository).findVisibleById(99L, USER_ID);
        verifyNoInteractions(foodMapper);
    }

    @Test
    void shouldCreateFoodWithCompatibleWeightUnits() {
        FoodRequest request = new FoodRequest(
                "Harina",
                FoodCategory.values()[0],
                Set.of(UnitOfMeasure.KG, UnitOfMeasure.G),
                null,
                null);

        when(foodRepository.existsByNameIgnoreCaseAndOwner_Id(
                "Harina", USER_ID))
                .thenReturn(false);

        when(foodRepository.save(any(Food.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FoodResponse expectedResponse = mock(FoodResponse.class);

        when(foodMapper.toResponse(any(Food.class)))
                .thenReturn(expectedResponse);

        FoodResponse result = foodService.createFood(request);

        assertEquals(expectedResponse, result);

        verify(foodRepository).save(argThat(food -> food.getUnitOfMeasures().equals(
                Set.of(UnitOfMeasure.KG, UnitOfMeasure.G))
                && food.getOwner().equals(authenticatedUser)
                && food.isActive()));

        verify(foodMapper).toResponse(any(Food.class));
    }

    @Test
    void shouldRejectFoodWithWeightAndUnitMeasures() {
        FoodRequest request = new FoodRequest(
                "Harina",
                FoodCategory.values()[0],
                Set.of(UnitOfMeasure.KG, UnitOfMeasure.UNIT),
                null,
                null);

        when(foodRepository.existsByNameIgnoreCaseAndOwner_Id(
                "Harina", USER_ID))
                .thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> foodService.createFood(request));

        verify(foodRepository, never()).save(any(Food.class));
        verifyNoInteractions(foodMapper);
    }

    @Test
    void shouldCreateFoodWithEstimatedWeightPerUnit() {
        BigDecimal estimatedWeight = new BigDecimal("0.150");

        FoodRequest request = new FoodRequest(
                "Hamburguesa",
                FoodCategory.values()[0],
                Set.of(UnitOfMeasure.UNIT),
                null,
                estimatedWeight);

        when(foodRepository.existsByNameIgnoreCaseAndOwner_Id(
                "Hamburguesa", USER_ID))
                .thenReturn(false);

        when(foodRepository.save(any(Food.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        FoodResponse expectedResponse = mock(FoodResponse.class);

        when(foodMapper.toResponse(any(Food.class)))
                .thenReturn(expectedResponse);

        FoodResponse result = foodService.createFood(request);

        assertEquals(expectedResponse, result);

        verify(foodRepository).save(argThat(food -> estimatedWeight.equals(food.getEstimatedWeightPerUnitKg())
                && food.getOwner().equals(authenticatedUser)));

        verify(foodMapper).toResponse(any(Food.class));
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
                estimatedWeight);

        Food result = foodService.updateFoods(request, 1L);

        assertEquals(estimatedWeight, result.getEstimatedWeightPerUnitKg());
        assertEquals(authenticatedUser, result.getOwner());

        verify(foodRepository).findVisibleById(1L, USER_ID);
    }
}

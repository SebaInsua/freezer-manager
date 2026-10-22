package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.dto.StorageExitDetailRequest;
import com.freezermanager.dto.StorageExitRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageExitRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.StorageExitDetailRepository;

@ExtendWith(MockitoExtension.class)
public class StorageExitServiceTest {
    @Mock
    private StorageExitRepository storageExitRepository;

    @Mock
    private StorageExitDetailRepository storageExitDetailRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private FoodService foodService;

    @Mock
    private StorageItemRepository storageItemRepository;

    @InjectMocks
    private StorageExitService storageExitService;

    @Test
    public void shouldFindAllExtractions(){
        List<StorageExit> expectedExtraction = new ArrayList<>();
        StorageExit extraction = new StorageExit();
        extraction.setStorageExitId(1L);
        expectedExtraction.add(extraction);
        when(storageExitRepository.findAll()).thenReturn(expectedExtraction);
        List<StorageExit> result = storageExitService.getAllStorageExits();
        assertEquals(expectedExtraction, result);
        verify(storageExitRepository).findAll();
    }

    @Test
    public void shouldFindExtractionById(){
        StorageExit expectedExtraction = new StorageExit();
        expectedExtraction.setStorageExitId(1L);
        when(storageExitRepository.findById(1L)).thenReturn(Optional.of(expectedExtraction));
        StorageExit result = storageExitService.getStorageExitById(expectedExtraction.getStorageExitId());
        assertEquals(expectedExtraction, result);
        verify(storageExitRepository).findById(expectedExtraction.getStorageExitId());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenExtractionDoesNotExist(){
        when(storageExitRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> storageExitService.getStorageExitById(99L));
        verify(storageExitRepository).findById(99L);
    }

        @Test
    public void shouldCreateStorageExitAndSubtractStock() {
        Storage storage = new Storage();

        Food food = new Food();
        food.setFoodId(1L);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));

        StorageItem storageItem = new StorageItem();
        storageItem.setStorage(storage);
        storageItem.setFood(food);
        storageItem.setCantidad(new BigDecimal("5"));

        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(foodService.getActiveFoodById(1L)).thenReturn(food);
        when(storageItemRepository.findByStorageIdAndFoodId(1L, 1L))
            .thenReturn(Optional.of(storageItem));

        when(storageExitRepository.save(any(StorageExit.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        StorageExitDetailRequest detail = new StorageExitDetailRequest(
            1L, UnitOfMeasure.UNIT, new BigDecimal("2")
        );

        StorageExitRequest request = new StorageExitRequest(
            LocalDateTime.now(), 1L, List.of(detail)
        );

        StorageExit result = storageExitService.createStorageExit(request);

        assertNotNull(result);
        assertEquals(0, new BigDecimal("3").compareTo(storageItem.getCantidad()));
        assertEquals(1, result.getDetails().size());

        verify(storageItemRepository).save(storageItem);
        verify(storageExitRepository).save(any(StorageExit.class));
    }

        @Test
    public void shouldThrowExceptionWhenStockIsInsufficient() {
        Storage storage = new Storage();

        Food food = new Food();
        food.setFoodId(1L);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));

        StorageItem storageItem = new StorageItem();
        storageItem.setStorage(storage);
        storageItem.setFood(food);
        storageItem.setCantidad(new BigDecimal("5"));

        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(foodService.getActiveFoodById(1L)).thenReturn(food);
        when(storageItemRepository.findByStorageIdAndFoodId(1L, 1L))
            .thenReturn(Optional.of(storageItem));

        StorageExitDetailRequest detail = new StorageExitDetailRequest(
            1L, UnitOfMeasure.UNIT, new BigDecimal("6")
        );

        StorageExitRequest request = new StorageExitRequest(
            LocalDateTime.now(), 1L, List.of(detail)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> storageExitService.createStorageExit(request)
        );

        assertEquals(0, new BigDecimal("5").compareTo(storageItem.getCantidad()));
        verify(storageExitRepository, never()).save(any(StorageExit.class));
    }

        @Test
    public void shouldThrowExceptionWhenStorageItemDoesNotExist() {
        Storage storage = new Storage();

        Food food = new Food();
        food.setFoodId(1L);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));

        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(foodService.getActiveFoodById(1L)).thenReturn(food);
        when(storageItemRepository.findByStorageIdAndFoodId(1L, 1L))
            .thenReturn(Optional.empty());

        StorageExitDetailRequest detail = new StorageExitDetailRequest(
            1L, UnitOfMeasure.UNIT, new BigDecimal("2")
        );

        StorageExitRequest request = new StorageExitRequest(
            LocalDateTime.now(), 1L, List.of(detail)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> storageExitService.createStorageExit(request)
        );

        verify(storageItemRepository, never()).save(any(StorageItem.class));
        verify(storageExitRepository, never()).save(any(StorageExit.class));
    }

        @Test
    public void shouldThrowExceptionWhenUnitIsNotAllowed() {
        Storage storage = new Storage();

        Food food = new Food();
        food.setFoodId(1L);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));

        when(storageRepository.findById(1L)).thenReturn(Optional.of(storage));
        when(foodService.getActiveFoodById(1L)).thenReturn(food);

        StorageExitDetailRequest detail = new StorageExitDetailRequest(
            1L, UnitOfMeasure.KG, new BigDecimal("2")
        );

        StorageExitRequest request = new StorageExitRequest(
            LocalDateTime.now(), 1L, List.of(detail)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> storageExitService.createStorageExit(request)
        );

        verify(storageItemRepository, never()).save(any(StorageItem.class));
        verify(storageExitRepository, never()).save(any(StorageExit.class));
    }

        @Test
    public void shouldThrowExceptionWhenFoodIsInactive() {
        Storage storage = new Storage();

        when(storageRepository.findById(1L))
            .thenReturn(Optional.of(storage));

        when(foodService.getActiveFoodById(1L))
            .thenThrow(new IllegalArgumentException(
                "No se puede utilizar un alimento inactivo"
            ));

        StorageExitDetailRequest detail = new StorageExitDetailRequest(
            1L, UnitOfMeasure.UNIT, new BigDecimal("2")
        );

        StorageExitRequest request = new StorageExitRequest(
            LocalDateTime.now(), 1L, List.of(detail)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> storageExitService.createStorageExit(request)
        );

        verify(storageItemRepository, never())
            .save(any(StorageItem.class));

        verify(storageExitRepository, never())
            .save(any(StorageExit.class));
    }
}

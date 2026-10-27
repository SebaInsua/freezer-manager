package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.dto.StorageEntryRequest;
import com.freezermanager.dto.StorageEntryDetailRequest;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;




@ExtendWith(MockitoExtension.class)
public class StorageEntryServiceTest {
    @Mock
    private StorageEntryRepository storageEntryRepository;

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private StorageItemRepository storageItemRepository;

    @Mock
    private StorageBatchRepository storageBatchRepository;

    @Mock
    private FoodService foodService;

    @Mock
    private StorageEntryDetailService storageEntryDetailService;

    @Mock
    private UnitConversionService unitConversionService;

    @InjectMocks
    private StorageEntryService storageEntryService;

    @Test
    public void shouldFindAllEntries() {
        StorageEntry register = new StorageEntry();
        register.setStorageEntryId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StorageEntry> expectedRegister = new PageImpl<>(List.of(register));

        when(storageEntryRepository.findAll(pageable)).thenReturn(expectedRegister);

        Page<StorageEntry> result = storageEntryService.getAllStorageEntries(pageable);

        assertEquals(expectedRegister, result);
        verify(storageEntryRepository).findAll(pageable);
    }

    @Test
    public void shouldFindEntryById(){
        StorageEntry expectedRegister = new StorageEntry();
        expectedRegister.setStorageEntryId(1L);
        when(storageEntryRepository.findById(1L)).thenReturn(Optional.of(expectedRegister));
        StorageEntry result = storageEntryService.getStorageEntryById(expectedRegister.getStorageEntryId());
        assertEquals(expectedRegister, result);
        verify(storageEntryRepository).findById(expectedRegister.getStorageEntryId());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenEntryDoesNotExist(){
        when(storageEntryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> storageEntryService.getStorageEntryById(99L));
        verify(storageEntryRepository).findById(99L);
    }

        @Test
    public void shouldCreateBatchWhenStorageEntryIsCreated() {
        // Arrange: preparar los datos
        Storage storage = new Storage();
        storage.setId(1L);

        Food food = new Food();
        food.setFoodId(5L);

        BigDecimal quantity = new BigDecimal("2.00");
        BigDecimal normalizedQuantity = new BigDecimal("2000.00");

        StorageEntryDetailRequest detailRequest =
            new StorageEntryDetailRequest(
                5L,
                UnitOfMeasure.KG,
                quantity
            );

        StorageEntryRequest request = new StorageEntryRequest(
            LocalDateTime.now(),
            1L,
            List.of(detailRequest)
        );

        StorageEntryDetail detail = new StorageEntryDetail();

        when(storageRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(storage));

        when(foodService.getActiveFoodById(5L))
            .thenReturn(food);

        when(unitConversionService.toInventoryUnit(
            quantity,
            UnitOfMeasure.KG
        )).thenReturn(normalizedQuantity);

        when(storageItemRepository.findByStorageIdAndFoodId(1L, 5L))
            .thenReturn(Optional.empty());

        when(storageEntryDetailService.buildEntryDetail(
            eq(food),
            eq(detailRequest),
            any(StorageEntry.class)
        )).thenAnswer(invocation -> {
            StorageEntry entry = invocation.getArgument(2);
            detail.setStorageEntry(entry);
            detail.setFood(food);
            detail.setCantidad(quantity);
            detail.setSelectedUnit(UnitOfMeasure.KG);
            return detail;
        });

        when(storageEntryRepository.save(any(StorageEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act: crear la entrada
        StorageEntry result = storageEntryService.createStorageEntry(request);

        // Assert: comprobar la entrada creada
        assertNotNull(result);
        assertEquals(1, result.getDetails().size());

        // Assert: comprobar que se guarda un lote con cantidades normalizadas
        verify(storageBatchRepository).saveAll(argThat(batches -> {
            List<StorageBatch> savedBatches = new ArrayList<>();
            batches.forEach(savedBatches::add);

            assertEquals(1, savedBatches.size());

            StorageBatch batch = savedBatches.get(0);

            assertSame(detail, batch.getStorageEntryDetail());
            assertEquals(
                0,
                normalizedQuantity.compareTo(batch.getInitialQuantity())
            );
            assertEquals(
                0,
                normalizedQuantity.compareTo(batch.getAvailableQuantity())
            );

            return true;
        }));
    }

        @Test
    public void shouldCreateOneBatchForEachEntryDetail() {
        // Arrange
        Storage storage = new Storage();
        storage.setId(1L);

        Food food1 = new Food();
        food1.setFoodId(5L);

        Food food2 = new Food();
        food2.setFoodId(6L);

        BigDecimal quantity1 = new BigDecimal("2.00");
        BigDecimal quantity2 = new BigDecimal("500.00");

        BigDecimal normalizedQuantity1 = new BigDecimal("2000.00");
        BigDecimal normalizedQuantity2 = new BigDecimal("500.00");

        StorageEntryDetailRequest requestDetail1 =
            new StorageEntryDetailRequest(
                5L,
                UnitOfMeasure.KG,
                quantity1
            );

        StorageEntryDetailRequest requestDetail2 =
            new StorageEntryDetailRequest(
                6L,
                UnitOfMeasure.G,
                quantity2
            );

        StorageEntryRequest request = new StorageEntryRequest(
            LocalDateTime.now(),
            1L,
            List.of(requestDetail1, requestDetail2)
        );

        StorageEntryDetail detail1 = new StorageEntryDetail();
        StorageEntryDetail detail2 = new StorageEntryDetail();

        when(storageRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(storage));

        when(foodService.getActiveFoodById(5L)).thenReturn(food1);
        when(foodService.getActiveFoodById(6L)).thenReturn(food2);

        when(unitConversionService.toInventoryUnit(quantity1, UnitOfMeasure.KG))
            .thenReturn(normalizedQuantity1);

        when(unitConversionService.toInventoryUnit(quantity2, UnitOfMeasure.G))
            .thenReturn(normalizedQuantity2);

        when(storageItemRepository.findByStorageIdAndFoodId(1L, 5L))
            .thenReturn(Optional.empty());

        when(storageItemRepository.findByStorageIdAndFoodId(1L, 6L))
            .thenReturn(Optional.empty());

        when(storageEntryDetailService.buildEntryDetail(
            eq(food1),
            eq(requestDetail1),
            any(StorageEntry.class)
        )).thenAnswer(invocation -> {
            StorageEntry entry = invocation.getArgument(2);
            detail1.setStorageEntry(entry);
            detail1.setFood(food1);
            detail1.setCantidad(quantity1);
            detail1.setSelectedUnit(UnitOfMeasure.KG);
            return detail1;
        });

        when(storageEntryDetailService.buildEntryDetail(
            eq(food2),
            eq(requestDetail2),
            any(StorageEntry.class)
        )).thenAnswer(invocation -> {
            StorageEntry entry = invocation.getArgument(2);
            detail2.setStorageEntry(entry);
            detail2.setFood(food2);
            detail2.setCantidad(quantity2);
            detail2.setSelectedUnit(UnitOfMeasure.G);
            return detail2;
        });

        when(storageEntryRepository.save(any(StorageEntry.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        StorageEntry result = storageEntryService.createStorageEntry(request);

        // Assert: la entrada contiene ambos detalles
        assertNotNull(result);
        assertEquals(2, result.getDetails().size());

        // Assert: cada detalle tiene su propio lote
        verify(storageBatchRepository).saveAll(argThat(batches -> {
            List<StorageBatch> savedBatches = new ArrayList<>();
            batches.forEach(savedBatches::add);

            assertEquals(2, savedBatches.size());

            StorageBatch batch1 = savedBatches.stream()
                .filter(batch -> batch.getStorageEntryDetail() == detail1)
                .findFirst()
                .orElseThrow();

            StorageBatch batch2 = savedBatches.stream()
                .filter(batch -> batch.getStorageEntryDetail() == detail2)
                .findFirst()
                .orElseThrow();

            assertEquals(
                0,
                normalizedQuantity1.compareTo(batch1.getInitialQuantity())
            );
            assertEquals(
                0,
                normalizedQuantity1.compareTo(batch1.getAvailableQuantity())
            );

            assertEquals(
                0,
                normalizedQuantity2.compareTo(batch2.getInitialQuantity())
            );
            assertEquals(
                0,
                normalizedQuantity2.compareTo(batch2.getAvailableQuantity())
            );

            return true;
        }));
    }
}

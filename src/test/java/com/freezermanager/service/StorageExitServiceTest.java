
package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.dto.StorageExitDetailRequest;
import com.freezermanager.dto.StorageExitRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.StorageExitDetailRepository;
import com.freezermanager.repository.StorageExitRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
class StorageExitServiceTest {

    private static final String AUTHENTICATED_EMAIL = "usuario@test.com";

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

    @Mock
    private UnitConversionService unitConversionService;

    @Mock
    private StorageExitDetailService storageExitDetailService;

    @Mock
    private StorageBatchRepository storageBatchRepository;

    @InjectMocks
    private StorageExitService storageExitService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        AUTHENTICATED_EMAIL,
                        null,
                        List.of()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Storage mockOwnedStorage(Storage storage) {
        when(storageRepository.findByIdAndOwner_EmailIgnoreCase(
                storage.getId(),
                AUTHENTICATED_EMAIL
        )).thenReturn(Optional.of(storage));

        when(storageRepository.findByIdForUpdate(storage.getId()))
                .thenReturn(Optional.of(storage));

        return storage;
    }

    private void mockBatchOwnedByUser(Long batchId) {
        when(storageBatchRepository
                .existsByIdAndStorageEntryDetail_StorageEntry_Storage_Owner_EmailIgnoreCase(
                        batchId,
                        AUTHENTICATED_EMAIL
                )).thenReturn(true);
    }

    private StorageBatch createBatch(
            Storage storage,
            Food food,
            BigDecimal quantity) {

        StorageEntry entry = new StorageEntry();
        entry.setStorage(storage);

        StorageEntryDetail entryDetail = new StorageEntryDetail();
        entryDetail.setStorageEntry(entry);
        entryDetail.setFood(food);

        StorageBatch batch = new StorageBatch();
        batch.setId(10L);
        batch.setStorageEntryDetail(entryDetail);
        batch.setInitialQuantity(quantity);
        batch.setAvailableQuantity(quantity);
        batch.setActive(true);

        return batch;
    }

    private StorageExitDetail createExitDetail(
            Food food,
            StorageExitDetailRequest request,
            StorageExit exit) {

        StorageExitDetail detail = new StorageExitDetail();
        detail.setFood(food);
        detail.setCantidad(request.cantidad());
        detail.setSelectedUnit(request.selectedUnit());
        detail.setStorageExit(exit);

        return detail;
    }

    @Test
    void shouldFindAllExtractions() {
        StorageExit extraction = new StorageExit();
        extraction.setStorageExitId(1L);

        Pageable pageable = PageRequest.of(0, 10);

        Page<StorageExit> expectedPage =
                new PageImpl<>(List.of(extraction), pageable, 1);

        when(storageExitRepository.findAllByStorage_Owner_EmailIgnoreCase(
                AUTHENTICATED_EMAIL,
                pageable
        )).thenReturn(expectedPage);

        Page<StorageExit> result =
                storageExitService.getAllStorageExits(pageable);

        assertEquals(expectedPage, result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(storageExitRepository)
                .findAllByStorage_Owner_EmailIgnoreCase(
                        AUTHENTICATED_EMAIL,
                        pageable
                );
    }

    @Test
    void shouldFindExtractionById() {
        StorageExit expectedExtraction = new StorageExit();
        expectedExtraction.setStorageExitId(1L);

        when(storageExitRepository
                .findByStorageExitIdAndStorage_Owner_EmailIgnoreCase(
                        1L,
                        AUTHENTICATED_EMAIL
                )).thenReturn(Optional.of(expectedExtraction));

        StorageExit result =
                storageExitService.getStorageExitById(1L);

        assertEquals(expectedExtraction, result);

        verify(storageExitRepository)
                .findByStorageExitIdAndStorage_Owner_EmailIgnoreCase(
                        1L,
                        AUTHENTICATED_EMAIL
                );
    }

    @Test
    void shouldThrowNotFoundExceptionWhenExtractionDoesNotExist() {
        when(storageExitRepository
                .findByStorageExitIdAndStorage_Owner_EmailIgnoreCase(
                        99L,
                        AUTHENTICATED_EMAIL
                )).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> storageExitService.getStorageExitById(99L)
        );

        verify(storageExitRepository)
                .findByStorageExitIdAndStorage_Owner_EmailIgnoreCase(
                        99L,
                        AUTHENTICATED_EMAIL
                );
    }

    @Test
    void shouldCreateStorageExitAndSubtractStock() {
        Storage storage = new Storage();
        storage.setId(1L);
        mockOwnedStorage(storage);

        Food food = new Food();
        food.setFoodId(1L);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));

        StorageItem storageItem = new StorageItem();
        storageItem.setStorage(storage);
        storageItem.setFood(food);
        storageItem.setCantidad(new BigDecimal("5"));

        StorageBatch batch = createBatch(
                storage,
                food,
                new BigDecimal("5")
        );

        when(foodService.getActiveFoodById(1L))
                .thenReturn(food);

        when(storageBatchRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(batch));

        mockBatchOwnedByUser(10L);

        when(storageItemRepository.findByStorageIdAndFoodIdForUpdate(
                1L,
                1L
        )).thenReturn(Optional.of(storageItem));

        when(unitConversionService.toInventoryUnit(
                new BigDecimal("2"),
                UnitOfMeasure.UNIT
        )).thenReturn(new BigDecimal("2"));

        StorageExitDetailRequest detailRequest =
                new StorageExitDetailRequest(
                        1L,
                        10L,
                        UnitOfMeasure.UNIT,
                        new BigDecimal("2")
                );

        when(storageExitDetailService.buildExitDetail(
                eq(food),
                eq(detailRequest),
                any(StorageExit.class)
        )).thenAnswer(invocation -> createExitDetail(
                invocation.getArgument(0),
                invocation.getArgument(1),
                invocation.getArgument(2)
        ));

        when(storageExitRepository.save(any(StorageExit.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StorageExitRequest request = new StorageExitRequest(
                LocalDateTime.now(),
                1L,
                List.of(detailRequest)
        );

        StorageExit result =
                storageExitService.createStorageExit(request);

        assertNotNull(result);
        assertEquals(1, result.getDetails().size());

        assertEquals(
                0,
                new BigDecimal("3").compareTo(storageItem.getCantidad()),
                "El stock agregado debe quedar en 3"
        );

        assertEquals(
                0,
                new BigDecimal("3").compareTo(batch.getAvailableQuantity()),
                "El stock disponible del lote debe quedar en 3"
        );

        assertTrue(batch.isActive());

        StorageExitDetail savedDetail = result.getDetails().get(0);
        assertSame(batch, savedDetail.getStorageBatch());

        verify(storageBatchRepository).save(batch);
        verify(storageItemRepository).save(storageItem);
        verify(storageExitRepository).save(any(StorageExit.class));
    }

    @Test
    void shouldThrowExceptionWhenStockIsInsufficient() {
        Storage storage = new Storage();
        storage.setId(1L);
        mockOwnedStorage(storage);

        Food food = new Food();
        food.setFoodId(1L);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));

        StorageItem storageItem = new StorageItem();
        storageItem.setStorage(storage);
        storageItem.setFood(food);
        storageItem.setCantidad(new BigDecimal("5"));

        StorageBatch batch = createBatch(
                storage,
                food,
                new BigDecimal("5")
        );

        when(foodService.getActiveFoodById(1L))
                .thenReturn(food);

        when(storageBatchRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(batch));

        mockBatchOwnedByUser(10L);

        when(unitConversionService.toInventoryUnit(
                new BigDecimal("6"),
                UnitOfMeasure.UNIT
        )).thenReturn(new BigDecimal("6"));

        StorageExitDetailRequest detailRequest =
                new StorageExitDetailRequest(
                        1L,
                        10L,
                        UnitOfMeasure.UNIT,
                        new BigDecimal("6")
                );

        when(storageExitDetailService.buildExitDetail(
                eq(food),
                eq(detailRequest),
                any(StorageExit.class)
        )).thenAnswer(invocation -> createExitDetail(
                invocation.getArgument(0),
                invocation.getArgument(1),
                invocation.getArgument(2)
        ));

        StorageExitRequest request = new StorageExitRequest(
                LocalDateTime.now(),
                1L,
                List.of(detailRequest)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> storageExitService.createStorageExit(request)
        );

        assertEquals(
                0,
                new BigDecimal("5").compareTo(batch.getAvailableQuantity())
        );

        verify(storageItemRepository, never())
                .save(any(StorageItem.class));

        verify(storageBatchRepository, never())
                .save(any(StorageBatch.class));

        verify(storageExitRepository, never())
                .save(any(StorageExit.class));
    }

    @Test
    void shouldThrowExceptionWhenStorageItemDoesNotExist() {
        Storage storage = new Storage();
        storage.setId(1L);
        mockOwnedStorage(storage);

        Food food = new Food();
        food.setFoodId(1L);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));

        StorageBatch batch = createBatch(
                storage,
                food,
                new BigDecimal("5")
        );

        when(foodService.getActiveFoodById(1L))
                .thenReturn(food);

        when(storageBatchRepository.findByIdForUpdate(10L))
                .thenReturn(Optional.of(batch));

        mockBatchOwnedByUser(10L);

        when(unitConversionService.toInventoryUnit(
                new BigDecimal("2"),
                UnitOfMeasure.UNIT
        )).thenReturn(new BigDecimal("2"));

        when(storageItemRepository.findByStorageIdAndFoodIdForUpdate(
                1L,
                1L
        )).thenReturn(Optional.empty());

        StorageExitDetailRequest detailRequest =
                new StorageExitDetailRequest(
                        1L,
                        10L,
                        UnitOfMeasure.UNIT,
                        new BigDecimal("2")
                );

        when(storageExitDetailService.buildExitDetail(
                eq(food),
                eq(detailRequest),
                any(StorageExit.class)
        )).thenAnswer(invocation -> createExitDetail(
                invocation.getArgument(0),
                invocation.getArgument(1),
                invocation.getArgument(2)
        ));

        StorageExitRequest request = new StorageExitRequest(
                LocalDateTime.now(),
                1L,
                List.of(detailRequest)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> storageExitService.createStorageExit(request)
        );

        verify(storageItemRepository, never())
                .save(any(StorageItem.class));

        verify(storageBatchRepository, never())
                .save(any(StorageBatch.class));

        verify(storageExitRepository, never())
                .save(any(StorageExit.class));
    }

    @Test
    void shouldThrowExceptionWhenUnitIsNotAllowed() {
        Storage storage = new Storage();
        storage.setId(1L);
        mockOwnedStorage(storage);

        Food food = new Food();
        food.setFoodId(1L);
        food.setUnitOfMeasures(Set.of(UnitOfMeasure.UNIT));

        when(foodService.getActiveFoodById(1L))
                .thenReturn(food);

        doThrow(new IllegalArgumentException("Unidad no permitida"))
                .when(foodService)
                .validateSelectedUnit(food, UnitOfMeasure.KG);

        StorageExitDetailRequest detailRequest =
                new StorageExitDetailRequest(
                        1L,
                        10L,
                        UnitOfMeasure.KG,
                        new BigDecimal("2")
                );

        StorageExitRequest request = new StorageExitRequest(
                LocalDateTime.now(),
                1L,
                List.of(detailRequest)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> storageExitService.createStorageExit(request)
        );

        verify(storageBatchRepository, never())
                .findByIdForUpdate(anyLong());

        verify(storageItemRepository, never())
                .save(any(StorageItem.class));

        verify(storageExitRepository, never())
                .save(any(StorageExit.class));
    }

    @Test
    void shouldThrowExceptionWhenFoodIsInactive() {
        Storage storage = new Storage();
        storage.setId(1L);
        mockOwnedStorage(storage);

        when(foodService.getActiveFoodById(1L))
                .thenThrow(new IllegalArgumentException(
                        "No se puede utilizar un alimento inactivo"
                ));

        StorageExitDetailRequest detailRequest =
                new StorageExitDetailRequest(
                        1L,
                        10L,
                        UnitOfMeasure.UNIT,
                        new BigDecimal("2")
                );

        StorageExitRequest request = new StorageExitRequest(
                LocalDateTime.now(),
                1L,
                List.of(detailRequest)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> storageExitService.createStorageExit(request)
        );

        verify(storageBatchRepository, never())
                .findByIdForUpdate(anyLong());

        verify(storageItemRepository, never())
                .save(any(StorageItem.class));

        verify(storageExitRepository, never())
                .save(any(StorageExit.class));
    }
}

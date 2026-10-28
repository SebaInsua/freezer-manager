
package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.freezermanager.domain.storage.ExpirationDateType;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.dto.StorageEntryDetailRequest;
import com.freezermanager.dto.StorageEntryRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.StorageEntryDetailRepository;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;

@ExtendWith(MockitoExtension.class)
class StorageEntryServiceTest {

    private static final String AUTHENTICATED_EMAIL = "usuario@test.com";

    @Mock
    private StorageEntryRepository storageEntryRepository;

    @Mock
    private StorageEntryDetailRepository storageEntryDetailRepository;

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

    private Storage mockOwnedStorage(Long id) {
        Storage storage = new Storage();
        storage.setId(id);

        when(storageRepository.findByIdAndOwner_EmailIgnoreCase(
                id, AUTHENTICATED_EMAIL
        )).thenReturn(Optional.of(storage));

        when(storageRepository.findByIdForUpdate(id))
                .thenReturn(Optional.of(storage));

        return storage;
    }

    @Test
    void shouldFindAllEntries() {
        StorageEntry entry = new StorageEntry();
        entry.setStorageEntryId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StorageEntry> expectedPage = new PageImpl<>(
                List.of(entry), pageable, 1
        );

        when(storageEntryRepository.findAllByStorage_Owner_EmailIgnoreCase(
                AUTHENTICATED_EMAIL, pageable
        )).thenReturn(expectedPage);

        Page<StorageEntry> result =
                storageEntryService.getAllStorageEntries(pageable);

        assertEquals(expectedPage, result);

        verify(storageEntryRepository)
                .findAllByStorage_Owner_EmailIgnoreCase(
                        AUTHENTICATED_EMAIL, pageable
                );
    }

    @Test
    void shouldFindEntryById() {
        StorageEntry expectedEntry = new StorageEntry();
        expectedEntry.setStorageEntryId(1L);

        when(storageEntryRepository
                .findByStorageEntryIdAndStorage_Owner_EmailIgnoreCase(
                        1L, AUTHENTICATED_EMAIL
                )).thenReturn(Optional.of(expectedEntry));

        StorageEntry result = storageEntryService.getStorageEntryById(1L);

        assertEquals(expectedEntry, result);

        verify(storageEntryRepository)
                .findByStorageEntryIdAndStorage_Owner_EmailIgnoreCase(
                        1L, AUTHENTICATED_EMAIL
                );
    }

    @Test
    void shouldThrowNotFoundExceptionWhenEntryDoesNotExist() {
        when(storageEntryRepository
                .findByStorageEntryIdAndStorage_Owner_EmailIgnoreCase(
                        99L, AUTHENTICATED_EMAIL
                )).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> storageEntryService.getStorageEntryById(99L)
        );

        verify(storageEntryRepository)
                .findByStorageEntryIdAndStorage_Owner_EmailIgnoreCase(
                        99L, AUTHENTICATED_EMAIL
                );
    }

    @Test
    void shouldCreateBatchWhenStorageEntryIsCreated() {
        Storage storage = mockOwnedStorage(1L);

        Food food = new Food();
        food.setFoodId(5L);

        BigDecimal quantity = new BigDecimal("2.00");
        BigDecimal normalizedQuantity = new BigDecimal("2000.00");

        StorageEntryDetailRequest detailRequest =
                new StorageEntryDetailRequest(
                        5L, UnitOfMeasure.KG, quantity, null
                );

        StorageEntryRequest request = new StorageEntryRequest(
                LocalDateTime.now(), 1L, List.of(detailRequest)
        );

        StorageEntryDetail detail = new StorageEntryDetail();

        when(foodService.getActiveFoodById(5L)).thenReturn(food);
        when(unitConversionService.toInventoryUnit(
                quantity, UnitOfMeasure.KG
        )).thenReturn(normalizedQuantity);

        when(storageItemRepository.findByStorageIdAndFoodId(1L, 5L))
                .thenReturn(Optional.empty());

        when(storageEntryDetailService.buildEntryDetail(
                eq(food), eq(detailRequest), any(StorageEntry.class)
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

        StorageEntry result = storageEntryService.createStorageEntry(request);

        assertNotNull(result);
        assertEquals(1, result.getDetails().size());

        verify(storageBatchRepository).saveAll(argThat(batches -> {
            List<StorageBatch> savedBatches = new ArrayList<>();
            batches.forEach(savedBatches::add);

            assertEquals(1, savedBatches.size());

            StorageBatch batch = savedBatches.get(0);
            assertSame(detail, batch.getStorageEntryDetail());
            assertEquals(
                    0, normalizedQuantity.compareTo(batch.getInitialQuantity())
            );
            assertEquals(
                    0, normalizedQuantity.compareTo(batch.getAvailableQuantity())
            );

            return true;
        }));
    }

    @Test
    void shouldCreateOneBatchForEachEntryDetail() {
        mockOwnedStorage(1L);

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
                        5L, UnitOfMeasure.KG, quantity1, null
                );

        StorageEntryDetailRequest requestDetail2 =
                new StorageEntryDetailRequest(
                        6L, UnitOfMeasure.G, quantity2, null
                );

        StorageEntryRequest request = new StorageEntryRequest(
                LocalDateTime.now(), 1L,
                List.of(requestDetail1, requestDetail2)
        );

        StorageEntryDetail detail1 = new StorageEntryDetail();
        StorageEntryDetail detail2 = new StorageEntryDetail();

        when(foodService.getActiveFoodById(5L)).thenReturn(food1);
        when(foodService.getActiveFoodById(6L)).thenReturn(food2);

        when(unitConversionService.toInventoryUnit(
                quantity1, UnitOfMeasure.KG
        )).thenReturn(normalizedQuantity1);

        when(unitConversionService.toInventoryUnit(
                quantity2, UnitOfMeasure.G
        )).thenReturn(normalizedQuantity2);

        when(storageItemRepository.findByStorageIdAndFoodId(1L, 5L))
                .thenReturn(Optional.empty());

        when(storageItemRepository.findByStorageIdAndFoodId(1L, 6L))
                .thenReturn(Optional.empty());

        when(storageEntryDetailService.buildEntryDetail(
                eq(food1), eq(requestDetail1), any(StorageEntry.class)
        )).thenAnswer(invocation -> {
            StorageEntry entry = invocation.getArgument(2);
            detail1.setStorageEntry(entry);
            detail1.setFood(food1);
            detail1.setCantidad(quantity1);
            detail1.setSelectedUnit(UnitOfMeasure.KG);
            return detail1;
        });

        when(storageEntryDetailService.buildEntryDetail(
                eq(food2), eq(requestDetail2), any(StorageEntry.class)
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

        StorageEntry result = storageEntryService.createStorageEntry(request);

        assertNotNull(result);
        assertEquals(2, result.getDetails().size());

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
                    0, normalizedQuantity1.compareTo(batch1.getInitialQuantity())
            );
            assertEquals(
                    0, normalizedQuantity1.compareTo(batch1.getAvailableQuantity())
            );
            assertEquals(
                    0, normalizedQuantity2.compareTo(batch2.getInitialQuantity())
            );
            assertEquals(
                    0, normalizedQuantity2.compareTo(batch2.getAvailableQuantity())
            );

            return true;
        }));
    }

    @Test
    void shouldUseActualExpirationDateWhenProvided() {
        mockOwnedStorage(1L);

        Food food = new Food();
        food.setFoodId(5L);
        food.setDefaultShelfLifeDays(90);

        LocalDate movementDate = LocalDate.of(2026, 10, 19);
        LocalDate actualExpirationDate = LocalDate.of(2027, 2, 10);
        BigDecimal quantity = new BigDecimal("2.00");
        BigDecimal normalizedQuantity = new BigDecimal("2000.00");

        StorageEntryDetailRequest detailRequest =
                new StorageEntryDetailRequest(
                        5L, UnitOfMeasure.KG, quantity, actualExpirationDate
                );

        StorageEntryRequest request = new StorageEntryRequest(
                movementDate.atTime(12, 0), 1L, List.of(detailRequest)
        );

        StorageEntryDetail detail = new StorageEntryDetail();

        when(foodService.getActiveFoodById(5L)).thenReturn(food);
        when(unitConversionService.toInventoryUnit(
                quantity, UnitOfMeasure.KG
        )).thenReturn(normalizedQuantity);
        when(storageItemRepository.findByStorageIdAndFoodId(1L, 5L))
                .thenReturn(Optional.empty());

        when(storageEntryDetailService.buildEntryDetail(
                eq(food), eq(detailRequest), any(StorageEntry.class)
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

        storageEntryService.createStorageEntry(request);

        verify(storageBatchRepository).saveAll(argThat(batches -> {
            StorageBatch batch = batches.iterator().next();

            assertEquals(actualExpirationDate, batch.getExpirationDate());
            assertEquals(
                    ExpirationDateType.ACTUAL, batch.getExpirationDateType()
            );

            return true;
        }));
    }

    @Test
    void shouldEstimateExpirationDateWhenActualDateIsNotProvided() {
        mockOwnedStorage(1L);

        Food food = new Food();
        food.setFoodId(5L);
        food.setDefaultShelfLifeDays(90);

        LocalDate movementDate = LocalDate.of(2026, 10, 19);
        LocalDate expectedExpirationDate = LocalDate.of(2027, 1, 17);
        BigDecimal quantity = new BigDecimal("2.00");
        BigDecimal normalizedQuantity = new BigDecimal("2000.00");

        StorageEntryDetailRequest detailRequest =
                new StorageEntryDetailRequest(
                        5L, UnitOfMeasure.KG, quantity, null
                );

        StorageEntryRequest request = new StorageEntryRequest(
                movementDate.atTime(12, 0), 1L, List.of(detailRequest)
        );

        StorageEntryDetail detail = new StorageEntryDetail();

        when(foodService.getActiveFoodById(5L)).thenReturn(food);
        when(unitConversionService.toInventoryUnit(
                quantity, UnitOfMeasure.KG
        )).thenReturn(normalizedQuantity);
        when(storageItemRepository.findByStorageIdAndFoodId(1L, 5L))
                .thenReturn(Optional.empty());

        when(storageEntryDetailService.buildEntryDetail(
                eq(food), eq(detailRequest), any(StorageEntry.class)
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

        storageEntryService.createStorageEntry(request);

        verify(storageBatchRepository).saveAll(argThat(batches -> {
            StorageBatch batch = batches.iterator().next();

            assertEquals(expectedExpirationDate, batch.getExpirationDate());
            assertEquals(
                    ExpirationDateType.ESTIMATED, batch.getExpirationDateType()
            );

            return true;
        }));
    }

    @Test
    void shouldLeaveExpirationUndefinedWhenNoDateOrShelfLifeExists() {
        mockOwnedStorage(1L);

        Food food = new Food();
        food.setFoodId(5L);
        food.setDefaultShelfLifeDays(null);

        BigDecimal quantity = new BigDecimal("2.00");
        BigDecimal normalizedQuantity = new BigDecimal("2000.00");

        StorageEntryDetailRequest detailRequest =
                new StorageEntryDetailRequest(
                        5L, UnitOfMeasure.KG, quantity, null
                );

        StorageEntryRequest request = new StorageEntryRequest(
                LocalDateTime.of(2026, 10, 19, 12, 0),
                1L, List.of(detailRequest)
        );

        StorageEntryDetail detail = new StorageEntryDetail();

        when(foodService.getActiveFoodById(5L)).thenReturn(food);
        when(unitConversionService.toInventoryUnit(
                quantity, UnitOfMeasure.KG
        )).thenReturn(normalizedQuantity);
        when(storageItemRepository.findByStorageIdAndFoodId(1L, 5L))
                .thenReturn(Optional.empty());

        when(storageEntryDetailService.buildEntryDetail(
                eq(food), eq(detailRequest), any(StorageEntry.class)
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

        storageEntryService.createStorageEntry(request);

        verify(storageBatchRepository).saveAll(argThat(batches -> {
            StorageBatch batch = batches.iterator().next();

            assertNull(batch.getExpirationDate());
            assertEquals(
                    ExpirationDateType.UNDEFINED, batch.getExpirationDateType()
            );

            return true;
        }));
    }
}

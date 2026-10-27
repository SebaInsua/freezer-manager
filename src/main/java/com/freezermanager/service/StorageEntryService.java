package com.freezermanager.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.freezermanager.repository.StorageEntryDetailRepository;
import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.storage.ExpirationDateType;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.dto.StorageEntryDetailRequest;
import com.freezermanager.dto.StorageEntryRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.repository.StorageBatchRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageEntryService {
    private final StorageEntryDetailRepository storageEntryDetailRepository;
    private final StorageEntryRepository storageEntryRepository;
    private final FoodService foodService;
    private final StorageEntryDetailService storageEntryDetailService;
    private final StorageItemRepository storageItemRepository;
    private final UnitConversionService unitConversionService;
    private final StorageRepository storageRepository;
    private final StorageBatchRepository storageBatchRepository;

    private void updateInventory(Storage storage, Food food, BigDecimal normalizedQuantity) {
        var storageItem = storageItemRepository
            .findByStorageIdAndFoodId(storage.getId(), food.getFoodId());

        if (storageItem.isPresent()) {
            var item = storageItem.get();
            item.setCantidad(item.getCantidad().add(normalizedQuantity));
            storageItemRepository.save(item);
        } else {
            StorageItem item = new StorageItem();
            item.setStorage(storage);
            item.setFood(food);
            item.setCantidad(normalizedQuantity);
            storageItemRepository.save(item);
        }
    }

    public Page<StorageEntry> getAllStorageEntries(Pageable pageable) {
        return storageEntryRepository.findAll(pageable);
    }

    public StorageEntry getStorageEntryById(Long id){
        return storageEntryRepository.findById(id).orElseThrow(() -> new NotFoundException("No existe un registro de ingreso con el ID: " + id));
    }

    public List<StorageEntryDetail> getDetailsByEntryId(Long id){
        if (!storageEntryRepository.existsById(id)){
            throw new NotFoundException("No existe un registro de ingreso con el ID: "+ id);
        }
        return storageEntryDetailRepository.findDetailsByStorageEntryId(id);
    }


    @Transactional
    public StorageEntry createStorageEntry(StorageEntryRequest request) {
        Storage storage = storageRepository.findByIdForUpdate(request.storageId())
            .orElseThrow(() -> new NotFoundException(
                "No existe un depósito con el ID: " + request.storageId()
            ));

        StorageEntry entry = new StorageEntry();
        List<StorageEntryDetail> listDetails = new ArrayList<>();
        List<StorageBatch> batches = new ArrayList<>();

        for (StorageEntryDetailRequest item : request.details()) {
            Food foodToSave = foodService.getActiveFoodById(item.foodId());

            foodService.validateSelectedUnit(foodToSave, item.selectedUnit());

            BigDecimal quantityNormalized =
                unitConversionService.toInventoryUnit(
                    item.cantidad(),
                    item.selectedUnit()
                );

            StorageEntryDetail detail =
                storageEntryDetailService.buildEntryDetail(
                    foodToSave,
                    item,
                    entry
                );

            listDetails.add(detail);

            StorageBatch batch = new StorageBatch();
            batch.setStorageEntryDetail(detail);
            if (item.expirationDate() != null) {
                batch.setExpirationDate(item.expirationDate());
                batch.setExpirationDateType(ExpirationDateType.ACTUAL);
            } else if (foodToSave.getDefaultShelfLifeDays() != null) {
                batch.setExpirationDate(
                    request.movementDateTime()
                        .toLocalDate()
                        .plusDays(foodToSave.getDefaultShelfLifeDays())
                );
                batch.setExpirationDateType(ExpirationDateType.ESTIMATED);
            } else {
                batch.setExpirationDate(null);
                batch.setExpirationDateType(ExpirationDateType.UNDEFINED);
            }
            batch.setInitialQuantity(quantityNormalized);
            batch.setAvailableQuantity(quantityNormalized);

            batches.add(batch);

            updateInventory(storage, foodToSave, quantityNormalized);
        }

        entry.setStorage(storage);
        entry.setRecordedAt(LocalDateTime.now());
        entry.setMovementDateTime(request.movementDateTime());
        entry.setDetails(listDetails);

        StorageEntry savedEntry = storageEntryRepository.save(entry);

        storageBatchRepository.saveAll(batches);

        return savedEntry;
    }

}
package com.freezermanager.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.freezermanager.repository.StorageEntryDetailRepository;
import com.freezermanager.domain.food.Food;
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

    public List<StorageEntry> getAllStorageEntries(){
        return storageEntryRepository.findAll();
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
    public StorageEntry createStorageEntry(StorageEntryRequest request){
        Storage storage = storageRepository.findByIdForUpdate(request.storageId())
        .orElseThrow(() -> new NotFoundException(
            "No existe un depósito con el ID: " + request.storageId()
        ));
        StorageEntry entry = new StorageEntry();
        List<StorageEntryDetail> listDetails = new ArrayList<>();
        for(StorageEntryDetailRequest item : request.details()){
            Food foodToSave = foodService.getActiveFoodById(item.foodId());
            foodService.validateSelectedUnit(foodToSave, item.selectedUnit());
            BigDecimal quantityNormalized = unitConversionService.toInventoryUnit(item.cantidad(),item.selectedUnit());
            StorageEntryDetail detalleAIngresar = storageEntryDetailService.buildEntryDetail(foodToSave,item,entry);
            listDetails.add(detalleAIngresar);
            updateInventory(storage,foodToSave,quantityNormalized);
        }
        entry.setStorage(storage);
        entry.setRecordedAt(LocalDateTime.now());
        entry.setMovementDateTime(request.movementDateTime());
        entry.setDetails(listDetails);

        storageEntryRepository.save(entry);
        return entry;
    }
}
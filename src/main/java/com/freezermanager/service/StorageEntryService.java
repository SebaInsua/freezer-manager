package com.freezermanager.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.freezermanager.repository.StorageEntryDetailRepository;
import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.dto.StorageEntryDetailRequest;
import com.freezermanager.dto.StorageEntryRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageEntryRepository;
import com.freezermanager.repository.StorageItemRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageEntryService {
    private final StorageEntryDetailRepository storageEntryDetailRepository;
    private final StorageEntryRepository storageEntryRepository;
    private final FoodService foodService;
    private final StorageService storageService;
    private final StorageEntryDetailService storageEntryDetailService;
    private final StorageItemRepository storageItemRepository;
    private final UnitConversionService unitConversionService;

    private void updateInventory(Storage storage,Food food,BigDecimal normalizedQuantity){
        Optional<StorageItem> storageItem = storageItemRepository.findByStorageIdAndFoodId(storage.getId(),food.getFoodId());
        if(storageItem.isPresent()){
            StorageItem itemToActualize = storageItem.get();
            itemToActualize.setCantidad(
                itemToActualize.getCantidad().add(normalizedQuantity)
            );
            storageItemRepository.save(itemToActualize);
        }else{
            StorageItem nuevoItem = new StorageItem();
            nuevoItem.setStorage(storage);
            nuevoItem.setFood(food);
            nuevoItem.setCantidad(normalizedQuantity);
            storageItemRepository.save(nuevoItem);
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
        Storage storage = storageService.getStorageById(request.storageId());
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
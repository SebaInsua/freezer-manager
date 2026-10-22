package com.freezermanager.service;

import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.StorageEntryDetailRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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
import com.freezermanager.repository.StorageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageEntryService {
    private final StorageEntryDetailRepository storageEntryDetailRepository;
    private final StorageEntryRepository storageEntryRepository;
    private final FoodService foodService;
    private final StorageRepository storageRepository;
    private final StorageItemRepository storageItemRepository;

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
        Storage storage = storageRepository.findById(request.storageId()).orElseThrow(()->new NotFoundException("No existe storage con ID: "+ request.storageId()));
        StorageEntry entry = new StorageEntry();
        List<StorageEntryDetail> listDetails = new ArrayList<>();
        for(StorageEntryDetailRequest item : request.details()){
            Food foodToSave = foodService.getActiveFoodById(item.foodId());
            if(foodToSave.getUnitOfMeasures().contains(item.selectedUnit())){
                StorageEntryDetail detalleAIngresar = new StorageEntryDetail();
                detalleAIngresar.setFood(foodToSave);
                detalleAIngresar.setSelectedUnit(item.selectedUnit());
                detalleAIngresar.setCantidad(item.cantidad());
                detalleAIngresar.setStorageEntry(entry);
                listDetails.add(detalleAIngresar);
                Optional<StorageItem> storageItem = storageItemRepository.findByStorageIdAndFoodId(request.storageId(), foodToSave.getFoodId());
                if(storageItem.isPresent()){
                    StorageItem itemToActualize = storageItem.get();
                    itemToActualize.setCantidad(
                        itemToActualize.getCantidad().add(item.cantidad())
                    );
                    storageItemRepository.save(itemToActualize);
                }else{
                    StorageItem nuevoItem = new StorageItem();
                    nuevoItem.setStorage(storage);
                    nuevoItem.setFood(foodToSave);
                    nuevoItem.setCantidad(item.cantidad());
                    storageItemRepository.save(nuevoItem);
                }
            }else{
                throw new IllegalArgumentException("La unidad seleccionada no es apta para este alimento.");
            }
        }
        entry.setStorage(storage);
        entry.setRecordedAt(LocalDateTime.now());
        entry.setMovementDateTime(request.movementDateTime());
        entry.setDetails(listDetails);

        storageEntryRepository.save(entry);
        return entry;
    }
}
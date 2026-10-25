package com.freezermanager.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.dto.StorageExitDetailRequest;
import com.freezermanager.dto.StorageExitRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageExitDetailRepository;
import com.freezermanager.repository.StorageExitRepository;
import com.freezermanager.repository.StorageItemRepository;
import com.freezermanager.repository.StorageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageExitService {
    private final StorageExitRepository storageExitRepository;
    private final StorageExitDetailRepository storageExitDetailRepository;
    private final StorageRepository storageRepository;
    private final FoodService foodService;
    private final StorageItemRepository storageItemRepository;
    private final UnitConversionService unitConversionService;
    private final StorageExitDetailService storageExitDetailService;

    private void decreaseInventory(Storage storage,Food food,BigDecimal normalizedQuantity){
        StorageItem storageItem = storageItemRepository.findByStorageIdAndFoodIdForUpdate(storage.getId(), food.getFoodId()).orElseThrow(()-> new IllegalArgumentException("El item que desea actualizar no ha sido encontrado o no existe."));
        if (storageItem.getCantidad().compareTo(normalizedQuantity) < 0) {
            throw new IllegalArgumentException("No hay stock suficiente para realizar el retiro");
        }
        BigDecimal stockRestante = storageItem.getCantidad().subtract(normalizedQuantity);
        if (stockRestante.compareTo(BigDecimal.ZERO) == 0) {
            storageItemRepository.delete(storageItem);
        } else {
            storageItem.setCantidad(stockRestante);
            storageItemRepository.save(storageItem);
        }
    }

    public List<StorageExit> getAllStorageExits(){
        return storageExitRepository.findAll();
    }

    public StorageExit getStorageExitById(Long id){
        return storageExitRepository.findById(id).orElseThrow(() -> new NotFoundException("No existe un registro de egreso con el ID: "+ id));
    }

    public List<StorageExitDetail> getDetailsByExitId(Long id){
        if(!storageExitRepository.existsById(id)){
            throw new NotFoundException("No existe un egreso con el ID: "+id);
        }
        return storageExitDetailRepository.findDetailsByStorageExitId(id);
    }

        @Transactional
    public StorageExit createStorageExit(StorageExitRequest request){
        Storage storage = storageRepository.findByIdForUpdate(request.storageId())
        .orElseThrow(() -> new NotFoundException(
            "No existe un depósito con el ID: " + request.storageId()
        ));
        StorageExit exit = new StorageExit();
        List<StorageExitDetail> listDetails = new ArrayList<>();
        for(StorageExitDetailRequest item : request.details()){
            Food foodToSave = foodService.getActiveFoodById(item.foodId());
            foodService.validateSelectedUnit(foodToSave,item.selectedUnit());
            BigDecimal quantityNormalized = unitConversionService.toInventoryUnit(item.cantidad(),item.selectedUnit());
            StorageExitDetail detalleAIngresar = storageExitDetailService.buildExitDetail(foodToSave, item, exit);
            listDetails.add(detalleAIngresar);
            decreaseInventory(storage, foodToSave, quantityNormalized);
        }
        exit.setStorage(storage);
        exit.setRecordedAt(LocalDateTime.now());
        exit.setMovementDateTime(request.movementDateTime());
        exit.setDetails(listDetails);

        storageExitRepository.save(exit);
        return exit;
    }
}

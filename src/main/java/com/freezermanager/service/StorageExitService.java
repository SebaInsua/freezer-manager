package com.freezermanager.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageExitService {
    private final StorageExitRepository storageExitRepository;

    private final StorageExitDetailRepository storageExitDetailRepository;

    private final StorageRepository storageRepository;

    private final FoodService foodService;

    private final StorageItemRepository storageItemRepository;

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
        Storage storage = storageRepository.findById(request.storageId()).orElseThrow(()->new NotFoundException("No existe storage con ID: "+ request.storageId()));
        StorageExit exit = new StorageExit();
        List<StorageExitDetail> listDetails = new ArrayList<>();
        for(StorageExitDetailRequest item : request.details()){
            Food foodToSave = foodService.getActiveFoodById(item.foodId());
            if(foodToSave.getUnitOfMeasures().contains(item.selectedUnit())){
                StorageExitDetail detalleAIngresar = new StorageExitDetail();
                detalleAIngresar.setFood(foodToSave);
                detalleAIngresar.setSelectedUnit(item.selectedUnit());
                detalleAIngresar.setCantidad(item.cantidad());
                detalleAIngresar.setStorageExit(exit);
                listDetails.add(detalleAIngresar);
                Optional<StorageItem> storageItem = storageItemRepository.findByStorageIdAndFoodId(request.storageId(), foodToSave.getFoodId());
                if(storageItem.isPresent()){
                    StorageItem itemToActualize = storageItem.get();
                    if (itemToActualize.getCantidad().compareTo(item.cantidad()) >= 0 ){
                        itemToActualize.setCantidad(
                        itemToActualize.getCantidad().subtract(item.cantidad())
                    );
                    }else{
                        throw new IllegalArgumentException("No hay stock suficiente para realizar el retiro");
                    }
                    storageItemRepository.save(itemToActualize);
                }else{
                    throw new IllegalArgumentException("El item que desea actualizar no ha sido encontrado o no existe.");
                }
            }else{
                throw new IllegalArgumentException("La unidad seleccionada no es apta para este alimento.");
            }
        }
        exit.setStorage(storage);
        exit.setRecordedAt(LocalDateTime.now());
        exit.setMovementDateTime(request.movementDateTime());
        exit.setDetails(listDetails);

        storageExitRepository.save(exit);
        return exit;
    }
}

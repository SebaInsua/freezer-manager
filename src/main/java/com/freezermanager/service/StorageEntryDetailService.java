package com.freezermanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.dto.StorageEntryDetailRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageEntryDetailRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageEntryDetailService {
    private final StorageEntryDetailRepository storageEntryDetailRepository;

    public StorageEntryDetail buildEntryDetail(Food food,StorageEntryDetailRequest detail, StorageEntry entry){
            StorageEntryDetail detalleAIngresar = new StorageEntryDetail();
            detalleAIngresar.setFood(food);
            detalleAIngresar.setSelectedUnit(detail.selectedUnit());
            detalleAIngresar.setCantidad(detail.cantidad());
            detalleAIngresar.setStorageEntry(entry);
            return detalleAIngresar;
    }

    public List<StorageEntryDetail> getAllStorageEntriesDetails(){
        return storageEntryDetailRepository.findAll();
    }

    public StorageEntryDetail getStorageEntryDetailById(Long id){
        return storageEntryDetailRepository.findById(id).orElseThrow(() -> new NotFoundException("No existe un detalle de ingreso con el ID: " + id));
    }
}

package com.freezermanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageItemService {

    private final StorageItemRepository storageItemRepository;

    public List<StorageItem> getStorageItems(){
        return storageItemRepository.findAll();
    }

    public StorageItem getStorageItem(Long id){
        return storageItemRepository.findById(id).orElseThrow(() -> new NotFoundException("No existe un elemento en el inventario con el ID: " + id));
    }
}

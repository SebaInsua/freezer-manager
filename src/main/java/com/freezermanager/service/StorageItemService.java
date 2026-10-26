
package com.freezermanager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageItemService {

    private final StorageItemRepository storageItemRepository;

    public Page<StorageItem> getStorageItems(Pageable pageable) {
        return storageItemRepository.findAll(pageable);
    }

    public StorageItem getStorageItem(Long id) {
        return storageItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un elemento en el inventario con el ID: " + id));
    }
}

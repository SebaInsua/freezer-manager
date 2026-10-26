
package com.freezermanager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageRepository storageRepository;

    public Page<Storage> getStorages(Pageable pageable) {
        return storageRepository.findAll(pageable);
    }

    public Storage getStorageById(Long id) {
        return storageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un Freezer con el ID: " + id));
    }
}

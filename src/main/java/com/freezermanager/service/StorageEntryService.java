package com.freezermanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageEntryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageEntryService {
    private final StorageEntryRepository storageEntryRepository;

    public List<StorageEntry> getAllStorageEntries(){
        return storageEntryRepository.findAll();
    }

    public StorageEntry getStorageEntryById(Long id){
        return storageEntryRepository.findById(id).orElseThrow(() -> new NotFoundException("No existe un registro de ingreso con el ID: " + id));
    }
}

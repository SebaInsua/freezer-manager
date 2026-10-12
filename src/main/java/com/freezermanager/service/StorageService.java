package com.freezermanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageService {
    private final StorageRepository storageRepository;

    public List<Storage> getStorages(){
        return storageRepository.findAll();
    }

    public Storage getStorage(Long id){
        return storageRepository.findById(id).orElseThrow(() -> new NotFoundException("No existe un Freezer con el ID: "+id));
    }
}

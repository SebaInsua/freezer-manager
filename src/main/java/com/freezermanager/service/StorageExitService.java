package com.freezermanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageExitRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageExitService {
    private final StorageExitRepository storageExitRepository;

    public List<StorageExit> getAllStorageExits(){
        return storageExitRepository.findAll();
    }

    public StorageExit getStorageExitById(Long id){
        return storageExitRepository.findById(id).orElseThrow(() -> new NotFoundException("No existe un registro de egreso con el ID: "+ id));
    }
}

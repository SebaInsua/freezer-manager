package com.freezermanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageEntryDetailRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageEntryDetailService {
    private final StorageEntryDetailRepository storageEntryDetailRepository;

    public List<StorageEntryDetail> getAllStorageEntriesDetails(){
        return storageEntryDetailRepository.findAll();
    }

    public StorageEntryDetail getStorageEntryDetailById(Long id){
        return storageEntryDetailRepository.findById(id).orElseThrow(() -> new NotFoundException("No existe un detalle de ingreso con el ID: " + id));
    }
}

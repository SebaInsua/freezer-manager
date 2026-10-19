package com.freezermanager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.service.StorageEntryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/storagesentry")
public class StorageEntryController {
    private final StorageEntryService storageEntryService;

    @GetMapping
    public ResponseEntity<List<StorageEntry>> getAllEntries() {
        return ResponseEntity.ok(storageEntryService.getAllStorageEntries());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorageEntry> getStorageEntryById(@PathVariable Long id) {
        return ResponseEntity.ok(storageEntryService.getStorageEntryById(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<List<StorageEntryDetail>> getDetailsByEntryId(@PathVariable Long id){
        return ResponseEntity.ok(storageEntryService.getDetailsByEntryId(id));
    }
}
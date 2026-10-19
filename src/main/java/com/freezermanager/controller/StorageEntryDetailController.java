package com.freezermanager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.service.StorageEntryDetailService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/storage-entrydetail")
public class StorageEntryDetailController {
    private final StorageEntryDetailService storageEntryDetailService;

    @GetMapping
    public ResponseEntity<List<StorageEntryDetail>> getAllStoragesDetails() {
        return ResponseEntity.ok(storageEntryDetailService.getAllStorageEntriesDetails());
    }
    @GetMapping("/{id}")
    public ResponseEntity<StorageEntryDetail> getStorageDetailById(@PathVariable Long id) {
        return ResponseEntity.ok(storageEntryDetailService.getStorageEntryDetailById(id));
    }
}

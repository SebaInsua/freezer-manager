package com.freezermanager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.service.StorageItemService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/storageitems")
public class StorageItemController {
    private final StorageItemService storageItemService;

    @GetMapping
    public ResponseEntity<List<StorageItem>> getAllItems(){
        return ResponseEntity.ok(storageItemService.getStorageItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StorageItem> getItemById(@PathVariable Long id){
        return ResponseEntity.ok(storageItemService.getStorageItem(id));
    }
}

package com.freezermanager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.service.StorageExitService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/storagesexit")
public class StorageExitController {
    private final StorageExitService storageExitService;
    @GetMapping
    public ResponseEntity<List<StorageExit>> getAllStoragesExits() {
        return ResponseEntity.ok(storageExitService.getAllStorageExits());
    }
    @GetMapping("/{id}")
    public ResponseEntity<StorageExit> getStorageExitById(@PathVariable Long id) {
        return ResponseEntity.ok(storageExitService.getStorageExitById(id));
    }
}

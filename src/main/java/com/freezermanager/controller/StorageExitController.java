package com.freezermanager.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.dto.StorageExitRequest;
import com.freezermanager.service.StorageExitService;

import jakarta.validation.Valid;
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
    @GetMapping("/{id}/details")
    public ResponseEntity<List<StorageExitDetail>> getDetailsByExitId(@PathVariable Long id){
        return ResponseEntity.ok(storageExitService.getDetailsByExitId(id));
    }
    @PostMapping
    public ResponseEntity<StorageExit> createStorageExit(@Valid @RequestBody StorageExitRequest request) {
        StorageExit exit = storageExitService.createStorageExit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(exit);
    }
}

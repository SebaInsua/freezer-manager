
package com.freezermanager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.dto.StorageBatchResponse;
import com.freezermanager.service.StorageBatchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storagebatches")
public class StorageBatchController {

    private final StorageBatchService storageBatchService;

    @GetMapping
    public ResponseEntity<List<StorageBatchResponse>> getAllBatches() {
        return ResponseEntity.ok(storageBatchService.getAllBatches());
    }
}


package com.freezermanager.controller;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.freezermanager.dto.StorageBatchResponse;
import com.freezermanager.service.StorageBatchService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storagebatches")
public class StorageBatchController {

    private final StorageBatchService storageBatchService;

    @GetMapping
    public ResponseEntity<Page<StorageBatchResponse>> getAllBatches(
            @PageableDefault(
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        return ResponseEntity.ok(storageBatchService.getAllBatches(pageable));
    }
}

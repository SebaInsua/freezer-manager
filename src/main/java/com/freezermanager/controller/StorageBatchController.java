
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storagebatches")
@SecurityRequirement(name = "bearerAuth")
public class StorageBatchController {

    private final StorageBatchService storageBatchService;

    @Operation(
        summary = "Listar lotes de inventario",
        description = "Obtiene los lotes de inventario del usuario autenticado, paginados y ordenados por identificador."
    )
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

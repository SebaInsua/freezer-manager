package com.freezermanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.dto.ErrorResponse;
import com.freezermanager.service.StorageItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/storageitems")
public class StorageItemController {

    private final StorageItemService storageItemService;

    @Operation(
        summary = "Listar elementos del inventario",
        description = "Obtiene todos los elementos de inventario registrados en los almacenamientos."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Elementos del inventario obtenidos correctamente"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<StorageItem>> getAllItems(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable) {
        return ResponseEntity.ok(storageItemService.getStorageItems(pageable));
    }


    @Operation(
        summary = "Obtener un elemento del inventario",
        description = "Busca un elemento del inventario por su identificador."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Elemento del inventario encontrado correctamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No se encontró un elemento del inventario con ese ID",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<StorageItem> getItemById(@PathVariable Long id){
        return ResponseEntity.ok(storageItemService.getStorageItem(id));
    }
}

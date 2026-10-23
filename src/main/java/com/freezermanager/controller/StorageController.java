package com.freezermanager.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.dto.ErrorResponse;
import com.freezermanager.service.StorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storages")
public class StorageController {
    private final StorageService storageService;

    @Operation(
        summary = "Listar almacenamientos",
        description = "Obtiene todos los almacenamientos registrados."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de almacenamientos obtenida correctamente"
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
    public ResponseEntity<List<Storage>> getAllStorages() {
        return ResponseEntity.ok(storageService.getStorages());
    }

    @Operation(
        summary = "Obtener un almacenamiento",
        description = "Busca un almacenamiento por su identificador."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Almacenamiento encontrado"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No existe un almacenamiento con ese identificador",
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
    public ResponseEntity<Storage> getStorageById(@PathVariable Long id) {
        return ResponseEntity.ok(storageService.getStorageById(id));
    }
}

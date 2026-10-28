package com.freezermanager.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.dto.ErrorResponse;
import com.freezermanager.dto.StorageCapacityResponse;
import com.freezermanager.dto.StorageRequest;
import com.freezermanager.service.StorageCapacityService;
import com.freezermanager.service.StorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storages")
public class StorageController {
    private final StorageService storageService;
    private final StorageCapacityService storageCapacityService;

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
    public ResponseEntity<Page<Storage>> getAllStorages(
            @PageableDefault(
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return ResponseEntity.ok(storageService.getStorages(pageable));
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

    @Operation(
        summary = "Consultar capacidad estimada de un almacenamiento",
        description = """
            Calcula la carga estimada a partir de los lotes activos con cantidad disponible.
            Considera un límite recomendado del 80% de la capacidad nominal y un factor
            aproximado de 3.5 litros por kilogramo. Para alimentos registrados por unidad,
            utiliza el peso estimado por unidad configurado en el alimento.
            Si algún lote por unidad no tiene ese peso configurado, la estimación será parcial.
            Estos valores son estimaciones operativas, no una medición exacta del espacio físico.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Capacidad estimada obtenida correctamente"
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
    @GetMapping("/{id}/capacity")
    public ResponseEntity<StorageCapacityResponse> getStorageCapacity(
            @PathVariable Long id) {
        return ResponseEntity.ok(storageCapacityService.getCapacity(id));
    }

    @PostMapping
    public ResponseEntity<Storage> createStorage(
            @Valid @RequestBody StorageRequest request) {
        Storage storage = storageService.createStorage(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(storage);
    }
}

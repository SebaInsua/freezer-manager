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

import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.dto.ErrorResponse;
import com.freezermanager.dto.StorageEntryRequest;
import com.freezermanager.service.StorageEntryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/storagesentry")
public class StorageEntryController {
    private final StorageEntryService storageEntryService;
    @Operation(
        summary = "Listar ingresos",
        description = "Obtiene todos los registros de ingreso de mercadería."
        )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de ingresos obtenida correctamente"
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
    public ResponseEntity<List<StorageEntry>> getAllEntries() {
        return ResponseEntity.ok(storageEntryService.getAllStorageEntries());
    }

    @Operation(
        summary = "Obtener un ingreso",
        description = "Busca un registro de ingreso por su identificador."
        )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ingreso encontrado"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No existe un registro de ingreso con ese identificador",
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
    public ResponseEntity<StorageEntry> getStorageEntryById(@PathVariable Long id) {
        return ResponseEntity.ok(storageEntryService.getStorageEntryById(id));
    }

    @Operation(
        summary = "Listar detalles de un ingreso",
        description = "Obtiene los detalles asociados a un registro de ingreso."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Detalles obtenidos correctamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No existe un registro de ingreso con ese identificador",
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
    @GetMapping("/{id}/details")
    public ResponseEntity<List<StorageEntryDetail>> getDetailsByEntryId(@PathVariable Long id){
        return ResponseEntity.ok(storageEntryService.getDetailsByEntryId(id));
    }

    @Operation(
        summary = "Registrar un ingreso",
        description = "Registra un ingreso de alimentos en un freezer y actualiza las cantidades del inventario."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Ingreso registrado correctamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Los datos enviados no son válidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No existe el freezer o alguno de los alimentos indicados",
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
    @PostMapping
    public ResponseEntity<StorageEntry> createStorageEntry(@Valid @RequestBody StorageEntryRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(storageEntryService.createStorageEntry(request));
    }
}
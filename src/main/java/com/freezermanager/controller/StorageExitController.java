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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;

import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.dto.ErrorResponse;
import com.freezermanager.dto.StorageExitRequest;
import com.freezermanager.service.StorageExitService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/storagesexit")
public class StorageExitController {
    private final StorageExitService storageExitService;

    @Operation(
        summary = "Listar egresos",
        description = "Obtiene todos los registros de egreso de almacenamiento."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de egresos obtenida correctamente"),
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
    public ResponseEntity<Page<StorageExit>> getAllStoragesExits(
            @PageableDefault(
                    size = 10,
                    sort = "storageExitId",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {
        return ResponseEntity.ok(
                storageExitService.getAllStorageExits(pageable)
        );
    }

    @Operation(
        summary = "Obtener un egreso",
        description = "Busca un registro de egreso por su identificador."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Egreso encontrado correctamente"),
        @ApiResponse(
            responseCode = "404",
            description = "No existe un registro de egreso con ese ID",
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
    public ResponseEntity<StorageExit> getStorageExitById(@PathVariable Long id) {
        return ResponseEntity.ok(storageExitService.getStorageExitById(id));
    }

    @Operation(
        summary = "Listar detalles de un egreso",
        description = "Obtiene los detalles asociados a un registro de egreso."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Detalles obtenidos correctamente"),
        @ApiResponse(
            responseCode = "404",
            description = "No existe un registro de egreso con ese ID",
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
    public ResponseEntity<List<StorageExitDetail>> getDetailsByExitId(@PathVariable Long id){
        return ResponseEntity.ok(storageExitService.getDetailsByExitId(id));
    }

    @Operation(
        summary = "Registrar un egreso",
        description = "Registra un egreso de almacenamiento y actualiza el inventario correspondiente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Egreso creado correctamente"),
        @ApiResponse(
            responseCode = "400",
            description = "Los datos enviados no son válidos o el stock es insuficiente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No se encontró un recurso necesario para registrar el egreso",
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
    public ResponseEntity<StorageExit> createStorageExit(@Valid @RequestBody StorageExitRequest request) {
        StorageExit exit = storageExitService.createStorageExit(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(exit);
    }
}

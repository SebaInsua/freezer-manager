
package com.freezermanager.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.dto.StorageEntryRequest;
import com.freezermanager.dto.response.StorageEntryDetailResponse;
import com.freezermanager.dto.response.StorageEntryResponse;
import com.freezermanager.mapper.StorageEntryMapper;
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
    private final StorageEntryMapper storageEntryMapper;

    @Operation(
            summary = "Listar ingresos de almacenamiento",
            description = "Obtiene los ingresos de almacenamiento del usuario autenticado, paginados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ingresos obtenidos correctamente"),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping
    public ResponseEntity<Page<StorageEntryResponse>> getAllEntries(
            @PageableDefault(
                    size = 10,
                    sort = "storageEntryId",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        Page<StorageEntryResponse> response =
                storageEntryService.getAllStorageEntries(pageable)
                        .map(storageEntryMapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener un ingreso de almacenamiento",
            description = "Obtiene un ingreso por su identificador, siempre que pertenezca al usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ingreso obtenido correctamente"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ingreso inexistente o no accesible",
                    content = @Content(schema = @Schema(implementation = com.freezermanager.dto.ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<StorageEntryResponse> getStorageEntryById(
            @PathVariable Long id) {

        StorageEntryResponse response = storageEntryMapper.toResponse(
                storageEntryService.getStorageEntryById(id)
        );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener detalles de un ingreso",
            description = "Obtiene los detalles de un ingreso de almacenamiento accesible para el usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Detalles obtenidos correctamente"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Ingreso inexistente o no accesible",
                    content = @Content(schema = @Schema(implementation = com.freezermanager.dto.ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @GetMapping("/{id}/details")
    public ResponseEntity<List<StorageEntryDetailResponse>> getDetailsByEntryId(
            @PathVariable Long id) {

        List<StorageEntryDetailResponse> response =
                storageEntryMapper.toDetailResponses(
                        storageEntryService.getDetailsByEntryId(id)
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Registrar un ingreso de almacenamiento",
            description = "Registra un ingreso y sus detalles para un almacenamiento del usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ingreso creado correctamente"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Solicitud inválida o datos no permitidos",
                    content = @Content(schema = @Schema(implementation = com.freezermanager.dto.ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Almacenamiento o alimento inexistente o no accesible",
                    content = @Content(schema = @Schema(implementation = com.freezermanager.dto.ErrorResponse.class))
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    @PostMapping
    public ResponseEntity<StorageEntryResponse> createStorageEntry(
            @Valid @RequestBody StorageEntryRequest request) {

        StorageEntryResponse response = storageEntryMapper.toResponse(
                storageEntryService.createStorageEntry(request)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

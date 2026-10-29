
package com.freezermanager.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.dto.ErrorResponse;
import com.freezermanager.dto.response.StorageEntryDetailResponse;
import com.freezermanager.mapper.StorageEntryMapper;
import com.freezermanager.service.StorageEntryDetailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/storage-entrydetail")
public class StorageEntryDetailController {

    private final StorageEntryDetailService storageEntryDetailService;
    private final StorageEntryMapper storageEntryMapper;

    @Operation(
        summary = "Listar detalles de ingresos",
        description = "Obtiene todos los detalles de los registros de ingreso de almacenamiento."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Detalles de ingresos obtenidos correctamente"
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
    public ResponseEntity<Page<StorageEntryDetailResponse>> getAllStoragesDetails(
            @PageableDefault(
                size = 10,
                sort = "entryDetailId",
                direction = Sort.Direction.ASC
            ) Pageable pageable) {

        Page<StorageEntryDetailResponse> responses =
                storageEntryDetailService
                    .getAllStorageEntriesDetails(pageable)
                    .map(storageEntryMapper::toDetailResponse);

        return ResponseEntity.ok(responses);
    }

    @Operation(
        summary = "Obtener un detalle de ingreso",
        description = "Busca un detalle de ingreso por su identificador."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Detalle de ingreso encontrado correctamente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "No se encontró un detalle de ingreso con ese ID",
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
    public ResponseEntity<StorageEntryDetailResponse> getStorageDetailById(
            @PathVariable Long id) {

        StorageEntryDetail detail =
                storageEntryDetailService.getStorageEntryDetailById(id);

        return ResponseEntity.ok(
                storageEntryMapper.toDetailResponse(detail)
        );
    }
}

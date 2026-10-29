
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

import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.dto.ErrorResponse;
import com.freezermanager.dto.response.StorageItemResponse;
import com.freezermanager.mapper.StorageItemMapper;
import com.freezermanager.service.StorageItemService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/storageitems")
public class StorageItemController {

    private final StorageItemService storageItemService;
    private final StorageItemMapper storageItemMapper;

    @Operation(
            summary = "Listar elementos del inventario",
            description = "Obtiene los elementos del inventario pertenecientes "
                    + "a los almacenamientos del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Elementos del inventario obtenidos correctamente",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StorageItemResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Se requiere autenticación"
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
    public ResponseEntity<Page<StorageItemResponse>> getAllItems(
            @PageableDefault(
                    size = 10,
                    sort = "id",
                    direction = Sort.Direction.ASC
            ) Pageable pageable) {

        Page<StorageItemResponse> response = storageItemService
                .getStorageItems(pageable)
                .map(storageItemMapper::toResponse);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtener un elemento del inventario",
            description = "Busca un elemento por su identificador, "
                    + "si pertenece a un almacenamiento del usuario autenticado."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Elemento del inventario encontrado",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = StorageItemResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Se requiere autenticación"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No se encontró un elemento con ese ID",
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
    public ResponseEntity<StorageItemResponse> getItemById(
            @PathVariable Long id) {

        StorageItem item = storageItemService.getStorageItem(id);

        return ResponseEntity.ok(storageItemMapper.toResponse(item));
    }
}

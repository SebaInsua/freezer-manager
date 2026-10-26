package com.freezermanager.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.domain.food.Food;
import com.freezermanager.dto.ErrorResponse;
import com.freezermanager.dto.FoodRequest;
import com.freezermanager.service.FoodService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/foods")
public class FoodController {
    
    private final FoodService foodService;
    @Operation(
    summary = "Listar alimentos",
    description = "Obtiene los alimentos. Permite filtrar por estado activo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de alimentos obtenida correctamente"),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )),
        @ApiResponse(
            responseCode = "400",
            description = "El parámetro active no tiene un valor booleano válido"
        )
    })
    @GetMapping
    public ResponseEntity<Page<Food>> getFoods(
        @RequestParam(required = false) Boolean active,
        @PageableDefault(
            size = 10,sort = "name",
            direction = Sort.Direction.ASC
        )
        Pageable pageable
        ){
        return ResponseEntity.ok(foodService.getFoods(active,pageable));
    }

    @Operation(
    summary = "Obtener un alimento",
    description = "Busca un alimento por su identificador."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alimento encontrado"),
        @ApiResponse(
        responseCode = "404",
        description = "No existe un alimento con ese identificador",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = ErrorResponse.class)
        )),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            ))
    })

    @GetMapping("/{id}")
    public ResponseEntity<Food> getFood(@PathVariable Long id){
        return ResponseEntity.ok(foodService.getFoodById(id));
    }

    @Operation(
    summary = "Crear un alimento",
    description = "Registra un nuevo alimento con su categoría y unidades de medida."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Alimento creado correctamente"),
        @ApiResponse(
            responseCode = "400",
            description = "Los datos enviados no son válidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe un alimento con ese nombre",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    @PostMapping
    public ResponseEntity<Food> createFood(@Valid @RequestBody FoodRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(foodService.createFood(request));
    }

    @Operation(
    summary = "Actualizar un alimento",
    description = "Actualiza el nombre, la categoría y las unidades de medida de un alimento existente."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Alimento actualizado correctamente"),
        @ApiResponse(
            responseCode = "400",
            description = "Los datos enviados no son válidos",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )),
        @ApiResponse(
            responseCode = "404",
            description = "No existe un alimento con ese identificador",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )),
        @ApiResponse(
            responseCode = "409",
            description = "Ya existe un alimento con ese nombre",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    @PutMapping("/{id}")
    public ResponseEntity<Food> updateFood(@Valid @RequestBody FoodRequest request,@PathVariable Long id){
        return ResponseEntity.ok(foodService.updateFoods(request, id));
    }

    @Operation(
    summary = "Desactivar un alimento",
    description = "Desactiva un alimento existente sin eliminarlo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Alimento desactivado correctamente"),
        @ApiResponse(
            responseCode = "404",
            description = "No existe un alimento con ese identificador",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            ))
    })
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateFood(@PathVariable Long id){
        foodService.deactivateFood(id);
        return ResponseEntity.noContent().build();
    }
}

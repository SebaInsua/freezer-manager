package com.freezermanager.controller;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.domain.storage.MovementType;
import com.freezermanager.dto.response.MovementResponse;
import com.freezermanager.service.MovementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movements")
public class MovementController {

    private final MovementService movementService;

    @Operation(
        summary = "Consultar historial de movimientos",
        description = """
            Obtiene el historial unificado de ingresos y egresos
            del usuario autenticado. Permite filtrar por tipo,
            depósito, alimento y rango de fechas.
            Los resultados se ordenan cronológicamente de forma descendente.
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Historial obtenido correctamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parámetros de consulta inválidos"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Usuario no autenticado"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Acceso denegado"
        )
    })
    @GetMapping
    public ResponseEntity<Page<MovementResponse>> getMovements(

            @RequestParam(required = false)
            MovementType type,

            @RequestParam(required = false)
            Long storageId,

            @RequestParam(required = false)
            Long foodId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate,

            @PageableDefault(size = 10)
            Pageable pageable
    ) {
        Page<MovementResponse> response =
                movementService.getMovements(
                        type,
                        storageId,
                        foodId,
                        fromDate,
                        toDate,
                        pageable
                );

        return ResponseEntity.ok(response);
    }
}
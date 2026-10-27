
package com.freezermanager.dto;

import java.math.BigDecimal;

import com.freezermanager.domain.food.UnitOfMeasure;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StorageExitDetailRequest(
    @NotNull(message = "El alimento es obligatorio")
    Long foodId,

    @NotNull(message = "El lote es obligatorio")
    @Positive(message = "El ID del lote debe ser mayor que cero")
    Long batchId,

    @NotNull(message = "La unidad seleccionada es obligatoria")
    UnitOfMeasure selectedUnit,

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor que cero")
    BigDecimal cantidad
){}

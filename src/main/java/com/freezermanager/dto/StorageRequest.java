
package com.freezermanager.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StorageRequest(
        @Size(max = 100, message = "La marca no puede superar los 100 caracteres.")
        String marca,

        @Size(max = 100, message = "El modelo no puede superar los 100 caracteres.")
        String modelo,

        @NotNull(message = "La capacidad en litros es obligatoria.")
        @DecimalMin(value = "0.0", inclusive = false,
                message = "La capacidad debe ser mayor que cero.")
        BigDecimal capacidadLitros
) {
}

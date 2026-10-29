package com.freezermanager.dto.response;

import java.math.BigDecimal;

public record StorageResponse(
        Long id,
        String marca,
        String modelo,
        BigDecimal capacidadLitros
) {
}
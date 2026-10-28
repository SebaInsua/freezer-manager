package com.freezermanager.dto;

import java.math.BigDecimal;

public record StorageCapacityResponse(
    Long storageId,
    BigDecimal capacidadNominalLitros,
    BigDecimal porcentajeMaximoRecomendado,
    BigDecimal capacidadRecomendadaLitros,
    BigDecimal cargaActualKgEstimada,
    BigDecimal ocupacionEquivalenteLitros,
    BigDecimal porcentajeOcupacionRecomendado,
    BigDecimal capacidadRestanteRecomendadaLitros,
    boolean estimacionCompleta,
    String estado
) {}
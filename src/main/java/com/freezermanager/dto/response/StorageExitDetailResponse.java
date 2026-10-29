
package com.freezermanager.dto.response;

import java.math.BigDecimal;

import com.freezermanager.domain.food.UnitOfMeasure;

public record StorageExitDetailResponse(
        Long exitDetailId,
        FoodResponse food,
        UnitOfMeasure selectedUnit,
        BigDecimal cantidad,
        Long batchId
) {
}

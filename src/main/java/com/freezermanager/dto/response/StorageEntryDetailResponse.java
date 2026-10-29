package com.freezermanager.dto.response;

import java.math.BigDecimal;

import com.freezermanager.domain.food.UnitOfMeasure;

public record StorageEntryDetailResponse(
        Long entryDetailId,
        FoodResponse food,
        UnitOfMeasure selectedUnit,
        BigDecimal cantidad
) {
}
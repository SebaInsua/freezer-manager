package com.freezermanager.dto.response;

import java.math.BigDecimal;
import java.util.Set;

import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.food.UnitOfMeasure;

public record FoodResponse(
        Long foodId,
        String name,
        FoodCategory category,
        Set<UnitOfMeasure> unitOfMeasures,
        boolean active,
        Integer defaultShelfLifeDays,
        BigDecimal estimatedWeightPerUnitKg
) {
}
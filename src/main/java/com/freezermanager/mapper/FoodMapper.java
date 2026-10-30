
package com.freezermanager.mapper;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.freezermanager.domain.food.Food;
import com.freezermanager.dto.response.FoodResponse;

@Component
public class FoodMapper {

    public FoodResponse toResponse(Food food) {
        return new FoodResponse(
            food.getFoodId(),
            food.getName(),
            food.getCategory(),
            Set.copyOf(food.getUnitOfMeasures()),
            food.isActive(),
            food.getDefaultShelfLifeDays(),
            food.getEstimatedWeightPerUnitKg()
        );
    }
}

package com.freezermanager.dto;

import java.util.Set;

import com.freezermanager.domain.food.FoodCategory;
import com.freezermanager.domain.food.UnitOfMeasure;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FoodRequest(
    @NotBlank(message = "El nombre no puede estar vacio.")
    String name,
    @NotNull(message = "El alimento debe tener una categoria.")
    FoodCategory category,
    @NotEmpty(message = "No puede haber un alimento sin unidades de medida.")
    Set<UnitOfMeasure> unitOfMeasures,
    @Positive(message = "La vida útil predeterminada debe ser mayor que cero.")
    Integer defaultShelfLifeDays
){}

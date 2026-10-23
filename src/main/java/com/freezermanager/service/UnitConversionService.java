
package com.freezermanager.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.food.UnitOfMeasure;

@Service
public class UnitConversionService {

    private static final BigDecimal GRAMS_PER_KILOGRAM =
            new BigDecimal("1000");

    public BigDecimal toInventoryUnit(
            BigDecimal cantidad,
            UnitOfMeasure selectedUnit) {

        if (cantidad == null || selectedUnit == null) {
            throw new IllegalArgumentException(
                    "La cantidad y la unidad son obligatorias."
            );
        }

        return switch (selectedUnit) {
            case KG -> cantidad.multiply(GRAMS_PER_KILOGRAM);
            case G, UNIT -> cantidad;
        };
    }
}

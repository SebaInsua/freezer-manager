
package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import com.freezermanager.domain.food.UnitOfMeasure;

public class UnitConversionServiceTest {

    private final UnitConversionService unitConversionService =
            new UnitConversionService();

    @Test
    public void shouldConvertKilogramsToGrams() {
        BigDecimal result = unitConversionService.toInventoryUnit(
                new BigDecimal("2"),
                UnitOfMeasure.KG
        );

        assertEquals(0, result.compareTo(new BigDecimal("2000")));
    }

    @Test
    public void shouldKeepGramsUnchanged() {
        BigDecimal result = unitConversionService.toInventoryUnit(
                new BigDecimal("500"),
                UnitOfMeasure.G
        );

        assertEquals(0, result.compareTo(new BigDecimal("500")));
    }

    @Test
    public void shouldKeepUnitsUnchanged() {
        BigDecimal result = unitConversionService.toInventoryUnit(
                new BigDecimal("3"),
                UnitOfMeasure.UNIT
        );

        assertEquals(0, result.compareTo(new BigDecimal("3")));
    }

    @Test
    public void shouldRejectNullQuantity() {
        assertThrows(
                IllegalArgumentException.class,
                () -> unitConversionService.toInventoryUnit(
                        null,
                        UnitOfMeasure.KG
                )
        );
    }

    @Test
    public void shouldRejectNullUnit() {
        assertThrows(
                IllegalArgumentException.class,
                () -> unitConversionService.toInventoryUnit(
                        new BigDecimal("2"),
                        null
                )
        );
    }
}

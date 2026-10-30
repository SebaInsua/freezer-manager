
package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.dto.StorageExitDetailRequest;

class StorageExitDetailServiceTest {

    private StorageExitDetailService storageExitDetailService;

    @BeforeEach
    void setUp() {
        storageExitDetailService = new StorageExitDetailService();
    }

    @Test
    void shouldBuildExitDetailCorrectly() {
        Food food = new Food();
        StorageExit exit = new StorageExit();

        StorageExitDetailRequest request = new StorageExitDetailRequest(
                1L,
                10L,
                UnitOfMeasure.UNIT,
                new BigDecimal("2"));
        StorageExitDetail result = storageExitDetailService.buildExitDetail(food, request, exit);

        assertSame(food, result.getFood());
        assertEquals(new BigDecimal("2"), result.getCantidad());
        assertEquals(UnitOfMeasure.UNIT, result.getSelectedUnit());
        assertSame(exit, result.getStorageExit());
    }
}


package com.freezermanager.mapper;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.dto.response.FoodResponse;
import com.freezermanager.dto.response.StorageEntryDetailResponse;
import com.freezermanager.dto.response.StorageEntryResponse;
import com.freezermanager.dto.response.StorageResponse;

@Component
public class StorageEntryMapper {

    @Transactional(readOnly = true)
    public StorageEntryResponse toResponse(StorageEntry entry) {
        return new StorageEntryResponse(
                entry.getStorageEntryId(),
                entry.getMovementDateTime(),
                entry.getRecordedAt(),
                toDetailResponses(entry.getDetails()),
                toStorageResponse(entry.getStorage())
        );
    }

    @Transactional(readOnly = true)
    public List<StorageEntryResponse> toResponses(
            List<StorageEntry> entries) {
        return entries.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StorageEntryDetailResponse> toDetailResponses(
            List<StorageEntryDetail> details) {
        return details.stream()
                .map(this::toDetailResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StorageEntryDetailResponse toDetailResponse(
            StorageEntryDetail detail) {
        return new StorageEntryDetailResponse(
                detail.getEntryDetailId(),
                toFoodResponse(detail.getFood()),
                detail.getSelectedUnit(),
                detail.getCantidad()
        );
    }

    private StorageResponse toStorageResponse(Storage storage) {
        return new StorageResponse(
                storage.getId(),
                storage.getMarca(),
                storage.getModelo(),
                storage.getCapacidadLitros()
        );
    }

    private FoodResponse toFoodResponse(Food food) {
        return new FoodResponse(
                food.getFoodId(),
                food.getName(),
                food.getCategory(),
                food.getUnitOfMeasures(),
                food.isActive(),
                food.getDefaultShelfLifeDays(),
                food.getEstimatedWeightPerUnitKg()
        );
    }
}

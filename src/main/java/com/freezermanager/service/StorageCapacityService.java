package com.freezermanager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.dto.StorageCapacityResponse;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageBatchRepository;
import com.freezermanager.repository.StorageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageCapacityService {

    private static final BigDecimal RECOMMENDED_PERCENTAGE =
            new BigDecimal("0.80");

    private static final BigDecimal LITERS_PER_KILOGRAM =
            new BigDecimal("3.5");

    private static final BigDecimal GRAMS_PER_KILOGRAM =
            new BigDecimal("1000");

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final StorageRepository storageRepository;
    private final StorageBatchRepository storageBatchRepository;

    @Transactional(readOnly = true)
    public StorageCapacityResponse getCapacity(Long storageId) {
        Storage storage = storageRepository.findById(storageId)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un Freezer con el ID: " + storageId));

        BigDecimal nominalCapacity = storage.getCapacidadLitros();
        BigDecimal recommendedCapacity = nominalCapacity
                .multiply(RECOMMENDED_PERCENTAGE);

        BigDecimal estimatedLoadKg = ZERO;
        boolean completeEstimate = true;

        for (StorageBatch batch :
                storageBatchRepository.findAllByStorageIdWithDetails(storageId)) {

            if (!batch.isActive()
                    || batch.getAvailableQuantity().compareTo(ZERO) <= 0) {
                continue;
            }

            var detail = batch.getStorageEntryDetail();
            BigDecimal quantity = batch.getAvailableQuantity();

            if (detail.getSelectedUnit() == UnitOfMeasure.UNIT) {
                BigDecimal weightPerUnit =
                        detail.getFood().getEstimatedWeightPerUnitKg();

                if (weightPerUnit == null) {
                    completeEstimate = false;
                    continue;
                }

                estimatedLoadKg = estimatedLoadKg.add(
                        quantity.multiply(weightPerUnit));
            } else {
                estimatedLoadKg = estimatedLoadKg.add(
                        quantity.divide(
                                GRAMS_PER_KILOGRAM,
                                6,
                                RoundingMode.HALF_UP));
            }
        }

        BigDecimal estimatedLiters = estimatedLoadKg
                .multiply(LITERS_PER_KILOGRAM);

        BigDecimal occupancyPercentage = recommendedCapacity.compareTo(ZERO) == 0
                ? ZERO
                : estimatedLiters
                        .divide(recommendedCapacity, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"));

        BigDecimal remainingCapacity = recommendedCapacity
                .subtract(estimatedLiters)
                .max(ZERO);

        String status;

        if (estimatedLiters.compareTo(recommendedCapacity) > 0) {
            status = "LIMITE_SUPERADO";
        } else if (!completeEstimate) {
            status = "ESTIMACION_PARCIAL";
        } else {
            status = "DENTRO_DEL_LIMITE";
        }

        return new StorageCapacityResponse(
                storage.getId(),
                nominalCapacity.setScale(2, RoundingMode.HALF_UP),
                new BigDecimal("80.00"),
                recommendedCapacity.setScale(2, RoundingMode.HALF_UP),
                estimatedLoadKg.setScale(3, RoundingMode.HALF_UP),
                estimatedLiters.setScale(2, RoundingMode.HALF_UP),
                occupancyPercentage.setScale(2, RoundingMode.HALF_UP),
                remainingCapacity.setScale(2, RoundingMode.HALF_UP),
                completeEstimate,
                status
        );
    }
}
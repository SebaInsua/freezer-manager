
package com.freezermanager.mapper;

import java.util.List;
import java.util.Collections;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.dto.response.StorageExitDetailResponse;
import com.freezermanager.dto.response.StorageExitResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StorageExitMapper {

    private final FoodMapper foodMapper;
    private final StorageMapper storageMapper;

    @Transactional(readOnly = true)
    public StorageExitResponse toResponse(StorageExit exit) {
        return new StorageExitResponse(
                exit.getStorageExitId(),
                exit.getMovementDateTime(),
                exit.getRecordedAt(),
                toDetailResponses(exit.getDetails()),
                storageMapper.toResponse(exit.getStorage())
        );
    }

    @Transactional(readOnly = true)
    public List<StorageExitResponse> toResponses(
            List<StorageExit> exits) {
        return exits.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<StorageExitDetailResponse> toDetailResponses(
            List<StorageExitDetail> details) {

        if (details == null) {
            return Collections.emptyList();
        }

        return details.stream()
                .map(this::toDetailResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public StorageExitDetailResponse toDetailResponse(
            StorageExitDetail detail) {
        return new StorageExitDetailResponse(
                detail.getExitDetailId(),
                foodMapper.toResponse(detail.getFood()),
                detail.getSelectedUnit(),
                detail.getCantidad(),
                detail.getStorageBatch().getId()
        );
    }
}

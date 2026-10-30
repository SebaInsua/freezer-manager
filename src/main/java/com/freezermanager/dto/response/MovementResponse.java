package com.freezermanager.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.freezermanager.domain.storage.MovementType;

public record MovementResponse(
        Long movementId,
        MovementType type,
        LocalDateTime movementDateTime,
        LocalDateTime recordedAt,
        List<MovementDetailResponse> details,
        StorageResponse storage
) {
}
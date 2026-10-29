package com.freezermanager.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record StorageEntryResponse(
        Long storageEntryId,
        LocalDateTime movementDateTime,
        LocalDateTime recordedAt,
        List<StorageEntryDetailResponse> details,
        StorageResponse storage
) {
}
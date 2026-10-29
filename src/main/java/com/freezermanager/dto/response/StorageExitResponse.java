
package com.freezermanager.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record StorageExitResponse(
        Long storageExitId,
        LocalDateTime movementDateTime,
        LocalDateTime recordedAt,
        List<StorageExitDetailResponse> details,
        StorageResponse storage
) {
}

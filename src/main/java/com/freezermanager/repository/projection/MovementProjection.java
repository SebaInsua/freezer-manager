package com.freezermanager.repository.projection;

import java.time.LocalDateTime;

import com.freezermanager.domain.storage.MovementType;

public interface MovementProjection {

    Long getMovementId();

    MovementType getType();

    LocalDateTime getMovementDateTime();

    LocalDateTime getRecordedAt();

    Long getStorageId();
}

package com.freezermanager.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.freezermanager.domain.storage.ExpirationDateType;

public record StorageBatchResponse(
    Long batchId,
    Long storageId,
    String storageName,
    Long foodId,
    String foodName,
    BigDecimal initialQuantity,
    BigDecimal availableQuantity,
    LocalDate expirationDate,
    ExpirationDateType expirationDateType,
    boolean active
) {}

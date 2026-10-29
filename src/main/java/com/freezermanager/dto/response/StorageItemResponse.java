
package com.freezermanager.dto.response;

import java.math.BigDecimal;

public record StorageItemResponse(
        Long id,
        StorageResponse storage,
        FoodResponse food,
        BigDecimal cantidad
) {
}

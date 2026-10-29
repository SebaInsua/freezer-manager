
package com.freezermanager.mapper;

import org.springframework.stereotype.Component;

import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.dto.response.StorageItemResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class StorageItemMapper {

    private final StorageMapper storageMapper;
    private final FoodMapper foodMapper;

    public StorageItemResponse toResponse(StorageItem item) {
        return new StorageItemResponse(
                item.getId(),
                storageMapper.toResponse(item.getStorage()),
                foodMapper.toResponse(item.getFood()),
                item.getCantidad()
        );
    }
}

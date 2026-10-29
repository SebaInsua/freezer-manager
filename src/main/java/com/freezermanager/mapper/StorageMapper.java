
package com.freezermanager.mapper;

import org.springframework.stereotype.Component;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.dto.response.StorageResponse;

@Component
public class StorageMapper {

    public StorageResponse toResponse(Storage storage) {
        return new StorageResponse(
                storage.getId(),
                storage.getMarca(),
                storage.getModelo(),
                storage.getCapacidadLitros()
        );
    }
}

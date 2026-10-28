
package com.freezermanager.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.storage.StorageBatch;
import com.freezermanager.dto.StorageBatchResponse;
import com.freezermanager.repository.StorageBatchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageBatchService {

    private final StorageBatchRepository storageBatchRepository;

    private String getAuthenticatedEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay un usuario autenticado.");
        }

        return authentication.getName();
    }

    @Transactional(readOnly = true)
    public List<StorageBatchResponse> getAllBatches() {
        String email = getAuthenticatedEmail();

        return storageBatchRepository.findAllWithDetailsByOwnerEmail(email)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private StorageBatchResponse toResponse(StorageBatch batch) {
        var detail = batch.getStorageEntryDetail();
        var entry = detail.getStorageEntry();
        var storage = entry.getStorage();
        var food = detail.getFood();

        String storageName = String.join(
                " ",
                storage.getMarca() == null ? "" : storage.getMarca(),
                storage.getModelo() == null ? "" : storage.getModelo()
        ).trim();

        return new StorageBatchResponse(
                batch.getId(),
                storage.getId(),
                storageName,
                food.getFoodId(),
                food.getName(),
                batch.getInitialQuantity(),
                batch.getAvailableQuantity(),
                batch.getExpirationDate(),
                batch.getExpirationDateType(),
                batch.isActive()
        );
    }
}

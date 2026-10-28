
package com.freezermanager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageItemRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageItemService {

    private final StorageItemRepository storageItemRepository;

    public Page<StorageItem> getStorageItems(Pageable pageable) {
        String email = getAuthenticatedEmail();

        return storageItemRepository
                .findAllByStorage_Owner_EmailIgnoreCase(email, pageable);
    }

    public StorageItem getStorageItem(Long id) {
        String email = getAuthenticatedEmail();

        return storageItemRepository
                .findByIdAndStorage_Owner_EmailIgnoreCase(id, email)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un elemento en el inventario con el ID: " + id
                ));
    }

    private String getAuthenticatedEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay un usuario autenticado.");
        }

        return authentication.getName();
    }
}

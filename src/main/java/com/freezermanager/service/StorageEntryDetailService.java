package com.freezermanager.service;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.dto.StorageEntryDetailRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageEntryDetailRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageEntryDetailService {
    private final StorageEntryDetailRepository storageEntryDetailRepository;

    private String getAuthenticatedEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No hay un usuario autenticado.");
        }

        return authentication.getName();
    }

    public StorageEntryDetail buildEntryDetail(Food food,StorageEntryDetailRequest detail, StorageEntry entry){
            StorageEntryDetail detalleAIngresar = new StorageEntryDetail();
            detalleAIngresar.setFood(food);
            detalleAIngresar.setSelectedUnit(detail.selectedUnit());
            detalleAIngresar.setCantidad(detail.cantidad());
            detalleAIngresar.setStorageEntry(entry);
            return detalleAIngresar;
    }

    public Page<StorageEntryDetail> getAllStorageEntriesDetails(
            Pageable pageable) {

        String email = getAuthenticatedEmail();

        return storageEntryDetailRepository
                .findAllByStorageEntry_Storage_Owner_EmailIgnoreCase(
                        email,
                        pageable
                );
    }

    public StorageEntryDetail getStorageEntryDetailById(Long id) {
        String email = getAuthenticatedEmail();

        return storageEntryDetailRepository
                .findByEntryDetailIdAndStorageEntry_Storage_Owner_EmailIgnoreCase(
                        id,
                        email
                )
                .orElseThrow(() -> new NotFoundException(
                        "No existe un detalle de ingreso con el ID: " + id
                ));
    }
}

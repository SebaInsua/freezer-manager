
package com.freezermanager.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.user.User;
import com.freezermanager.dto.StorageRequest;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final StorageRepository storageRepository;
    private final UserRepository userRepository;

    public Page<Storage> getStorages(Pageable pageable) {
        String email = getAuthenticatedEmail();
        return storageRepository.findAllByOwner_EmailIgnoreCase(email, pageable);
    }

    public Storage getStorageById(Long id) {
        String email = getAuthenticatedEmail();

        return storageRepository.findByIdAndOwner_EmailIgnoreCase(id, email)
                .orElseThrow(() -> new NotFoundException(
                        "No existe un Freezer con el ID: " + id
                ));
    }

    private String getAuthenticatedEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "No hay un usuario autenticado."
            );
        }

        return authentication.getName();
    }

    @Transactional
    public Storage createStorage(StorageRequest request) {
        String email = getAuthenticatedEmail();

        User owner = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException(
                        "No existe el usuario autenticado."
                ));

        Storage storage = new Storage();
        storage.setMarca(request.marca());
        storage.setModelo(request.modelo());
        storage.setCapacidadLitros(request.capacidadLitros());
        storage.setOwner(owner);

        return storageRepository.save(storage);
    }

}

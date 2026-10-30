
package com.freezermanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.freezermanager.domain.storage.StorageExit;

public interface StorageExitRepository
        extends JpaRepository<StorageExit, Long> {

    List<StorageExit> findAllByStorageId(Long storageId);

    Page<StorageExit> findAllByStorage_Owner_EmailIgnoreCase(
            String email,
            Pageable pageable);

    Optional<StorageExit> findByStorageExitIdAndStorage_Owner_EmailIgnoreCase(
            Long id,
            String email);

    boolean existsByStorageExitIdAndStorage_Owner_EmailIgnoreCase(
            Long id,
            String email);
}

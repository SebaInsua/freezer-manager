
package com.freezermanager.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.freezermanager.domain.storage.StorageEntry;

public interface StorageEntryRepository
        extends JpaRepository<StorageEntry, Long> {

    Page<StorageEntry> findAllByStorage_Owner_EmailIgnoreCase(
        String email,
        Pageable pageable);

    Optional<StorageEntry> findByStorageEntryIdAndStorage_Owner_EmailIgnoreCase(
        Long id,
        String email);

    boolean existsByStorageEntryIdAndStorage_Owner_EmailIgnoreCase(
        Long id,
        String email);
}

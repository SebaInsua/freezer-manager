
package com.freezermanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.freezermanager.domain.storage.Storage;

import jakarta.persistence.LockModeType;

public interface StorageRepository extends JpaRepository<Storage, Long> {

    Page<Storage> findAllByOwner_EmailIgnoreCase(
            String email,
            Pageable pageable);

    Optional<Storage> findByIdAndOwner_EmailIgnoreCase(
            Long id,
            String email);

    List<Storage> findAllByIdInAndOwner_EmailIgnoreCase(
            List<Long> ids,
            String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Storage s WHERE s.id = :storageId")
    Optional<Storage> findByIdForUpdate(
            @Param("storageId") Long storageId);
}

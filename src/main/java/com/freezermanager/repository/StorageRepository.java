
package com.freezermanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.freezermanager.domain.storage.Storage;

import jakarta.persistence.LockModeType;

public interface StorageRepository extends JpaRepository<Storage, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Storage s WHERE s.id = :storageId")
    java.util.Optional<Storage> findByIdForUpdate(
        @Param("storageId") Long storageId
    );
}


package com.freezermanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import com.freezermanager.domain.storage.StorageBatch;

public interface StorageBatchRepository extends JpaRepository<StorageBatch, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM StorageBatch b WHERE b.id = :batchId")
    Optional<StorageBatch> findByIdForUpdate(@Param("batchId") Long batchId);
}

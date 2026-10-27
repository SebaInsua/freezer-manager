
package com.freezermanager.repository;

import java.util.List;
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

    @Query("""
    SELECT b FROM StorageBatch b
    JOIN FETCH b.storageEntryDetail detail
    JOIN FETCH detail.food food
    JOIN FETCH detail.storageEntry entry
    JOIN FETCH entry.storage storage
    ORDER BY b.id ASC
    """)
    List<StorageBatch> findAllWithDetails();
}

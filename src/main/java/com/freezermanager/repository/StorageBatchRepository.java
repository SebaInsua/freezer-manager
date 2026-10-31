
package com.freezermanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

    @Query("""
            SELECT b FROM StorageBatch b
            JOIN FETCH b.storageEntryDetail detail
            JOIN FETCH detail.food food
            JOIN FETCH detail.storageEntry entry
            WHERE entry.storage.id = :storageId
            ORDER BY b.id ASC
            """)
    List<StorageBatch> findAllByStorageIdWithDetails(
            @Param("storageId") Long storageId);

    boolean existsByIdAndStorageEntryDetail_StorageEntry_Storage_Owner_EmailIgnoreCase(
            Long id,
            String email);

    @Query(
            value = """
                    SELECT b FROM StorageBatch b
                    JOIN FETCH b.storageEntryDetail detail
                    JOIN FETCH detail.food food
                    JOIN FETCH detail.storageEntry entry
                    JOIN FETCH entry.storage storage
                    JOIN FETCH storage.owner owner
                    WHERE LOWER(owner.email) = LOWER(:email)
                    ORDER BY b.id ASC
                    """,
            countQuery = """
                    SELECT COUNT(b) FROM StorageBatch b
                    JOIN b.storageEntryDetail detail
                    JOIN detail.storageEntry entry
                    JOIN entry.storage storage
                    JOIN storage.owner owner
                    WHERE LOWER(owner.email) = LOWER(:email)
                    """
    )
    Page<StorageBatch> findAllWithDetailsByOwnerEmail(
            @Param("email") String email,
            Pageable pageable);

    @Query("""
            SELECT b FROM StorageBatch b
            JOIN FETCH b.storageEntryDetail detail
            JOIN FETCH detail.food food
            JOIN FETCH detail.storageEntry entry
            WHERE entry.storage.id = :storageId
            AND b.active = true
            AND b.availableQuantity > 0
            ORDER BY b.id ASC
            """)
    List<StorageBatch> findActiveBatchesByStorageIdWithDetails(
            @Param("storageId") Long storageId);
}

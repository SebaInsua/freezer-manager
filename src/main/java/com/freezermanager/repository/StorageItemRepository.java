package com.freezermanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import com.freezermanager.domain.storage.StorageItem;

import jakarta.persistence.LockModeType;

import java.util.Optional;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

public interface StorageItemRepository extends JpaRepository<StorageItem,Long>{
    @Query("""
        SELECT si FROM StorageItem si
        WHERE si.storage.id = :storageId
        AND si.food.foodId = :foodId
        """)
        Optional<StorageItem> findByStorageIdAndFoodId(
            @Param("storageId") Long storageId,
            @Param("foodId") Long foodId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT si FROM StorageItem si
        WHERE si.storage.id = :storageId
        AND si.food.foodId = :foodId
        """)
        Optional<StorageItem> findByStorageIdAndFoodIdForUpdate(
            @Param("storageId") Long storageId,
            @Param("foodId") Long foodId
    );
    Page<StorageItem> findAllByStorage_Owner_EmailIgnoreCase(
            String email,
            Pageable pageable
    );

    Optional<StorageItem> findByIdAndStorage_Owner_EmailIgnoreCase(
            Long id,
            String email
    );
}

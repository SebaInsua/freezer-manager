package com.freezermanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freezermanager.domain.storage.StorageItem;

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
}

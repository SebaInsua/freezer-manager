package com.freezermanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.freezermanager.domain.storage.StorageExitDetail;

public interface StorageExitDetailRepository extends JpaRepository<StorageExitDetail,Long>{
        @Query("""
        SELECT s FROM StorageExitDetail s
        JOIN FETCH s.storageExit d
        WHERE d.storageExitId = :id
        """)
    List<StorageExitDetail> findDetailsByStorageExitId(@Param("id") Long id);
}

package com.freezermanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.freezermanager.domain.storage.StorageEntryDetail;

public interface StorageEntryDetailRepository extends JpaRepository<StorageEntryDetail,Long>{
    @Query("""
        SELECT s FROM StorageEntryDetail s
        JOIN FETCH s.storageEntry d
        WHERE d.storageEntryId = :id
        """)
    List<StorageEntryDetail> findDetailsByStorageEntryId(@Param("id") Long id);
}


package com.freezermanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.freezermanager.domain.storage.StorageExitDetail;

public interface StorageExitDetailRepository
        extends JpaRepository<StorageExitDetail, Long> {

    @Query("""
        SELECT s FROM StorageExitDetail s
        JOIN FETCH s.storageExit d
        WHERE d.storageExitId = :id
        """)
    List<StorageExitDetail> findDetailsByStorageExitId(
            @Param("id") Long id
    );

    @Query("""
        SELECT s FROM StorageExitDetail s
        JOIN FETCH s.storageExit d
        JOIN d.storage st
        JOIN st.owner u
        WHERE d.storageExitId = :id
          AND LOWER(u.email) = LOWER(:email)
        """)
    List<StorageExitDetail> findDetailsByExitIdAndOwnerEmail(
            @Param("id") Long id,
            @Param("email") String email
    );
}

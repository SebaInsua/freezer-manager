
package com.freezermanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.freezermanager.domain.storage.StorageEntryDetail;

public interface StorageEntryDetailRepository
        extends JpaRepository<StorageEntryDetail, Long> {

    @Query("""
            SELECT s FROM StorageEntryDetail s
            JOIN FETCH s.storageEntry d
            WHERE d.storageEntryId = :id
            """)
    List<StorageEntryDetail> findDetailsByStorageEntryId(
            @Param("id") Long id);

    @Query("""
            SELECT s FROM StorageEntryDetail s
            JOIN FETCH s.storageEntry d
            JOIN d.storage st
            JOIN st.owner u
            WHERE d.storageEntryId = :id
            AND LOWER(u.email) = LOWER(:email)
            """)
    List<StorageEntryDetail> findDetailsByEntryIdAndOwnerEmail(
            @Param("id") Long id,
            @Param("email") String email);

    Page<StorageEntryDetail> findAllByStorageEntry_Storage_Owner_EmailIgnoreCase(
            String email,
            Pageable pageable);

    Optional<StorageEntryDetail> findByEntryDetailIdAndStorageEntry_Storage_Owner_EmailIgnoreCase(
            Long id,
            String email);

    @Query("""
            SELECT d FROM StorageEntryDetail d
            JOIN FETCH d.food
            JOIN FETCH d.storageEntry e
            JOIN FETCH e.storage
            WHERE e.storageEntryId IN :ids
            AND LOWER(e.storage.owner.email) = LOWER(:email)
            """)
    List<StorageEntryDetail> findAllByEntryIdsAndOwnerEmail(
            @Param("ids") List<Long> ids,
            @Param("email") String email);
}

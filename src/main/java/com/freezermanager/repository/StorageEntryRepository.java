package com.freezermanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freezermanager.domain.storage.StorageEntry;

public interface StorageEntryRepository extends JpaRepository<StorageEntry,Long>{
    
}

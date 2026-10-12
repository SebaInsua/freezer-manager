package com.freezermanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freezermanager.domain.storage.StorageItem;

public interface StorageItemRepository extends JpaRepository<StorageItem,Long>{
    
}

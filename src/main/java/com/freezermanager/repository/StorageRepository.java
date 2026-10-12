package com.freezermanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freezermanager.domain.storage.Storage;

public interface StorageRepository extends JpaRepository<Storage,Long>{
    
}

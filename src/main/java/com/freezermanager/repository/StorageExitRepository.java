package com.freezermanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freezermanager.domain.storage.StorageExit;

public interface StorageExitRepository extends JpaRepository<StorageExit,Long>{
    List<StorageExit> findAllByStorageId(Long storageId);
}

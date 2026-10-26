package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {
    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private StorageService storageService;


    @Test
    public void shouldFindAllStorages() {
        Storage freezer = new Storage();
        freezer.setId(1L);
        freezer.setMarca("Philco");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Storage> expectedPage = new PageImpl<>(List.of(freezer), pageable, 1);

        when(storageRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<Storage> result = storageService.getStorages(pageable);

        assertEquals(expectedPage, result);
        assertEquals(1, result.getTotalElements());
        verify(storageRepository).findAll(pageable);
    }


    @Test
    public void shouldFindStorageById(){
        Storage expectedStorage = new Storage();
        expectedStorage.setId(1L);
        expectedStorage.setMarca("Philco");
        when(storageRepository.findById(expectedStorage.getId())).thenReturn(Optional.of(expectedStorage));
        Storage result = storageService.getStorageById(expectedStorage.getId());
        assertEquals(expectedStorage, result);
        verify(storageRepository).findById(expectedStorage.getId());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenStorageDoesNotExist(){
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> storageService.getStorageById(99L));
        verify(storageRepository).findById(99L);
    }
}

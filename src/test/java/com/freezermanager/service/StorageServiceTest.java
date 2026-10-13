package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
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

@ExtendWith(MockitoExtension.class)
public class StorageServiceTest {
    @Mock
    private StorageRepository storageRepository;

    @InjectMocks
    private StorageService storageService;

    @Test
    public void shouldFindAllStorages(){
        List<Storage> expectedStorage = new ArrayList<>();
        Storage freezer = new Storage();
        freezer.setId(1L);
        freezer.setMarca("Philco");
        expectedStorage.add(freezer);
        when(storageRepository.findAll()).thenReturn(expectedStorage);

        List<Storage> result = storageService.getStorages();
        assertEquals(expectedStorage, result);
        verify(storageRepository).findAll();
    }

    @Test
    public void shouldFindStorageById(){
        Storage expectedStorage = new Storage();
        expectedStorage.setId(1L);
        expectedStorage.setMarca("Philco");
        when(storageRepository.findById(expectedStorage.getId())).thenReturn(Optional.of(expectedStorage));
        Storage result = storageService.getStorage(expectedStorage.getId());
        assertEquals(expectedStorage, result);
        verify(storageRepository).findById(expectedStorage.getId());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenStorageDoesNotExist(){
        when(storageRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> storageService.getStorage(99L));
        verify(storageRepository).findById(99L);
    }
}

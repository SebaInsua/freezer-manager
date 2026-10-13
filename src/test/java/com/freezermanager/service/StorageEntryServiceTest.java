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

import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageEntryRepository;

@ExtendWith(MockitoExtension.class)
public class StorageEntryServiceTest {
    @Mock
    private StorageEntryRepository storageEntryRepository;

    @InjectMocks
    private StorageEntryService storageEntryService;

    @Test
    public void shouldFindAllEntries(){
        List<StorageEntry> expectedRegister = new ArrayList<>();
        StorageEntry register = new StorageEntry();
        register.setStorageEntryId(1L);
        expectedRegister.add(register);
        when(storageEntryRepository.findAll()).thenReturn(expectedRegister);
        List<StorageEntry> result = storageEntryService.getAllStorageEntries();
        assertEquals(expectedRegister, result);
        verify(storageEntryRepository).findAll();
    }

    @Test
    public void shouldFindEntryById(){
        StorageEntry expectedRegister = new StorageEntry();
        expectedRegister.setStorageEntryId(1L);
        when(storageEntryRepository.findById(1L)).thenReturn(Optional.of(expectedRegister));
        StorageEntry result = storageEntryService.getStorageEntryById(expectedRegister.getStorageEntryId());
        assertEquals(expectedRegister, result);
        verify(storageEntryRepository).findById(expectedRegister.getStorageEntryId());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenEntryDoesNotExist(){
        when(storageEntryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> storageEntryService.getStorageEntryById(99L));
        verify(storageEntryRepository).findById(99L);
    }
}

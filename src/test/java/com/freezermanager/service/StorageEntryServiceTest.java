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

import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageEntryRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class StorageEntryServiceTest {
    @Mock
    private StorageEntryRepository storageEntryRepository;

    @InjectMocks
    private StorageEntryService storageEntryService;

    @Test
    public void shouldFindAllEntries() {
        StorageEntry register = new StorageEntry();
        register.setStorageEntryId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StorageEntry> expectedRegister = new PageImpl<>(List.of(register));

        when(storageEntryRepository.findAll(pageable)).thenReturn(expectedRegister);

        Page<StorageEntry> result = storageEntryService.getAllStorageEntries(pageable);

        assertEquals(expectedRegister, result);
        verify(storageEntryRepository).findAll(pageable);
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

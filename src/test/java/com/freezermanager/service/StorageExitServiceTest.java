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

import com.freezermanager.domain.storage.StorageExit;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageExitRepository;

@ExtendWith(MockitoExtension.class)
public class StorageExitServiceTest {
    @Mock
    private StorageExitRepository storageExitRepository;

    @InjectMocks
    private StorageExitService storageExitService;

    @Test
    public void shouldFindAllExtractions(){
        List<StorageExit> expectedExtraction = new ArrayList<>();
        StorageExit extraction = new StorageExit();
        extraction.setStorageExitId(1L);
        expectedExtraction.add(extraction);
        when(storageExitRepository.findAll()).thenReturn(expectedExtraction);
        List<StorageExit> result = storageExitService.getAllStorageExits();
        assertEquals(expectedExtraction, result);
        verify(storageExitRepository).findAll();
    }

    @Test
    public void shouldFindExtractionById(){
        StorageExit expectedExtraction = new StorageExit();
        expectedExtraction.setStorageExitId(1L);
        when(storageExitRepository.findById(1L)).thenReturn(Optional.of(expectedExtraction));
        StorageExit result = storageExitService.getStorageExitById(expectedExtraction.getStorageExitId());
        assertEquals(expectedExtraction, result);
        verify(storageExitRepository).findById(expectedExtraction.getStorageExitId());
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenExtractionDoesNotExist(){
        when(storageExitRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> storageExitService.getStorageExitById(99L));
        verify(storageExitRepository).findById(99L);
    }
}


package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class StorageServiceTest {

    private static final String AUTHENTICATED_EMAIL = "usuario@test.com";

    @Mock
    private StorageRepository storageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        AUTHENTICATED_EMAIL,
                        null,
                        List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldFindAllStorages() {
        Storage freezer = new Storage();
        freezer.setId(1L);
        freezer.setMarca("Philco");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Storage> expectedPage = new PageImpl<>(List.of(freezer), pageable, 1);

        when(storageRepository.findAllByOwner_EmailIgnoreCase(
                AUTHENTICATED_EMAIL, pageable)).thenReturn(expectedPage);

        Page<Storage> result = storageService.getStorages(pageable);

        assertEquals(expectedPage, result);
        assertEquals(1, result.getTotalElements());

        verify(storageRepository).findAllByOwner_EmailIgnoreCase(
                AUTHENTICATED_EMAIL, pageable);
        verifyNoMoreInteractions(storageRepository);
    }

    @Test
    void shouldFindStorageById() {
        Storage expectedStorage = new Storage();
        expectedStorage.setId(1L);
        expectedStorage.setMarca("Philco");

        when(storageRepository.findByIdAndOwner_EmailIgnoreCase(
                1L, AUTHENTICATED_EMAIL)).thenReturn(Optional.of(expectedStorage));

        Storage result = storageService.getStorageById(1L);

        assertEquals(expectedStorage, result);

        verify(storageRepository).findByIdAndOwner_EmailIgnoreCase(
                1L, AUTHENTICATED_EMAIL);
        verifyNoMoreInteractions(storageRepository);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenStorageDoesNotExist() {
        when(storageRepository.findByIdAndOwner_EmailIgnoreCase(
                99L, AUTHENTICATED_EMAIL)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> storageService.getStorageById(99L));

        verify(storageRepository).findByIdAndOwner_EmailIgnoreCase(
                99L, AUTHENTICATED_EMAIL);
        verifyNoMoreInteractions(storageRepository);
    }
}

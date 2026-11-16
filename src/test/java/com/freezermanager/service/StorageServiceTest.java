
package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.freezermanager.domain.storage.Storage;
import com.freezermanager.domain.user.User;
import com.freezermanager.dto.StorageRequest;
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

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private StorageService storageService;

    @BeforeEach
    void setUp() {
        when(currentUserProvider.getCurrentUsername())
                .thenReturn(AUTHENTICATED_EMAIL);
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

    @Test
    void shouldReturnEmptyPage(){
        Pageable pageable = PageRequest.of(0, 10);
        Page<Storage> expectedPage = new PageImpl<>(List.of(), pageable, 0);

        when(storageRepository.findAllByOwner_EmailIgnoreCase(
            AUTHENTICATED_EMAIL,pageable)).thenReturn(expectedPage);

        Page<Storage> result = storageService.getStorages(pageable);

        assertEquals(expectedPage, result);
        assertEquals(0, result.getTotalElements());

        verify(storageRepository).findAllByOwner_EmailIgnoreCase(
                AUTHENTICATED_EMAIL, pageable);
        verifyNoMoreInteractions(storageRepository);
    }

    @Test
    void shouldCreateStorage(){
        User owner = new User();
        owner.setEmail(AUTHENTICATED_EMAIL);

        Storage storageExpected = new Storage();
        storageExpected.setCapacidadLitros(new BigDecimal(150));
        storageExpected.setMarca("Philco");
        storageExpected.setModelo("PH150");
        storageExpected.setOwner(owner);

        StorageRequest request = new StorageRequest(storageExpected.getMarca(),storageExpected.getModelo(),storageExpected.getCapacidadLitros());

        when(userRepository.findByEmailIgnoreCase(AUTHENTICATED_EMAIL)).thenReturn(Optional.of(owner));
        
        when(storageRepository.save(
                org.mockito.ArgumentMatchers.any(Storage.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Storage result = storageService.createStorage(request);

        ArgumentCaptor<Storage> captor = ArgumentCaptor.forClass(Storage.class);
        verify(storageRepository).save(captor.capture());
        Storage capturedStorage = captor.getValue();

        assertEquals("Philco", capturedStorage.getMarca());
        assertEquals("PH150", capturedStorage.getModelo());
        assertEquals(new BigDecimal(150), capturedStorage.getCapacidadLitros());
        assertEquals(owner, capturedStorage.getOwner());

        assertInstanceOf(Storage.class,result);
    }

    @Test
    void shouldThrowNotFoundErrorWhenUserDoesntExist(){
        StorageRequest request = new StorageRequest("Philco","PH150",new BigDecimal(150));
        String messageExpected = "No existe el usuario autenticado.";

        when(userRepository.findByEmailIgnoreCase(AUTHENTICATED_EMAIL)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(NotFoundException.class, () -> storageService.createStorage(request));

        assertEquals(messageExpected, exception.getMessage());

        verify(userRepository).findByEmailIgnoreCase(AUTHENTICATED_EMAIL);
        verify(storageRepository,never()).save(org.mockito.ArgumentMatchers.any(Storage.class));
    }
}

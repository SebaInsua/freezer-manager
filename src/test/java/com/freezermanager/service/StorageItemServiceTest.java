
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

import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageItemRepository;

@ExtendWith(MockitoExtension.class)
class StorageItemServiceTest {

    private static final String AUTHENTICATED_EMAIL = "usuario@test.com";

    @Mock
    private StorageItemRepository storageItemRepository;

    @InjectMocks
    private StorageItemService storageItemService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        AUTHENTICATED_EMAIL,
                        null,
                        List.of()
                )
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldFindAllItems() {
        StorageItem item = new StorageItem();
        item.setId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StorageItem> expectedItems = new PageImpl<>(List.of(item));

        when(storageItemRepository.findAllByStorage_Owner_EmailIgnoreCase(
                AUTHENTICATED_EMAIL, pageable
        )).thenReturn(expectedItems);

        Page<StorageItem> result = storageItemService.getStorageItems(pageable);

        assertEquals(expectedItems, result);
        verify(storageItemRepository).findAllByStorage_Owner_EmailIgnoreCase(
                AUTHENTICATED_EMAIL, pageable
        );
        verifyNoMoreInteractions(storageItemRepository);
    }

    @Test
    void shouldFindItemById() {
        StorageItem expectedItem = new StorageItem();
        expectedItem.setId(1L);

        when(storageItemRepository.findByIdAndStorage_Owner_EmailIgnoreCase(
                1L, AUTHENTICATED_EMAIL
        )).thenReturn(Optional.of(expectedItem));

        StorageItem result = storageItemService.getStorageItem(1L);

        assertEquals(expectedItem, result);
        verify(storageItemRepository).findByIdAndStorage_Owner_EmailIgnoreCase(
                1L, AUTHENTICATED_EMAIL
        );
        verifyNoMoreInteractions(storageItemRepository);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenItemDoesNotExist() {
        when(storageItemRepository.findByIdAndStorage_Owner_EmailIgnoreCase(
                99L, AUTHENTICATED_EMAIL
        )).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> storageItemService.getStorageItem(99L)
        );

        verify(storageItemRepository).findByIdAndStorage_Owner_EmailIgnoreCase(
                99L, AUTHENTICATED_EMAIL
        );
        verifyNoMoreInteractions(storageItemRepository);
    }
}

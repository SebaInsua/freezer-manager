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

import com.freezermanager.domain.storage.StorageItem;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageItemRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@ExtendWith(MockitoExtension.class)
public class StorageItemServiceTest {
    @Mock
    private StorageItemRepository storageItemRepository;

    @InjectMocks
    private StorageItemService storageItemService;


        @Test
    public void shouldFindAllItems() {
        StorageItem item = new StorageItem();
        item.setId(1L);

        Pageable pageable = PageRequest.of(0, 10);
        Page<StorageItem> expectedItems = new PageImpl<>(List.of(item));

        when(storageItemRepository.findAll(pageable)).thenReturn(expectedItems);

        Page<StorageItem> result = storageItemService.getStorageItems(pageable);

        assertEquals(expectedItems, result);
        verify(storageItemRepository).findAll(pageable);
    }


        @Test
    public void shouldFindItemById(){
        StorageItem expectedItem = new StorageItem();
        expectedItem.setId(1L);
        when(storageItemRepository.findById(1L)).thenReturn(Optional.of(expectedItem));
        StorageItem result = storageItemService.getStorageItem(expectedItem.getId());
        assertEquals(expectedItem, result);
        verify(storageItemRepository).findById(expectedItem.getId());
    }

        @Test
    public void shouldThrowNotFoundExceptionWhenItemDoesNotExist(){
        when(storageItemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> storageItemService.getStorageItem(99L));
        verify(storageItemRepository).findById(99L);
    }
}

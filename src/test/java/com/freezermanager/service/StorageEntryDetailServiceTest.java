package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.repository.StorageEntryDetailRepository;

@ExtendWith(MockitoExtension.class)
class StorageEntryDetailServiceTest {

    @Mock
    private StorageEntryDetailRepository storageEntryDetailRepository;

    @InjectMocks
    private StorageEntryDetailService storageEntryDetailService;

    @Test
    void shouldFindAllStorageEntryDetails() {
        StorageEntryDetail detail = new StorageEntryDetail();
        detail.setEntryDetailId(1L);
        detail.setCantidad(new BigDecimal("2"));

        Pageable pageable = PageRequest.of(0, 10);
        Page<StorageEntryDetail> expectedPage =
                new PageImpl<>(List.of(detail), pageable, 1);

        when(storageEntryDetailRepository.findAll(pageable))
                .thenReturn(expectedPage);

        Page<StorageEntryDetail> result =
                storageEntryDetailService.getAllStorageEntriesDetails(pageable);

        assertEquals(expectedPage, result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(storageEntryDetailRepository).findAll(pageable);
    }
}
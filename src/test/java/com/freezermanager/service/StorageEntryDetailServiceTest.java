
package com.freezermanager.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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

import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.StorageEntryDetailRepository;

@ExtendWith(MockitoExtension.class)
class StorageEntryDetailServiceTest {

    private static final String AUTHENTICATED_EMAIL = "usuario@test.com";

    @Mock
    private StorageEntryDetailRepository storageEntryDetailRepository;

    @InjectMocks
    private StorageEntryDetailService storageEntryDetailService;

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
    void shouldFindAllStorageEntryDetails() {
        StorageEntryDetail detail = new StorageEntryDetail();
        detail.setEntryDetailId(1L);
        detail.setCantidad(new BigDecimal("2"));

        Pageable pageable = PageRequest.of(0, 10);

        Page<StorageEntryDetail> expectedPage = new PageImpl<>(List.of(detail), pageable, 1);

        when(storageEntryDetailRepository
                .findAllByStorageEntry_Storage_Owner_EmailIgnoreCase(
                        AUTHENTICATED_EMAIL,
                        pageable))
                .thenReturn(expectedPage);

        Page<StorageEntryDetail> result = storageEntryDetailService.getAllStorageEntriesDetails(
                pageable);

        assertEquals(expectedPage, result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        verify(storageEntryDetailRepository)
                .findAllByStorageEntry_Storage_Owner_EmailIgnoreCase(
                        AUTHENTICATED_EMAIL,
                        pageable);
    }

    @Test
    void shouldFindStorageEntryDetailById() {
        StorageEntryDetail expectedDetail = new StorageEntryDetail();
        expectedDetail.setEntryDetailId(1L);
        expectedDetail.setCantidad(new BigDecimal("2"));

        when(storageEntryDetailRepository
                .findByEntryDetailIdAndStorageEntry_Storage_Owner_EmailIgnoreCase(
                        1L,
                        AUTHENTICATED_EMAIL))
                .thenReturn(Optional.of(expectedDetail));

        StorageEntryDetail result = storageEntryDetailService.getStorageEntryDetailById(1L);

        assertEquals(expectedDetail, result);

        verify(storageEntryDetailRepository)
                .findByEntryDetailIdAndStorageEntry_Storage_Owner_EmailIgnoreCase(
                        1L,
                        AUTHENTICATED_EMAIL);
    }

    @Test
    void shouldThrowNotFoundExceptionWhenDetailDoesNotExist() {
        when(storageEntryDetailRepository
                .findByEntryDetailIdAndStorageEntry_Storage_Owner_EmailIgnoreCase(
                        99L,
                        AUTHENTICATED_EMAIL))
                .thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(
                NotFoundException.class,
                () -> storageEntryDetailService.getStorageEntryDetailById(99L));

        verify(storageEntryDetailRepository)
                .findByEntryDetailIdAndStorageEntry_Storage_Owner_EmailIgnoreCase(
                        99L,
                        AUTHENTICATED_EMAIL);
    }
}

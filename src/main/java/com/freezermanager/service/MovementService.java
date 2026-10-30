package com.freezermanager.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.freezermanager.domain.storage.MovementType;
import com.freezermanager.domain.storage.StorageEntryDetail;
import com.freezermanager.domain.storage.StorageExitDetail;
import com.freezermanager.dto.response.MovementDetailResponse;
import com.freezermanager.dto.response.MovementResponse;
import com.freezermanager.mapper.FoodMapper;
import com.freezermanager.mapper.StorageMapper;
import com.freezermanager.repository.MovementRepository;
import com.freezermanager.repository.StorageEntryDetailRepository;
import com.freezermanager.repository.StorageExitDetailRepository;
import com.freezermanager.repository.StorageRepository;
import com.freezermanager.repository.projection.MovementProjection;
import com.freezermanager.dto.response.StorageResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MovementService {

    private final MovementRepository movementRepository;
    private final StorageEntryDetailRepository entryDetailRepository;
    private final StorageExitDetailRepository exitDetailRepository;
    private final StorageRepository storageRepository;
    private final FoodMapper foodMapper;
    private final StorageMapper storageMapper;

    private String getAuthenticatedEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException(
                    "No hay un usuario autenticado."
            );
        }

        return authentication.getName();
    }

    @Transactional(readOnly = true)
    public Page<MovementResponse> getMovements(
            MovementType type,
            Long storageId,
            Long foodId,
            java.time.LocalDateTime fromDate,
            java.time.LocalDateTime toDate,
            Pageable pageable
    ) {
        if (fromDate != null && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "La fecha inicial no puede ser posterior a la fecha final."
            );
        }

        String email = getAuthenticatedEmail();

        Pageable pagination = PageRequest.of(
        pageable.getPageNumber(),
        pageable.getPageSize()
        );

        Page<MovementProjection> movements =
                movementRepository.findMovements(
                        email,
                        type != null ? type.name() : null,
                        storageId,
                        foodId,
                        fromDate,
                        toDate,
                        pagination
                );

        if (movements.isEmpty()) {
            return Page.empty(pagination);
        }

        List<Long> entryIds = movements.getContent().stream()
                .filter(m -> m.getType() == MovementType.ENTRY)
                .map(movement -> movement.getMovementId())
                .toList();

        List<Long> exitIds = movements.getContent().stream()
                .filter(m -> m.getType() == MovementType.EXIT)
                .map(movement -> movement.getMovementId())
                .toList();

        Map<Long, List<StorageEntryDetail>> entryDetails =
                entryIds.isEmpty()
                        ? Map.of()
                        : entryDetailRepository
                                .findAllByEntryIdsAndOwnerEmail(entryIds, email)
                                .stream()
                                .collect(Collectors.groupingBy(
                                        d -> d.getStorageEntry()
                                                .getStorageEntryId()
                                ));

        Map<Long, List<StorageExitDetail>> exitDetails =
                exitIds.isEmpty()
                        ? Map.of()
                        : exitDetailRepository
                                .findAllByExitIdsAndOwnerEmail(exitIds, email)
                                .stream()
                                .collect(Collectors.groupingBy(
                                        d -> d.getStorageExit()
                                                .getStorageExitId()
                                ));

        List<Long> storageIds = movements.getContent().stream()
                .map(movement -> movement.getStorageId())
                .distinct()
                .toList();

        Map<Long, StorageResponse> storages =
                storageRepository
                        .findAllByIdInAndOwner_EmailIgnoreCase(storageIds, email)
                        .stream()
                        .collect(Collectors.toMap(
                                (storage -> storage.getId()),
                                storageMapper::toResponse
                        ));

        List<MovementResponse> responses = new ArrayList<>();

        for (MovementProjection movement : movements.getContent()) {

            StorageResponse storage = storages.get(movement.getStorageId());

            if (storage == null) {
                throw new IllegalStateException(
                        "No se encontró un depósito accesible."
                );
            }

            List<MovementDetailResponse> details;

            if (movement.getType() == MovementType.ENTRY) {
                details = entryDetails
                        .getOrDefault(movement.getMovementId(), List.of())
                        .stream()
                        .map(d -> new MovementDetailResponse(
                                d.getEntryDetailId(),
                                foodMapper.toResponse(d.getFood()),
                                d.getSelectedUnit(),
                                d.getCantidad(),
                                null
                        ))
                        .toList();

            } else {
                details = exitDetails
                        .getOrDefault(movement.getMovementId(), List.of())
                        .stream()
                        .map(d -> new MovementDetailResponse(
                                d.getExitDetailId(),
                                foodMapper.toResponse(d.getFood()),
                                d.getSelectedUnit(),
                                d.getCantidad(),
                                d.getStorageBatch().getId()
                        ))
                        .toList();
            }

            responses.add(new MovementResponse(
                    movement.getMovementId(),
                    movement.getType(),
                    movement.getMovementDateTime(),
                    movement.getRecordedAt(),
                    details,
                    storage
            ));
        }

        return new PageImpl<>(
                responses,
                pagination,
                movements.getTotalElements()
        );
    }
}
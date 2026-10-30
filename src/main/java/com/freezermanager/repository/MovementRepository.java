package com.freezermanager.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.freezermanager.domain.storage.StorageEntry;
import com.freezermanager.repository.projection.MovementProjection;

public interface MovementRepository
        extends Repository<StorageEntry, Long> {

    @Query(value = """
            SELECT movements.movement_id AS "movementId",
                   movements.movement_type AS "type",
                   movements.movement_date_time AS "movementDateTime",
                   movements.recorded_at AS "recordedAt",
                   movements.storage_id AS "storageId"
            FROM (
                SELECT se.storage_entry_id AS movement_id,
                       'ENTRY' AS movement_type,
                       se.movement_date_time,
                       se.recorded_at,
                       se.storage_id
                FROM storage_entries se
                JOIN storages s ON s.id = se.storage_id
                JOIN users u ON u.id = s.user_id
                WHERE LOWER(u.email) = LOWER(:email)
                AND (:type IS NULL OR :type = 'ENTRY')
                AND (:storageId IS NULL OR se.storage_id = :storageId)
                AND (:fromDate IS NULL OR se.movement_date_time >= :fromDate)
                AND (:toDate IS NULL OR se.movement_date_time <= :toDate)
                AND (
                    :foodId IS NULL OR EXISTS (
                        SELECT 1 FROM storage_entry_details sed
                        WHERE sed.storage_entry_id = se.storage_entry_id
                        AND sed.food_id = :foodId
                    )
                )

                UNION ALL

                SELECT sx.storage_exit_id AS movement_id,
                       'EXIT' AS movement_type,
                       sx.movement_date_time,
                       sx.recorded_at,
                       sx.storage_id
                FROM storage_exits sx
                JOIN storages s ON s.id = sx.storage_id
                JOIN users u ON u.id = s.user_id
                WHERE LOWER(u.email) = LOWER(:email)
                AND (:type IS NULL OR :type = 'EXIT')
                AND (:storageId IS NULL OR sx.storage_id = :storageId)
                AND (:fromDate IS NULL OR sx.movement_date_time >= :fromDate)
                AND (:toDate IS NULL OR sx.movement_date_time <= :toDate)
                AND (
                    :foodId IS NULL OR EXISTS (
                        SELECT 1 FROM storage_exit_details sxd
                        WHERE sxd.storage_exit_id = sx.storage_exit_id
                        AND sxd.food_id = :foodId
                    )
                )
            ) movements
            ORDER BY movements.movement_date_time DESC,
                     movements.movement_type ASC,
                     movements.movement_id DESC
            """, countQuery = """
            SELECT COUNT(*)
            FROM (
                SELECT se.storage_entry_id
                FROM storage_entries se
                JOIN storages s ON s.id = se.storage_id
                JOIN users u ON u.id = s.user_id
                WHERE LOWER(u.email) = LOWER(:email)
                AND (:type IS NULL OR :type = 'ENTRY')
                AND (:storageId IS NULL OR se.storage_id = :storageId)
                AND (:fromDate IS NULL OR se.movement_date_time >= :fromDate)
                AND (:toDate IS NULL OR se.movement_date_time <= :toDate)
                AND (
                    :foodId IS NULL OR EXISTS (
                        SELECT 1 FROM storage_entry_details sed
                        WHERE sed.storage_entry_id = se.storage_entry_id
                        AND sed.food_id = :foodId
                    )
                )

                UNION ALL

                SELECT sx.storage_exit_id
                FROM storage_exits sx
                JOIN storages s ON s.id = sx.storage_id
                JOIN users u ON u.id = s.user_id
                WHERE LOWER(u.email) = LOWER(:email)
                AND (:type IS NULL OR :type = 'EXIT')
                AND (:storageId IS NULL OR sx.storage_id = :storageId)
                AND (:fromDate IS NULL OR sx.movement_date_time >= :fromDate)
                AND (:toDate IS NULL OR sx.movement_date_time <= :toDate)
                AND (
                    :foodId IS NULL OR EXISTS (
                        SELECT 1 FROM storage_exit_details sxd
                        WHERE sxd.storage_exit_id = sx.storage_exit_id
                        AND sxd.food_id = :foodId
                    )
                )
            ) movements
            """, nativeQuery = true)
    Page<MovementProjection> findMovements(
            @Param("email") String email,
            @Param("type") String type,
            @Param("storageId") Long storageId,
            @Param("foodId") Long foodId,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
}
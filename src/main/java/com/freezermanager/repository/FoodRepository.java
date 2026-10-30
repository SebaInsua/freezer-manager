
package com.freezermanager.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.freezermanager.domain.food.Food;

public interface FoodRepository extends JpaRepository<Food, Long> {

    @Query("""
            SELECT f
            FROM Food f
            WHERE f.active = :active
              AND (f.owner IS NULL OR f.owner.id = :ownerId)
            """)
    Page<Food> findVisibleFoods(
            @Param("active") boolean active,
            @Param("ownerId") Long ownerId,
            Pageable pageable);

    @Query("""
            SELECT f
            FROM Food f
            WHERE f.foodId = :foodId
              AND (f.owner IS NULL OR f.owner.id = :ownerId)
            """)
    Optional<Food> findVisibleById(
            @Param("foodId") Long foodId,
            @Param("ownerId") Long ownerId);

    @Query("""
            SELECT f
            FROM Food f
            WHERE f.foodId = :foodId
              AND f.active = true
              AND (f.owner IS NULL OR f.owner.id = :ownerId)
            """)
    Optional<Food> findActiveVisibleById(
            @Param("foodId") Long foodId,
            @Param("ownerId") Long ownerId);

    boolean existsByNameIgnoreCaseAndOwnerIsNull(String name);

    boolean existsByNameIgnoreCaseAndOwner_Id(String name, Long ownerId);

    boolean existsByNameIgnoreCaseAndOwner_IdAndFoodIdNot(
            String name, Long ownerId, Long foodId);

    boolean existsByNameIgnoreCaseAndOwnerIsNullAndFoodIdNot(
            String name, Long foodId);
}

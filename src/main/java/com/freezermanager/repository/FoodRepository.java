package com.freezermanager.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.freezermanager.domain.food.Food;

public interface FoodRepository extends JpaRepository<Food,Long>{
    public boolean existsByNameIgnoreCase(String name);
    public boolean existsByNameIgnoreCaseAndFoodIdNot(String name,Long id);
    public Page<Food>findAllByActive(boolean active,Pageable pageable);
}
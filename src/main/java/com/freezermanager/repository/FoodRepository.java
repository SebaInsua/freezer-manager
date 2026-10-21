package com.freezermanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freezermanager.domain.food.Food;

public interface FoodRepository extends JpaRepository<Food,Long>{
    public boolean existsByNameIgnoreCase(String name);
    public boolean existsByNameIgnoreCaseAndFoodIdNot(String name,Long id);
    public List<Food>findAllByActive(boolean active);
}
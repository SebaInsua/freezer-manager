package com.freezermanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.freezermanager.domain.food.Food;

public interface FoodRepository extends JpaRepository<Food,Long>{
}
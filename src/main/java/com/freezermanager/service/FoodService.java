package com.freezermanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.food.Food;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.FoodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {
    private final FoodRepository foodRepository;

    public List<Food> getFoods(){
        return foodRepository.findAll();
    }

    public Food getFoodById(Long id){
        return foodRepository.findById(id).orElseThrow(() -> new NotFoundException("No existe un alimento con el ID: " + id));
    }
}

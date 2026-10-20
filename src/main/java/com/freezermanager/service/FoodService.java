package com.freezermanager.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.freezermanager.domain.food.Food;
import com.freezermanager.dto.FoodRequest;
import com.freezermanager.exception.AlreadyExistException;
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

    public Food createFood(FoodRequest request){
        if(foodRepository.existsByNameIgnoreCase(request.name())){
            throw new AlreadyExistException("Ya existe un alimento con el nombre: "+ request.name());
        }
        Food foodToSave = new Food();
        foodToSave.setActive(true);
        foodToSave.setName(request.name());
        foodToSave.setCategory(request.category());
        foodToSave.setUnitOfMeasures(request.unitOfMeasures());
        return foodRepository.save(foodToSave);
    }
}

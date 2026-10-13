package com.freezermanager.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.freezermanager.domain.food.Food;
import com.freezermanager.service.FoodService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/foods")
public class FoodController {
    
    private final FoodService foodService;

    @GetMapping
    public ResponseEntity<List<Food>> getFoods(){
        return ResponseEntity.ok(foodService.getFoods());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Food> getFood(@PathVariable Long id){
        return ResponseEntity.ok(foodService.getFoodById(id));
    }
}

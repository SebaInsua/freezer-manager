package com.freezermanager.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.dto.FoodRequest;
import com.freezermanager.exception.AlreadyExistException;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.FoodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {
    private final FoodRepository foodRepository;

    private void validateUnits(Set<UnitOfMeasure> units) {
        boolean hasWeightUnit = units.contains(UnitOfMeasure.KG)
                || units.contains(UnitOfMeasure.G);

        boolean hasUnit = units.contains(UnitOfMeasure.UNIT);

        if (hasWeightUnit && hasUnit) {
            throw new IllegalArgumentException(
                    "No se pueden combinar unidades de peso con unidades individuales."
            );
        }
    }

    public void validateSelectedUnit(Food food, UnitOfMeasure selectedUnit){
        if (!food.getUnitOfMeasures().contains(selectedUnit)) {
            throw new IllegalArgumentException("La unidad seleccionada no es apta para este alimento.");
        }
    }

    public List<Food> getFoods(Boolean active){
        if(active == null){
            return foodRepository.findAllByActive(true);
        }
        return foodRepository.findAllByActive(active);
    }

    public Food getFoodById(Long id){
        return foodRepository.findById(id).orElseThrow(() -> new NotFoundException("No existe un alimento con el ID: " + id));
    }

    public Food createFood(FoodRequest request){
        if(foodRepository.existsByNameIgnoreCase(request.name())){
            throw new AlreadyExistException("Ya existe un alimento con el nombre: "+ request.name());
        }
        validateUnits(request.unitOfMeasures());
        Food foodToSave = new Food();
        foodToSave.setActive(true);
        foodToSave.setName(request.name());
        foodToSave.setCategory(request.category());
        foodToSave.setUnitOfMeasures(request.unitOfMeasures());
        return foodRepository.save(foodToSave);
    }

    @Transactional
    public Food updateFoods(FoodRequest request, Long id){
        Food foodToUpdate = getFoodById(id);
        if(foodRepository.existsByNameIgnoreCaseAndFoodIdNot(request.name(),id)){
            throw new AlreadyExistException("El nombre del alimento ya existe.");
        }
        validateUnits(request.unitOfMeasures());
        foodToUpdate.setName(request.name());
        foodToUpdate.setCategory(request.category());
        foodToUpdate.setUnitOfMeasures(request.unitOfMeasures());
        return foodToUpdate;
    }

    @Transactional
    public void deactivateFood(Long id){
        Food foodToDeactivate = getFoodById(id);
        foodToDeactivate.setActive(false);
    }

    public Food getActiveFoodById(Long id){
        Food alimentoABuscar = foodRepository.findById(id).orElseThrow(()-> new NotFoundException("No existe alimento con el ID: "+id));
        if(!alimentoABuscar.isActive()){
            throw new IllegalArgumentException("No se puede ingresar un alimento inactivo");
        }
        return alimentoABuscar;
    }
}

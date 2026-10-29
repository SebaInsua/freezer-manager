
package com.freezermanager.service;

import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.UnitOfMeasure;
import com.freezermanager.domain.user.User;
import com.freezermanager.dto.FoodRequest;
import com.freezermanager.exception.AlreadyExistException;
import com.freezermanager.exception.NotFoundException;
import com.freezermanager.repository.FoodRepository;
import com.freezermanager.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FoodService {

        private final FoodRepository foodRepository;
        private final UserRepository userRepository;

        private User getAuthenticatedUser() {
                Authentication authentication =
                        SecurityContextHolder.getContext().getAuthentication();

                if (authentication == null
                        || !authentication.isAuthenticated()
                        || authentication.getName() == null
                        || authentication.getName().equals("anonymousUser")) {
                throw new IllegalStateException("No hay un usuario autenticado.");
                }

                return userRepository.findByEmailIgnoreCase(authentication.getName())
                        .orElseThrow(() ->
                                new UsernameNotFoundException("No existe el usuario autenticado."));
        }

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

        public void validateSelectedUnit(Food food, UnitOfMeasure selectedUnit) {
                if (!food.getUnitOfMeasures().contains(selectedUnit)) {
                throw new IllegalArgumentException(
                        "La unidad seleccionada no es apta para este alimento."
                );
                }
        }

        public Page<Food> getFoods(Boolean active, Pageable pageable) {
                User user = getAuthenticatedUser();
                boolean activeFilter = active == null || active;

                return foodRepository.findVisibleFoods(
                        activeFilter,
                        user.getId(),
                        pageable
                );
        }

        public Food getFoodById(Long id) {
                User user = getAuthenticatedUser();

                return foodRepository.findVisibleById(id, user.getId())
                        .orElseThrow(() ->
                                new NotFoundException("No existe un alimento con el ID: " + id));
        }

        public Food getActiveFoodById(Long id) {
                User user = getAuthenticatedUser();

                Food food = foodRepository.findVisibleById(id, user.getId())
                        .orElseThrow(() ->
                                new NotFoundException(
                                        "No existe un alimento con el ID: " + id));

                if (!food.isActive()) {
                        throw new IllegalArgumentException(
                                "No se puede ingresar un alimento inactivo.");
                }

                return food;
        }

        public Food createFood(FoodRequest request) {
                User user = getAuthenticatedUser();

                if (foodRepository.existsByNameIgnoreCaseAndOwner_Id(
                        request.name(), user.getId())) {
                throw new AlreadyExistException(
                        "Ya tenés un alimento personalizado con ese nombre."
                );
                }

                validateUnits(request.unitOfMeasures());

                Food foodToSave = new Food();
                foodToSave.setOwner(user);
                foodToSave.setActive(true);
                foodToSave.setName(request.name());
                foodToSave.setCategory(request.category());
                foodToSave.setUnitOfMeasures(request.unitOfMeasures());
                foodToSave.setDefaultShelfLifeDays(request.defaultShelfLifeDays());
                foodToSave.setEstimatedWeightPerUnitKg(
                        request.estimatedWeightPerUnitKg()
                );

                return foodRepository.save(foodToSave);
        }

        @Transactional
        public Food updateFoods(FoodRequest request, Long id) {
                User user = getAuthenticatedUser();

                Food foodToUpdate = foodRepository
                        .findVisibleById(id, user.getId())
                        .orElseThrow(() ->
                                new NotFoundException("No existe un alimento con el ID: " + id));

                if (foodToUpdate.getOwner() == null) {
                throw new IllegalArgumentException(
                        "No se puede modificar un alimento del catálogo global."
                );
                }

                if (foodRepository.existsByNameIgnoreCaseAndOwner_IdAndFoodIdNot(
                        request.name(), user.getId(), id)) {
                throw new AlreadyExistException(
                        "Ya tenés otro alimento personalizado con ese nombre."
                );
                }

                validateUnits(request.unitOfMeasures());

                foodToUpdate.setName(request.name());
                foodToUpdate.setCategory(request.category());
                foodToUpdate.setUnitOfMeasures(request.unitOfMeasures());
                foodToUpdate.setDefaultShelfLifeDays(request.defaultShelfLifeDays());
                foodToUpdate.setEstimatedWeightPerUnitKg(
                        request.estimatedWeightPerUnitKg()
                );

                return foodToUpdate;
        }

        @Transactional
        public void deactivateFood(Long id) {
                User user = getAuthenticatedUser();

                Food foodToDeactivate = foodRepository
                        .findVisibleById(id, user.getId())
                        .orElseThrow(() ->
                                new NotFoundException("No existe un alimento con el ID: " + id));

                if (foodToDeactivate.getOwner() == null) {
                throw new IllegalArgumentException(
                        "No se puede desactivar un alimento del catálogo global."
                );
                }

                foodToDeactivate.setActive(false);
        }
}

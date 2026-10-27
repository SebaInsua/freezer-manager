package com.freezermanager.domain.food;

import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "foods")
@Getter
@Setter
@NoArgsConstructor
public class Food {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long foodId;

    @NotBlank
    @Column(nullable = false,unique = true)
    private String name;

    @Enumerated(EnumType.STRING)
    private FoodCategory category;

    @ElementCollection(targetClass = UnitOfMeasure.class,fetch = FetchType.LAZY)
    @CollectionTable(name = "food_unit_of_measure",joinColumns = @JoinColumn(name = "food_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "unit_of_measure",nullable = false)
    @NotEmpty
    private Set<UnitOfMeasure> unitOfMeasures;

    @Column(nullable = false)
    private boolean active = true;

    @Positive(message = "La vida útil predeterminada debe ser mayor que cero.")
    @Column(name = "default_shelf_life_days")
    private Integer defaultShelfLifeDays;
}
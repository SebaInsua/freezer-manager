package com.freezermanager.domain.storage;

import java.math.BigDecimal;

import com.freezermanager.domain.food.Food;
import com.freezermanager.domain.food.UnitOfMeasure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "storage_exit_details")
@Getter
@Setter
@NoArgsConstructor
public class StorageExitDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long exitDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id",nullable = false)
    private Food food;

    @Enumerated(EnumType.STRING)
    @Column(name = "unit_selected",nullable = false)
    private UnitOfMeasure selectedUnit;
    
    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_exit_id",nullable = false)
    private StorageExit storageExit;
}

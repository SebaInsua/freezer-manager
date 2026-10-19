package com.freezermanager.domain.storage;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "storage_entry_details")
@Getter
@Setter
@NoArgsConstructor
public class StorageEntryDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long entryDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id",nullable = false)
    @NotNull
    private Food food;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "unit_selected",nullable = false)
    private UnitOfMeasure selectedUnit;

    @Positive
    @NotNull
    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal cantidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_entry_id",nullable = false)
    @NotNull
    @JsonBackReference
    private StorageEntry storageEntry;
}

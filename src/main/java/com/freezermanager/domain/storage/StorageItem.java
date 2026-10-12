package com.freezermanager.domain.storage;

import java.math.BigDecimal;

import com.freezermanager.domain.food.Food;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "storage_items",uniqueConstraints = {
        @UniqueConstraint(
        name = "uk_storage_item_food_storage",
        columnNames = {"food_id","storage_id"}
        )
    })
@Getter
@Setter
@NoArgsConstructor
public class StorageItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id",nullable = false)
    private Storage storage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "food_id",nullable = false)
    private Food food;

    @Column(nullable = false)
    private BigDecimal cantidad;
}

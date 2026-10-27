
package com.freezermanager.domain.storage;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "storage_batches")
@Getter
@Setter
@NoArgsConstructor
public class StorageBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
        name = "entry_detail_id",
        nullable = false,
        unique = true
    )
    @NotNull
    private StorageEntryDetail storageEntryDetail;

    @Column(name = "initial_quantity", nullable = false, precision = 10, scale = 2)
    @NotNull
    @Positive
    private BigDecimal initialQuantity;

    @DecimalMin("0.00")
    @NotNull
    @Column(name = "available_quantity", nullable = false, precision = 10, scale = 2)
    private BigDecimal availableQuantity;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "expiration_date")
    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "expiration_date_type", nullable = false)
    private ExpirationDateType expirationDateType = ExpirationDateType.UNDEFINED;

    @PrePersist
    private void initializeActive() {
        this.active = true;
    }
}

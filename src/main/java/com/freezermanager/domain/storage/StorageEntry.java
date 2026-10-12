package com.freezermanager.domain.storage;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "storage_entries")
@Getter
@Setter
@NoArgsConstructor
public class StorageEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storageEntryId;

    @Column(nullable = false)
    @NotNull(message = "La fecha y hora del ingreso es obligatoria")
    private LocalDateTime movementDateTime;

    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    @NotNull(message = "La fecha y hora del registro es obligatoria")
    private LocalDateTime recordedAt;

    @OneToMany(mappedBy = "storageEntry",cascade = CascadeType.ALL,orphanRemoval = true)
    List<StorageEntryDetail> details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id",nullable = false)
    @NotNull
    private Storage storage;
}

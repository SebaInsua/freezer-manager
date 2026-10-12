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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "storage_exits")
@Getter
@Setter
@NoArgsConstructor
public class StorageExit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long storageExitId;

    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    private LocalDateTime exitDateTime;

    @OneToMany(mappedBy = "storageExit",cascade = CascadeType.ALL,orphanRemoval = true)
    List<StorageExitDetail> details;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_id",nullable = false)
    private Storage storage;
}

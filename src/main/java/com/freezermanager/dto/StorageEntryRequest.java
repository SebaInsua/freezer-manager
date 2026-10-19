
package com.freezermanager.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record StorageEntryRequest(
    @NotNull(message = "La fecha y hora del ingreso es obligatoria")
    LocalDateTime movementDateTime,

    @NotNull(message = "El freezer es obligatorio")
    Long storageId,

    @NotEmpty(message = "El ingreso debe tener al menos un detalle")
    List<@Valid StorageEntryDetailRequest> details
) {}

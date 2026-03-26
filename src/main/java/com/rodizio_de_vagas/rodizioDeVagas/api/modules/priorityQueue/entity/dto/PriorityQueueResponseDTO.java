package com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;

public record PriorityQueueResponseDTO(
    Long id,
    Category category,
    Integer positionInLine,
    String fiscalFullName,
    Boolean fiscalHaveALicense
) {}

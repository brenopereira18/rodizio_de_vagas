package com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;

public record WorkCategoryPreferenceResponseDTO(
    Long id,
    Category category,
    Boolean active
) {
}

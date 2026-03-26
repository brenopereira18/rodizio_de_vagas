package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkStatus;

import java.time.LocalDateTime;

public record WorkResponseDTO(
    Long id,
    String title,
    String location,
    LocalDateTime serviceDate,
    LocalDateTime serviceEndDate,
    Long managerId,
    String managerName,
    LocalDateTime registrationLimit,
    Category category,
    Integer numberOfVacancies,
    WorkStatus workStatus,
    String observation,
    LocalDateTime createdAt
) {
}

package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;

import java.time.LocalDateTime;

public record RequestCreateWorkDTO(
    String title,
    String location,
    LocalDateTime serviceDate,
    Long managerId,
    Category category,
    Integer numberOfVacancies
) {}

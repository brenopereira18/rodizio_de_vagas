package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public record RequestCreateOrUpdateWorkDTO(
    Long id,
    String title,
    String location,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime serviceDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    LocalDateTime serviceEndDate,

    Long managerId,
    Category category,
    Integer numberOfVacancies
) {}

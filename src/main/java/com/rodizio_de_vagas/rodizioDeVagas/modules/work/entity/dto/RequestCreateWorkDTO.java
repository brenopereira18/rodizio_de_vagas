package com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;

import java.time.LocalDateTime;

public record RequestCreateWorkDTO(
    String title,
    String location,
    LocalDateTime serviceDate,
    Long managerId,
    Category category,
    Integer numberOfVacancies
) {}

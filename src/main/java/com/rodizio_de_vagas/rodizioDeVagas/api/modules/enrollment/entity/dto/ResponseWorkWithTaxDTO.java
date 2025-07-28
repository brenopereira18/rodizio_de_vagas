package com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;

import java.time.LocalDateTime;
import java.util.List;

public record ResponseWorkWithTaxDTO(
    String title,
    String location,
    LocalDateTime serviceDate,
    UserEntity manager,
    Category category,
    int numberOfVacancies,
    List<ResponseTaxInfosDTO> tax
) {}

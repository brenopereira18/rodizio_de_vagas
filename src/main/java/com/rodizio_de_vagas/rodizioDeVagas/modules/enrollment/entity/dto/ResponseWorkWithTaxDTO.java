package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;

import java.time.LocalDateTime;
import java.util.List;

public record ResponseWorkWithTaxDTO(
    String workName,
    String local,
    LocalDateTime serviceDate,
    UserEntity manager,
    Category category,
    List<ResponseTaxInfosDTO> tax
) {}

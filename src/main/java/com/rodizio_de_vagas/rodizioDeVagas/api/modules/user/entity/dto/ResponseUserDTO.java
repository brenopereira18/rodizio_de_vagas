package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserRole;

public record ResponseUserDTO(
    Long id,
    String fullName,
    String registration,
    String phoneNumber,
    Boolean haveALicense,
    UserRole userRole
) {}

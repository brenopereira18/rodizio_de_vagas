package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserRole;

public record RequestCreateUserDTO(
    String fullName,
    String registration,
    String phoneNumber,
    UserRole userRole,
    Boolean haveALicense
) {}

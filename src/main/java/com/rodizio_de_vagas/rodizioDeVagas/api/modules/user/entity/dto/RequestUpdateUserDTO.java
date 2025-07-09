package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto;

public record RequestUpdateUserDTO(
    String password,
    String phoneNumber
) {}

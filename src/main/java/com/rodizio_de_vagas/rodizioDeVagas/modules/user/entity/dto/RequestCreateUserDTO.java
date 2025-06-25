package com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.dto;

public record RequestCreateUserDTO(
    String fullName,
    String registration,
    String phoneNumber
) {}

package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto;

public record ResponseUserDTO(
    Long id,
    String fullName,
    String registration,
    String phoneNumber
) {}

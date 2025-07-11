package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto;

import jakarta.validation.constraints.Size;

public record RequestUpdateUserDTO(

    @Size(min = 6, max = 12, message = "A senha deve ter de 6 a 12 caracteres")
    String password,
    String phoneNumber
) {}

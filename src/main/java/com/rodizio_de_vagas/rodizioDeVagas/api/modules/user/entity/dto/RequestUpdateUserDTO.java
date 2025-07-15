package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RequestUpdateUserDTO(

    @Size(min = 6, max = 12, message = "A senha deve ter de 6 a 12 caracteres")
    String password,
    String phoneNumber,
    List<Category> categorys
) {}

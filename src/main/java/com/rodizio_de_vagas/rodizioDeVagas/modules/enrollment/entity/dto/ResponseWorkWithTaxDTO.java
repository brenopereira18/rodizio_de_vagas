package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto;

import java.util.List;

public record ResponseWorkWithTaxDTO(
    String workName,
    List<ResponseTaxInfosDTO> tax
) {}

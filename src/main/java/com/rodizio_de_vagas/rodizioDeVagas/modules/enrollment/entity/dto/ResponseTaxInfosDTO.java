package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.SubscriptionStatus;

public record ResponseTaxInfosDTO(
    String name,
    SubscriptionStatus status
) {}

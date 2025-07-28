package com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;

public record ResponseTaxInfosDTO(
    String fullName,
    SubscriptionStatus subscriptionStatus
) {}

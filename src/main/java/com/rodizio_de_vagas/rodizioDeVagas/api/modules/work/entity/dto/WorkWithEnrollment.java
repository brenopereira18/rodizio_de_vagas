package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;

public record WorkWithEnrollment(
    WorkEntity work,
    EnrollmentEntity enrollment,
    boolean hasPriority,
    boolean canApply
) {
}

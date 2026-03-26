package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkStatus;

import java.time.LocalDateTime;

public record WorkWithEnrollmentDTO(
    // Dados do serviço
    Long workId,
    String title,
    String location,
    LocalDateTime serviceDate,
    LocalDateTime serviceEndDate,
    String managerName,
    Category category,
    Integer numberOfVacancies,
    WorkStatus workStatus,
    String observation,

    // Dados da inscrição do fiscal (null se não inscrito)
    Long enrollmentId,
    SubscriptionStatus subscriptionStatus,

    // Flags de controle de UI
    boolean hasPriority,
    boolean canApply
) {
}

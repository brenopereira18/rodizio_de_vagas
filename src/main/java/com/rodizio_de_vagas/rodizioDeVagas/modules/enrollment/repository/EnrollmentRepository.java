package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.repository;

import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    int countByWorkEntityAndSubscriptionStatus(WorkEntity workEntity, SubscriptionStatus subscriptionStatus);
    Optional<EnrollmentEntity> findById(Long id);
}

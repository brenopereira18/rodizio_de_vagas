package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.repository;

import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    int countByWorkEntityAndSubscriptionStatus(WorkEntity workEntity, SubscriptionStatus subscriptionStatus);
    Optional<EnrollmentEntity> findById(Long id);

    @Query("""
        SELECT e FROM EnrollmentEntity e
        WHERE (e.workEntity.workStatus = 'CLOSED' OR e.workEntity.serviceDate < CURRENT_TIMESTAMP)
        AND e.subscriptionStatus = 'ACCEPTED'
        AND e.workEntity.serviceDate BETWEEN :startDate AND :endDate
    """)
    List<EnrollmentEntity> findConfirmedEnrollmentsByMonth(LocalDateTime startDate, LocalDateTime endDate);
}

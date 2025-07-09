package com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.repository;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<EnrollmentEntity, Long> {

    int countByWorkEntityAndSubscriptionStatus(WorkEntity workEntity, SubscriptionStatus subscriptionStatus);

    Optional<EnrollmentEntity> findByWorkEntityIdAndUserEntityRegistrationAndSubscriptionStatus(Long workId, String registration, SubscriptionStatus status);

    Optional<EnrollmentEntity> findById(Long id);

    @Query("""
            SELECT e FROM EnrollmentEntity e
            WHERE (e.workEntity.workStatus = 'CLOSED' OR e.workEntity.serviceDate < CURRENT_TIMESTAMP)
            AND e.subscriptionStatus = 'ACCEPTED'
            AND e.workEntity.serviceDate BETWEEN :startDate AND :endDate
        """)
    List<EnrollmentEntity> findConfirmedEnrollmentsByMonth(LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
            SELECT e FROM EnrollmentEntity e
            JOIN e.workEntity w
            JOIN NotificationEntity n ON n.userEntity = e.userEntity AND n.workEntity = e.workEntity
            WHERE w.registrationLimit > CURRENT_TIMESTAMP
            AND n.responseDeadline < CURRENT_TIMESTAMP
            AND e.subscriptionStatus = 'WAITING'
        """)
    List<EnrollmentEntity> findExpiredWaitingEnrollments();

    @Query("SELECT e.userEntity.id FROM EnrollmentEntity e WHERE e.workEntity.id = :workId")
    List<Long> findUserIdsByWorkId(@Param("workId") Long workId);

    boolean existsByWorkEntityAndSubscriptionStatus(WorkEntity work, SubscriptionStatus status);

    @Modifying
    @Query("""
        UPDATE EnrollmentEntity e SET e.subscriptionStatus = 'CANCELLED' WHERE e.userEntity.id = :userId AND e.workEntity.category = :category AND e.subscriptionStatus = 'WAITING' AND e.workEntity.id <> :currentWorkId
    """)
    void cancelOtherEnrollmentsInCategory(@Param("userId") Long userId, @Param("category") Category category, @Param("currentWorkId") Long currentWorkId);

    List<EnrollmentEntity> findByUserEntityIdAndWorkEntityCategoryAndSubscriptionStatus(Long userId, Category category, SubscriptionStatus status);


}

package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.repository;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkRepository extends JpaRepository<WorkEntity, Long> {

    List<WorkEntity> findByWorkStatusInAndServiceDateAfter(List<WorkStatus> status, LocalDateTime now);

    Optional<WorkEntity> findById(Long id);

    @Query("""
        SELECT w FROM WorkEntity w
        WHERE w.workStatus IN ('OPEN', 'FREE')
        AND (w.registrationLimit < CURRENT_TIMESTAMP OR w.serviceDate < CURRENT_TIMESTAMP)
    """)
    List<WorkEntity> findExpiredWorks();

    List<WorkEntity> findByWorkStatusAndServiceDateAfter(WorkStatus workStatus, LocalDateTime date);

    @Query("SELECT w FROM WorkEntity w WHERE w.serviceDate >= :startDate ORDER BY w.createdAt DESC")
    List<WorkEntity> findAllWithEnrollmentsFromLastMonth(LocalDateTime startDate);

}

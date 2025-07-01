package com.rodizio_de_vagas.rodizioDeVagas.modules.work.repository;

import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkRepository extends JpaRepository<WorkEntity, Long> {

    Optional<WorkEntity> findById(Long id);
    List<WorkEntity> findByWorkStatus(WorkStatus status);
    List<WorkEntity> findByWorkStatusIn(List<WorkStatus> status);

    @Query("""
    SELECT w FROM WorkEntity w
    WHERE w.workStatus = 'OPEN'
      AND w.registrationLimit < CURRENT_TIMESTAMP
    """)
    List<WorkEntity> findExpiredWorks();

}

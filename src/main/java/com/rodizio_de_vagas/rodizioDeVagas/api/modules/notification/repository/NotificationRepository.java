package com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.repository;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    Optional<NotificationEntity> findById(Long id);

    @Query("SELECT n.userEntity.id FROM NotificationEntity n WHERE n.workEntity.id = :workId")
    List<Long> findUserIdsByWorkId(@Param("workId") Long workId);
}

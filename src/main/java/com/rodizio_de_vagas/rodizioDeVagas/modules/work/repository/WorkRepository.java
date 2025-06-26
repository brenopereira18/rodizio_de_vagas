package com.rodizio_de_vagas.rodizioDeVagas.modules.work.repository;

import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WorkRepository extends JpaRepository<WorkEntity, Long> {

    Optional<WorkEntity> findById(Long id);
}

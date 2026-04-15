package com.rodizio_de_vagas.rodizioDeVagas.api.modules.processedEvent.repository;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.processedEvent.entity.ProcessedEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEventEntity, String> {
}

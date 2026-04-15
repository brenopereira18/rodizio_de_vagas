package com.rodizio_de_vagas.rodizioDeVagas.api.modules.processedEvent.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Table(name = "eventos_processados")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedEventEntity {

    @Id
    private String eventId;

    private LocalDateTime processedAt = LocalDateTime.now();

    public ProcessedEventEntity(String eventId) {
        this.eventId = eventId;
    }
}

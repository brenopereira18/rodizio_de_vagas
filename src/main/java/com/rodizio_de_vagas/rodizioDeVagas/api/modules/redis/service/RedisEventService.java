package com.rodizio_de_vagas.rodizioDeVagas.api.modules.redis.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.redis.events.TaxRefusalEvent;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisEventService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${app.redis.stream.fiscal-events}")
    private String streamName;

    public void publishTaxRefusal(TaxRefusalEvent event) {
        try {
            // Publicar evento no Redis Stream
            var recordId = redisTemplate.opsForStream()
                .add(streamName, event.toMap());

            log.info("Evento publicado no Redis: stream={}, recordId={}, fiscalId={}, categoria={}",
                streamName, recordId, event.getTaxId(), event.getCategory());

        } catch (Exception e) {
            log.error("ERRO ao publicar evento no Redis: fiscalId={}, categoria={}, erro={}",
                event.getTaxId(), event.getCategory(), e.getMessage());

            // TODO: Implementar fallback (salvar no banco para processamento posterior)
            throw new RuntimeException("Falha ao publicar evento", e);
        }
    }

    public void publishTaxRefusal(Long fiscalId, String registration,
                                     Category category, Long serviceId) {
        TaxRefusalEvent event = TaxRefusalEvent.create(
            fiscalId, registration, category, serviceId
        );
        publishTaxRefusal(event);
    }
}

package com.rodizio_de_vagas.rodizioDeVagas.api.modules.redis.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.redis.events.TaxRefusalEvent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RedisEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PriorityQueueService priorityQueueService;

    @Value("${app.redis.stream.fiscal-events}")
    private String streamName;

    @Value("${app.redis.consumer.group}")
    private String consumerGroup;

    @Value("${app.redis.consumer.instance}")
    private String consumerInstance;

    @Value("${app.events.processing.batch-size}")
    private int batchSize;

    @Value("${app.events.processing.timeout}")
    private long timeoutMs;

    @PostConstruct
    public void initializeConsumerGroup() {
        try {
            // Cria o stream se não existir
            Boolean streamExists = redisTemplate.hasKey(streamName);
            if (Boolean.FALSE.equals(streamExists)) {
                redisTemplate.opsForStream().add(streamName, Map.of("init", "true"));
                log.info("Stream criado: {}", streamName);
            }

            // Criar consumer group se não existir
            redisTemplate.opsForStream()
                .createGroup(streamName, ReadOffset.from("0"), consumerGroup);
            log.info("Consumer group criado: stream={}, group={}", streamName, consumerGroup);

        } catch (Exception e) {
            // Consumer group já existe, ignorar
            log.debug("Consumer group já existe: stream={}, group={}", streamName, consumerGroup);
        }
    }

    @Scheduled(fixedDelay = 3000)
    public void consumeEvents() {
        try {
            // Ler eventos do stream
            List<MapRecord<String, Object, Object>> records = redisTemplate
                .opsForStream()
                .read(Consumer.from(consumerGroup, consumerInstance),
                    StreamReadOptions.empty()
                        .count(batchSize)
                        .block(Duration.ofMillis(timeoutMs)),
                    StreamOffset.create(streamName, ReadOffset.lastConsumed()));

            if (records == null || records.isEmpty()) return;
            log.debug("Processando {} eventos do Redis", records.size());

            for (MapRecord<String, Object, Object> record : records) {
                processEvent(record);
            }
        } catch (Exception e) {
            log.error("Erro ao consumir eventos do Redis: {}", e.getMessage());
        }
    }

    public void processEvent(MapRecord<String, Object, Object> record) {
        try {
            Map<String, Object> eventData = convertToStringObjectMap(record.getValue());

            // Converter record para evento
            TaxRefusalEvent event = TaxRefusalEvent.fromMap(eventData);

            log.info("Processando evento: eventId={}, fiscalId={}, categoria={}",
                event.eventId(), event.taxId(), event.category());

            // Verificar se fiscal ainda está na fila
            boolean fiscalInQueue = priorityQueueService.isFiscalInQueue(
                event.taxId(), event.category()
            );

            if (!fiscalInQueue) {
                log.warn("Fiscal {} não está mais na fila {}, ignorando evento {}",
                    event.taxId(), event.category(), event.eventId());
                acknowledgeEvent(record);
                return;
            }

            // Reorganizar fila
            priorityQueueService.sendFiscalToEndOfQueue(
                Set.of(event.taxId()),
                event.category()
            );

            // Confirmar processamento
            acknowledgeEvent(record);

            log.info("Evento processado com sucesso: eventId={}, fiscalId={}, categoria={}",
                event.eventId(), event.taxId(), event.category());

        } catch (Exception e) {
            log.error("ERRO ao processar evento: recordId={}, erro={}",
                record.getId(), e.getMessage(), e);
        }
    }

    private Map<String, Object> convertToStringObjectMap(Map<Object, Object> originalMap) {
        return originalMap.entrySet().stream()
            .collect(Collectors.toMap(
                entry -> entry.getKey().toString(),
                Map.Entry::getValue
            ));
    }

    private void acknowledgeEvent(MapRecord<String, Object, Object> record) {
        try {
            redisTemplate.opsForStream()
                .acknowledge(streamName, consumerGroup, record.getId());
            log.debug("Evento confirmado: recordId={}", record.getId());

        } catch (Exception e) {
            log.error("Erro ao confirmar evento: recordId={}, erro={}",
                record.getId(), e.getMessage());
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void reprocessPendingEvents() {
        try {
            List<MapRecord<String, Object, Object>> records = redisTemplate
                .opsForStream()
                .read(
                    Consumer.from(consumerGroup, consumerInstance),
                    StreamReadOptions.empty().count(batchSize),
                    StreamOffset.create(streamName, ReadOffset.from("0"))
                );

            if (records == null || records.isEmpty()) return;
            log.info("Reprocessando {} eventos pendentes", records.size());

            for (MapRecord<String, Object, Object> record : records) {
                processEvent(record); // 👈 reutiliza seu método atual
            }
        } catch (Exception e) {
            log.error("Erro ao reprocessar eventos pendentes", e);
        }
    }
}

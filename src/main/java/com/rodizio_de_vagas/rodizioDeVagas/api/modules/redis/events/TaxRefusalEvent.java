package com.rodizio_de_vagas.rodizioDeVagas.api.modules.redis.events;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@Builder
public record TaxRefusalEvent(
    String eventId,
    Long taxId,
    String taxRegistration,
    Category category,
    Long serviceId,
    LocalDateTime timestamp
) {

    public static TaxRefusalEvent create(Long taxId, String registration, Category category, Long serviceId) {
        return TaxRefusalEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .taxId(taxId)
            .taxRegistration(registration)
            .category(category)
            .serviceId(serviceId)
            .timestamp(LocalDateTime.now())
            .build();
    }

    // Converter para Map (formato Redis Stream)
    public Map<String, Object> toMap() {
        return Map.of(
            "eventId",         eventId,
            "taxId",           taxId.toString(),
            "taxRegistration", taxRegistration,
            "category",        category.name(),
            "serviceId",       serviceId.toString(),
            "timestamp",       timestamp.toString()
        );
    }

    // Criar a partir de Map (leitura Redis Stream)
    public static TaxRefusalEvent fromMap(Map<String, Object> map) {
        validateField(map, "eventId");
        validateField(map, "taxId");
        validateField(map, "taxRegistration");
        validateField(map, "category");
        validateField(map, "serviceId");
        validateField(map, "timestamp");

        return TaxRefusalEvent.builder()
            .eventId(map.get("eventId").toString())
            .taxId(Long.valueOf(map.get("taxId").toString()))
            .taxRegistration(map.get("taxRegistration").toString())
            .category(Category.valueOf(map.get("category").toString()))
            .serviceId(Long.valueOf(map.get("serviceId").toString()))
            .timestamp(LocalDateTime.parse(map.get("timestamp").toString()))
            .build();
    }

    private static void validateField(Map<String, Object> map, String field) {
        if (map.get(field) == null) {
            throw new IllegalArgumentException(
                "Campo obrigatório ausente no evento Redis: " + field
            );
        }
    }
}

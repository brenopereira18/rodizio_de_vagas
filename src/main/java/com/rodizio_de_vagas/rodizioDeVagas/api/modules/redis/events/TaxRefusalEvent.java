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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxRefusalEvent {

    private String eventId;
    private Long taxId;
    private String taxRegistration;
    private Category category;
    private Long serviceId;
    private LocalDateTime timestamp;

    public static TaxRefusalEvent create(Long taxId, String registration,
                                            Category category, Long serviceId) {
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
        Map<String, Object> map = new HashMap<>();
        map.put("eventId", eventId);
        map.put("taxId", taxId.toString());
        map.put("taxRegistration", taxRegistration);
        map.put("category", category.name());
        map.put("serviceId", serviceId.toString());
        map.put("timestamp", timestamp.toString());
        return map;
    }

    // Criar a partir de Map (leitura Redis Stream)
    public static TaxRefusalEvent fromMap(Map<String, Object> map) {
        return TaxRefusalEvent.builder()
            .eventId((String) map.get("eventId"))
            .taxId(Long.valueOf(map.get("taxId").toString()))
            .taxRegistration((String) map.get("taxRegistration"))
            .category(Category.valueOf(map.get("category").toString()))
            .serviceId(Long.valueOf(map.get("serviceId").toString()))
            .timestamp(LocalDateTime.parse(map.get("timestamp").toString()))
            .build();
    }
}

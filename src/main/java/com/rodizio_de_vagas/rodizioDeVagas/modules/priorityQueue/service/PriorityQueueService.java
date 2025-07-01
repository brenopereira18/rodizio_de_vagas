package com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.service;

import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.repository.PriorityQueueRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PriorityQueueService {

    @Autowired
    private PriorityQueueRepository priorityQueueRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Cria automaticamente as filas de prioridade para todas as categorias
     * com todos os fiscais ordenados por nome.
     */
    @PostConstruct
    public void initializePriorityQueues() {
        System.out.println("🔄 Inicializando filas de prioridade...");

        for (Category category : Category.values()) {
            // Verifica se a fila já foi criada
            if (priorityQueueRepository.existsByCategory(category)) {
                System.out.println("Fila da categoria " + category + " já existe. Pulando...");
                continue; // Evita recriar a fila se já existir
            }

            List<UserEntity> users = userRepository.findAllByOrderByFullNameAsc();

            int position = 1;
            for (UserEntity user : users) {
                PriorityQueueEntity queue = PriorityQueueEntity.builder()
                    .userEntity(user)
                    .category(category)
                    .positionInLine(position)
                    .build();

                priorityQueueRepository.save(queue);
                position++;
            }

            System.out.println("Fila da categoria " + category + " criada com sucesso.");
        }

        System.out.println("✅ Todas as filas foram criadas.");
    }
}

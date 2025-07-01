package com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.service;

import com.rodizio_de_vagas.rodizioDeVagas.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.repository.PriorityQueueRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserRole;
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

            List<UserEntity> users = userRepository.findByUserRoleOrderByFullNameAsc(UserRole.FISCAL);

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

    public void deactivateFiscalFromCategory(Long userId, Category category) {
        PriorityQueueEntity queue = priorityQueueRepository.findByUserEntityIdAndCategory(userId, category)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado na fila"));

        int leavingPosition = queue.getPositionInLine();
        priorityQueueRepository.delete(queue);

        // Atualizar posições
        List<PriorityQueueEntity> remaining = priorityQueueRepository.findByCategoryOrderByPositionInLine(category);
        for (PriorityQueueEntity q : remaining) {
            if (q.getPositionInLine() > leavingPosition) {
                q.setPositionInLine(q.getPositionInLine() - 1);
                priorityQueueRepository.save(q);
            }
        }
    }

    public void activateFiscalInCategory(Long userId, Category category) {
        long queueSize = priorityQueueRepository.countByCategory(category);

        PriorityQueueEntity queue = PriorityQueueEntity.builder()
            .userEntity(userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado")))
            .category(category)
            .positionInLine((int) queueSize + 1)
            .build();

        priorityQueueRepository.save(queue);
    }

    // passa os fiscais para o final da fila
    public void sendFiscalToEndOfQueue(UserEntity user, Category category) {
        PriorityQueueEntity queue = priorityQueueRepository.findByUserEntityIdAndCategory(user.getId(), category)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado na fila"));

        int oldPosition = queue.getPositionInLine();
        long queueSize = priorityQueueRepository.countByCategory(category);

        // Atualizar posição do fiscal
        queue.setPositionInLine((int) queueSize);
        priorityQueueRepository.save(queue);

        // Atualizar posições dos fiscais que estavam atrás
        List<PriorityQueueEntity> affected = priorityQueueRepository.findByCategoryOrderByPositionInLine(category)
            .stream()
            .filter(q -> q.getPositionInLine() > oldPosition && q.getPositionInLine() < queueSize)
            .toList();

        for (PriorityQueueEntity q : affected) {
            q.setPositionInLine(q.getPositionInLine() - 1);
            priorityQueueRepository.save(q);
        }
    }


}

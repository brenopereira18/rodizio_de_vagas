package com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.service;

import com.rodizio_de_vagas.rodizioDeVagas.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.repository.PriorityQueueRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserRole;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
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
        for (Category category : Category.values()) {
            // Verifica se a fila já foi criada
            if (this.priorityQueueRepository.existsByCategory(category)) {
                System.out.println("Fila da categoria " + category + " já existe. Pulando...");
                continue;
            }

            List<UserEntity> users = this.userRepository.findByUserRoleOrderByFullNameAsc(UserRole.FISCAL);

            int position = 1;
            for (UserEntity user : users) {
                PriorityQueueEntity queue = PriorityQueueEntity.builder()
                    .userEntity(user)
                    .category(category)
                    .positionInLine(position)
                    .build();

                this.priorityQueueRepository.save(queue);
                position++;
            }
        }
    }

    public void deactivateFiscalFromCategory(String registration, Category category) {
        PriorityQueueEntity queue = this.priorityQueueRepository.findByUserEntityRegistrationAndCategory(registration, category)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado na fila"));

        int leavingPosition = queue.getPositionInLine();
        this.priorityQueueRepository.delete(queue);

        // Atualizar posições
        List<PriorityQueueEntity> remaining = this.priorityQueueRepository.findByCategoryOrderByPositionInLine(category);
        for (PriorityQueueEntity q : remaining) {
            if (q.getPositionInLine() > leavingPosition) {
                q.setPositionInLine(q.getPositionInLine() - 1);
                this.priorityQueueRepository.save(q);
            }
        }
    }

    public void activateFiscalInCategory(String registration, Category category) {
        int maxPosition = this.priorityQueueRepository.findMaxPositionByCategory(category).orElse(0);

        PriorityQueueEntity queue = PriorityQueueEntity.builder()
            .userEntity(userRepository.findByRegistration(registration).orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado")))
            .category(category)
            .positionInLine(maxPosition + 1)
            .build();

        this.priorityQueueRepository.save(queue);
    }

    // Passa os fiscais para o final da fila
    @Transactional
    public void sendFiscalToEndOfQueue(UserEntity user, Category category) {
        // Bloqueia toda a fila da categoria
        List<PriorityQueueEntity> priorityQueue = this.priorityQueueRepository.findByCategoryOrderByPositionInLineWithLock(category);

        PriorityQueueEntity queue = priorityQueue.stream()
            .filter(q -> q.getUserEntity().getId().equals(user.getId()))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado na fila"));

        int oldPosition = queue.getPositionInLine();

        // Remove o fiscal com valor impossível
        queue.setPositionInLine(-1 * queue.getId().intValue());
        this.priorityQueueRepository.save(queue);
        this.priorityQueueRepository.flush(); // Garante que o banco reconheceu que ele saiu

        // Atualiza as posições
        int position = 1;
        for (PriorityQueueEntity q : priorityQueue) {
            if (!q.getId().equals(queue.getId())) { // Ignora o fiscal que saiu temporariamente
                q.setPositionInLine(position++);
            }
        }
        this.priorityQueueRepository.saveAll(priorityQueue);
        this.priorityQueueRepository.flush(); // Garante que a fila foi atualizada sem sobreposição

        // Coloca o fiscal no final
        queue.setPositionInLine(position);
        this.priorityQueueRepository.save(queue);
        this.priorityQueueRepository.flush(); // Garante persistência final
    }
}

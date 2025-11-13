package com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.repository.PriorityQueueRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserRole;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

    @Transactional
    public void deactivateFiscalFromCategory(String registration, Category category) {
        List<PriorityQueueEntity> priorityQueue = this.priorityQueueRepository.findByCategoryOrderByPositionInLineWithLock(category);

        PriorityQueueEntity taxToDeactivate = priorityQueue.stream()
            .filter(q -> q.getUserEntity().getRegistration().equals(registration))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado na fila"));

        priorityQueue.remove(taxToDeactivate);
        this.priorityQueueRepository.delete(taxToDeactivate);
        this.priorityQueueRepository.flush();

        // 5. Reordenar as posições dos fiscais restantes na lista em memória
        // O loop deve reatribuir posições contíguas, sem lacunas
        int newPosition = 1;
        for (PriorityQueueEntity q : priorityQueue) { // priorityQueue agora não contém o fiscal deletado
            q.setPositionInLine(newPosition++);
        }

        // 6. Salvar todas as entidades restantes com as novas posições
        this.priorityQueueRepository.saveAll(priorityQueue);
        this.priorityQueueRepository.flush();
    }

    @Transactional
    public void activateFiscalInCategory(String registration, Category category) {
        // 1. Obter o UserEntity (Fiscal)
        UserEntity tax = userRepository.findByRegistration(registration)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado para ativar na categoria."));

        // 2. Obter a fila com lock (bom para concorrência)
        List<PriorityQueueEntity> queue = this.priorityQueueRepository.findByCategoryOrderByPositionInLineWithLock(category);

        // 3. VERIFICAÇÃO CRUCIAL: Verificar se o fiscal JÁ ESTÁ na fila para esta categoria
        boolean alreadyInQueue = queue.stream()
            .anyMatch(q -> q.getUserEntity().getId().equals(tax.getId()));

        if (alreadyInQueue) {
            System.out.println("Fiscal " + tax.getRegistration() + " já está na fila para a categoria " + category + ". Ignorando adição.");
            return;
        }

        // 4. Calcular a próxima posição
        int maxPosition = queue.stream()
            .mapToInt(PriorityQueueEntity::getPositionInLine)
            .max()
            .orElse(0); // Se a fila estiver vazia, a primeira posição será 1

        // 5. Criar e salvar a nova entidade
        PriorityQueueEntity newQueueEntry = PriorityQueueEntity.builder()
            .userEntity(tax)
            .category(category)
            .positionInLine(maxPosition + 1)
            .build();

        this.priorityQueueRepository.save(newQueueEntry);
        this.priorityQueueRepository.flush();
    }

    /**
     * Move um conjunto de fiscais específicos para o final da fila de uma determinada categoria.
     * Esta operação é atômica para a categoria.
     *
     * @param userIdsToMoveToEndOfQueue Um conjunto de IDs de fiscais a serem movidos.
     * @param category A categoria da fila de prioridade.
     */
    @Transactional
    public void sendFiscalToEndOfQueue(Set<Long> userIdsToMoveToEndOfQueue, Category category) {
        if (userIdsToMoveToEndOfQueue == null || userIdsToMoveToEndOfQueue.isEmpty()) {
            return;
        }

        System.out.println("=== INÍCIO sendFiscalToEndOfQueue ===");
        System.out.println("Categoria: " + category);
        System.out.println("Fiscais a mover (IDs): " + userIdsToMoveToEndOfQueue);

        try {
            // Executar reorganização com CTE
            this.priorityQueueRepository.reorderQueueWithCTE(
                userIdsToMoveToEndOfQueue,
                category.name()
            );

            System.out.println("✅ Fila reorganizada com sucesso!");

            // Log da fila após reorganização (opcional para debug)
            List<PriorityQueueEntity> updatedQueue = this.priorityQueueRepository
                .findByCategoryOrderByPositionInLine(category);

            System.out.println("Fila APÓS a operação:");
            for (PriorityQueueEntity pq : updatedQueue) {
                System.out.println("  - Fiscal ID: " + pq.getUserEntity().getId() +
                    ", Posição: " + pq.getPositionInLine() +
                    ", Matrícula: " + pq.getUserEntity().getRegistration());
            }

        } catch (Exception e) {
            System.err.println("❌ ERRO ao reorganizar fila: " + e.getMessage());
            throw e;
        }

        System.out.println("=== FIM sendFiscalToEndOfQueue ===");
    }

    /**
     * Move um único fiscal para o final da fila de uma determinada categoria.
     * Este é um método de conveniência que chama a versão mais robusta.
     *
     * @param user O UserEntity do fiscal a ser movido.
     * @param category A categoria da fila de prioridade.
     */
    @Transactional // A transação é necessária aqui também
    public void sendFiscalToEndOfQueue(UserEntity user, Category category) {
        Set<Long> userIds = new HashSet<>();
        userIds.add(user.getId());
        // Chama o método que lida com a reorganização da fila de forma robusta
        this.sendFiscalToEndOfQueue(userIds, category);
    }

    /**
     * Verifica se um fiscal está na fila de uma categoria
     */
    public boolean isFiscalInQueue(Long fiscalId, Category category) {
        try {
            return priorityQueueRepository
                .existsByUserEntityIdAndCategory(fiscalId, category);

        } catch (Exception e) {
            System.err.println("Erro ao verificar fiscal na fila: fiscalId=" + fiscalId +
                ", categoria=" + category + ", erro=" + e.getMessage());
            return false;
        }
    }

    public Map<Category, List<PriorityQueueEntity>> getAllQueuesGroupedByCategory() {
        List<PriorityQueueEntity> allQueues = priorityQueueRepository.findAll();
        return allQueues.stream()
            .sorted(Comparator.comparingInt(PriorityQueueEntity::getPositionInLine))
            .collect(Collectors.groupingBy(PriorityQueueEntity::getCategory));
    }
}

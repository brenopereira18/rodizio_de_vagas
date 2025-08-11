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

        // Bloqueia toda a fila da categoria para escrita para evitar concorrência externa.
        List<PriorityQueueEntity> currentQueue = this.priorityQueueRepository
            .findByCategoryOrderByPositionInLineWithLock(category);

        if (currentQueue.isEmpty()) {
            return;
        }

        // Separa os fiscais em duas listas: os que serão movidos e os que permanecerão
        List<PriorityQueueEntity> fiscalsToBeMoved = new ArrayList<>();
        List<PriorityQueueEntity> remainingFiscals = new ArrayList<>();

        for (PriorityQueueEntity pqEntity : currentQueue) {
            if (userIdsToMoveToEndOfQueue.contains(pqEntity.getUserEntity().getId())) {
                fiscalsToBeMoved.add(pqEntity);
            } else {
                remainingFiscals.add(pqEntity);
            }
        }

        // Reorganiza as posições dos fiscais que permanecem na parte inicial da fila
        int currentPosition = 1;
        for (PriorityQueueEntity fiscal : remainingFiscals) {
            fiscal.setPositionInLine(currentPosition++);
        }

        // Atribui as novas posições aos fiscais que foram movidos para o final
        // E os adiciona à lista 'remainingFiscals' para serem salvos junto
        for (PriorityQueueEntity fiscal : fiscalsToBeMoved) {
            fiscal.setPositionInLine(currentPosition++);
            remainingFiscals.add(fiscal);
        }

        // Salva todas as entidades da fila de uma vez.
        // Com a constraint DEFERRABLE, o banco de dados só verificará a unicidade
        // (categoria, posicao_na_fila) no COMMIT da transação.
        this.priorityQueueRepository.saveAll(remainingFiscals);
        this.priorityQueueRepository.flush();
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

    public Map<Category, List<PriorityQueueEntity>> getAllQueuesGroupedByCategory() {
        List<PriorityQueueEntity> allQueues = priorityQueueRepository.findAll();
        return allQueues.stream()
            .sorted(Comparator.comparingInt(PriorityQueueEntity::getPositionInLine))
            .collect(Collectors.groupingBy(PriorityQueueEntity::getCategory));
    }
}

package com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity.dto.PriorityQueueResponseDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.repository.PriorityQueueRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PriorityQueueService {

    private final PriorityQueueRepository priorityQueueRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Transactional
    public void deactivateFiscalFromCategory(String registration, Category category) {
        UserEntity fiscal = userRepository.findByRegistration(registration)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado"));

        validateFiscalCanLeaveQueue(fiscal.getId(), category);

        List<PriorityQueueEntity> priorityQueue = this.priorityQueueRepository.findByCategoryOrderByPositionInLineWithLock(category);

        PriorityQueueEntity taxToDeactivate = priorityQueue.stream()
            .filter(q -> q.getUserEntity().getRegistration().equals(registration))
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado na fila"));

        priorityQueue.remove(taxToDeactivate);
        this.priorityQueueRepository.delete(taxToDeactivate);
        this.priorityQueueRepository.flush();

        // Reordenar posições sem lacunas após a remoção
        for (int i = 0; i < priorityQueue.size(); i++) {
            priorityQueue.get(i).setPositionInLine(i + 1);
        }

        priorityQueueRepository.saveAll(priorityQueue);
        priorityQueueRepository.flush();

        log.info("Fiscal {} removido da fila da categoria {}", registration, category);
    }

    /**
     * Valida se o fiscal pode sair da fila verificando inscrições ativas
     */
    public void validateFiscalCanLeaveQueue(Long fiscalId, Category category) {
        LocalDateTime now = LocalDateTime.now();

        // Buscar inscrições WAITING em serviços FUTUROS
        List<EnrollmentEntity> waitingEnrollments = enrollmentRepository
            .findByUserEntityIdAndSubscriptionStatusAndWorkCategoryAndDateAfter(
                fiscalId, SubscriptionStatus.WAITING, category, now);

        // Buscar inscrições ACCEPTED em serviços FUTUROS
        List<EnrollmentEntity> acceptedEnrollments = enrollmentRepository
            .findByUserEntityIdAndSubscriptionStatusAndWorkCategoryAndDateAfter(
                fiscalId, SubscriptionStatus.ACCEPTED, category, now);

        if (!waitingEnrollments.isEmpty()) {
            String workTitles = waitingEnrollments.stream()
                .map(e -> e.getWorkEntity().getTitle() + " (" +
                    e.getWorkEntity().getServiceEndDate().format(DATE_FORMATTER) + ")")
                .collect(Collectors.joining(", "));

            throw new IllegalStateException(
                "Não é possível sair da fila. Você possui serviços futuros aguardando resposta: " + workTitles +
                    ". Recuse os serviços antes de sair da fila."
            );
        }

        if (!acceptedEnrollments.isEmpty()) {
            String workTitles = acceptedEnrollments.stream()
                .map(e -> e.getWorkEntity().getTitle() + " (" +
                    e.getWorkEntity().getServiceEndDate().format(DATE_FORMATTER) + ")")
                .collect(Collectors.joining(", "));

            throw new IllegalStateException(
                "Não é possível sair da fila. Você possui serviços futuros aceitos: " + workTitles +
                    ". Cancele as inscrições antes de sair da fila."
            );
        }
    }

    @Transactional
    public void activateFiscalInCategory(String registration, Category category) {
        UserEntity tax = userRepository.findByRegistration(registration)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado para ativar na categoria."));

        List<PriorityQueueEntity> queue = this.priorityQueueRepository.findByCategoryOrderByPositionInLineWithLock(category);

        // Verificar se o fiscal JÁ ESTÁ na fila para esta categoria
        boolean alreadyInQueue = queue.stream()
            .anyMatch(q -> q.getUserEntity().getId().equals(tax.getId()));

        if (alreadyInQueue) {
            log.warn("Fiscal {} já está na fila para a categoria {}. Ignorando adição.",
                registration, category);
            return;
        }

        // Calcular a próxima posição
        int maxPosition = queue.stream()
            .mapToInt(PriorityQueueEntity::getPositionInLine)
            .max()
            .orElse(0); // Se a fila estiver vazia, a primeira posição será 1

        // Criar e salvar a nova entidade
        PriorityQueueEntity newQueueEntry = PriorityQueueEntity.builder()
            .userEntity(tax)
            .category(category)
            .positionInLine(maxPosition + 1)
            .build();

        this.priorityQueueRepository.save(newQueueEntry);
        this.priorityQueueRepository.flush();

        log.info("Fiscal {} adicionado à fila da categoria {} na posição {}",
            registration, category, maxPosition + 1);
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

        log.debug("Movendo fiscais {} para o final da fila da categoria {}",
            userIdsToMoveToEndOfQueue, category);

        priorityQueueRepository.reorderQueueWithCTE(
            userIdsToMoveToEndOfQueue,
            category.name()
        );

        if (log.isDebugEnabled()) {
            priorityQueueRepository
                .findByCategoryOrderByPositionInLine(category)
                .forEach(pq -> log.debug("  Fila {}: Fiscal ID={}, Posição={}, Matrícula={}",
                    category,
                    pq.getUserEntity().getId(),
                    pq.getPositionInLine(),
                    pq.getUserEntity().getRegistration()));
        }

        log.info("Fila da categoria {} reorganizada. Fiscais movidos: {}",
            category, userIdsToMoveToEndOfQueue);
    }

    public void sendFiscalToEndOfQueue(UserEntity user, Category category) {
        sendFiscalToEndOfQueue(Set.of(user.getId()), category);
    }

    /**
     * Verifica se um fiscal está na fila de uma categoria
     */
    public boolean isFiscalInQueue(Long fiscalId, Category category) {
        return priorityQueueRepository.existsByUserEntityIdAndCategory(fiscalId, category);
    }

    @Transactional
    public Map<Category, List<PriorityQueueResponseDTO>> getAllQueuesGroupedByCategory() {
        return priorityQueueRepository.findAll()
            .stream()
            .sorted(Comparator.comparingInt(PriorityQueueEntity::getPositionInLine))
            .collect(Collectors.groupingBy(
                PriorityQueueEntity::getCategory,
                Collectors.mapping(this::toResponseDTO, Collectors.toList())
            ));
    }

    private PriorityQueueResponseDTO toResponseDTO(PriorityQueueEntity entity) {
        return new PriorityQueueResponseDTO(
            entity.getId(),
            entity.getCategory(),
            entity.getPositionInLine(),
            entity.getUserEntity().getFullName(),
            entity.getUserEntity().getHaveALicense()
        );
    }
}

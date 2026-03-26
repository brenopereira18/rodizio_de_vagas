package com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.dto.ResponseTaxInfosDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.dto.ResponseWorkWithTaxDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.service.NotificationService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.redis.service.RedisEventService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.repository.WorkRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final WorkRepository workRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final RedisEventService redisEventService;

    /**
     * Atualiza a resposta do fiscal (aceitar ou recusar) e o move para o final da fila.
     *
     * @param workId   id do trabalho
     * @param registration   matrícula do fiscal
     * @param accepted se aceitou ou recusou o trabalho
     */
    @Transactional
    public void respondToEnrollment(Long workId, String registration, boolean accepted) {
        EnrollmentEntity enrollment = this.enrollmentRepository.findByWorkEntityIdAndUserEntityRegistrationAndSubscriptionStatus(
                workId, registration, SubscriptionStatus.WAITING)
            .orElseThrow(() -> new ResourceNotFoundException("Inscrição pendente não encontrada."));

        if (enrollment.getSubscriptionStatus() != SubscriptionStatus.WAITING) {
            throw new IllegalStateException("Esta inscrição já foi respondida.");
        }

        if (accepted) {
            WorkEntity work = enrollment.getWorkEntity();
            int acceptedCount = enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);

            if (acceptedCount >= work.getNumberOfVacancies()) {
                throw new IllegalStateException("Não há mais vagas disponíveis para este serviço.");
            }
            handleEnrollmentAcceptance(enrollment);
        } else {
            handleEnrollmentRejection(enrollment);
        }
        finalizeWorkStatus(enrollment.getWorkEntity());
    }

    @Transactional
    public void reusePreviousEnrollment(Long workId, String registration) {
        UserEntity fiscal = userRepository.findByRegistration(registration)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado"));

        WorkEntity work = workRepository.findById(workId)
            .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        // Verifica se ainda há vaga
        int totalAccepted = enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);
        if (totalAccepted >= work.getNumberOfVacancies()) {
            throw new ResourceNotFoundException("Não há vagas disponíveis para este serviço");
        }

        // Busca a inscrição reutilizável
        EnrollmentEntity enrollment = enrollmentRepository
            .findByWorkEntityAndUserEntityAndSubscriptionStatusIn(
                work,
                fiscal,
                List.of(SubscriptionStatus.REFUSED, SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED, SubscriptionStatus.WAITING)
            )
            .orElseThrow(() -> new ResourceNotFoundException("Inscrição anterior não encontrada"));

        // Atualiza status da inscrição
        enrollment.setSubscriptionStatus(SubscriptionStatus.ACCEPTED);
        enrollmentRepository.save(enrollment);

        // Se após aceitar essa, não sobrarem vagas, fecha o serviço
        if (totalAccepted + 1 >= work.getNumberOfVacancies()) {
            work.setWorkStatus(WorkStatus.CLOSED);
            workRepository.save(work);
        }
    }

    private void handleEnrollmentAcceptance(EnrollmentEntity enrollment) {
        enrollment.setSubscriptionStatus(SubscriptionStatus.ACCEPTED);
        this.enrollmentRepository.save(enrollment);

        WorkEntity work = enrollment.getWorkEntity();
        UserEntity user = enrollment.getUserEntity();
        Category category = work.getCategory();

        // Coleta inscrições WAITING do mesmo fiscal na mesma categoria
        // antes de cancelá-las, para notificar o próximo da fila em cada serviço afetado.
        List<EnrollmentEntity> otherPendingEnrollments = this.enrollmentRepository
            .findByUserEntityIdAndWorkEntityCategoryAndSubscriptionStatus(user.getId(), category, SubscriptionStatus.WAITING)
            .stream()
            .filter(e -> !e.getWorkEntity().getId().equals(work.getId()))
            .toList();

        // Cancela em batch via UPDATE — uma única query no banco.
        this.enrollmentRepository.cancelOtherEnrollmentsInCategory(user.getId(), category, work.getId());

        // Notifica o próximo fiscal para cada serviço que ficou sem inscrição.
        otherPendingEnrollments.forEach(pendingEnrollment ->
            notificationService.notifyNextTax(pendingEnrollment.getWorkEntity(), category)
        );
    }

    private void handleEnrollmentRejection(EnrollmentEntity enrollment) {
        enrollment.setSubscriptionStatus(SubscriptionStatus.REFUSED);
        this.enrollmentRepository.save(enrollment);

        WorkEntity work = enrollment.getWorkEntity();
        
        this.redisEventService.publishTaxRefusal(enrollment.getUserEntity().getId(), enrollment.getUserEntity().getRegistration(), work.getCategory(), work.getId());

        int totalAccepted = this.enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);

        if (totalAccepted >= work.getNumberOfVacancies()) {
            closeWork(work);
        }
    }

    private void finalizeWorkStatus(WorkEntity work) {
        int totalAccepted = this.enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);
        int waitingCount = this.enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.WAITING);

        if (totalAccepted >= work.getNumberOfVacancies()) {
            closeWork(work);
            return;
        }

        // Se há fiscais suficientes aguardando para cobrir as vagas restantes,
        // não notifica ninguém novo — evita over-notificação.
        if (totalAccepted + waitingCount >= work.getNumberOfVacancies()) {
            return;
        }

        // Só neste ponto notifica o próximo da fila
        this.notificationService.notifyNextTax(work, work.getCategory());
    }

    private void closeWork(WorkEntity work) {
        work.setWorkStatus(WorkStatus.CLOSED);
        workRepository.save(work);
    }

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void processExpiredNotifications() {
        LocalTime now = LocalTime.now(ZoneId.of("America/Sao_Paulo"));
        if (now.isBefore(LocalTime.of(7, 0)) || now.isAfter(LocalTime.of(20, 0))) {
            log.debug("Fora do horário útil, scheduler de inscrições expiradas ignorado.");
            return;
        }

        List<EnrollmentEntity> expiredEnrollments = this.enrollmentRepository.findExpiredWaitingEnrollments();
        if (expiredEnrollments.isEmpty()) {
            log.debug("Nenhuma inscrição expirada encontrada.");
            return;
        }

        log.info("Processando {} inscrições expiradas.", expiredEnrollments.size());

        expiredEnrollments.forEach(e -> e.setSubscriptionStatus(SubscriptionStatus.EXPIRED));
        enrollmentRepository.saveAll(expiredEnrollments);

        // Publica eventos Redis e agrupa por categoria para notificar o próximo fiscal.
        Map<Category, List<EnrollmentEntity>> groupedByCategory = expiredEnrollments.stream()
            .collect(Collectors.groupingBy(e -> e.getWorkEntity().getCategory()));

        groupedByCategory.forEach((category, enrollments) -> {
            enrollments.forEach(enrollment ->
                redisEventService.publishTaxRefusal(
                    enrollment.getUserEntity().getId(),
                    enrollment.getUserEntity().getRegistration(),
                    enrollment.getWorkEntity().getCategory(),
                    enrollment.getWorkEntity().getId()
                )
            );
            notificationService.notifyNextTax(enrollments.get(0).getWorkEntity(), category);
        });

        log.info("Processamento de inscrições expiradas concluído. Total: {}", expiredEnrollments.size());
    }

    /**
     * Agrupa inscrições realizadas em um determinado mês e ano, organizadas por título do serviço.
     *
     * @return Lista de serviços com as respectivas inscrições e status de cada inscrito.
     */
    @Transactional
    public List<ResponseWorkWithTaxDTO> getGroupedEnrollmentsByMonth() {
        LocalDate startDate = LocalDate.now().minusDays(30);

        List<WorkEntity> works = this.workRepository.findAllWithEnrollmentsFromLastMonth(startDate.atStartOfDay())
            .stream()
            .sorted(Comparator.comparing(WorkEntity::getCreatedAt).reversed())
            .toList();

        return works.stream()
            .map(work -> new ResponseWorkWithTaxDTO(
                work.getId(),
                work.getTitle(),
                work.getLocation(),
                work.getServiceDate(),
                work.getServiceEndDate(),
                work.getManager() != null ? work.getManager().getFullName() : null,
                work.getCategory(),
                work.getNumberOfVacancies(),
                work.getObservation(),
                buildTaxInfos(work)
            ))
            .toList();
    }

    private List<ResponseTaxInfosDTO> buildTaxInfos(WorkEntity work) {
        if (work.getEnrollments() == null) return List.of();

        return work.getEnrollments().stream()
            .filter(e -> e.getSubscriptionStatus() == SubscriptionStatus.ACCEPTED)
            .map(e -> new ResponseTaxInfosDTO(
                e.getUserEntity().getFullName(),
                e.getSubscriptionStatus()
            ))
            .toList();
    }

    @Transactional
    public void cancelEnrollment(Long enrollmentId, String registration) throws AccessDeniedException {
        EnrollmentEntity enrollment = this.enrollmentRepository.findById(enrollmentId).orElseThrow(() ->
            new ResourceNotFoundException("Inscrição não encontrada"));

        if (!enrollment.getUserEntity().getRegistration().equals(registration)) {
            throw new AccessDeniedException("Você não tem permissão para cancelar esta inscrição.");
        }

        enrollment.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
        this.enrollmentRepository.save(enrollment);

        WorkEntity work = enrollment.getWorkEntity();

        // Caso ainda esteja no prazo de inscrição, ainda pode chamar o próximo da fila
        if (work.getRegistrationLimit().isAfter(LocalDateTime.now())) {
            handleOpenRegistration(work);
        } else {
            // Se passou do prazo de inscrição, serviço fica livre para qualquer fiscal
            work.setWorkStatus(WorkStatus.FREE);
            workRepository.save(work);
        }
    }

    private void handleOpenRegistration(WorkEntity work) {
        int totalAccepted = enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);

        if (totalAccepted < work.getNumberOfVacancies()) {
            notificationService.notifyNextTax(work, work.getCategory());
        } else {
            closeWork(work);
        }
    }
}

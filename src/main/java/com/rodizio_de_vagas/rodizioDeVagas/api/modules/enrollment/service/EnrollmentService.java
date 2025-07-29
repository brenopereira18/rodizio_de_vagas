package com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.dto.ResponseTaxInfosDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.dto.ResponseWorkWithTaxDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.service.NotificationService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.repository.WorkRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PriorityQueueService priorityQueueService;

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

        priorityQueueService.sendFiscalToEndOfQueue(enrollment.getUserEntity(), enrollment.getWorkEntity().getCategory());
        finalizeWorkStatus(enrollment.getWorkEntity());
    }

    @Transactional
    public void reusePreviousEnrollment(Long workId, String registration) {
        // Busca o fiscal
        UserEntity fiscal = userRepository.findByRegistration(registration)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado"));

        // Busca o serviço
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
        totalAccepted += 1;
        if (totalAccepted >= work.getNumberOfVacancies()) {
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

        List<EnrollmentEntity> worksWithRegistrationOnHold = this.enrollmentRepository
            .findByUserEntityIdAndWorkEntityCategoryAndSubscriptionStatus(user.getId(), category, SubscriptionStatus.WAITING)
            .stream()
            .filter(e -> !e.getWorkEntity().getId().equals(work.getId()))
            .toList();

        this.enrollmentRepository.cancelOtherEnrollmentsInCategory(user.getId(), category, work.getId());

        worksWithRegistrationOnHold.forEach(pendingEnrollment ->
            notificationService.notifyNextFiscal(pendingEnrollment.getWorkEntity(), category)
        );
    }

    private void handleEnrollmentRejection(EnrollmentEntity enrollment) {
        enrollment.setSubscriptionStatus(SubscriptionStatus.REFUSED);
        this.enrollmentRepository.save(enrollment);

        WorkEntity work = enrollment.getWorkEntity();
        Category category = work.getCategory();

        int totalAccepted = this.enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);

        if (totalAccepted < work.getNumberOfVacancies()) {
            this.notificationService.notifyNextFiscal(work, category);
        } else {
            closeWork(work);
        }
    }

    private void finalizeWorkStatus(WorkEntity work) {
        int totalAccepted = this.enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);
        int totalVacancies = work.getNumberOfVacancies();

        if (totalAccepted >= totalVacancies) {
            closeWork(work);
            return;
        }

        // Só notificar próximo fiscal se ninguém com prioridade estiver pendente
        List<EnrollmentEntity> waitingList = this.enrollmentRepository
            .findByWorkEntityIdAndSubscriptionStatus(work.getId(), SubscriptionStatus.WAITING);

        if (!waitingList.isEmpty()) {
            // Ainda há fiscais aguardando, não notifica ninguém novo ainda
            return;
        }

        // Só neste ponto notifica o próximo da fila
        this.notificationService.notifyNextFiscal(work, work.getCategory());
    }

    private void closeWork(WorkEntity work) {
        work.setWorkStatus(WorkStatus.CLOSED);
        workRepository.save(work);
    }

    @Scheduled(cron = "0 */10 * * * *")
    @Transactional
    public void processExpiredNotifications() {
        List<EnrollmentEntity> expiredEnrollments = this.enrollmentRepository.findExpiredWaitingEnrollments();

        for (EnrollmentEntity enrollment : expiredEnrollments) {
            enrollment.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            this.enrollmentRepository.save(enrollment);

            // Move fiscal para o final da fila
            priorityQueueService.sendFiscalToEndOfQueue(enrollment.getUserEntity(), enrollment.getWorkEntity().getCategory());

            // Notifica o próximo fiscal
            notificationService.notifyNextFiscal(enrollment.getWorkEntity(), enrollment.getWorkEntity().getCategory());
        }
    }

    /**
     * Agrupa inscrições realizadas em um determinado mês e ano, organizadas por título do serviço.
     *
     * @return Lista de serviços com as respectivas inscrições e status de cada inscrito.
     */
    public List<ResponseWorkWithTaxDTO> getGroupedEnrollmentsByMonth() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);

        List<WorkEntity> works = this.workRepository.findAllWithEnrollmentsFromLastMonth(startDate.atStartOfDay())
            .stream()
            .sorted(Comparator.comparing(WorkEntity::getCreatedAt).reversed())
            .toList();
        Map<WorkEntity, List<ResponseTaxInfosDTO>> groupedEnrollments = groupEnrollmentsFromWorks(works);
        return buildResponse(groupedEnrollments);
    }

    // Agrupa as inscrições por título do serviço e mapeia as informações fiscais de cada inscrito
    private Map<WorkEntity, List<ResponseTaxInfosDTO>> groupEnrollmentsFromWorks(List<WorkEntity> works) {
        return works.stream()
            .collect(Collectors.toMap(
                work -> work,
                work -> {
                    if (work.getEnrollments() == null) return List.of();
                    return work.getEnrollments().stream()
                        .filter(e -> e.getSubscriptionStatus() == SubscriptionStatus.ACCEPTED)
                        .map(this::convertToTaxInfosDTO)
                        .toList();
                },
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
    }

    private ResponseTaxInfosDTO convertToTaxInfosDTO(EnrollmentEntity enrollment) {
        return new ResponseTaxInfosDTO(
            enrollment.getUserEntity().getFullName(),
            enrollment.getSubscriptionStatus()
        );
    }

    // Constrói a lista de resposta formatada para retornar os grupos com os respectivos inscritos
    private List<ResponseWorkWithTaxDTO> buildResponse(Map<WorkEntity, List<ResponseTaxInfosDTO>> groupedEnrollments) {
        return groupedEnrollments.entrySet().stream()
            .map(entry -> {
                WorkEntity work = entry.getKey();
                return new ResponseWorkWithTaxDTO(
                    work.getTitle(),
                    work.getLocation(),
                    work.getServiceDate(),
                    work.getManager(),
                    work.getCategory(),
                    work.getNumberOfVacancies(),
                    entry.getValue()
                );
            })
            .collect(Collectors.toList());
    }

    /**
     * Cancela inscrição do fiscal em um trabalho.
     *
     * @param enrollmentId id da inscrição
     */
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
            notificationService.notifyNextFiscal(work, work.getCategory());
        } else {
            closeWork(work);
        }
    }
}

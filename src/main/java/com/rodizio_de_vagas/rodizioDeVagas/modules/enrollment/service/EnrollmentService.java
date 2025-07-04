package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.service;

import com.rodizio_de_vagas.rodizioDeVagas.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto.ResponseTaxInfosDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto.ResponseWorkWithTaxDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.notification.service.NotificationService;
import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.repository.WorkRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
     * @param userId   id do fiscal
     * @param accepted se aceitou ou recusou o trabalho
     */
    @Transactional
    public void respondToEnrollment(Long workId, Long userId, boolean accepted) {
        EnrollmentEntity enrollment = this.enrollmentRepository.findByWorkEntityIdAndUserEntityIdAndSubscriptionStatus(
                workId, userId, SubscriptionStatus.WAITING)
            .orElseThrow(() -> new ResourceNotFoundException("Inscrição pendente não encontrada."));

        if (enrollment.getSubscriptionStatus() != SubscriptionStatus.WAITING) {
            throw new IllegalStateException("Esta inscrição já foi respondida.");
        }

        if (accepted) {
            handleEnrollmentAcceptance(enrollment);
        } else {
            handleEnrollmentRejection(enrollment);
        }
        priorityQueueService.sendFiscalToEndOfQueue(enrollment.getUserEntity(), enrollment.getWorkEntity().getCategory());
        finalizeWorkStatus(enrollment.getWorkEntity());
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
        boolean hasPending = this.enrollmentRepository.existsByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.WAITING);

        if (totalAccepted < work.getNumberOfVacancies() && !hasPending) {
            this.notificationService.notifyNextFiscal(work, work.getCategory());
        } else {
            closeWork(work);
        }
    }

    private void closeWork(WorkEntity work) {
        work.setWorkStatus(WorkStatus.CLOSED);
        workRepository.save(work);
    }

    @Scheduled(cron = "0 0 * * * *")
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
     * @param month Mês das inscrições a serem consultadas.
     * @param year  Ano das inscrições a serem consultadas.
     * @return Lista de serviços com as respectivas inscrições e status de cada inscrito.
     */
    public List<ResponseWorkWithTaxDTO> getGroupedEnrollmentsByMonth(int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<EnrollmentEntity> enrollments = this.enrollmentRepository.findConfirmedEnrollmentsByMonth(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));
        Map<WorkEntity, List<ResponseTaxInfosDTO>> groupedEnrollments = groupEnrollments(enrollments);
        return buildResponse(groupedEnrollments);
    }

    // Agrupa as inscrições por título do serviço e mapeia as informações fiscais de cada inscrito
    private Map<WorkEntity, List<ResponseTaxInfosDTO>> groupEnrollments(List<EnrollmentEntity> enrollments) {
        return enrollments.stream()
            .collect(Collectors.groupingBy(
                EnrollmentEntity::getWorkEntity,
                Collectors.mapping(
                    e -> new ResponseTaxInfosDTO(e.getUserEntity().getFullName(), e.getSubscriptionStatus()),
                    Collectors.toList()
                )
            ));
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
    public void cancelEnrollment(Long enrollmentId) {
        EnrollmentEntity enrollment = this.enrollmentRepository.findById(enrollmentId).orElseThrow(() ->
            new ResourceNotFoundException("Inscrição não encontrada"));

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

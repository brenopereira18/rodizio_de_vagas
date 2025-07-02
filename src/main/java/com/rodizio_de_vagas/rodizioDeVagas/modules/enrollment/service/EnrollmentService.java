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
        EnrollmentEntity enrollment = enrollmentRepository.findByWorkEntityIdAndUserEntityIdAndSubscriptionStatus(
                workId, userId, SubscriptionStatus.WAITING)
            .orElseThrow(() -> new ResourceNotFoundException("Inscrição pendente não encontrada."));

        WorkEntity work = enrollment.getWorkEntity();
        UserEntity user = enrollment.getUserEntity();
        Category category = work.getCategory();

        if (accepted) {
            enrollment.setSubscriptionStatus(SubscriptionStatus.ACCEPTED);
            enrollmentRepository.save(enrollment);
        } else {
            enrollment.setSubscriptionStatus(SubscriptionStatus.REFUSED);
            enrollmentRepository.save(enrollment);
        }

        // Move o fiscal para o final da fila
        priorityQueueService.sendFiscalToEndOfQueue(user, category);

        // Verifica se ainda há vagas e notifica o próximo fiscal
        int totalAccepted = enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);

        if (totalAccepted < work.getNumberOfVacancies()) {
            notificationService.notifyNextFiscal(work, category);
        } else {
            work.setWorkStatus(WorkStatus.CLOSED);
            workRepository.save(work);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void processExpiredNotifications() {
        List<EnrollmentEntity> expiredEnrollments = enrollmentRepository.findExpiredWaitingEnrollments();

        for (EnrollmentEntity enrollment : expiredEnrollments) {
            enrollment.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            enrollmentRepository.save(enrollment);

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

        // Agrupa as inscrições por título do serviço e mapeia as informações fiscais de cada inscrito
        Map<WorkEntity, List<ResponseTaxInfosDTO>> grouped = enrollments.stream()
            .collect(Collectors.groupingBy(
                EnrollmentEntity::getWorkEntity,
                Collectors.mapping(
                    e -> new ResponseTaxInfosDTO(e.getUserEntity().getFullName(), e.getSubscriptionStatus()),
                    Collectors.toList()
                )
            ));

        // Constrói a lista de resposta formatada para retornar os grupos com os respectivos inscritos
        return grouped.entrySet().stream()
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

        // Caso ainda esteja no prazo de inscrição
        if (work.getRegistrationLimit().isAfter(LocalDateTime.now())) {
            // Ainda pode chamar o próximo da fila
            int totalAccepted = enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);

            if (totalAccepted < work.getNumberOfVacancies()) {
                // Notifica o próximo fiscal da fila
                notificationService.notifyNextFiscal(work, work.getCategory());
            } else {
                // Se todas as vagas estiverem preenchidas, fecha o serviço
                work.setWorkStatus(WorkStatus.CLOSED);
                workRepository.save(work);
            }
        } else {
            // Se passou do prazo de inscrição, serviço fica livre para qualquer fiscal
            work.setWorkStatus(WorkStatus.FREE);
            workRepository.save(work);
        }
    }
}

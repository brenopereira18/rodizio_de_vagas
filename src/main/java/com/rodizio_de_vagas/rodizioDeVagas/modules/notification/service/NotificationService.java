package com.rodizio_de_vagas.rodizioDeVagas.modules.notification.service;

import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.notification.entity.NotificationEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.notification.repository.NotificationRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.repository.PriorityQueueRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PriorityQueueRepository priorityQueueRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public void notifyInitialsTax(WorkEntity work) {
        Category category = work.getCategory();
        int numberOfVacancies = work.getNumberOfVacancies();

        List<PriorityQueueEntity> queue = this.priorityQueueRepository.findByCategoryOrderByPositionInLine(category);

        // Seleciona os primeiros fiscais da fila
        List<PriorityQueueEntity> selectedTax = queue.stream()
            .limit(numberOfVacancies)
            .toList();

        for (PriorityQueueEntity taxQueue : selectedTax) {
            UserEntity tax = taxQueue.getUserEntity();
            notifyTax(tax, work);
        }
    }

    @Transactional
    public void notifyNextFiscal(WorkEntity work, Category category) {
        List<PriorityQueueEntity> queue = this.priorityQueueRepository.findByCategoryOrderByPositionInLine(category);

        // Verifica quem já foi notificado ou já está inscrito nesse trabalho.
        List<Long> notifiedUserIds = notificationRepository.findUserIdsByWorkId(work.getId());
        List<Long> enrolledUserIds = enrollmentRepository.findUserIdsByWorkId(work.getId());

        // Filtra o próximo fiscal que ainda não foi notificado e não está inscrito.
        Optional<PriorityQueueEntity> nextFiscalOpt = queue.stream()
            .filter(q -> !notifiedUserIds.contains(q.getUserEntity().getId()) && !enrolledUserIds.contains(q.getUserEntity().getId()))
            .findFirst();

        if (nextFiscalOpt.isEmpty()) {
            System.out.println("Nenhum fiscal disponível para notificação.");
            return;
        }

        UserEntity nextFiscal = nextFiscalOpt.get().getUserEntity();
        notifyTax(nextFiscal, work);
    }


    private void notifyTax(UserEntity tax, WorkEntity work) {
        NotificationEntity notification = NotificationEntity.builder()
            .workEntity(work)
            .userEntity(tax)
            .shippingDate(LocalDateTime.now())
            .responseDeadline(LocalDateTime.now().plusHours(2))
            .message("Olá " + tax.getFullName() + ", temos um serviço disponível no qual você tem prioridade. Acesse o link para visualizá-lo.")
            .workLink("url-do-trabalho")
            .build();

        this.notificationRepository.save(notification);

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .workEntity(work)
            .userEntity(tax)
            .subscriptionStatus(SubscriptionStatus.WAITING)
            .build();

        this.enrollmentRepository.save(enrollment);

        // (Opcional) Disparar notificação real via WhatsApp
    }


}

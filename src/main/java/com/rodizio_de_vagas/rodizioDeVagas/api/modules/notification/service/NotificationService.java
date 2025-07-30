package com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.evolutionAPI.service.WhatsappNotificationService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.entity.NotificationEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.repository.NotificationRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.repository.PriorityQueueRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.repository.WorkRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
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

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private WhatsappNotificationService whatsappNotificationService;

    public void notifyInitialsTax(WorkEntity work) {
        Category category = work.getCategory();
        int numberOfVacancies = work.getNumberOfVacancies();

        List<PriorityQueueEntity> queue = this.priorityQueueRepository.findByCategoryOrderByPositionInLine(category);

        // Seleciona os primeiros fiscais da fila
        List<PriorityQueueEntity> selectedTax = queue.stream()
            .limit(numberOfVacancies)
            .toList();

        Flux.fromIterable(selectedTax)
            .delayElements(Duration.ofSeconds(8))
            .doOnNext(taxQueue -> {
                UserEntity tax = taxQueue.getUserEntity();
                notifyTax(tax, work);
            })
            .subscribe();
    }

    @Transactional
    public void notifyNextTax(WorkEntity work, Category category) {
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
            work.setWorkStatus(WorkStatus.FREE);
            workRepository.save(work);
            return;
        }

        UserEntity nextFiscal = nextFiscalOpt.get().getUserEntity();
        Mono.delay(Duration.ofSeconds(8))
            .doOnNext(i -> notifyTax(nextFiscal, work))
            .subscribe();
    }

    private void notifyTax(UserEntity tax, WorkEntity work) {
        String link = "https://rodizio-de-vagas.onrender.com/home/servicos/disponiveis";
        String message = "Olá " + tax.getFullName() + ", temos um serviço disponível da categoria " + work.getCategory() +
            " no qual você tem prioridade. Acesse o link para visualizá-lo: " + link;

        NotificationEntity notification = NotificationEntity.builder()
            .workEntity(work)
            .userEntity(tax)
            .shippingDate(LocalDateTime.now())
            .responseDeadline(LocalDateTime.now().plusHours(4))
            .message(message)
            .build();

        this.notificationRepository.save(notification);

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .workEntity(work)
            .userEntity(tax)
            .subscriptionStatus(SubscriptionStatus.WAITING)
            .build();

        this.enrollmentRepository.save(enrollment);
        whatsappNotificationService.sendMessage(tax.getPhoneNumber(), notification.getMessage());
    }
}

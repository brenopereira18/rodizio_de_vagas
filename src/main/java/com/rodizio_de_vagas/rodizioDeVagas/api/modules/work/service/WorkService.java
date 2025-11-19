package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.service.NotificationService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.RequestCreateOrUpdateWorkDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.WorkWithEnrollment;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.repository.WorkRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class WorkService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    public WorkEntity createWork(RequestCreateOrUpdateWorkDTO dto) {
        UserEntity manager = this.userRepository.findById(dto.managerId()).orElseThrow(() ->
            new ResourceNotFoundException("Supervisor não encontrado."));

        WorkEntity work = WorkEntity.builder()
            .title(dto.title())
            .location(dto.location())
            .serviceDate(dto.serviceDate())
            .serviceEndDate(dto.serviceEndDate())
            .manager(manager)
            .registrationLimit(dto.serviceDate().minusDays(1))
            .category(dto.category())
            .numberOfVacancies(dto.numberOfVacancies())
            .build();

        work = this.workRepository.save(work);
        this.notificationService.notifyInitialsTax(work);
        return work;
    }

    public List<WorkWithEnrollment> getWorksByFilter(String registration, String filter) {
        if ("INSCRITOS".equalsIgnoreCase(filter)) {
            return getEnrolledWorksForTax(registration);
        } else if ("FREE".equalsIgnoreCase(filter)) {
            return getFreeWorks(registration);
        } else {
            return getAvailableWorksForTax(registration);
        }
    }

    public List<WorkWithEnrollment> getFreeWorks(String registration) {
        // Busca todos os serviços com status FREE e data futura
        List<WorkEntity> works = this.workRepository.findByWorkStatusAndServiceDateAfter(
            WorkStatus.FREE,
            LocalDateTime.now()
        );

        // Busca todas as inscrições do fiscal
        List<EnrollmentEntity> userEnrollments = this.enrollmentRepository
            .findByUserEntityRegistration(registration);

        // Coleta os IDs dos serviços onde o fiscal tem inscrição ACCEPTED
        List<Long> acceptedWorkIds = userEnrollments.stream()
            .filter(e -> e.getSubscriptionStatus() == SubscriptionStatus.ACCEPTED)
            .map(e -> e.getWorkEntity().getId())
            .toList();

        // Retorna os serviços onde o fiscal NÃO tem inscrição ACCEPTED
        return works.stream()
            .filter(work -> !acceptedWorkIds.contains(work.getId()))
            .map(work -> {
                boolean hasPriority = true; // <- força como verdadeiro para não exibir aviso
                boolean canApply = true;    // <- sempre pode se inscrever em serviços FREE
                return new WorkWithEnrollment(work, null, hasPriority, canApply);
            })
            .toList();
    }

    public int countFreeWorksForUser(String registration) {
        return getFreeWorks(registration).size();
    }

    public List<WorkWithEnrollment> getEnrolledWorksForTax(String registration) {
        List<EnrollmentEntity> acceptedEnrollments = this.enrollmentRepository
            .findByUserEntityRegistrationAndSubscriptionStatus(registration, SubscriptionStatus.ACCEPTED);

        return acceptedEnrollments.stream()
            .map(EnrollmentEntity::getWorkEntity)
            .filter(work -> work.getServiceDate().isAfter(LocalDateTime.now()))
            .map(work -> {
                EnrollmentEntity enrollment = acceptedEnrollments.stream()
                    .filter(e -> e.getWorkEntity().getId().equals(work.getId()))
                    .findFirst()
                    .orElse(null);

                boolean hasPriority = isUserInPriorityForWork(work.getId(), registration, work.getNumberOfVacancies());
                boolean canApply = work.getWorkStatus() != WorkStatus.FREE &&
                    shouldAllowUserToApply(work, enrollment, hasPriority);

                return new WorkWithEnrollment(work, enrollment, hasPriority, canApply);
            })
            .toList();
    }

    public List<WorkWithEnrollment> getAvailableWorksForTax(String registration) {
        this.updateExpiredWorksToFree();

        List<WorkEntity> availableWorks = this.workRepository.findByWorkStatusInAndServiceDateAfter(
            List.of(WorkStatus.OPEN),
            LocalDateTime.now()
        );

        List<EnrollmentEntity> userEnrollments = this.enrollmentRepository
            .findByUserEntityRegistration(registration);

        return availableWorks.stream()
            .map(work -> {
                EnrollmentEntity enrollment = userEnrollments.stream()
                    .filter(e -> e.getWorkEntity().getId().equals(work.getId()))
                    .findFirst()
                    .orElse(null);

                if (enrollment != null &&
                    (enrollment.getSubscriptionStatus() == SubscriptionStatus.ACCEPTED ||
                        enrollment.getSubscriptionStatus() == SubscriptionStatus.REFUSED)) {
                    return null;
                }

                boolean hasPriority = isUserInPriorityForWork(work.getId(), registration, work.getNumberOfVacancies());
                boolean canApply = shouldAllowUserToApply(work, enrollment, hasPriority);

                return new WorkWithEnrollment(work, enrollment, hasPriority, canApply);
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public boolean isUserInPriorityForWork(Long workId, String registration, int numberOfVacancies) {
        List<EnrollmentEntity> waitingList = this.enrollmentRepository
            .findByWorkEntityIdAndSubscriptionStatus(workId, SubscriptionStatus.WAITING);

        return waitingList.stream()
            .map(e -> e.getUserEntity().getRegistration())
            .limit(numberOfVacancies)
            .anyMatch(reg -> reg.equals(registration));
    }

    private boolean shouldAllowUserToApply(WorkEntity work, EnrollmentEntity enrollment, boolean hasPriority) {
        if (work.getWorkStatus() == WorkStatus.FREE) {
            return true;
        }

        if (work.getWorkStatus() == WorkStatus.OPEN && hasPriority) {
            return enrollment == null || enrollment.getSubscriptionStatus() == SubscriptionStatus.WAITING;
        }

        return false;
    }

    public WorkEntity updateWork(RequestCreateOrUpdateWorkDTO dto) {
        WorkEntity work = workRepository.findById(dto.id())
            .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));

        UserEntity manager = userRepository.findById(dto.managerId())
            .orElseThrow(() -> new ResourceNotFoundException("Supervisor não encontrado."));

        work.setTitle(dto.title());
        work.setLocation(dto.location());
        work.setServiceDate(dto.serviceDate());
        work.setServiceEndDate(dto.serviceEndDate());
        work.setRegistrationLimit(dto.serviceDate().minusDays(1));
        work.setManager(manager);
        work.setCategory(dto.category());
        work.setNumberOfVacancies(dto.numberOfVacancies());

        return workRepository.save(work);
    }

    public void deleteWork(Long id) {
        WorkEntity work = this.workRepository.findById(id).orElseThrow(() ->
            new ResourceNotFoundException("Serviço não encontrado"));

        this.workRepository.delete(work);
    }

    // Atualiza o status do trabalho para "livre" se passar da data limite de inscrições e ainda tiver sobrando vaga
    // Verifica de hora em hora, todos os dias
    @Scheduled(cron = "0 0 * * * *")
    public void updateExpiredWorksToFree() {
        List<WorkEntity> works = this.workRepository.findExpiredWorks();

        for (WorkEntity work : works) {
            if (work.getServiceDate().isBefore(LocalDateTime.now())) {
                work.setWorkStatus(WorkStatus.CLOSED);
                continue;
            }

            int totalEnrollments = enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);

            if (totalEnrollments < work.getNumberOfVacancies()) {
                // Ainda tem vaga sobrando, pode ficar LIVRE
                work.setWorkStatus(WorkStatus.FREE);
            } else {
                // Se todas as vagas foram preenchidas, mantemos CLOSED
                work.setWorkStatus(WorkStatus.CLOSED);
            }
        }
        this.workRepository.saveAll(works);
    }

    public void updateObservation(Long id, String observation) {
        WorkEntity servico = workRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        servico.setObservation(observation);
        workRepository.save(servico);
    }
}
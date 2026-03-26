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
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkFilterType;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.WorkResponseDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.WorkWithEnrollmentDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.repository.WorkRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Service
@RequiredArgsConstructor
public class WorkService {

    private final UserRepository userRepository;
    private final WorkRepository workRepository;
    private final NotificationService notificationService;
    private final EnrollmentRepository enrollmentRepository;

    public WorkResponseDTO createWork(RequestCreateOrUpdateWorkDTO dto) {
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
        return toResponseDTO(work);
    }

    public List<WorkWithEnrollmentDTO> getWorksByFilter(String registration, WorkFilterType filter) {
        return switch (filter) {
            case INSCRITOS -> getEnrolledWorksForTax(registration);
            case FREE      -> getFreeWorks(registration);
            default        -> getAvailableWorksForTax(registration);
        };
    }

    public List<WorkWithEnrollmentDTO> getFreeWorks(String registration) {
        // Busca todos os serviços com status FREE e data futura
        List<WorkEntity> works = this.workRepository.findByWorkStatusAndServiceDateAfter(
            WorkStatus.FREE,
            LocalDateTime.now()
        );

        // Busca todas as inscrições do fiscal
        // Coleta os IDs dos serviços onde o fiscal tem inscrição ACCEPTED
        List<Long> acceptedWorkIds = enrollmentRepository
            .findByUserEntityRegistration(registration)
            .stream()
            .filter(e -> e.getSubscriptionStatus() == SubscriptionStatus.ACCEPTED)
            .map(e -> e.getWorkEntity().getId())
            .toList();

        // Retorna os serviços onde o fiscal NÃO tem inscrição ACCEPTED
        return works.stream()
            .filter(work -> !acceptedWorkIds.contains(work.getId()))
            .map(work -> toWorkWithEnrollmentDTO(work, null, true, true))
            .toList();
    }

    public long countFreeWorksForUser(String registration) {
        List<WorkEntity> freeWorks = workRepository.findByWorkStatusAndServiceDateAfter(
            WorkStatus.FREE, LocalDateTime.now());

        List<Long> acceptedWorkIds = enrollmentRepository
            .findByUserEntityRegistration(registration)
            .stream()
            .filter(e -> e.getSubscriptionStatus() == SubscriptionStatus.ACCEPTED)
            .map(e -> e.getWorkEntity().getId())
            .toList();

        return freeWorks.stream()
            .filter(work -> !acceptedWorkIds.contains(work.getId()))
            .count();
    }

    public List<WorkWithEnrollmentDTO> getEnrolledWorksForTax(String registration) {
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

                return toWorkWithEnrollmentDTO(work, enrollment, hasPriority, canApply);
            })
            .toList();
    }

    public List<WorkWithEnrollmentDTO> getAvailableWorksForTax(String registration) {
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

                return toWorkWithEnrollmentDTO(work, enrollment, hasPriority, canApply);
            })
            .filter(Objects::nonNull)
            .toList();
    }

    public boolean isUserInPriorityForWork(Long workId, String registration, int numberOfVacancies) {
        return enrollmentRepository
            .findByWorkEntityIdAndSubscriptionStatus(workId, SubscriptionStatus.WAITING)
            .stream()
            .map(e -> e.getUserEntity().getRegistration())
            .limit(numberOfVacancies)
            .anyMatch(reg -> reg.equals(registration));
    }

    @Transactional
    public WorkResponseDTO updateWork(RequestCreateOrUpdateWorkDTO dto) {
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

        return toResponseDTO(workRepository.save(work));
    }

    @Transactional
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

        works.forEach(work -> {
            if (work.getServiceDate().isBefore(LocalDateTime.now())) {
                work.setWorkStatus(WorkStatus.CLOSED);
                return;
            }

            int totalAccepted = enrollmentRepository
                .countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);

            work.setWorkStatus(
                totalAccepted < work.getNumberOfVacancies() ? WorkStatus.FREE : WorkStatus.CLOSED
            );
        });
        this.workRepository.saveAll(works);
    }

    @Transactional
    public void updateObservation(Long id, String observation) {
        WorkEntity servico = workRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado"));

        servico.setObservation(observation);
        workRepository.save(servico);
    }

    private WorkResponseDTO toResponseDTO(WorkEntity work) {
        return new WorkResponseDTO(
            work.getId(),
            work.getTitle(),
            work.getLocation(),
            work.getServiceDate(),
            work.getServiceEndDate(),
            work.getManager() != null ? work.getManager().getId() : null,
            work.getManager() != null ? work.getManager().getFullName() : null,
            work.getRegistrationLimit(),
            work.getCategory(),
            work.getNumberOfVacancies(),
            work.getWorkStatus(),
            work.getObservation(),
            work.getCreatedAt()
        );
    }

    private WorkWithEnrollmentDTO toWorkWithEnrollmentDTO(
        WorkEntity work,
        EnrollmentEntity enrollment,
        boolean hasPriority,
        boolean canApply) {

        return new WorkWithEnrollmentDTO(
            work.getId(),
            work.getTitle(),
            work.getLocation(),
            work.getServiceDate(),
            work.getServiceEndDate(),
            work.getManager() != null ? work.getManager().getFullName() : null,
            work.getCategory(),
            work.getNumberOfVacancies(),
            work.getWorkStatus(),
            work.getObservation(),
            enrollment != null ? enrollment.getId() : null,
            enrollment != null ? enrollment.getSubscriptionStatus() : null,
            hasPriority,
            canApply
        );
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
}
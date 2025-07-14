package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.service.NotificationService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.RequestCreateOrUpdateWorkDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.repository.WorkRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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
            .manager(manager)
            .registrationLimit(dto.serviceDate().minusDays(1))
            .category(dto.category())
            .numberOfVacancies(dto.numberOfVacancies())
            .build();

        work = this.workRepository.save(work);
        this.notificationService.notifyInitialsTax(work);
        return work;
    }

    public List<WorkEntity> getAllWorks(String status) {
        this.updateExpiredWorksToFree();
        List<WorkEntity> works;

        if (status.equalsIgnoreCase("DISPONIVEIS")) {
            works = this.workRepository.findByWorkStatusInAndServiceDateAfter(
                List.of(WorkStatus.OPEN, WorkStatus.FREE),
                LocalDateTime.now());
        } else if (status.equalsIgnoreCase("ENCERRADAS")) {
            works = this.workRepository.findByWorkStatus(WorkStatus.CLOSED);
        } else {
            throw new IllegalArgumentException("Filtro de status inválido.");
        }
        return works;
    }

    public WorkEntity updateWork(RequestCreateOrUpdateWorkDTO dto) {
        WorkEntity work = workRepository.findById(dto.id())
            .orElseThrow(() -> new ResourceNotFoundException("Serviço não encontrado."));

        UserEntity manager = userRepository.findById(dto.managerId())
            .orElseThrow(() -> new ResourceNotFoundException("Supervisor não encontrado."));

        work.setTitle(dto.title());
        work.setLocation(dto.location());
        work.setServiceDate(dto.serviceDate());
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
}

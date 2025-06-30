package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.service;

import com.rodizio_de_vagas.rodizioDeVagas.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.repository.WorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private UserRepository userRepository;

    public EnrollmentEntity createEnrollment(Long workId, Long userId) {
        WorkEntity work = this.workRepository.findById(workId).orElseThrow(() ->
            new ResourceNotFoundException("Trabalho não encontrado"));

        int totalEnrollments = this.enrollmentRepository.countByWorkEntityAndSubscriptionStatus(work, SubscriptionStatus.ACCEPTED);

        if (totalEnrollments >= work.getNumberOfVacancies()) {
            work.setWorkStatus(WorkStatus.CLOSED);
            this.workRepository.save(work);
            throw new RuntimeException("Não a mais vagas disponíveis para esse serviço");
        }

        EnrollmentEntity enrollment = EnrollmentEntity.builder()
            .workEntity(work)
            .userEntity(this.userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado")))
            .subscriptionStatus(SubscriptionStatus.ACCEPTED)
            .build();

        return this.enrollmentRepository.save(enrollment);
    }

    public void cancelEnrollment(Long enrollmentId) {
        EnrollmentEntity enrollment = this.enrollmentRepository.findById(enrollmentId).orElseThrow(() ->
            new ResourceNotFoundException("Inscrição não encontrada"));

        enrollment.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
        this.enrollmentRepository.save(enrollment);

        WorkEntity work = enrollment.getWorkEntity();

        if (work.getWorkStatus() == WorkStatus.CLOSED) {
            work.setWorkStatus(WorkStatus.FREE);
            this.workRepository.save(work);
        }
    }
}

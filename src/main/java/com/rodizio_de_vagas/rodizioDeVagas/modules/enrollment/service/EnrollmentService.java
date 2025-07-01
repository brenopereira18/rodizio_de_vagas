package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.service;

import com.rodizio_de_vagas.rodizioDeVagas.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto.ResponseTaxInfosDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto.ResponseWorkWithTaxDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.repository.WorkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    /**
     * Agrupa inscrições realizadas em um determinado mês e ano, organizadas por título do serviço.
     *
     * @param month Mês das inscrições a serem consultadas.
     * @param year Ano das inscrições a serem consultadas.
     * @return Lista de serviços com as respectivas inscrições e status de cada inscrito.
     */
    public List<ResponseWorkWithTaxDTO> getGroupedEnrollmentsByMonth(int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        List<EnrollmentEntity> enrollments = this.enrollmentRepository.findByWorkEntityServiceDateBetween(startDate.atStartOfDay(), endDate.atTime(LocalTime.MAX));

        // Agrupa as inscrições por título do serviço e mapeia as informações fiscais de cada inscrito
        Map<String, List<ResponseTaxInfosDTO>> grouped = enrollments.stream()
            .collect(Collectors.groupingBy(
                enrollment -> enrollment.getWorkEntity().getTitle(),
                Collectors.mapping(
                    enrollment -> new ResponseTaxInfosDTO(enrollment.getUserEntity().getFullName(), enrollment.getSubscriptionStatus()),
                    Collectors.toList()
                )
            ));

        // Constrói a lista de resposta formatada para retornar os grupos com os respectivos inscritos
        return grouped.entrySet().stream()
            .map(entry -> new ResponseWorkWithTaxDTO(entry.getKey(), entry.getValue())).collect(Collectors.toList());
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

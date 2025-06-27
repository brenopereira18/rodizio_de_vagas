package com.rodizio_de_vagas.rodizioDeVagas.modules.work.service;

import com.rodizio_de_vagas.rodizioDeVagas.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.dto.RequestCreateWorkDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.repository.WorkRepository;
import org.hibernate.jdbc.Work;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    public WorkEntity createWork(RequestCreateWorkDTO dto) {
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
        return this.workRepository.save(work);
    }

    public List<WorkEntity> getAllWorks(String status) {
        List<WorkEntity> works;

        if (status.equalsIgnoreCase("DISPONIVEIS")) {
            works = this.workRepository.findByWorkStatusIn(List.of(WorkStatus.OPEN, WorkStatus.FREE));
        } else if (status.equalsIgnoreCase("ENCERRADAS")) {
            works = this.workRepository.findByWorkStatus(WorkStatus.CLOSED);
        } else {
            throw new IllegalArgumentException("Filtro de status inválido.");
        }
        return works;
    }

    public void deleteWork(Long id) {
        WorkEntity work = this.workRepository.findById(id).orElseThrow(() ->
            new ResourceNotFoundException("Serviço não encontrado"));

        this.workRepository.delete(work);
    }
}

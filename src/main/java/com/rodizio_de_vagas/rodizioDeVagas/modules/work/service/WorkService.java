package com.rodizio_de_vagas.rodizioDeVagas.modules.work.service;

import com.rodizio_de_vagas.rodizioDeVagas.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.dto.RequestCreateWorkDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkService {

    @Autowired
    private UserRepository userRepository;

    public WorkEntity createWork(RequestCreateWorkDTO dto) {
        UserEntity manager = this.userRepository.findById(dto.manager().getId()).orElseThrow(() ->
            new ResourceNotFoundException("Supervisor não encontrado."));

        WorkEntity work = new WorkEntity();
        work.setTitle(dto.title());
        work.setLocation(dto.location());
        work.setServiceDate(dto.serviceDate());
        work.setManager(manager);
        work.setRegistrationLimit(dto.serviceDate().minusDays(1));
        work.setCategory(dto.category());
        work.setNumberOfVacancies(dto.numberOfVacancies());

        return work;
    }
}

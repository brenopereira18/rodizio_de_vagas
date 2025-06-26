package com.rodizio_de_vagas.rodizioDeVagas.modules.user.service;

import com.rodizio_de_vagas.rodizioDeVagas.exceptions.EntityAlreadyExistsException;
import com.rodizio_de_vagas.rodizioDeVagas.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.dto.RequestCreateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.dto.RequestUpdateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.dto.ResponseManagerDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserEntity createUser(RequestCreateUserDTO userDTO) {
        this.userRepository.findByRegistration(userDTO.registration())
            .ifPresent(u -> {
                throw new EntityAlreadyExistsException("Fiscal já cadastrado.");
            });

        UserEntity user = new UserEntity();
        user.setFullName(userDTO.fullName());
        user.setRegistration(userDTO.registration());
        user.setPhoneNumber(userDTO.phoneNumber());
        user.setPassword(user.getRegistration());
        return this.userRepository.save(user);
    }

     public UserEntity getUser(String registration) {
        return this.userRepository.findByRegistration(registration).orElseThrow(() ->
            new ResourceNotFoundException("Fiscal não encontrado."));
     }

     public List<ResponseManagerDTO> getAllManagers() {
        List<UserEntity> managers = this.userRepository.findByUserRole("SUPERVISOR");

        return managers.stream().map(
            s -> new ResponseManagerDTO(s.getId(), s.getFullName())
        ).toList();
     }

     public UserEntity updateUser(String registration, RequestUpdateUserDTO dto) {
        UserEntity user = this.userRepository.findByRegistration(registration).orElseThrow(() ->
            new ResourceNotFoundException("Fiscal não encontrado."));

        user.setPhoneNumber(dto.phoneNumber());
        user.setPassword(dto.password());

        return this.userRepository.save(user);
     }

     public void deleteUser(String registration) {
         UserEntity user = this.userRepository.findByRegistration(registration).orElseThrow(() ->
             new ResourceNotFoundException("Fiscal não encontrado."));

         this.userRepository.delete(user);
     }
}

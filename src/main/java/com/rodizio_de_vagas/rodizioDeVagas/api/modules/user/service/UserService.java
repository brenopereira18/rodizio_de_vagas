package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.EntityAlreadyExistsException;
import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.service.WorkCategoryPreferenceService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestCreateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestUpdateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.ResponseManagerDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkCategoryPreferenceService workCategoryPreferenceService;

    public UserEntity createUser(RequestCreateUserDTO userDTO) {
        this.userRepository.findByRegistration(userDTO.registration())
            .ifPresent(u -> {
                throw new EntityAlreadyExistsException("Fiscal já cadastrado.");
            });

        String encryptedPassword = new BCryptPasswordEncoder().encode(userDTO.registration());
        UserEntity user = UserEntity.builder()
            .fullName(userDTO.fullName())
            .registration(userDTO.registration())
            .phoneNumber(userDTO.phoneNumber())
            .password(encryptedPassword)
            .userRole(userDTO.userRole())
            .build();

        this.userRepository.save(user);
        this.workCategoryPreferenceService.createInitialPreferencesForUser(user);
        return user;
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

        String encryptedPassword = new BCryptPasswordEncoder().encode(dto.password());
        user.setPhoneNumber(dto.phoneNumber());
        user.setPassword(encryptedPassword);
        return this.userRepository.save(user);
     }

     public void deleteUser(String registration) {
         UserEntity user = this.userRepository.findByRegistration(registration).orElseThrow(() ->
             new ResourceNotFoundException("Fiscal não encontrado."));

         this.userRepository.delete(user);
     }
}

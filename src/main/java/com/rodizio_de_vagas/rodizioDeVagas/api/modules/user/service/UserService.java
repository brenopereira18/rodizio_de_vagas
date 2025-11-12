package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.EntityAlreadyExistsException;
import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.service.WorkCategoryPreferenceService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.repository.EnrollmentRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.entity.NotificationEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.repository.NotificationRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.repository.PriorityQueueRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserRole;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestCreateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestUpdateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.ResponseUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Stream;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PriorityQueueService priorityQueueService;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private PriorityQueueRepository priorityQueueRepository;

    @Autowired
    private WorkCategoryPreferenceService workCategoryPreferenceService;

    public UserEntity createUser(RequestCreateUserDTO userDTO) {
        this.userRepository.findByRegistration(userDTO.registration())
            .ifPresent(u -> {
                throw new EntityAlreadyExistsException("Fiscal já cadastrado.");
            });

        this.userRepository.findByPhoneNumber(userDTO.phoneNumber())
            .ifPresent(u -> {
                throw new EntityAlreadyExistsException("Telefone já cadastrado.");
            });

        String encryptedPassword = new BCryptPasswordEncoder().encode(userDTO.registration());
        UserEntity user = UserEntity.builder()
            .fullName(userDTO.fullName())
            .registration(userDTO.registration())
            .phoneNumber(userDTO.phoneNumber())
            .password(encryptedPassword)
            .haveALicense(false)
            .userRole(userDTO.userRole())
            .build();

        this.userRepository.save(user);
        this.workCategoryPreferenceService.createInitialPreferencesForUser(user);

        if (user.getUserRole().equals(UserRole.FISCAL)) {
            for (Category category : Category.values()) {
                this.priorityQueueService.activateFiscalInCategory(user.getRegistration(), category);
            }
        }
        return user;
    }

     public UserEntity getUser(String registration) {
        return this.userRepository.findByRegistration(registration).orElseThrow(() ->
            new ResourceNotFoundException("Fiscal não encontrado."));
     }

     public List<ResponseUserDTO> getAllTax() {
         List<UserEntity> tax = this.userRepository.findByUserRole(UserRole.FISCAL);

         return tax.stream().map(
             s -> new ResponseUserDTO(s.getId(), s.getFullName(), s.getRegistration(), s.getPhoneNumber(), s.getHaveALicense())
         ).toList();
     }

     public List<ResponseUserDTO> getAllAdmins() {
         List<UserEntity> admins = this.userRepository.findByUserRole(UserRole.ADMINISTRADOR);

         return admins.stream().map(
             s -> new ResponseUserDTO(s.getId(), s.getFullName(), s.getRegistration(), s.getPhoneNumber(), s.getHaveALicense())
         ).toList();
     }

     public List<ResponseUserDTO> getAllManagers() {
        List<UserEntity> managers = this.userRepository.findByUserRole(UserRole.SUPERVISOR);

        return managers.stream().map(
            s -> new ResponseUserDTO(s.getId(), s.getFullName(), s.getRegistration(), s.getPhoneNumber(), s.getHaveALicense())
        ).toList();
     }

    public List<ResponseUserDTO> getAllAdminsAndManagers() {
        List<ResponseUserDTO> admins = getAllAdmins();
        List<ResponseUserDTO> managers = getAllManagers();

        List<ResponseUserDTO> combinedList = Stream.concat(admins.stream(), managers.stream()).toList();
        return combinedList;
    }

     public UserEntity updateUser(String registration, RequestUpdateUserDTO dto) {
        UserEntity user = this.userRepository.findByRegistration(registration).orElseThrow(() ->
            new ResourceNotFoundException("Fiscal não encontrado."));

        user.setPhoneNumber(dto.phoneNumber());
        user.setHaveALicense(dto.haveALicense());

        if (dto.password() != null && !dto.password().isBlank()) {
            String encryptedPassword = new BCryptPasswordEncoder().encode(dto.password());
            user.setPassword(encryptedPassword);
        }
        this.userRepository.save(user);

        this.workCategoryPreferenceService.syncPreferences(user.getRegistration(), dto.categorys());
        return user;
     }

     public void deleteUser(String registration) {
         UserEntity user = this.userRepository.findByRegistration(registration).orElseThrow(() ->
             new ResourceNotFoundException("Fiscal não encontrado."));

         if (user.getUserRole().equals(UserRole.FISCAL)) {
             for (Category category : Category.values()) {
                 if (priorityQueueRepository.findByUserEntityRegistrationAndCategory(registration, category).isPresent()) {
                     this.priorityQueueService.deactivateFiscalFromCategory(registration, category);
                 }
             }
         }

         List<NotificationEntity> userNotifications = notificationRepository.findByUserEntity(user);
         if (!userNotifications.isEmpty()) {
             notificationRepository.deleteAll(userNotifications);
             notificationRepository.flush();
         }

         List<EnrollmentEntity> userEnrollments = this.enrollmentRepository.findByUserEntity(user);
         if (!userEnrollments.isEmpty()) {
             enrollmentRepository.deleteAll(userEnrollments);
             enrollmentRepository.flush();
         }
         this.userRepository.delete(user);
     }
}

package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.EntityAlreadyExistsException;
import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.service.WorkCategoryPreferenceService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PriorityQueueService priorityQueueService;
    private final EnrollmentRepository enrollmentRepository;
    private final NotificationRepository notificationRepository;
    private final PriorityQueueRepository priorityQueueRepository;
    private final WorkCategoryPreferenceService workCategoryPreferenceService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseUserDTO createUser(RequestCreateUserDTO userDTO) {
        this.userRepository.findByRegistration(userDTO.registration())
            .ifPresent(u -> {
                throw new EntityAlreadyExistsException("Fiscal já cadastrado.");
            });

        this.userRepository.findByPhoneNumber(userDTO.phoneNumber())
            .ifPresent(u -> {
                throw new EntityAlreadyExistsException("Telefone já cadastrado.");
            });

        String encryptedPassword = passwordEncoder.encode(userDTO.registration());
        UserEntity user = UserEntity.builder()
            .fullName(userDTO.fullName())
            .registration(userDTO.registration())
            .phoneNumber(userDTO.phoneNumber())
            .password(encryptedPassword)
            .email(userDTO.email())
            .haveALicense(false)
            .userRole(userDTO.userRole())
            .build();

        this.userRepository.save(user);
        this.workCategoryPreferenceService.createInitialPreferencesForUser(user);

        return toResponseDTO(user);
    }

    public ResponseUserDTO getUser(String registration) {
        UserEntity user = this.userRepository.findByRegistration(registration).orElseThrow(() ->
            new ResourceNotFoundException("Fiscal não encontrado."));
        return toResponseDTO(user);
    }

     public List<ResponseUserDTO> getAllTax() {
         return this.userRepository.findByUserRole(UserRole.FISCAL)
             .stream().map(this::toResponseDTO).toList();
     }

     public List<ResponseUserDTO> getAllAdmins() {
         return this.userRepository.findByUserRole(UserRole.ADMINISTRADOR)
             .stream().map(this::toResponseDTO).toList();
     }

     public List<ResponseUserDTO> getAllManagers() {
         return this.userRepository.findByUserRole(UserRole.SUPERVISOR)
             .stream().map(this::toResponseDTO).toList();
     }

    public List<ResponseUserDTO> getAllAdminsAndManagers() {
        return Stream.concat(getAllAdmins().stream(), getAllManagers().stream()).toList();
    }

    @Transactional
     public ResponseUserDTO updateUser(String registration, RequestUpdateUserDTO dto) {
        UserEntity user = this.userRepository.findByRegistration(registration).orElseThrow(() ->
            new ResourceNotFoundException("Fiscal não encontrado."));

        if (dto.phoneNumber() != null && !dto.phoneNumber().equals(user.getPhoneNumber())) {
            this.userRepository.findByPhoneNumber(dto.phoneNumber())
                .ifPresent(u -> {
                    throw new EntityAlreadyExistsException("Telefone já cadastrado por outro usuário.");
                });
            user.setPhoneNumber(dto.phoneNumber());
        }
        user.setHaveALicense(Boolean.TRUE.equals(dto.haveALicense()));

        if (dto.email() != null) {
            user.setEmail(dto.email());
        }

        if (dto.password() != null && !dto.password().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.password()));
        }

        this.userRepository.save(user);

        List<Category> categories = dto.categories() != null ? dto.categories() : List.of();
        this.workCategoryPreferenceService.syncPreferences(registration, categories);

        return toResponseDTO(user);
     }

    @Transactional
     public void deleteUser(String registration) {
         UserEntity user = this.userRepository.findByRegistration(registration).orElseThrow(() ->
             new ResourceNotFoundException("Fiscal não encontrado."));

         if (user.getUserRole().equals(UserRole.FISCAL)) {
             boolean hasActiveEnrollments = enrollmentRepository
                 .findByUserEntity(user)
                 .stream()
                 .anyMatch(e ->
                     (e.getSubscriptionStatus() == SubscriptionStatus.WAITING ||
                         e.getSubscriptionStatus() == SubscriptionStatus.ACCEPTED) &&
                         e.getWorkEntity().getServiceDate().isAfter(LocalDateTime.now())
                 );

             if (hasActiveEnrollments) {
                 throw new IllegalStateException(
                     "Não é possível excluir o fiscal pois ele possui inscrições ativas em serviços futuros. " +
                         "Cancele ou recuse as inscrições antes de excluir."
                 );
             }

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

    private ResponseUserDTO toResponseDTO(UserEntity user) {
        return new ResponseUserDTO(
            user.getId(),
            user.getFullName(),
            user.getRegistration(),
            user.getPhoneNumber(),
            user.getEmail(),
            user.getHaveALicense(),
            user.getUserRole()
        );
    }
}

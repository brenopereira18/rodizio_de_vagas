package com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity.WorkCategoryPreferenceEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity.dto.WorkCategoryPreferenceResponseDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.repository.WorkCategoryPreferenceRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserRole;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkCategoryPreferenceService {

    private final WorkCategoryPreferenceRepository workCategoryPreferenceRepository;
    private final PriorityQueueService priorityQueueService;
    private final UserRepository userRepository;

    public List<WorkCategoryPreferenceResponseDTO> getPreferencesByUser(String registration) {
        return workCategoryPreferenceRepository
            .findByUserEntityRegistration(registration)
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }

    public List<WorkCategoryPreferenceEntity> getPreferenceEntitiesByUser(String registration) {
        return workCategoryPreferenceRepository.findByUserEntityRegistration(registration);
    }

    @Transactional
    public void updatePreference(String registration, Category category, boolean active) {
        WorkCategoryPreferenceEntity preference = this.workCategoryPreferenceRepository.findByUserEntityRegistrationAndCategory(registration, category)
            .orElseThrow(() -> new ResourceNotFoundException("Preferência não encontrada."));

        boolean wasActive = preference.getActive();

        preference.setActive(active);
        this.workCategoryPreferenceRepository.save(preference);

        if (wasActive && !active) {
            // Fiscal está desativando a categoria, remover da fila
            priorityQueueService.deactivateFiscalFromCategory(registration, category);
        } else if (!wasActive && active) {
            // Fiscal está ativando a categoria, adicionar no final da fila
            priorityQueueService.activateFiscalInCategory(registration, category);
        }
    }

    @Transactional
    public void syncPreferences(String registration, List<Category> activeCategories) {
        List<WorkCategoryPreferenceEntity> currentPreferences = workCategoryPreferenceRepository.findByUserEntityRegistration(registration);

        UserEntity user = userRepository.findByRegistration(registration)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado"));

        currentPreferences.stream()
            .filter(pref -> pref.getActive() && !activeCategories.contains(pref.getCategory()))
            .map(WorkCategoryPreferenceEntity::getCategory)
            .forEach(category -> priorityQueueService.validateFiscalCanLeaveQueue(user.getId(), category));

        currentPreferences.forEach(preference -> {
            boolean shouldBeActive = activeCategories.contains(preference.getCategory());
            if (!preference.getActive().equals(shouldBeActive)) {
                updatePreference(registration, preference.getCategory(), shouldBeActive);
            }
        });
    }

    @Transactional
    public void createInitialPreferencesForUser(UserEntity user) {
        if (user.getUserRole() == UserRole.SUPERVISOR || user.getUserRole() == UserRole.ADMINISTRADOR ) {
            return;
        }

        for (Category category : Category.values()) {
            WorkCategoryPreferenceEntity preference = WorkCategoryPreferenceEntity.builder()
                .userEntity(user)
                .category(category)
                .active(true)
                .build();
            this.workCategoryPreferenceRepository.save(preference);
        }
    }

    private WorkCategoryPreferenceResponseDTO toResponseDTO(WorkCategoryPreferenceEntity entity) {
        return new WorkCategoryPreferenceResponseDTO(
            entity.getId(),
            entity.getCategory(),
            entity.getActive()
        );
    }
}

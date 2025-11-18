package com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity.WorkCategoryPreferenceEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.repository.WorkCategoryPreferenceRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserRole;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkCategoryPreferenceService {

    @Autowired
    private WorkCategoryPreferenceRepository workCategoryPreferenceRepository;

    @Autowired
    private PriorityQueueService priorityQueueService;

    @Autowired
    private UserRepository userRepository;

    public List<WorkCategoryPreferenceEntity> getPreferencesByUser(String registration) {
        return this.workCategoryPreferenceRepository.findByUserEntityRegistration(registration);
    }

    public void updatePreference(String registration, Category category, boolean active) {
        WorkCategoryPreferenceEntity preference = this.workCategoryPreferenceRepository.findByUserEntityRegistrationAndCategory(registration, category)
            .orElseThrow(() -> new ResourceNotFoundException("Preferência não encontrada."));

        boolean wasActive = preference.isActive();

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

    public void syncPreferences(String registration, List<Category> activeCategories) {
        List<WorkCategoryPreferenceEntity> currentPreferences = workCategoryPreferenceRepository.findByUserEntityRegistration(registration);

        UserEntity user = userRepository.findByRegistration(registration)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado"));

        List<Category> categoriesToDeactivate = currentPreferences.stream()
            .filter(pref -> pref.isActive() && !activeCategories.contains(pref.getCategory()))
            .map(WorkCategoryPreferenceEntity::getCategory)
            .toList();

        categoriesToDeactivate.forEach(category ->
            this.priorityQueueService.validateFiscalCanLeaveQueue(user.getId(), category)
        );

        currentPreferences.forEach(preference -> {
            boolean shouldBeActive = activeCategories.contains(preference.getCategory());
            if (preference.isActive() != shouldBeActive) {
                updatePreference(registration, preference.getCategory(), shouldBeActive);
            }
        });
    }

    public void createInitialPreferencesForUser(UserEntity user) {
        if (user.getUserRole() == UserRole.SUPERVISOR || user.getUserRole() == UserRole.ADMINISTRADOR ) {
            return;
        }

        for (Category category : Category.values()) {
            WorkCategoryPreferenceEntity preference = WorkCategoryPreferenceEntity.builder()
                .userEntity(user)
                .category(category)
                .isActive(true)
                .build();
            this.workCategoryPreferenceRepository.save(preference);
        }
    }
}

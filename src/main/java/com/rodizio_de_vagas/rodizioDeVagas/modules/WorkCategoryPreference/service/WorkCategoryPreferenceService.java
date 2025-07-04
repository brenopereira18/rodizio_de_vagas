package com.rodizio_de_vagas.rodizioDeVagas.modules.WorkCategoryPreference.service;

import com.rodizio_de_vagas.rodizioDeVagas.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.modules.WorkCategoryPreference.entity.WorkCategoryPreferenceEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.WorkCategoryPreference.repository.WorkCategoryPreferenceRepository;
import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkCategoryPreferenceService {

    @Autowired
    private WorkCategoryPreferenceRepository workCategoryPreferenceRepository;

    @Autowired
    private PriorityQueueService priorityQueueService;

    public List<WorkCategoryPreferenceEntity> getPreferencesByUser(Long userId) {
        return this.workCategoryPreferenceRepository.findByUserEntityId(userId);
    }

    public void updatePreference(Long userId, Category category, boolean active) {
        WorkCategoryPreferenceEntity preference = this.workCategoryPreferenceRepository.findByUserEntityIdAndCategory(userId, category)
            .orElseThrow(() -> new ResourceNotFoundException("Preferência não encontrada."));

        boolean wasActive = preference.isActive();

        preference.setActive(active);
        this.workCategoryPreferenceRepository.save(preference);

        if (wasActive && !active) {
            // Fiscal está desativando a categoria, remover da fila
            priorityQueueService.deactivateFiscalFromCategory(userId, category);
        } else if (!wasActive && active) {
            // Fiscal está ativando a categoria, adicionar no final da fila
            priorityQueueService.activateFiscalInCategory(userId, category);
        }
    }

    public void createInitialPreferencesForUser(UserEntity user) {
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

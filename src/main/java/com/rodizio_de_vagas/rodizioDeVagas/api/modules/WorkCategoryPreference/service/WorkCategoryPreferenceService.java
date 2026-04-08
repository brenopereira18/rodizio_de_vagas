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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
        log.info("=== INÍCIO updatePreference ===");
        log.info("Registration: {}, Category: {}, Active: {}", registration, category, active);

        WorkCategoryPreferenceEntity preference = this.workCategoryPreferenceRepository
            .findByUserEntityRegistrationAndCategory(registration, category)
            .orElseThrow(() -> new ResourceNotFoundException("Preferência não encontrada."));

        boolean wasActive = preference.getActive();
        log.info("Estado anterior da preferência: wasActive={}", wasActive);

        preference.setActive(active);
        this.workCategoryPreferenceRepository.save(preference);
        log.info("Preferência salva no banco com active={}", active);

        if (wasActive && !active) {
            log.info(">>> DESATIVANDO fiscal da categoria {}", category);
            priorityQueueService.deactivateFiscalFromCategory(registration, category);
        } else if (!wasActive && active) {
            log.info(">>> ATIVANDO fiscal na categoria {}", category);
            priorityQueueService.activateFiscalInCategory(registration, category);
        } else {
            log.info("--- Nenhuma mudança de estado na fila (wasActive={}, active={})", wasActive, active);
        }

        log.info("=== FIM updatePreference ===");
    }

    @Transactional
    public void syncPreferences(String registration, List<Category> activeCategories) {
        log.info("=== INÍCIO syncPreferences ===");
        log.info("Registration: {}, Categorias recebidas para ativar: {}", registration, activeCategories);

        List<WorkCategoryPreferenceEntity> currentPreferences =
            workCategoryPreferenceRepository.findByUserEntityRegistration(registration);

        UserEntity user = userRepository.findByRegistration(registration)
            .orElseThrow(() -> new ResourceNotFoundException("Fiscal não encontrado"));

        // Validar se pode sair das filas que estão sendo desativadas
        currentPreferences.stream()
            .filter(pref -> pref.getActive() && !activeCategories.contains(pref.getCategory()))
            .forEach(pref -> {
                log.info("Validando saída da categoria: {}", pref.getCategory());
                priorityQueueService.validateFiscalCanLeaveQueue(user.getId(), pref.getCategory());
            });

        // Atualizar cada preferência conforme necessário
        currentPreferences.forEach(preference -> {
            boolean shouldBeActive = activeCategories.contains(preference.getCategory());
            boolean isCurrentlyActive = preference.getActive();

            log.info("Categoria: {}, Estado atual: {}, Deveria estar: {}",
                preference.getCategory(), isCurrentlyActive, shouldBeActive);

            // Só atualiza se houver mudança de estado
            if (isCurrentlyActive != shouldBeActive) {
                log.info(">>> Mudança detectada para {}: {} -> {}",
                    preference.getCategory(), isCurrentlyActive, shouldBeActive);
                updatePreference(registration, preference.getCategory(), shouldBeActive);
            } else {
                log.info("--- Sem mudança para {}, ignorando", preference.getCategory());
            }
        });

        log.info("=== FIM syncPreferences ===");
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
                .active(false)
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

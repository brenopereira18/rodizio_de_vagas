package com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.repository;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity.WorkCategoryPreferenceEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkCategoryPreferenceRepository extends JpaRepository<WorkCategoryPreferenceEntity, Long> {

    List<WorkCategoryPreferenceEntity> findByUserEntityRegistration(String registration);

    Optional<WorkCategoryPreferenceEntity> findByUserEntityRegistrationAndCategory(String registration, Category category);
}

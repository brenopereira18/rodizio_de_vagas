package com.rodizio_de_vagas.rodizioDeVagas.modules.WorkCategoryPreference.repository;

import com.rodizio_de_vagas.rodizioDeVagas.modules.WorkCategoryPreference.entity.WorkCategoryPreferenceEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkCategoryPreferenceRepository extends JpaRepository<WorkCategoryPreferenceEntity, Long> {

    List<WorkCategoryPreferenceEntity> findByUserEntityId(Long userId);

    Optional<WorkCategoryPreferenceEntity> findByUserEntityIdAndCategory(Long userId, Category category);
}

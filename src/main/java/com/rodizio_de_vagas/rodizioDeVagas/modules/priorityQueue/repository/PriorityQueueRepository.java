package com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.repository;

import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PriorityQueueRepository extends JpaRepository<PriorityQueueEntity, Long> {

    boolean existsByCategory(Category category);
    List<PriorityQueueEntity> findByCategoryOrderByPositionInLine(Category category);
    Optional<PriorityQueueEntity> findByUserEntityIdAndCategory(Long userId, Category category);
    long countByCategory(Category category);
}

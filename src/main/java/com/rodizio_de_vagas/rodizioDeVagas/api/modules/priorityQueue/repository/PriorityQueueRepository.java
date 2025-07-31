package com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.repository;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PriorityQueueRepository extends JpaRepository<PriorityQueueEntity, Long> {

    boolean existsByCategory(Category category);
    List<PriorityQueueEntity> findByCategoryOrderByPositionInLine(Category category);
    Optional<PriorityQueueEntity> findByUserEntityRegistrationAndCategory(String registration, Category category);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM PriorityQueueEntity q WHERE q.category = :category ORDER BY q.positionInLine")
    List<PriorityQueueEntity> findByCategoryOrderByPositionInLineWithLock(@Param("category") Category category);

    @Query("SELECT MAX(q.positionInLine) FROM PriorityQueueEntity q WHERE q.category = :category")
    Optional<Integer> findMaxPositionByCategory(@Param("category") Category category);
}

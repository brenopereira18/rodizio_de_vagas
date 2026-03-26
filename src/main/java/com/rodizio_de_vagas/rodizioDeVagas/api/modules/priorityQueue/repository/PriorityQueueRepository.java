package com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.repository;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PriorityQueueRepository extends JpaRepository<PriorityQueueEntity, Long> {

    boolean existsByCategory(Category category);

    List<PriorityQueueEntity> findByCategoryOrderByPositionInLine(Category category);

    Optional<PriorityQueueEntity> findByUserEntityRegistrationAndCategory(String registration, Category category);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT q FROM PriorityQueueEntity q WHERE q.category = :category ORDER BY q.positionInLine")
    List<PriorityQueueEntity> findByCategoryOrderByPositionInLineWithLock(@Param("category") Category category);

    boolean existsByUserEntityIdAndCategory(Long userId, Category category);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query(value = """
        WITH reordered_queue AS (
            SELECT 
                id,
                fiscal_id,
                categoria,
                ROW_NUMBER() OVER (
                    ORDER BY 
                        CASE WHEN fiscal_id IN :fiscalIdsToMove THEN 2 ELSE 1 END,
                        posicao_na_fila
                ) as nova_posicao
            FROM fila_de_prioridade 
            WHERE categoria = :categoria
        )
        UPDATE fila_de_prioridade 
        SET posicao_na_fila = reordered_queue.nova_posicao
        FROM reordered_queue 
        WHERE fila_de_prioridade.id = reordered_queue.id
        """, nativeQuery = true)
    void reorderQueueWithCTE(@Param("fiscalIdsToMove") Set<Long> fiscalIdsToMove, @Param("categoria") String categoria);
}

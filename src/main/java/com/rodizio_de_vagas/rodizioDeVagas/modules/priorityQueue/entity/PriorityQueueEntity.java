package com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.entity;

import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fila_de_prioridade", uniqueConstraints = {@UniqueConstraint(columnNames = {"categoria", "posicao_na_fila"})})
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriorityQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fiscal_id", nullable = false)
    @NotNull
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private Category category;

    @Column(name = "posicao_na_fila")
    private Integer positionInLine;
}

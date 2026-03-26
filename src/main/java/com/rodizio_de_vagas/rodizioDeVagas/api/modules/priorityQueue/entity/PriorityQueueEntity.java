package com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "fila_de_prioridade", uniqueConstraints = {@UniqueConstraint(columnNames = {"categoria", "posicao_na_fila"})})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PriorityQueueEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_id", nullable = false)
    @NotNull
    @JsonIgnore
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private Category category;

    @Column(name = "posicao_na_fila")
    private Integer positionInLine;
}

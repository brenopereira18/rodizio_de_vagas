package com.rodizio_de_vagas.rodizioDeVagas.modules.jobCategoryPreference.entity;

import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "preferencia_de_trabalho")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JobCategoryPreference {

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

    @Column(name = "esta_ativo", nullable = false)
    private boolean isActive;
}

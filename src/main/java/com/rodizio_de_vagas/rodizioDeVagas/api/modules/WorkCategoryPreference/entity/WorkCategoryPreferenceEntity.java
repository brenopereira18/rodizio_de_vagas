package com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Table(name = "preferencia_de_trabalho")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WorkCategoryPreferenceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "fiscal_id", nullable = false)
    @NotNull
    @JsonIgnore
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private Category category;

    @Column(name = "esta_ativo", nullable = false)
    @Builder.Default
    private Boolean active = true;
}

package com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@Table(name = "inscricao")
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    @JsonIgnore
    private WorkEntity workEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiscal_id", nullable = false)
    @JsonIgnore
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_da_inscricao")
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.WAITING;
}

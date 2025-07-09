package com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@Table(name = "inscricao")
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "servico_id", nullable = false)
    private WorkEntity workEntity;

    @ManyToOne
    @JoinColumn(name = "fiscal_id", nullable = false)
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_da_inscricao")
    @Builder.Default
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.WAITING;
}

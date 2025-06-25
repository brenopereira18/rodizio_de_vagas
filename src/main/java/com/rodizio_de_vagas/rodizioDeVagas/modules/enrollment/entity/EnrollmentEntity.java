package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity;

import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Entity
@Data
@Table(name = "inscricao")
@AllArgsConstructor
@NoArgsConstructor
public class EnrollmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private WorkEntity workEntity;

    @ManyToOne
    private UserEntity userEntity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_da_inscricao")
    private SubscriptionStatus subscriptionStatus = SubscriptionStatus.WAITING;
}

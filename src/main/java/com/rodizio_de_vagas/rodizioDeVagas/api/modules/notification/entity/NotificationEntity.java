package com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.entity;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "notificacao")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trabalho_id", nullable = false)
    @NotNull
    private WorkEntity workEntity;

    @ManyToOne
    @JoinColumn(name = "fiscal_id", nullable = false)
    @NotNull
    private UserEntity userEntity;

    @CreationTimestamp
    @Column(name = "data_de_envio")
    private Instant shippingDate;

    @Column(name = "prazo_de_resposta", nullable = false)
    private Instant responseDeadline;

    @Column(name = "mensagem", nullable = false)
    @NotNull
    private String message;
}

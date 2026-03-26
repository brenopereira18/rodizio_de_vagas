package com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "restaurar_senha")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForgetPasswordTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false, name = "user_id", unique = true)
    @JsonIgnore
    private UserEntity userEntity;

    @Column(name = "tempo_de_expiracao", nullable = false)
    private Instant expiryTime;
}

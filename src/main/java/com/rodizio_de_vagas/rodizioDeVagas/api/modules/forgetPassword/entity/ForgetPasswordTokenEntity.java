package com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.entity;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "restaurar_senha")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ForgetPasswordTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(nullable = false, name = "user_id", unique = true)
    private UserEntity userEntity;

    @Column(name = "tempo_de_expiração")
    private Instant expiryTime;
}

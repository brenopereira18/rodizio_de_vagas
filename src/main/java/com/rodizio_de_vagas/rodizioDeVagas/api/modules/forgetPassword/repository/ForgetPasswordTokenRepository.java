package com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.repository;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.entity.ForgetPasswordTokenEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForgetPasswordTokenRepository extends JpaRepository<ForgetPasswordTokenEntity, Long> {
    Optional<ForgetPasswordTokenEntity> findByToken(String token);
    Optional<ForgetPasswordTokenEntity> findByUserEntity(UserEntity user);
}

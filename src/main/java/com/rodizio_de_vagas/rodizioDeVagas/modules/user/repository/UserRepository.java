package com.rodizio_de_vagas.rodizioDeVagas.modules.user.repository;

import com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByRegistration(String registration);
    List<UserEntity> findByUserRole(String role);
    Optional<UserEntity> findById(Long id);
}
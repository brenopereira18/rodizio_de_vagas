package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByRegistration(String registration);
    List<UserEntity> findByUserRole(UserRole role);
    Optional<UserEntity> findById(Long id);
    List<UserEntity> findByUserRoleOrderByFullNameAsc(UserRole userRole);

}
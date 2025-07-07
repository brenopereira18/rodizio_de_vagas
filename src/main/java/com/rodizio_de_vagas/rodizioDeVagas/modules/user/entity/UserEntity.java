package com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rodizio_de_vagas.rodizioDeVagas.modules.WorkCategoryPreference.entity.WorkCategoryPreferenceEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "fiscal")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_completo", nullable = false)
    @NotBlank
    private String fullName;

    @Column(name = "matricula", nullable = false, unique = true)
    @NotBlank
    @Pattern(regexp = "\\d{5}-\\d", message = "A matrícula deve estar no formato 00000-0")
    private String registration;

    @Column(name = "senha", nullable = false)
    @NotBlank
    @Size(min = 6, max = 12, message = "A senha deve ter de 6 a 12 caracteres")
    private String password;

    @Column(name = "telefone", nullable = false, unique = true)
    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "O telefone deve conter 11 dígitos numéricos, ex: 11987654321")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "funcao")
    private UserRole userRole;

    @OneToMany(mappedBy = "userEntity", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<WorkCategoryPreferenceEntity> preferences = new ArrayList<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(userRole.name()));
    }

    @Override
    public String getUsername() {
        return this.registration;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}

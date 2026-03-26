package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity.WorkCategoryPreferenceEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "fiscal")
@Getter
@Setter
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

    @JsonIgnore
    @Column(name = "senha", nullable = false)
    @NotBlank
    private String password;

    @Column(name = "tem_habilitação")
    private Boolean haveALicense;

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
        return List.of(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
    }

    @Override
    public String getUsername() {
        return this.registration;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

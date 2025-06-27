package com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fiscal")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_completo", nullable = false)
    @NotBlank
    private String fullName;

    @Column(name = "matricula", nullable = false, unique = true)
    @NotBlank
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
}

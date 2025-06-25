package com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "servico")
@AllArgsConstructor
@NoArgsConstructor
public class WorkEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "titulo", nullable = false)
    @NotBlank
    private String title;

    @Column(name = "local", nullable = false)
    @NotBlank
    private String location;

    @Column(name = "data_do_servico", nullable = false)
    @Future(message = "A data do serviço ainda não pode ter ocorrido")
    @NotNull
    private LocalDateTime serviceDate;

    @Column(name = "data_limite_de_inscricao", nullable = false)
    @Future()
    @NotNull
    private LocalDateTime registrationLimit;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "categoria", nullable = false)
    private Category category;

    @Column(name = "numero_de_vagas", nullable = false)
    @NotNull
    private Integer numberOfVacancies;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status_do_servico", nullable = false)
    private WorkStatus workStatus = WorkStatus.OPEN;
}

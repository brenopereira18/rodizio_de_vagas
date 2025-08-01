package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.notification.entity.NotificationEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.validation.OnCreate;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "servico")
@AllArgsConstructor
@NoArgsConstructor
@Builder
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
    @Future(message = "A data do serviço ainda não pode ter ocorrido", groups = OnCreate.class)
    @NotNull
    private LocalDateTime serviceDate;

    @Column(name = "termino_do_servico", nullable = false)
    @Future(message = "A data do serviço ainda não pode ter ocorrido", groups = OnCreate.class)
    @NotNull
    private LocalDateTime serviceEndDate;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private UserEntity manager;

    @Column(name = "data_limite_de_inscricao", nullable = false)
    @Future(groups = OnCreate.class)
    @NotNull
    private LocalDateTime registrationLimit;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "categoria", nullable = false)
    private Category category;

    @Column(name = "numero_de_vagas", nullable = false)
    @NotNull
    private Integer numberOfVacancies;

    @OneToMany(mappedBy = "workEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<EnrollmentEntity> enrollments;

    @OneToMany(mappedBy = "workEntity", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<NotificationEntity> notifications;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(name = "status_do_servico", nullable = false)
    @Builder.Default
    private WorkStatus workStatus = WorkStatus.OPEN;

    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observation;
}

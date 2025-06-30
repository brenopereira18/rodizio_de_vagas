package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.controller;

import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto.RequestCreateEnrollmentDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/enrollment")
@RestController
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<String> createEnrollment(@RequestBody RequestCreateEnrollmentDTO dto) {
        this.enrollmentService.createEnrollment(dto.workId(), dto.userId());
        return ResponseEntity.status(HttpStatus.CREATED).body("Inscrição criada com sucesso");
    }

    @GetMapping
    public ResponseEntity<EnrollmentEntity> getEnrollment() {

    }

    @PutMapping("/{id}/cancelled")
    public ResponseEntity<String> cancelEnrollment(@PathVariable Long enrollmentId) {
        this.enrollmentService.cancelEnrollment(enrollmentId);
        return ResponseEntity.ok("Inscrição cancelada com sucesso. O serviço está disponivel novamente.");
    }
}

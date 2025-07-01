package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.controller;

import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.EnrollmentEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto.RequestCreateEnrollmentDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto.ResponseWorkWithTaxDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/enrollment")
@RestController
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEntity<EnrollmentEntity> createEnrollment(@RequestBody RequestCreateEnrollmentDTO dto) {
        EnrollmentEntity enrollment = this.enrollmentService.createEnrollment(dto.workId(), dto.userId());
        return ResponseEntity.ok().body(enrollment);
    }

    @GetMapping
    public ResponseEntity<List<ResponseWorkWithTaxDTO>> getEnrollmentsGroupedByWork(@RequestParam int month, @RequestParam int year) {
        List<ResponseWorkWithTaxDTO> result = this.enrollmentService.getGroupedEnrollmentsByMonth(month, year);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/{id}/cancelled")
    public ResponseEntity<String> cancelEnrollment(@PathVariable Long id) {
        this.enrollmentService.cancelEnrollment(id);
        return ResponseEntity.ok("Inscrição cancelada com sucesso. O serviço está disponivel novamente.");
    }
}

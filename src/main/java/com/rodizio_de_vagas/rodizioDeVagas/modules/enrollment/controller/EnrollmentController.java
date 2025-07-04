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

    @PutMapping("/{workId}/respond/{userId}")
    public ResponseEntity<String> respondToEnrollment(@PathVariable Long workId, @PathVariable Long userId, @RequestParam boolean accepted) {
        enrollmentService.respondToEnrollment(workId, userId, accepted);
        String message = accepted ? "Inscrição realizada com sucesso." : "Trabalho recusado. Próximo fiscal será notificado.";
        return ResponseEntity.ok(message);
    }

    @GetMapping
    public ResponseEntity<List<ResponseWorkWithTaxDTO>> getEnrollmentsGroupedByWork(@RequestParam int month, @RequestParam int year) {
        List<ResponseWorkWithTaxDTO> result = this.enrollmentService.getGroupedEnrollmentsByMonth(month, year);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/{id}/cancelled")
    public ResponseEntity<String> cancelEnrollment(@PathVariable Long id) {
        this.enrollmentService.cancelEnrollment(id);
        return ResponseEntity.ok("Inscrição cancelada com sucesso.");
    }
}

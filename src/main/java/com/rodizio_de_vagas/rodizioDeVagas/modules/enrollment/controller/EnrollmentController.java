package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.controller;

import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity.dto.ResponseWorkWithTaxDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

@RequestMapping("/enrollment")
@RestController
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PutMapping("/{workId}/respond")
    public ResponseEntity<String> respondToEnrollment(@PathVariable Long workId, Principal principal, @RequestParam boolean accepted) {
        String registration = principal.getName();
        enrollmentService.respondToEnrollment(workId, registration, accepted);
        String message = accepted ? "Inscrição realizada com sucesso." : "Trabalho recusado. Próximo fiscal será notificado.";
        return ResponseEntity.ok(message);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public ResponseEntity<List<ResponseWorkWithTaxDTO>> getEnrollmentsGroupedByWork(@RequestParam int month, @RequestParam int year) {
        List<ResponseWorkWithTaxDTO> result = this.enrollmentService.getGroupedEnrollmentsByMonth(month, year);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/{id}/cancelled")
    public ResponseEntity<String> cancelEnrollment(@PathVariable Long id, Principal principal) throws AccessDeniedException {
        String registration = principal.getName();
        this.enrollmentService.cancelEnrollment(id, registration);
        return ResponseEntity.ok("Inscrição cancelada com sucesso.");
    }
}

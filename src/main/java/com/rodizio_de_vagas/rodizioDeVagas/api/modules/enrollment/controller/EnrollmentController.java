package com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.controller;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.dto.ResponseWorkWithTaxDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;

@RequestMapping("/enrollment")
@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PutMapping("/{workId}/respond")
    public ResponseEntity<String> respondToEnrollment(@PathVariable Long workId, Principal principal, @RequestParam boolean accepted) {
        enrollmentService.respondToEnrollment(workId, principal.getName(), accepted);
        String message = accepted ? "Inscrição realizada com sucesso." : "Trabalho recusado. Próximo fiscal será notificado.";
        return ResponseEntity.ok(message);
    }

    @PutMapping("/servicos/free/{id}/respond")
    public ResponseEntity<String> acceptedWorkFree(@PathVariable Long id, Principal principal) {
        enrollmentService.reusePreviousEnrollment(id, principal.getName());
        return ResponseEntity.ok("Inscrição realizada com sucesso.");
    }


    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @GetMapping
    public ResponseEntity<List<ResponseWorkWithTaxDTO>> getEnrollmentsGroupedByWork() {
        return ResponseEntity.ok(enrollmentService.getGroupedEnrollmentsByMonth());
    }

    @PutMapping("/{id}/cancelled")
    public ResponseEntity<String> cancelEnrollment(@PathVariable Long id, Principal principal) {
        try {
            enrollmentService.cancelEnrollment(id, principal.getName());
            return ResponseEntity.ok("Inscrição cancelada com sucesso.");
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }
}

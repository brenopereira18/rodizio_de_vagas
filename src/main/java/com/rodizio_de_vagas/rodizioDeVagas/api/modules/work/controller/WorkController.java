package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.controller;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.WorkResponseDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.RequestCreateOrUpdateWorkDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.WorkWithEnrollmentDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.validation.OnCreate;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequestMapping("/work")
@RestController
@RequiredArgsConstructor
public class WorkController {

    private final WorkService workService;

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<WorkResponseDTO> createWork(@Validated(OnCreate.class) @RequestBody RequestCreateOrUpdateWorkDTO dto) {
        WorkResponseDTO work = this.workService.createWork(dto);
        return ResponseEntity.status(201).body(work);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PutMapping
    public ResponseEntity<WorkResponseDTO> updateWork(@RequestBody RequestCreateOrUpdateWorkDTO dto) {
        WorkResponseDTO work = this.workService.updateWork(dto);
        return ResponseEntity.ok(work);
    }

    @GetMapping("/available")
    public ResponseEntity<List<WorkWithEnrollmentDTO>> getAvailableWorksForTax(Principal principal) {
        List<WorkWithEnrollmentDTO> works = this.workService.getAvailableWorksForTax(principal.getName());
        return ResponseEntity.ok(works);
    }

    @GetMapping("/my-works")
    public ResponseEntity<List<WorkWithEnrollmentDTO>> getMyWorks(Principal principal) {
        List<WorkWithEnrollmentDTO> myWorks = this.workService.getEnrolledWorksForTax(principal.getName());
        return ResponseEntity.ok(myWorks);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWork(@PathVariable Long id) {
        this.workService.deleteWork(id);
        return ResponseEntity.noContent().build();
    }
}

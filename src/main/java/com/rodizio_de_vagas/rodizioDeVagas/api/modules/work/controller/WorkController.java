package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.controller;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.RequestCreateOrUpdateWorkDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.WorkWithEnrollment;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.validation.OnCreate;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RequestMapping("/work")
@RestController
public class WorkController {

    @Autowired
    private WorkService workService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<WorkEntity> createWork(@Validated(OnCreate.class) @RequestBody RequestCreateOrUpdateWorkDTO dto) {
        WorkEntity work = this.workService.createWork(dto);
        return ResponseEntity.ok().body(work);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping
    public ResponseEntity<WorkEntity> updateWork(@RequestBody RequestCreateOrUpdateWorkDTO dto) {
        WorkEntity work = this.workService.updateWork(dto);
        return ResponseEntity.ok().body(work);
    }

    @GetMapping("/available")
    public ResponseEntity<List<WorkWithEnrollment>> getAvailableWorksForTax(Principal principal) {
        String registration = principal.getName();
        List<WorkWithEnrollment> works = this.workService.getAvailableWorksForTax(registration);
        return ResponseEntity.ok(works);
    }

    @GetMapping("/my-works")
    public ResponseEntity<List<WorkWithEnrollment>> getMyWorks(Principal principal) {
        String registration = principal.getName();
        List<WorkWithEnrollment> myWorks = this.workService.getEnrolledWorksForTax(registration);
        return ResponseEntity.ok(myWorks);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWork(@PathVariable Long id) {
        this.workService.deleteWork(id);
        return ResponseEntity.noContent().build();
    }
}

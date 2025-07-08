package com.rodizio_de_vagas.rodizioDeVagas.modules.work.controller;

import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.dto.RequestCreateWorkDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.service.WorkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/work")
@RestController
public class WorkController {

    @Autowired
    private WorkService workService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<WorkEntity> createWork(@Valid @RequestBody RequestCreateWorkDTO dto) {
        WorkEntity work = this.workService.createWork(dto);
        return ResponseEntity.ok().body(work);
    }

    @GetMapping("/works")
    public ResponseEntity<List<WorkEntity>> getAllWorks(@RequestParam(defaultValue = "DISPONIVEIS") String status) {
        List<WorkEntity> works = this.workService.getAllWorks(status);
        return ResponseEntity.ok(works);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWork(@PathVariable Long id) {
        this.workService.deleteWork(id);
        return ResponseEntity.noContent().build();
    }
}

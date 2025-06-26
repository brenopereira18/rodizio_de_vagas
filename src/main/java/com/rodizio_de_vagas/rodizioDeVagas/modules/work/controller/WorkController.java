package com.rodizio_de_vagas.rodizioDeVagas.modules.work.controller;

import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.WorkEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.dto.RequestCreateWorkDTO;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.service.WorkService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/work")
@RestController
public class WorkController {

    @Autowired
    private WorkService workService;

    @PostMapping
    public ResponseEntity<WorkEntity> createWork(@Valid @RequestBody RequestCreateWorkDTO dto) {
        WorkEntity work = this.workService.createWork(dto);
        return ResponseEntity.ok().body(work);
    }
}

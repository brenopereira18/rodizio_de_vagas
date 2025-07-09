package com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.controller;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity.WorkCategoryPreferenceEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.service.WorkCategoryPreferenceService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/preferences")
public class WorkCategoryPreferenceController {

    @Autowired
    private WorkCategoryPreferenceService workCategoryPreferenceService;

    @GetMapping
    public ResponseEntity<List<WorkCategoryPreferenceEntity>> getUserPreferences(Principal principal) {
        String registration = principal.getName();
        List<WorkCategoryPreferenceEntity> preferences = this.workCategoryPreferenceService.getPreferencesByUser(registration);
        return ResponseEntity.ok().body(preferences);
    }

    @PutMapping
    public ResponseEntity<String> updatePreference(Principal principal, @RequestParam Category category, @RequestParam boolean active) {
        String registration = principal.getName();
        this.workCategoryPreferenceService.updatePreference(registration, category, active);
        return ResponseEntity.ok("Preferência atualizada com sucesso.");
    }

}

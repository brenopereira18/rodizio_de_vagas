package com.rodizio_de_vagas.rodizioDeVagas.modules.WorkCategoryPreference.controller;

import com.rodizio_de_vagas.rodizioDeVagas.modules.WorkCategoryPreference.entity.WorkCategoryPreferenceEntity;
import com.rodizio_de_vagas.rodizioDeVagas.modules.WorkCategoryPreference.service.WorkCategoryPreferenceService;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/preferences")
public class WorkCategoryPreferenceController {

    @Autowired
    private WorkCategoryPreferenceService workCategoryPreferenceService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<WorkCategoryPreferenceEntity>> getUserPreferences(@PathVariable Long userId) {
        List<WorkCategoryPreferenceEntity> preferences = this.workCategoryPreferenceService.getPreferencesByUser(userId);
        return ResponseEntity.ok().body(preferences);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<String> updatePreference(@PathVariable Long userId, @RequestParam Category category, @RequestParam boolean active) {
        this.workCategoryPreferenceService.updatePreference(userId, category, active);
        return ResponseEntity.ok("Preferência atualizada com sucesso.");
    }

}

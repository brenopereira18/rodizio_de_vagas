package com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.controller;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity.dto.WorkCategoryPreferenceResponseDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.service.WorkCategoryPreferenceService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/preferences")
@RequiredArgsConstructor
public class WorkCategoryPreferenceController {

    private final WorkCategoryPreferenceService workCategoryPreferenceService;

    @GetMapping
    public ResponseEntity<List<WorkCategoryPreferenceResponseDTO>> getUserPreferences(Principal principal) {
        List<WorkCategoryPreferenceResponseDTO> preferences = this.workCategoryPreferenceService.getPreferencesByUser(principal.getName());
        return ResponseEntity.ok(preferences);
    }

    @PutMapping
    public ResponseEntity<String> updatePreference(Principal principal, @RequestParam Category category, @RequestParam boolean active) {
        this.workCategoryPreferenceService.updatePreference(principal.getName(), category, active);
        return ResponseEntity.ok("Preferência atualizada com sucesso.");
    }

}

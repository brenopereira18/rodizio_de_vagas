package com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.controller;

import com.rodizio_de_vagas.rodizioDeVagas.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/priority-queue")
@RestController
public class PriorityQueueController {

    @Autowired
    private PriorityQueueService priorityQueueService;

    @PutMapping("/activate")
    public ResponseEntity<String> activateFiscal(@RequestParam Long userId, @RequestParam Category category) {
        this.priorityQueueService.activateFiscalInCategory(userId, category);
        return ResponseEntity.ok().body("Fiscal ativado com sucesso.");
    }

    @PutMapping("/deactivate")
    public ResponseEntity<String> deactivateFiscal(@RequestParam Long userId, @RequestParam Category category) {
        this.priorityQueueService.deactivateFiscalFromCategory(userId, category);
        return ResponseEntity.ok().body("Fiscal desativado com sucesso.");
    }
}

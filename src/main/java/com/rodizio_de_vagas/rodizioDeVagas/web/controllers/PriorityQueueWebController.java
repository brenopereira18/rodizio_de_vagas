package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity.dto.PriorityQueueResponseDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequestMapping(RoutesController.BASE + "/fila-de-prioridade")
@RequiredArgsConstructor
public class PriorityQueueWebController {

    private final PriorityQueueService priorityQueueService;

    @GetMapping
    public String showPriorityQueue(Model model) {
        Map<Category, List<PriorityQueueResponseDTO>> queue = priorityQueueService.getAllQueuesGroupedByCategory();
        model.addAttribute("filasPrioridade", queue);
        model.addAttribute("pageTitle", "Fila de Prioridade");
        return "fragments/priority-queue";
    }
}

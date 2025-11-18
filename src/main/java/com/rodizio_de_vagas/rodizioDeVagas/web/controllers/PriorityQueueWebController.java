package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@PreAuthorize("hasAuthority('ADMINISTRADOR')")
@RequestMapping("/home/fila-de-prioridade")
public class PriorityQueueWebController {

    @Autowired
    private PriorityQueueService priorityQueueService;

    @GetMapping
    public String showPriorityQueue(Model model) {
        model.addAttribute("pageTitle", "Fila de Prioridade");
        Map<Category, List<PriorityQueueEntity>> queue = priorityQueueService.getAllQueuesGroupedByCategory();
        model.addAttribute("filasPrioridade", queue);
        return "fragments/priority-queue";
    }
}

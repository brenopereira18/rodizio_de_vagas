package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.EntityAlreadyExistsException;
import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.entity.PriorityQueueEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.priorityQueue.service.PriorityQueueService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserRole;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestCreateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service.UserService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/home/fiscais")
public class UserWebController {

    @Autowired
    private UserService userService;

    @Autowired
    private PriorityQueueService priorityQueueService;

    @GetMapping
    public String showUsers(Model model) {
        model.addAttribute("pageTitle", "Fiscais");
        model.addAttribute("funcoes", UserRole.values());
        model.addAttribute("fiscais", userService.getAllTax());
        model.addAttribute("supervisores", userService.getAllManagers());

        Map<Category, List<PriorityQueueEntity>> queue = priorityQueueService.getAllQueuesGroupedByCategory();
        model.addAttribute("filasPrioridade", queue);
        return "fragments/fiscais-supervisores";
    }

    @PostMapping
    public String createUser(@ModelAttribute RequestCreateUserDTO dto, RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(dto);
            redirectAttributes.addFlashAttribute("success", "Usuário criado com sucesso!");
        } catch (EntityAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/home/fiscais";
    }

    @PostMapping("/deletar")
    public String deleteFiscal(@RequestParam String registration, RedirectAttributes redirectAttrs) {
        try {
            userService.deleteUser(registration);
            redirectAttrs.addFlashAttribute("success", "Usuário deletado com sucesso!");
        } catch (ResourceNotFoundException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/home/fiscais";
    }
}

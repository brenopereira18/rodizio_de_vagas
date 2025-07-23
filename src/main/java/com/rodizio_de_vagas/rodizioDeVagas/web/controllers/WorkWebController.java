package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service.UserService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.RequestCreateOrUpdateWorkDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.WorkWithEnrollment;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/home/servicos")
public class WorkWebController {

    @Autowired
    private UserService userService;

    @Autowired
    private WorkService workService;

    @GetMapping
    public String redirectToDisponiveis() {
        return "redirect:/home/servicos/disponiveis";
    }


    @GetMapping("/disponiveis")
    public String showAvailableServices(@RequestParam(defaultValue = "DISPONIVEIS") String filter, Model model, Principal principal) {
        UserEntity user = userService.getUser(principal.getName());

        List<WorkWithEnrollment> services = workService.getWorksByFilter(user.getRegistration(), filter);

        model.addAttribute("filtro", filter);
        model.addAttribute("pageTitle", "Serviços");
        model.addAttribute("supervisores", userService.getAllManagers());
        model.addAttribute("categorias", Category.values());
        model.addAttribute("servicos", services);
        model.addAttribute("WAITING", SubscriptionStatus.WAITING);
        model.addAttribute("FREE", WorkStatus.FREE);

        model.addAttribute("usuario", user);
        return "fragments/services";
    }

    @PostMapping
    public String createService(@ModelAttribute RequestCreateOrUpdateWorkDTO dto, RedirectAttributes redirectAttributes) {
        try {
            workService.createWork(dto);
            redirectAttributes.addFlashAttribute("success", "Serviço criado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao criar serviço: " + e.getMessage());
        }
        return "redirect:/home/servicos/disponiveis";
    }

    @PostMapping("/atualizar")
    public String updateService(@ModelAttribute RequestCreateOrUpdateWorkDTO dto, RedirectAttributes redirectAttributes) {
        try {
            workService.updateWork(dto);
            redirectAttributes.addFlashAttribute("success", "Serviço atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar serviço: " + e.getMessage());
        }
        return "redirect:/home/servicos/disponiveis";
    }

    @PostMapping("/deletar")
    public String deleteWork(@RequestParam Long id, RedirectAttributes redirectAttrs) {
        try {
            workService.deleteWork(id);
            redirectAttrs.addFlashAttribute("success", "Serviço deletado com sucesso!");
        } catch (ResourceNotFoundException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/home/servicos/disponiveis";
    }

}


package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.ResourceNotFoundException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.SubscriptionStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.ResponseUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service.UserService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkFilterType;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.WorkStatus;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.RequestCreateOrUpdateWorkDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.dto.WorkWithEnrollmentDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/gerenciador_de_servico/servicos")
@RequiredArgsConstructor
public class WorkWebController {

    private final UserService userService;
    private final WorkService workService;

    @GetMapping
    public String redirectToDisponiveis() {
        return "redirect:/gerenciador_de_servico/servicos/disponiveis";
    }

    @GetMapping("/disponiveis")
    public String showAvailableServices(@RequestParam(defaultValue = "DISPONIVEIS") WorkFilterType filter, Model model, Principal principal) {
        ResponseUserDTO user = userService.getUser(principal.getName());

        List<WorkWithEnrollmentDTO> services = workService.getWorksByFilter(user.registration(), filter);

        model.addAttribute("filtro", filter);
        model.addAttribute("pageTitle", "Serviços");
        model.addAttribute("supervisores", userService.getAllAdminsAndManagers());
        model.addAttribute("categorias", Category.values());
        model.addAttribute("servicos", services);
        model.addAttribute("WAITING", SubscriptionStatus.WAITING);
        model.addAttribute("FREE", WorkStatus.FREE);
        model.addAttribute("freeServicesCount", workService.countFreeWorksForUser(user.registration()));
        model.addAttribute("usuario", user);
        return "fragments/services";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping
    public String createService(@ModelAttribute RequestCreateOrUpdateWorkDTO dto, RedirectAttributes redirectAttributes) {
        try {
            workService.createWork(dto);
            redirectAttributes.addFlashAttribute("success", "Serviço criado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao criar serviço: " + e.getMessage());
        }
        return "redirect:/gerenciador_de_servico/servicos/disponiveis";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping("/atualizar")
    public String updateService(@ModelAttribute RequestCreateOrUpdateWorkDTO dto, RedirectAttributes redirectAttributes) {
        try {
            workService.updateWork(dto);
            redirectAttributes.addFlashAttribute("success", "Serviço atualizado com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar serviço: " + e.getMessage());
        }
        return "redirect:/gerenciador_de_servico/servicos/disponiveis";
    }

    @PreAuthorize("hasAuthority('ROLE_ADMINISTRADOR')")
    @PostMapping("/deletar")
    public String deleteWork(@RequestParam Long id, RedirectAttributes redirectAttrs) {
        try {
            workService.deleteWork(id);
            redirectAttrs.addFlashAttribute("success", "Serviço deletado com sucesso!");
        } catch (ResourceNotFoundException e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/gerenciador_de_servico/servicos/disponiveis";
    }

}


package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.dto.ResponseWorkWithTaxDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.service.EnrollmentService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.service.WorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/inscricoes")
@PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('SUPERVISOR')")
@RequiredArgsConstructor
public class RegistrationWebController {

    private final EnrollmentService enrollmentService;
    private final WorkService workService;

    @GetMapping
    public String showRegistrations(Model model) {
        List<ResponseWorkWithTaxDTO> servicesWithEnrollments = enrollmentService.getGroupedEnrollmentsByMonth();
        model.addAttribute("servicos", servicesWithEnrollments);
        model.addAttribute("pageTitle", "Inscrições");
        return "fragments/enrollments";
    }

    @PostMapping("/servicos/{id}/observacao")
    public String updateObservation(@PathVariable Long id, @RequestParam String observation, RedirectAttributes redirectAttributes) {
        try {
            workService.updateObservation(id, observation);
            redirectAttributes.addFlashAttribute("success", "Observação atualizada com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar observação: " + e.getMessage());
        }

        return "redirect:/gerenciador_de_servico/inscricoes";
    }
}


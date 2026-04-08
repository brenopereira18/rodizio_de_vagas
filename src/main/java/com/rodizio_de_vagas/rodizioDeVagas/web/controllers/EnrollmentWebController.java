package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.security.Principal;

@Controller
@RequestMapping("/servicos")
@RequiredArgsConstructor
public class EnrollmentWebController {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentWebController.class);
    private final EnrollmentService enrollmentService;

    @PostMapping("/{id}/inscrever")
    public String register(@PathVariable("id") Long workId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            enrollmentService.respondToEnrollment(workId, principal.getName(), true);
            redirectAttributes.addFlashAttribute("success", "Inscrição realizada com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao se inscrever: " + e.getMessage());
        }
        return "redirect:/servicos";
    }

    @PostMapping("/free/{id}/inscrever")
    public String registerFreeService(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            enrollmentService.reusePreviousEnrollment(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Inscrição realizada com sucesso.");
        } catch (Exception e) {
            log.warn("Erro ao se inscrever em serviço livre", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao se inscrever: " + e.getMessage());
        }
        return "redirect:/servicos";
    }

    @PostMapping("/{id}/recusar")
    public String refuse(@PathVariable("id") Long workId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            enrollmentService.respondToEnrollment(workId, principal.getName(), false);
            redirectAttributes.addFlashAttribute("success", "Inscrição recusada com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao recusar inscrição: " + e.getMessage());
        }
        return "redirect:/servicos";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelEnrollment(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            enrollmentService.cancelEnrollment(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Inscrição cancelada com sucesso.");
        } catch (Exception e) {
            log.warn("Erro ao cancelar inscrição", e);
            redirectAttributes.addFlashAttribute("error", "Erro ao cancelar inscrição: " + e.getMessage());
        }

        return "redirect:/servicos?filtro=INSCRITOS";
    }

}

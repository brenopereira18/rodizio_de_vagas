package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.security.Principal;

@Controller
@RequestMapping("/inscricoes")
public class EnrollmentWebController {

    @Autowired
    private EnrollmentService enrollmentService;

    @PostMapping("/inscrever/{id}")
    public String register(@PathVariable("id") Long workId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            enrollmentService.respondToEnrollment(workId, principal.getName(), true);
            redirectAttributes.addFlashAttribute("success", "Inscrição realizada com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao se inscrever: " + e.getMessage());
        }
        return "redirect:/home/servicos";
    }

    @PostMapping("/servicos/free/inscrever/{id}")
    public String acceptedServiceFree(@PathVariable Long id, Principal principal) {
        enrollmentService.reusePreviousEnrollment(id, principal.getName());
        return "redirect:/home/servicos";
    }

    @PostMapping("/recusar/{id}")
    public String refuse(@PathVariable("id") Long workId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            enrollmentService.respondToEnrollment(workId, principal.getName(), false);
            redirectAttributes.addFlashAttribute("success", "Inscrição recusada com sucesso.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao recusar inscrição: " + e.getMessage());
        }
        return "redirect:/home/servicos";
    }

    @PostMapping("/cancelar/{id}")
    public String cancelEnrollment(@PathVariable Long id, Principal principal) throws AccessDeniedException {
        String registration = principal.getName();
        enrollmentService.cancelEnrollment(id, registration);

        return "redirect:/home/servicos?filtro=INSCRITOS";
    }

}

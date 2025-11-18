package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.dto.ResponseWorkWithTaxDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.service.EnrollmentService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.service.WorkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/home/inscricoes")
@PreAuthorize("hasAuthority('ADMINISTRADOR') or hasAuthority('SUPERVISOR')")
public class RegistrationWebController {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private WorkService workService;

    @GetMapping
    public String showRegistrations(Model model) {
        List<ResponseWorkWithTaxDTO> servicesWithEnrollments = enrollmentService.getGroupedEnrollmentsByMonth();
        model.addAttribute("servicos", servicesWithEnrollments);
        model.addAttribute("pageTitle", "Inscrições");
        return "fragments/enrollments";
    }

    @PostMapping("/servicos/{id}/observacao")
    public String updateObservation(@PathVariable Long id, @RequestParam String observation) {
        workService.updateObservation(id, observation);
        return "redirect:/home/inscricoes";
    }
}


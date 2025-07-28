package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity.dto.ResponseWorkWithTaxDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/home/inscricoes")
public class RegistrationWebController {

    @Autowired
    private EnrollmentService enrollmentService;

    @GetMapping
    public String showRegistrations(Model model) {
        List<ResponseWorkWithTaxDTO> servicesWithEnrollments = enrollmentService.getGroupedEnrollmentsByMonth();
        model.addAttribute("servicos", servicesWithEnrollments);
        model.addAttribute("pageTitle", "Inscrições");
        return "fragments/enrollments";
    }
}


package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home/inscricoes")
public class RegistrationWebController {

    @GetMapping
    public String showRegistrations(Model model) {
        model.addAttribute("pageTitle", "Inscrições");
        return "fragments/enrollments";
    }
}


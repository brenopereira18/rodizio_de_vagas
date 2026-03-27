package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LayoutController {

    @GetMapping("/")
    public String redirectToHome() {
        return "redirect:" + RoutesController.BASE + "/servicos";
    }
}

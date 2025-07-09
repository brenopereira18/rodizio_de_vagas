package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/servicos")
public class WorksController {

    @GetMapping
    public String works() {
        return "works";
    }
}

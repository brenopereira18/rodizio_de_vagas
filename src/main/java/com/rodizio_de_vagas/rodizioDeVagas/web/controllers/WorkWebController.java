package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service.UserService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/home/servicos")
public class WorkWebController {

    @Autowired
    private UserService userService;

    @GetMapping
    public String showServices(Model model) {
        model.addAttribute("pageTitle", "Serviços");
        model.addAttribute("supervisores", userService.getAllManagers());
        model.addAttribute("categorias", Category.values());
        return "fragments/servicos";
    }
}


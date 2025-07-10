package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.ResponseManagerDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service.UserService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/servicos")
    public String works(Model model) {
        model.addAttribute("pageTitle", "Serviços");

        List<ResponseManagerDTO> managers = userService.getAllManagers();
        model.addAttribute("supervisores", managers);

        model.addAttribute("categorias", Category.values());
        return "fragments/servicos";
    }

    @GetMapping("/inscricoes")
    public String registers(Model model) {
        model.addAttribute("pageTitle", "Inscrições");
        return "fragments/inscricoes";
    }

    @GetMapping("/fiscais")
    public String users(Model model) {
        model.addAttribute("pageTitle", "Fiscais");
        return "fragments/fiscais-supervisores";
    }
}

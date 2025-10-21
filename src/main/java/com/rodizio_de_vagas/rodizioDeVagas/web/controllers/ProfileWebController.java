package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity.WorkCategoryPreferenceEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.service.WorkCategoryPreferenceService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestUpdateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service.UserService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/home/perfil")
public class ProfileWebController {

    @Autowired
    private UserService userService;

    @Autowired
    private WorkCategoryPreferenceService workCategoryPreferenceService;

    @GetMapping
    public String profile(Model model, Principal principal) {
        UserEntity user = userService.getUser(principal.getName());
        List<WorkCategoryPreferenceEntity> preferences = workCategoryPreferenceService.getPreferencesByUser(user.getRegistration());

        RequestUpdateUserDTO dto = new RequestUpdateUserDTO(
            user.getPassword(),
            user.getPhoneNumber(),
            preferences.stream()
                .filter(WorkCategoryPreferenceEntity::isActive)
                .map(WorkCategoryPreferenceEntity::getCategory)
                .toList(),
            user.getHaveALicense()
        );

        model.addAttribute("usuario", user);
        model.addAttribute("preferencias", preferences);
        model.addAttribute("categorias", Category.values());
        model.addAttribute("updateDto", dto);
        model.addAttribute("pageTitle", "Perfil");
        return "fragments/profile";
    }

    @PostMapping("/atualizar")
    public String updateProfile(@ModelAttribute RequestUpdateUserDTO dto, Principal principal, RedirectAttributes redirectAttributes) {
        String registration = principal.getName();

        try {
            this.userService.updateUser(registration, dto);
            redirectAttributes.addFlashAttribute("success", "Dados atualizados com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erro ao atualizar: " + e.getMessage());
        }
        return "redirect:/home/perfil";
    }
}

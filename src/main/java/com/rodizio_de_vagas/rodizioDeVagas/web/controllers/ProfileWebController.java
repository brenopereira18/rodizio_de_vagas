package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.entity.dto.WorkCategoryPreferenceResponseDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.WorkCategoryPreference.service.WorkCategoryPreferenceService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.RequestUpdateUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.dto.ResponseUserDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.service.UserService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping(RoutesController.BASE + "/perfil")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileWebController {

    private final UserService userService;
    private final WorkCategoryPreferenceService workCategoryPreferenceService;

    @GetMapping
    public String profile(Model model, Principal principal) {
        ResponseUserDTO user = userService.getUser(principal.getName());
        List<WorkCategoryPreferenceResponseDTO> preferences = workCategoryPreferenceService.getPreferencesByUser(user.registration());

        RequestUpdateUserDTO dto = new RequestUpdateUserDTO(
            null,
            user.phoneNumber(),
            preferences.stream()
                .filter(WorkCategoryPreferenceResponseDTO::active)
                .map(WorkCategoryPreferenceResponseDTO::category)
                .toList(),
            user.haveALicense()
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
        return "redirect:/gerenciador_de_servico/perfil";
    }
}

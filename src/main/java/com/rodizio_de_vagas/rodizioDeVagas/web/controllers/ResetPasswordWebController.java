package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.entity.dto.ResetPasswordDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.service.ForgetPasswordTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(RoutesController.BASE + "/resetar-senha")
@RequiredArgsConstructor
public class ResetPasswordWebController {

    private final ForgetPasswordTokenService forgetPasswordTokenService;

    @GetMapping
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // Validação do token (exceções tratadas globalmente)
        this.forgetPasswordTokenService.validateAndRetrieveToken(token);

        model.addAttribute("resetPasswordDTO", new ResetPasswordDTO(token, null, null));
        model.addAttribute("tokenPresent", true);
        return "forgot-password";
    }

    @PostMapping
    public String processResetPassword(@Valid ResetPasswordDTO request, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // Validações de Bean Validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("tokenPresent", true);
            return "forgot-password";
        }

        // Validação de confirmação de senha
        if (!request.passwordsMatch()) {
            model.addAttribute("tokenPresent", true);
            model.addAttribute("error", "As senhas não coincidem.");
            return "forgot-password";
        }

        try {
            forgetPasswordTokenService.resetPassword(request.token(), request.newPassword());
            redirectAttributes.addFlashAttribute(
                "success",
                "Sua senha foi redefinida com sucesso! Faça login com a nova senha."
            );

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                "error",
                "Erro ao redefinir senha: " + e.getMessage()
            );
            return "redirect:/resetar-senha?token=" + request.token();
        }
        return "redirect:/fiscal/login";
    }
}

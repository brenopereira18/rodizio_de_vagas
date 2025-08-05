package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.dto.ResetPasswordDTO;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.service.ForgetPasswordTokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ResetPasswordWebController {

    @Autowired
    private ForgetPasswordTokenService forgetPasswordTokenService;

    @GetMapping("/resetar-senha")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        // se o token for inválido/expirado. O GlobalExceptionHandler capturará e redirecionará.
        this.forgetPasswordTokenService.validateAndRetrieveToken(token);

        // Se chegou aqui, o token é válido. Prepara o DTO para o formulário.
        model.addAttribute("resetPasswordDTO", new ResetPasswordDTO(token, null, null)); // Preenche o token no DTO
        model.addAttribute("tokenPresent", true);
        return "forgot-password";
    }

    @PostMapping("/resetar-senha")
    public String processResetPassword(@Valid ResetPasswordDTO request, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        // Em caso de erro de validação (ex: campos em branco, tamanho mínimo)
        if (bindingResult.hasErrors()) {
            model.addAttribute("tokenPresent", true); // Mantém o formulário de senha visível
            // Errors são adicionados automaticamente ao Model pelo Spring se BindingResult for passado
            // E o DTO também é adicionado como "resetPasswordRequest"
            return "forgot-password";
        }

        // Validação de confirmação de senha
        if (!request.passwordsMatch()) {
            model.addAttribute("tokenPresent", true); // Mantém o formulário de senha visível
            model.addAttribute("error", "As senhas não coincidem.");
            return "forgot-password";
        }

        // Se chegou aqui, as validações básicas passaram.
        // O serviço será chamado. Se ele lançar InvalidTokenException ou TokenExpiredException,
        // o GlobalExceptionHandler as tratará.
        forgetPasswordTokenService.resetPassword(request.getToken(), request.getNewPassword());

        redirectAttributes.addFlashAttribute("success", "Sua senha foi redefinida com sucesso! Faça login com a nova senha.");
        return "redirect:/fiscal/login";
    }
}

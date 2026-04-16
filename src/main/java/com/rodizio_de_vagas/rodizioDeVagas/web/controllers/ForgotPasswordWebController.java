package com.rodizio_de_vagas.rodizioDeVagas.web.controllers;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.service.ForgetPasswordTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/recuperar-senha")
@RequiredArgsConstructor
public class ForgotPasswordWebController {

    private final ForgetPasswordTokenService forgetPasswordTokenService;

    @GetMapping
    public String showForgotPasswordForm(Model model) {
        model.addAttribute("tokenPresent", false);
        return "forgot-password";
    }

    @PostMapping
    public String processForgotPasswordForm(@RequestParam("email") String email, Model model) {
        this.forgetPasswordTokenService.createPasswordResetTokenForFiscal(email);
        model.addAttribute("message", "Se seu email estiver cadastrado, você receberá um link para redefinir sua senha.");
        model.addAttribute("tokenPresent", false);
        return "forgot-password";
    }
}

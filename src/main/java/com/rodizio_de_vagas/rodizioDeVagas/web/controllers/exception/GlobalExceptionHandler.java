package com.rodizio_de_vagas.rodizioDeVagas.web.controllers.exception;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.InvalidTokenException;
import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.TokenExpiredException;
import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.UserNotFoundForPasswordResetException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Tratamento para o caso de usuário não encontrado no fluxo de recuperação de senha (Segurança!)
    @ExceptionHandler(UserNotFoundForPasswordResetException.class)
    public String handleUserNotFoundForPasswordResetException(UserNotFoundForPasswordResetException ex, Model model) {
        model.addAttribute("message", "Se seu telefone estiver cadastrado, você receberá um link para redefinir sua senha.");
        model.addAttribute("tokenPresent", false);
        System.err.println("Tentativa de redefinição de senha para usuário não encontrado (motivo de segurança): " + ex.getMessage());
        return "forgot-password";
    }

    // Tratamento para Token Inválido/Expirado (quando o link é acessado ou o token é validado a primeira vez)
    @ExceptionHandler({InvalidTokenException.class, TokenExpiredException.class})
    public String handleInvalidOrExpiredToken(RuntimeException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("tokenPresent", false);
        System.err.println("Erro de token: " + ex.getMessage());
        return "forgot-password";
    }

    // --- Tratamento para qualquer outra exceção inesperada ---
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        System.err.println("Erro inesperado (Global): " + ex.getMessage());
        ex.printStackTrace(); // Imprime o stack trace para depuração (remover ou logar em produção)

        model.addAttribute("error", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        model.addAttribute("tokenPresent", false);
        return "redirect:/services";
    }
}

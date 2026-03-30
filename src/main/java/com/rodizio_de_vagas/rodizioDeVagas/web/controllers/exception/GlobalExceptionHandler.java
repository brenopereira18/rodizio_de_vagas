package com.rodizio_de_vagas.rodizioDeVagas.web.controllers.exception;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.InvalidTokenException;
import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.TokenExpiredException;
import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.UserNotFoundForPasswordResetException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Tratamento para o caso de usuário não encontrado no fluxo de recuperação de senha (Segurança!)
    @ExceptionHandler(UserNotFoundForPasswordResetException.class)
    public String handleUserNotFoundForPasswordResetException(UserNotFoundForPasswordResetException ex, Model model) {
        model.addAttribute("message", "Se seu telefone estiver cadastrado, você receberá um link para redefinir sua senha.");
        model.addAttribute("tokenPresent", false);
        return "forgot-password";
    }

    // Tratamento para Token Inválido/Expirado (quando o link é acessado ou o token é validado a primeira vez)
    @ExceptionHandler({InvalidTokenException.class, TokenExpiredException.class})
    public String handleInvalidOrExpiredToken(RuntimeException ex, Model model) {
        model.addAttribute("error", ex.getMessage());
        model.addAttribute("tokenPresent", false);
        return "forgot-password";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public void handleNoResourceFound() {
        // 404 silencioso para recursos estáticos não encontrados
    }

    // --- Tratamento para qualquer outra exceção inesperada ---
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception ex, Model model) {
        logger.error("Erro inesperado", ex);

        model.addAttribute("error", "Ocorreu um erro inesperado. Tente novamente mais tarde.");
        model.addAttribute("tokenPresent", false);
        return "redirect:/gerenciador_de_servico/servicos";
    }
}

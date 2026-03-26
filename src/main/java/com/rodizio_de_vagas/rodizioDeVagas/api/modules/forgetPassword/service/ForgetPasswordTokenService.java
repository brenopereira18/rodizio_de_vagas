package com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.InvalidTokenException;
import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.TokenExpiredException;
import com.rodizio_de_vagas.rodizioDeVagas.api.exceptions.UserNotFoundForPasswordResetException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.evolutionAPI.service.WhatsappNotificationService;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.entity.ForgetPasswordTokenEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.forgetPassword.repository.ForgetPasswordTokenRepository;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ForgetPasswordTokenService {

    private final UserRepository userRepository;
    private final ForgetPasswordTokenRepository forgetPasswordTokenRepository;
    private final WhatsappNotificationService whatsappNotificationService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.url}")
    private String appUrl;

    @Transactional
    public void createPasswordResetTokenForFiscal(String phoneNumber) {
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(() ->
            new UserNotFoundForPasswordResetException("Usuário não encontrado."));

        // Gerar um novo token e definir a data de expiração
        String newTokenString = UUID.randomUUID().toString();
        Instant expiryTime = Instant.now().plus(15, ChronoUnit.MINUTES);

        // Reutiliza token existente (UPDATE) ou cria novo (INSERT).
        ForgetPasswordTokenEntity resetToken = forgetPasswordTokenRepository.findByUserEntity(user)
            .map(existingToken -> {
                existingToken.setToken(newTokenString);
                existingToken.setExpiryTime(expiryTime);
                return existingToken;
            }).orElseGet(() -> {
                return ForgetPasswordTokenEntity.builder()
                    .token(newTokenString)
                    .userEntity(user)
                    .expiryTime(expiryTime)
                    .build();
            });

        this.forgetPasswordTokenRepository.save(resetToken);

        String resetUrl = appUrl + "/resetar-senha?token=" + newTokenString;
        String message = "Olá, " + user.getFullName() + "! Para redefinir sua senha, clique no link: " + resetUrl + "\n\nEste link é válido por 15 minutos.";

        whatsappNotificationService.sendMessage(phoneNumber, message);
    }

    @Transactional
    public ForgetPasswordTokenEntity validateAndRetrieveToken(String token) {
        ForgetPasswordTokenEntity resetToken = forgetPasswordTokenRepository.findByToken(token)
            .orElseThrow(() -> new InvalidTokenException("Token de redefinição inválido ou não encontrado."));

        if (resetToken.getExpiryTime().isBefore(Instant.now())) {
            // Se o token estiver expirado, ele é deletado para não ser mais usado.
            forgetPasswordTokenRepository.delete(resetToken);
            throw new TokenExpiredException("O token de redefinição expirou.");
        }
        return resetToken;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        ForgetPasswordTokenEntity resetToken = validateAndRetrieveToken(token);
        UserEntity user = resetToken.getUserEntity();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Invalida o token após uso bem-sucedido.
        forgetPasswordTokenRepository.delete(resetToken);
    }
}

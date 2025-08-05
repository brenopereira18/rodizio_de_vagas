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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class ForgetPasswordTokenService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ForgetPasswordTokenRepository forgetPasswordTokenRepository;

    @Autowired
    private WhatsappNotificationService whatsappNotificationService;

    @Transactional
    public void createPasswordResetTokenForFiscal(String phoneNumber) {
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber).orElseThrow(() ->
            new UserNotFoundForPasswordResetException("Usuário não encontrado."));

        // 1. Gerar um novo token e definir a data de expiração
        String newTokenString = UUID.randomUUID().toString();
        Instant expiryTime = Instant.now().plus(15, ChronoUnit.MINUTES);

        // 2. Tentar encontrar um token existente para este usuário
        // Se encontrar, atualiza-o. Se não, cria um novo.
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

        // 3. Salvar o token no banco de dados
        // Se resetToken veio de .map(), o save fará um UPDATE.
        // Se resetToken veio de .orElseGet(), o save fará um INSERT.
        this.forgetPasswordTokenRepository.save(resetToken);

        // Use o tokenString recém-gerado, que agora está no objeto resetToken salvo/atualizado.
        String resetUrl = "http://rodizio-de-vagas.onrender.com/resetar-senha?token=" + resetToken.getToken();
        String message = "Olá, " + user.getFullName() + "! Para redefinir sua senha, clique no link: " + resetUrl + "\n\nEste link é válido por 15 minutos.";

        whatsappNotificationService.sendMessage(phoneNumber, message);
    }

    // Método para validar o token
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

        String encryptedPassword = new BCryptPasswordEncoder().encode(newPassword);
        user.setPassword(encryptedPassword);
        userRepository.save(user);

        // Invalida o token após o uso bem-sucedido, deletando-o do banco de dados.
        forgetPasswordTokenRepository.delete(resetToken);
    }
}

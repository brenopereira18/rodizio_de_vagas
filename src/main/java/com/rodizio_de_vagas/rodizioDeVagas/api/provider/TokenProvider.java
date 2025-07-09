package com.rodizio_de_vagas.rodizioDeVagas.api.provider;


import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity.UserEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class TokenProvider {

    @Value("${security.token.secret}")
    private String secret;

    public String generateToken(UserEntity userEntity) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                .withIssuer("rodizio_de_vagas")
                .withSubject(userEntity.getRegistration())
                .withExpiresAt(genExpirationDate())
                .sign(algorithm);
        } catch (JWTVerificationException e) {
            throw new RuntimeException("Erro ao gerar o token:", e);
        }
    }

    public String validationToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                .withIssuer("rodizio_de_vagas")
                .build()
                .verify(token)
                .getSubject();
        } catch (JWTVerificationException e) {
            return "";
        }
    }

    private Instant genExpirationDate() {
        return Instant.now().plus(Duration.ofDays(7));
    }
}

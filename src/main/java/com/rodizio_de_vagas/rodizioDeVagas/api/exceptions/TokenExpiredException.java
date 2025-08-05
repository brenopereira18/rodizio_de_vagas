package com.rodizio_de_vagas.rodizioDeVagas.api.exceptions;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) {
        super(message);
    }
}

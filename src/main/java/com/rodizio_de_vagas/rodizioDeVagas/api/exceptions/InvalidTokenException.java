package com.rodizio_de_vagas.rodizioDeVagas.api.exceptions;

public class InvalidTokenException extends RuntimeException {
    public InvalidTokenException(String message) {
        super(message);
    }
}

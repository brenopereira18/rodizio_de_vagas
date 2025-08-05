package com.rodizio_de_vagas.rodizioDeVagas.api.exceptions;

public class UserNotFoundForPasswordResetException extends RuntimeException {
    public UserNotFoundForPasswordResetException(String message) {
        super(message);
    }
}

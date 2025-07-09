package com.rodizio_de_vagas.rodizioDeVagas.api.exceptions;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String message) {
        super(message);
    }
}

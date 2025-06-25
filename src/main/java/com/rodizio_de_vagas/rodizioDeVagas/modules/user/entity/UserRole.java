package com.rodizio_de_vagas.rodizioDeVagas.modules.user.entity;

public enum UserRole {
    ADMIN("Administrador"),
    FISCAL("Fiscal");

    private String userRole;

    UserRole(String userRole) {
        this.userRole = userRole;
    }
}

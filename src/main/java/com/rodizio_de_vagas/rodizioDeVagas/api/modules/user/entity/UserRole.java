package com.rodizio_de_vagas.rodizioDeVagas.api.modules.user.entity;

public enum UserRole {
    ADMINISTRADOR("Administrador"),
    FISCAL("Fiscal"),
    SUPERVISOR("Supervisor");

    private String userRole;

    UserRole(String userRole) {
        this.userRole = userRole;
    }

    public String getUserRole() {
        return userRole;
    }
}

package com.rodizio_de_vagas.rodizioDeVagas.modules.enrollment.entity;

public enum SubscriptionStatus {
    WAITING("Aguardando"),
    ACCEPTED("Aceito"),
    REFUSED("Recusado"),
    CANCELLED("Cancelado"),
    EXPIRED("Expirado");

    private String subscriptionStatus;

    SubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }
}

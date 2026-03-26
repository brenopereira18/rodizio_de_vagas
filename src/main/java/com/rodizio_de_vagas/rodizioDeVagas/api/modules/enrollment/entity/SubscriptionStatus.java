package com.rodizio_de_vagas.rodizioDeVagas.api.modules.enrollment.entity;

public enum SubscriptionStatus {
    WAITING("Aguardando"),
    ACCEPTED("Aceito"),
    REFUSED("Recusado"),
    CANCELLED("Cancelado"),
    EXPIRED("Expirado");

    private final String subscriptionStatus;

    SubscriptionStatus(String subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public String getSubscriptionStatus() {
        return subscriptionStatus;
    }
}

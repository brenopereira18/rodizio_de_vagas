package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity;

public enum Category {
    AMBULANTE("Ambulante"),
    FEIRA_DE_SABADO("Feira de Sábado"),
    EVENTO("Evento"),
    FEIRA_DE_DOMINGO("Feira de Domingo");

    private final String category;

    Category(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}

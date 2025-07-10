package com.rodizio_de_vagas.rodizioDeVagas.api.modules.work.entity;

public enum Category {
    AMBULANTE("Ambulante"),
    FEIRA("Feira"),
    EVENTO("Evento");

    private String category;

    Category(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }
}

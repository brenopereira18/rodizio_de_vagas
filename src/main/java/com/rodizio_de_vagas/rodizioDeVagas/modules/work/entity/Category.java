package com.rodizio_de_vagas.rodizioDeVagas.modules.work.entity;

public enum Category {
    AMBULANTE("Ambulante"),
    FAIR("Feira"),
    EVENT("Evento");

    private String category;

    Category(String category) {
        this.category = category;
    }
}

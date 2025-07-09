package com.rodizio_de_vagas.rodizioDeVagas.api.modules.evolutionAPI.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvolutionApiRequestDTO {
    private String number;
    private String text;
    private Integer delay = 10000;

    public EvolutionApiRequestDTO(String number, String text) {
        this.number = number;
        this.text = text;
        this.delay = 10000;
    }
}

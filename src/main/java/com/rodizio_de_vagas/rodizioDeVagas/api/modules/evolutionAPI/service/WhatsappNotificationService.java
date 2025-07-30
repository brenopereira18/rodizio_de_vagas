package com.rodizio_de_vagas.rodizioDeVagas.api.modules.evolutionAPI.service;

import com.rodizio_de_vagas.rodizioDeVagas.api.modules.evolutionAPI.dto.EvolutionApiRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class WhatsappNotificationService {

    private final WebClient webClient;

    @Value("${evolution.api.token}")
    private String apiToken;

    @Value("${evolution.api.url}")
    private String apiUrl;

    public void sendMessage(String number, String text) {
        String formattedNumber = "55" + number;
        EvolutionApiRequestDTO request = new EvolutionApiRequestDTO(formattedNumber, text);

        webClient.post()
            .uri(apiUrl)
            .header("apikey", apiToken)
            .header("Content-Type", "application/json")
            .bodyValue(request)
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(response -> System.out.println("Mensagem enviada com sucesso: " + response))
            .doOnError(error -> System.out.println("Erro ao enviar mensagem: " + error.getMessage()))
            .subscribe();
    }
}

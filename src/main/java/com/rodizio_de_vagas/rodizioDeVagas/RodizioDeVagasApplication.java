package com.rodizio_de_vagas.rodizioDeVagas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RodizioDeVagasApplication {

	public static void main(String[] args) {
		SpringApplication.run(RodizioDeVagasApplication.class, args);
	}

}

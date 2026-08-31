package com.nuvexa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class NuvexaApplication {

	public static void main(String[] args) {
		// Fixa a timezone padrão da JVM antes de qualquer bean subir, para que todo
		// LocalDateTime.now() do backend (criadoEm/atualizadoEm, expiração de token,
		// timestamp de erro) reflita o horário de Brasília, não o default do host/container.
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
		SpringApplication.run(NuvexaApplication.class, args);
	}

}

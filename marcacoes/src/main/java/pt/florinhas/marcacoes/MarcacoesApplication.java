package pt.florinhas.marcacoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MarcacoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarcacoesApplication.class, args);
	}

}

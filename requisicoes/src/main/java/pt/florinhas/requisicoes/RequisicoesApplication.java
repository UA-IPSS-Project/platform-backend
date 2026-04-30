package pt.florinhas.requisicoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"pt.florinhas.requisicoes",
		"pt.florinhas.common_data"
})
@EntityScan(basePackages = {
		"pt.florinhas.requisicoes.domain",
		"pt.florinhas.common_data.domain"
})
@EnableJpaRepositories(basePackages = {
		"pt.florinhas.requisicoes.repository",
		"pt.florinhas.common_data.repository"
})
public class RequisicoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RequisicoesApplication.class, args);
	}

}

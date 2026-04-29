package pt.florinhas.candidaturas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(excludeName = {
		"de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration"
})
@EntityScan(basePackages = {
		"pt.florinhas.candidaturas.domain",
		"pt.florinhas.common_data.domain"
})
@EnableJpaRepositories(basePackages = {
		"pt.florinhas.candidaturas.repository",
		"pt.florinhas.common_data.repository"
})
public class CandidaturasApplication {

	public static void main(String[] args) {
		SpringApplication.run(CandidaturasApplication.class, args);
	}

}

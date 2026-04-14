package pt.florinhas.api_gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
		"pt.florinhas.api_gateway",
		"pt.florinhas.common_data"
})
@EntityScan(basePackages = {
		"pt.florinhas.common_data.domain"
})
@EnableJpaRepositories(basePackages = {
		"pt.florinhas.common_data.repository"
})
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}

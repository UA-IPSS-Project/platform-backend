package pt.florinhas.notificacoes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "pt.florinhas.notificacoes",
    "pt.florinhas.common_data"
})
@EntityScan(basePackages = {"pt.florinhas.common_data.domain"})
@EnableJpaRepositories(basePackages = {"pt.florinhas.common_data.repository"})
public class NotificacoesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificacoesApplication.class, args);
    }
}

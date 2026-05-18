package pt.florinhas.requisicoes;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
@EnableAsync
public class RequisicoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(RequisicoesApplication.class, args);
	}

	@Bean(name = "taskExecutor")
	TaskExecutor taskExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setMaxPoolSize(16);
		executor.setQueueCapacity(50);
		executor.setThreadNamePrefix("async-");
		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
		executor.initialize();
		return executor;
	}

}

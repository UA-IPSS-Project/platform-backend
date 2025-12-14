package pt.florinhas.marcacoes;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import pt.florinhas.marcacoes.domain.Funcionario;
import pt.florinhas.marcacoes.domain.FuncionarioTipo;
import pt.florinhas.marcacoes.repository.FuncionarioRepository;

@SpringBootApplication
@EnableScheduling
public class MarcacoesApplication {

	public static void main(String[] args) {
		SpringApplication.run(MarcacoesApplication.class, args);
	}

	/**
	 * CommandLineRunner que cria uma secretária admin ativa ao iniciar a aplicação,
	 * caso ainda não exista um funcionário com o NIF especificado.
	 */
	@Bean
	CommandLineRunner initAdminSecretaria(FuncionarioRepository funcionarioRepository,
			PasswordEncoder passwordEncoder) {
		return args -> {
			String adminNif = "999999999";

			if (!funcionarioRepository.existsByNif(adminNif)) {
				Funcionario admin = new Funcionario();
				admin.setNome("Admin Secretaria");
				admin.setEmail("admin@florinhas.pt");
				admin.setNif(adminNif);
				admin.setTelefone("999999999");
				admin.setPassHash(passwordEncoder.encode("admin123"));
				admin.setTipo(FuncionarioTipo.SECRETARIA);
				admin.setActivo(true);

				funcionarioRepository.save(admin);
				System.out.println(">>> Secretária admin criada com sucesso!");
			} else {
				System.out.println(">>> Secretária admin já existe.");
			}
		};
	}

}
